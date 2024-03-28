package com.booking.app.services.impl.scrape.train;

import com.booking.app.entity.BusTicket;
import com.booking.app.entity.Route;
import com.booking.app.entity.TrainComfortInfo;
import com.booking.app.entity.TrainTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import com.booking.app.mapper.TrainMapper;
import com.booking.app.props.LinkProps;
import com.booking.app.repositories.TrainTicketRepository;
import com.booking.app.services.ScraperService;
import com.booking.app.util.WebDriverFactory;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service("ticketsUa")
@RequiredArgsConstructor
@Log4j2
public class TicketsUaTrainServiceImpl implements ScraperService {

    private final LinkProps linkProps;

    private final TrainMapper trainMapper;

    private final WebDriverFactory webDriverFactory;

    private final TrainTicketRepository trainTicketRepository;

    private static final String DIV_TICKET = "div.railway-train-default";

    private static final String DIV_TICKET_NOT_FOUND = "div.railway-no-results-error";

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTickets(SseEmitter emitter, Route route, String language, Boolean doShow) throws ParseException, IOException {
        WebDriver driver = webDriverFactory.createInstance();
        driver.manage().window().setSize(new Dimension(1850, 1000));

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        String uri = determineBaseUri(language);

        requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver, uri, language);
        switchToMainTab(driver);

        if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

        waitForTickets(driver);

        List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));
        processScrapedTickets(emitter, route, language, doShow, elements, route.getDepartureDate());

        driver.quit();
        return CompletableFuture.completedFuture(true);
    }

    private static void switchToMainTab(WebDriver driver) {
        ArrayList<String> switchTabs = new ArrayList<>(driver.getWindowHandles());
        driver.switchTo().window(switchTabs.get(1));
    }

    private String determineBaseUri(String language) {
        return switch (language) {
            case ("ua") -> linkProps.getTicketsUaTrain();
            case ("eng") -> linkProps.getTicketsEngTrain();
            default ->
                    throw new UndefinedLanguageException("Incomprehensible language passed into " + HttpHeaders.CONTENT_LANGUAGE);
        };
    }

    @Override
    public CompletableFuture<Boolean> scrapeTicketUri(SseEmitter emitter, BusTicket ticket, String language) throws IOException, ParseException {
        throw new java.lang.UnsupportedOperationException();
    }

    private void processScrapedTickets(SseEmitter emitter, Route route, String language, Boolean doDisplay, List<WebElement> elements, String departureDate) throws IOException {
        log.info("Train tickets on ticketsUa: " + elements.size());

        for (int i = 0; i < elements.size() && i < 150; i++) {
            TrainTicket scrapedTicket = scrapeTicketInfo(elements.get(i), route, language, departureDate);
            TrainTicket trainTicket = scrapedTicket;

            if (route.getTickets().add(scrapedTicket)) {
                if (BooleanUtils.isTrue(doDisplay))
                    emitter.send(SseEmitter.event().name("TicketsUa train: ").data(trainMapper.toTrainTicketDto(scrapedTicket, language)));

            } else
                scrapedTicket = ((TrainTicket) route.getTickets().stream().filter(t -> t.equals(trainTicket)).findFirst().get()).addPrices(trainTicket);

            trainTicketRepository.save(scrapedTicket);
        }
    }

    private static TrainTicket scrapeTicketInfo(WebElement element, Route route, String language, String arrivalDate) {
        if (!element.findElements(By.cssSelector("div.t-tooltip-v2.display-i.rel.railway-train-duration-time__different-day-arrival")).isEmpty()) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-M-d");
            LocalDate date = LocalDate.parse(arrivalDate, formatter).plusDays(1);
            arrivalDate = date.format(formatter);
        }

        String travelTime = element.findElement(By.cssSelector("div.col-auto.font-size-13.font-600.color-21")).getText();

        String[] parts = travelTime.split("[^\\d]+");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int totalMinutes = hours * 60 + minutes;

        String carrier = language.equals("eng") ? "Ukrainian Railways" : "Укрзалізниця";

        List<WebElement> comforts = element.findElement(By.cssSelector("div.railway-train-buttons-seats")).findElements(By.cssSelector("div.row.f-center-left.mb-1"));

        List<TrainComfortInfo> list = new LinkedList<>();

        for (WebElement webElement : comforts) {

            String price = webElement.findElement(By.cssSelector("div.no-wrap.t-price.color-17.font-bold.theme-default")).getText().replaceAll("[^\\d\\.]+", "");
            list.add(TrainComfortInfo.builder()
                    .comfort(webElement.findElement(By.cssSelector("div.col-14.color-21.text-overflow")).getText())
                    .price(new BigDecimal(price)).build());
//                    .link(webElement.findElement(By.cssSelector("a.btn")).getAttribute("href")).build());
        }
        String[] stations = getPlaces(element);
        return createTicket(element, route, totalMinutes, carrier, list, arrivalDate, stations[0], stations[1]);
    }

    private static TrainTicket createTicket(WebElement element, Route route, int totalMinutes, String carrier, List<TrainComfortInfo> list, String arrivalDate, String stationFrom, String stationAt) {
        return TrainTicket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .arrivalDate(arrivalDate)
                .placeFrom(stationFrom)
                .placeAt(stationAt)
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .departureTime(element.findElements(By.cssSelector("div.font-size-18.font-700")).get(0).getText())
                .arrivalTime(element.findElements(By.cssSelector("div.font-size-18.font-700")).get(1).getText())
                .carrier(carrier)
                .infoList(list)
                .build();
    }

    private static boolean areTicketsPresent(WebDriverWait wait, WebDriver driver) {
        try {
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET_NOT_FOUND)), ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET))));
            driver.findElement(By.cssSelector(DIV_TICKET));
            return true;
        } catch (Exception e) {
            driver.quit();
            log.info("Bus tickets on ticketsUA: NOT FOUND");
            return false;
        }
    }

    private static void waitForTickets(WebDriver driver) {
        int previousCount = 0;
        int currentCount = 0;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(2));
        try {
            do {
                previousCount = currentCount;

                ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight)");

                wait.until(ExpectedConditions.numberOfElementsToBeMoreThan(By.cssSelector(DIV_TICKET), previousCount));

                List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));
                currentCount = elements.size();
            } while (currentCount > previousCount);
        } catch (TimeoutException e) {

        }
    }

    private static void requestTickets(String departureCity, String arrivalCity, String departureDate, WebDriver driver, String url, String language) throws ParseException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(url);

        selectCity("/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[1]/div[1]/label/div/input", "/html/body/header/div/div[3]/div[6]/form/div[1]/div/div/div[1]/div[1]/div/div/div[1]/div[1]/button", departureCity, driver, wait);

        selectCity("/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[1]/div[3]/label/div/input", "/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[1]/div[3]/div/div/div[1]/div[1]/button", arrivalCity, driver, wait);

        selectDate(departureDate, driver, wait, language);

        Actions actions = new Actions(driver);
        actions.moveToElement(driver.findElement(By.xpath("/html/body/header/div/div[3]/div[6]/form/div[2]/div/div[2]/button"))).doubleClick().build().perform();
    }

    @SneakyThrows
    private static void selectCity(String inputXpath, String cityXpath, String city, WebDriver driver, WebDriverWait wait) {
        WebElement inputCity = driver.findElement(By.xpath(inputXpath));
        Actions actions = new Actions(driver);
        actions.moveToElement(inputCity).click().doubleClick().perform();

        inputCity.clear();

        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", inputCity);
        ((JavascriptExecutor) driver).executeScript("arguments[0].value = arguments[1];", inputCity, city);

        inputCity.sendKeys(Keys.ARROW_RIGHT);
        inputCity.sendKeys(Keys.ARROW_RIGHT);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(cityXpath)));
        WebElement button = driver.findElement(By.xpath(cityXpath));

        button.click();
    }

    private static boolean selectDate(String departureDate, WebDriver driver, WebDriverWait wait, String language) throws ParseException {
        try {
            synchronized (driver) {
                driver.wait(1000);
            }
        } catch (InterruptedException e) {
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputMonthFormat = new SimpleDateFormat("MMMM", new Locale("uk"));
        SimpleDateFormat outputYearFormat = new SimpleDateFormat("yyyy", new Locale("uk", "en"));
        SimpleDateFormat outputDayFormat = new SimpleDateFormat("d", new Locale("uk", "en"));

        if (language.equals("eng")) outputMonthFormat = new SimpleDateFormat("MMMM", new Locale("en"));
        if (language.equals("eng")) outputYearFormat = new SimpleDateFormat("yyyy", new Locale("en"));
        if (language.equals("eng")) outputDayFormat = new SimpleDateFormat("d", new Locale("en"));

        Date inputDate = inputFormat.parse(departureDate);

        String requestMonth = outputMonthFormat.format(inputDate);
        int requestMonthNumber = inputDate.getMonth();

        String requestYear = outputYearFormat.format(inputDate);
        String requestDay = outputDayFormat.format(inputDate);

        String firstDateSelector = "body > header > div > div.app-main-search-form.mb-4.mb-xl-10 > div.mt-2.seach-form-wrapper.search-form-railway > form > div.col-24.mb-2.mb-m-0.col-xl-21 > div > div > div.col-24.col-xl-8.border-wl-xl-1.border-color-6.t-menu-v2.open.search-form-calendar > div.t-menu-v2__content.ltr.top.theme-default.border-radius-8.bs-5 > div.search-form-calendar-body.mb-1 > div > div > div:nth-child(1) > div.search-form-calendar-body-month-name.f-center-center.rel.size-5 > div";

        String secondDateSelector = "/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/div/div/div[2]/div[1]/div";

        String calendarYear = driver.findElement(By.cssSelector(firstDateSelector)).getText().replaceAll(".* ", "");

        String firstMonth = driver.findElement(By.cssSelector(firstDateSelector)).getText().replaceAll(" .*", "").toLowerCase();
        String secondMonth = driver.findElement(By.xpath(secondDateSelector)).getText().replaceAll(" .*", "").toLowerCase();

        if (!((firstMonth.equals(requestMonth) || secondMonth.equals(requestMonth)) && calendarYear.equals(requestYear))) {
            return false;
        } else {

            List<WebElement> days = (requestMonthNumber % 2 == 0 ? driver.findElement(By.xpath("/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/div/div/div[1]")) : driver.findElement(By.xpath("/html/body/header/div/div[3]/div[6]/form/div[1]/div[1]/div/div[2]/div[2]/div[2]/div/div/div[2]"))).findElements(By.cssSelector("div.f-center-stretch")).stream().map(t -> t.findElements(By.cssSelector("div.f--grow.f--shrink.f--no-basis"))).flatMap(List::stream).toList();

            for (WebElement t : days) {
                try {
                    WebElement element = t.findElement(By.tagName("button")).findElements(By.tagName("div")).get(0);
                    if (element.getText().equals(requestDay)) {
                        Actions actions = new Actions(driver);
                        actions.moveToElement(t.findElement(By.tagName("button"))).click().build().perform();
                        break;
                    }

                } catch (Exception e) {
                }
            }

            return true;
        }
    }

    private static String[] getPlaces(WebElement element) {
        String[] stations = element.findElement(By.cssSelector("button.t-btn-atomic.cursor-pointer.theme-default.railway-train-route-popup-activator.as-link")).getText().split(" - ");
        return new String[]{stations[0], stations[1]};
    }

}

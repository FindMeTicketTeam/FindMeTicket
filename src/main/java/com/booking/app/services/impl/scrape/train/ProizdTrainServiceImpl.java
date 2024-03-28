package com.booking.app.services.impl.scrape.train;

import com.booking.app.props.LinkProps;
import com.booking.app.entity.BusTicket;
import com.booking.app.entity.Route;
import com.booking.app.entity.TrainComfortInfo;
import com.booking.app.entity.TrainTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import com.booking.app.mapper.TrainMapper;
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
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;

@Service("proizdTrainService")
@RequiredArgsConstructor
@Log4j2
public class ProizdTrainServiceImpl implements ScraperService {

    private final LinkProps linkProps;

    private final WebDriverFactory webDriverFactory;

    private final TrainMapper trainMapper;

    private final TrainTicketRepository trainRepository;

    private static final String DIV_TICKET = "div.trip-adaptive";

    private static final String DIV_TICKET_NOT_FOUND = "div.error.card";

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTickets(SseEmitter emitter, Route route, String language, Boolean doDisplay) throws ParseException, IOException {
        WebDriver driver = webDriverFactory.createInstance();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver, determineBaseUri(language), language);

        if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

        waitForTickets(driver);

        List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));

        processScrapedTickets(emitter, route, language, doDisplay, elements);

        driver.quit();
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public CompletableFuture<Boolean> scrapeTicketUri(SseEmitter emitter, BusTicket ticket, String language) {
        throw new java.lang.UnsupportedOperationException();
    }

    private static boolean areTicketsPresent(WebDriverWait wait, WebDriver driver) {
        try {
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET_NOT_FOUND)), ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET))));
            driver.findElement(By.cssSelector(DIV_TICKET));
            return true;
        } catch (Exception e) {
            driver.quit();
            log.info("Train tickets on proizd: NOT FOUND");
            return false;
        }
    }

    private void processScrapedTickets(SseEmitter emitter, Route route, String language, Boolean doDisplay, List<WebElement> elements) throws IOException {
        log.info("Train tickets on proizd: " + elements.size());

        for (int i = 0; i < elements.size() && i < 150; i++) {
            TrainTicket scrapedTicket = scrapeTicketInfo(elements.get(i), route, language);
            TrainTicket trainTicket = scrapedTicket;

            if (route.getTickets().add(scrapedTicket)) {
                if (BooleanUtils.isTrue(doDisplay))
                    emitter.send(SseEmitter.event().name("Proizd train: ").data(trainMapper.toTrainTicketDto(scrapedTicket, language)));

            } else
                scrapedTicket = ((TrainTicket) route.getTickets().stream().filter(t -> t.equals(trainTicket)).findFirst().get()).addPrices(trainTicket);

            trainRepository.save(scrapedTicket);
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

    public String determineBaseUri(String language) {
        return switch (language) {
            case ("ua") -> linkProps.getProizdUaTrain();
            case ("eng") -> linkProps.getProizdEngTrain();
            default ->
                    throw new UndefinedLanguageException("Incomprehensible language passed into " + HttpHeaders.CONTENT_LANGUAGE);
        };
    }

    private static TrainTicket scrapeTicketInfo(WebElement element, Route route, String language) {
        String arrivalDate = element.findElements(By.cssSelector("div.trip__date")).get(1).getText();
        arrivalDate = arrivalDate.substring(4);

        DateTimeFormatter ticketDate;
        ticketDate = language.equals("eng") ? DateTimeFormatter.ofPattern("MMMM dd yyyy", new Locale("en"))
                : DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("uk"));

        LocalDate date = LocalDate.parse(arrivalDate.trim() + " " + Year.now().getValue(), ticketDate);

        ticketDate = language.equals("eng") ? DateTimeFormatter.ofPattern("d.MM, EE", new Locale("en"))
                : DateTimeFormatter.ofPattern("d.MM, EE", new Locale("uk"));
        String formattedDate = date.format(ticketDate);

        String travelTime = element.findElement(By.cssSelector("div.travel-time__value")).getText();

        String[] parts = travelTime.split("[^\\d]+");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int totalMinutes = hours * 60 + minutes;

        String carrier = language.equals("eng") ? "Ukrainian Railways" : "Укрзалізниця";


        List<WebElement> elements = element.findElements(By.cssSelector("div.carriage"));

        List<TrainComfortInfo> list = new LinkedList<>();

        for (WebElement webElement : elements) {

            String price = webElement.findElement(By.cssSelector("div.carriage__price ")).getText().replaceAll("[^\\d\\.]+", "");
            String cleanedPrice = reformatPrice(price);
            list.add(TrainComfortInfo.builder()
                    .comfort(webElement.findElement(By.cssSelector("span.carriage__type")).getText())
                    .price(new BigDecimal(cleanedPrice))
                    .link(webElement.findElement(By.cssSelector("a.btn")).getAttribute("href")).build());
        }

        return createTicket(element, route, totalMinutes, formattedDate, carrier, list);
    }

    private static TrainTicket createTicket(WebElement element, Route route, int totalMinutes, String formattedTime, String carrier, List<TrainComfortInfo> list) {
        String[] places = element.findElement(By.cssSelector("div.trip-item__route")).getText().split(" — ");
        return TrainTicket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .arrivalDate(formattedTime)
                .placeFrom(places[0])
                .placeAt(places[1])
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .departureTime(element.findElements(By.cssSelector("div.trip__time ")).get(0).getText())
                .arrivalTime(element.findElements(By.cssSelector("div.trip__time ")).get(1).getText())
                .carrier(carrier)
                .infoList(list)
                .build();
    }

    private static void requestTickets(String departureCity, String arrivalCity, String departureDate, WebDriver driver, String url, String language) throws ParseException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(url);
        if (language.equals("ua"))
            selectCity(wait, departureCity, "//input[@placeholder='Звідки виїзд?']", "//li[@class='station-item active ng-star-inserted']", driver);
        else
            selectCity(wait, departureCity, "//input[@placeholder='Departure station']", "//li[@class='station-item active ng-star-inserted']", driver);

        if (language.equals("ua"))
            selectCity(wait, arrivalCity, "//input[@placeholder='Куди прямуєте?']", "//li[@class='station-item active station-item--arrival ng-star-inserted']", driver);
        else
            selectCity(wait, arrivalCity, "//input[@placeholder='Arrival station']", "//li[@class='station-item active station-item--arrival ng-star-inserted']", driver);

        selectDate(departureDate, driver, wait, language);

        driver.findElement(By.cssSelector("button.search-btn")).click();
    }

    @SneakyThrows
    private static void selectCity(WebDriverWait wait, String city, String inputXpath, String cityXpath, WebDriver driver) {
        WebElement inputCity = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputXpath)));
        Actions actions = new Actions(driver);
        actions.moveToElement(inputCity).click().build().perform();
        inputCity.clear();
        inputCity.sendKeys(city);

        wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath(cityXpath)));
    }

    private static void selectDate(String departureDate, WebDriver driver, WebDriverWait wait, String language) throws ParseException {
        WebElement dateFrom = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.search-field.date")));
        Actions actions = new Actions(driver);
        actions.moveToElement(dateFrom).doubleClick().build().perform();

        String calendarMonth = driver.findElement(By.cssSelector("li.calmonth")).getText();

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");

        SimpleDateFormat outputMonthFormat = language.equals("eng") ? new SimpleDateFormat("MMMM", new Locale("en"))
                : new SimpleDateFormat("MMMM", new Locale("uk"));

        SimpleDateFormat outputYearFormat = language.equals("eng") ? new SimpleDateFormat("yyyy", new Locale("en"))
                : new SimpleDateFormat("yyyy", new Locale("uk"));

        SimpleDateFormat outputDayFormat = language.equals("eng") ? new SimpleDateFormat("d", new Locale("en"))
                : new SimpleDateFormat("d", new Locale("uk"));

        Date inputDate = inputFormat.parse(departureDate);

        String requestMonth = outputMonthFormat.format(inputDate);
        String requestYear = outputYearFormat.format(inputDate);
        String requestDay = outputDayFormat.format(inputDate);

        calendarMonth = calendarMonth.substring(0, calendarMonth.length() - 5);

        String calendarYear = driver.findElement(By.cssSelector("li.calmonth")).getText();
        calendarYear = calendarYear.substring(calendarYear.length() - 4);

        while (!(calendarMonth.equalsIgnoreCase(requestMonth) && calendarYear.equals(requestYear))) {

            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.calnextmonth"))).click();

            calendarMonth = driver.findElement(By.cssSelector("li.calmonth")).getText();
            calendarMonth = calendarMonth.substring(0, calendarMonth.length() - 5);

            calendarYear = driver.findElement(By.cssSelector("li.calmonth")).getText();
            calendarYear = calendarYear.substring(calendarYear.length() - 4);
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.calbody")));

        List<WebElement> dates = driver.findElement(By.cssSelector("div.calbody")).findElements(By.tagName("li"));
        int indexOfFirstDay = dates.indexOf(dates.stream().filter(el -> el.getText().equals("1")).findFirst().orElse(null));
        List<WebElement> filteredLi = dates.subList(indexOfFirstDay, dates.size());
        WebElement liDate = filteredLi.stream().filter(el -> el.getText().equals(requestDay)).findFirst().orElse(null);
        actions.moveToElement(liDate).doubleClick().build().perform();
    }

    private static String reformatPrice(String price) {
        int indexOfDecimal = price.indexOf('.');
        if (indexOfDecimal != -1) {
            // If there is a decimal point
            String integerPart = price.substring(0, indexOfDecimal);
            String fractionalPart = price.substring(indexOfDecimal + 1);
            if (fractionalPart.length() > 2) {
                // If the fractional part is longer than 2 characters, truncate it
                fractionalPart = fractionalPart.substring(0, 2);
            }
            return integerPart + "." + fractionalPart;
        }
        // If there is no decimal point or the string is empty, return as is
        return price;
    }

}

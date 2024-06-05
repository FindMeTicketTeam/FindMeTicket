package com.booking.app.services.impl.scrape.bus;

import com.booking.app.constant.SiteConstants;
import com.booking.app.dto.UrlAndPriceDTO;
import com.booking.app.entity.ticket.Route;
import com.booking.app.entity.ticket.bus.BusInfo;
import com.booking.app.entity.ticket.bus.BusTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import com.booking.app.mapper.BusMapper;
import com.booking.app.props.LinkProps;
import com.booking.app.repositories.BusTicketRepository;
import com.booking.app.services.ScraperService;
import com.booking.app.util.WebDriverFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Year;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service("infobusBusService")
@RequiredArgsConstructor
@Log4j2
public class InfobusBusServiceImpl implements ScraperService {

    private final LinkProps linkProps;

    private final BusMapper busMapper;

    private final WebDriverFactory webDriverFactory;

    private final BusTicketRepository repository;

    private static final String DIV_TICKET = "div.main-detail-wrap";

    private static final String DIV_TICKET_NOT_FOUND = "div.col-sm-12.alert.alert-warning";

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTickets(SseEmitter emitter, Route route, String language, Boolean doShow, MutableBoolean emitterNotExpired) throws ParseException, IOException {

        WebDriver driver = webDriverFactory.createInstance();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            String url = determineBaseUri(language);
            requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver, url, language);

            if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

            waitForTickets(driver);

            List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));
            processScrapedTickets(emitter, route, language, doShow, elements, emitterNotExpired);


            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in INFOBUS BUS service: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        } finally {
            driver.quit();
        }

    }

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTicketUri(SseEmitter emitter, BusTicket ticket, String language, MutableBoolean emitterNotExpired) throws IOException, ParseException {
        WebDriver driver = webDriverFactory.createInstance();

        Route route = ticket.getRoute();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        String url = determineBaseUri(language);
        requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver, url, language);

        if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

        waitForTickets(driver);

        List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));
        processTicketInfo(emitter, ticket, elements, driver, wait, emitterNotExpired);

        driver.quit();
        return CompletableFuture.completedFuture(true);
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

    private void processScrapedTickets(SseEmitter emitter, Route route, String language, Boolean doShow, List<WebElement> elements, MutableBoolean emitterNotExpired) throws ParseException, IOException {
        log.info("Bus tickets on infobus: " + elements.size());
        for (int i = 0; i < elements.size() && i < 150; i++) {
            BusTicket scrapedTicket = scrapeTicketInfo(elements.get(i), route);

            BusTicket busTicket = scrapedTicket;

            if (route.getTickets().add(scrapedTicket)) {
                if (BooleanUtils.isTrue(doShow) && emitterNotExpired.booleanValue())
                    emitter.send(SseEmitter.event().name("Infobus bus: ").data(busMapper.ticketToTicketDto(scrapedTicket, language)));
            } else
                scrapedTicket = ((BusTicket) route.getTickets().stream().filter(t -> t.equals(busTicket)).findFirst().get()).addPrice(busTicket.getInfoList().get(0));

            repository.save(scrapedTicket);
        }
    }

    private static void processTicketInfo(SseEmitter emitter, BusTicket ticket, List<WebElement> elements, WebDriver driver, WebDriverWait wait, MutableBoolean emitterNotExpired) throws IOException {

        BusInfo priceInfo = ticket.getInfoList().stream().filter(t -> t.getSourceWebsite().equals(SiteConstants.INFOBUS)).findFirst().get();

        log.info("Bus tickets on infobus: " + elements.size());
        for (WebElement element : elements) {
            String price = element.findElement(By.cssSelector("span.price-number")).getText().replace(" UAH", "");

            String departureTime = element.findElement(By.cssSelector("div.departure")).findElement(By.cssSelector("div.day_time")).findElements(By.tagName("span")).get(2).getText();

            String arrivalTime = element.findElement(By.cssSelector("div.arrival")).findElement(By.cssSelector("div.day_time")).findElements(By.tagName("span")).get(2).getText();

            if (ticket.getDepartureTime().equals(departureTime) &&
                    ticket.getArrivalTime().equals(arrivalTime) &&
                    priceInfo.getPrice().compareTo(new BigDecimal(price)) == 0) {

                WebElement button = element.findElement(By.cssSelector("button.btn"));
                Actions actions = new Actions(driver);
                actions.moveToElement(button).doubleClick().build().perform();

//                wait.until(ExpectedConditions.urlContains("deeplink"));
                priceInfo.setLink(driver.getCurrentUrl());
                log.info("INFOBUS URL: " + driver.getCurrentUrl());
                break;
            }
        }

        if (priceInfo.getLink() != null) {
            if (emitterNotExpired.booleanValue()) {
                emitter.send(SseEmitter.event().name(SiteConstants.INFOBUS).data(
                        UrlAndPriceDTO.builder()
                                .price(priceInfo.getPrice())
                                .url(priceInfo.getLink())
                                .build()));
            }
        } else log.info("INFOBUS URL NOT FOUND");
    }

    private String determineBaseUri(String language) {
        return switch (language) {
            case ("ua") -> linkProps.getInfobusUaBus();
            case ("eng") -> linkProps.getInfobusEngBus();
            default -> throw new UndefinedLanguageException();
        };
    }

    private static boolean areTicketsPresent(WebDriverWait wait, WebDriver driver) {
        try {
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET_NOT_FOUND)), ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET))));
            driver.findElement(By.cssSelector(DIV_TICKET));
            return true;
        } catch (Exception e) {
            driver.quit();
            log.info("Bus tickets on infobus: NOT FOUND");
            return false;
        }
    }

    private static BusTicket scrapeTicketInfo(WebElement webTicket, Route route) throws ParseException {
        SimpleDateFormat ticketDate = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat formattedTicketDate = new SimpleDateFormat("dd.MM, E", new Locale("uk"));

        String arrivalDate = webTicket.findElements(By.cssSelector("span.day-preffix")).get(1).getText().substring(3) + "." + Year.now().getValue();

        Date date = ticketDate.parse(arrivalDate);

        String price = webTicket.findElement(By.cssSelector("span.price-number")).getText().replace(" UAH", "").split("-")[0].trim();
        String travelTime = webTicket.findElement(By.className("duration-time")).getText().toLowerCase().replace(" г", "год.").replace(" хв", "хв");

        String[] parts = travelTime.split("[^\\d]+");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int totalMinutes = hours * 60 + minutes;

        String carrier = webTicket.findElement(By.cssSelector("span.carrier-info")).findElement(By.cssSelector("a.text-g")).getText();

        if (carrier.isEmpty()) {
            carrier = URLDecoder.decode(webTicket.findElement(By.cssSelector("span.carrier-info")).findElement(By.cssSelector("a.text-g"))
                    .getAttribute("href")).replaceAll(".*/", "");
        }
        if (carrier.indexOf('/') != -1) carrier = carrier.substring(0, carrier.indexOf('/'));

        return createTicket(webTicket, route, price, carrier.trim(), totalMinutes, formattedTicketDate.format(date));
    }


    private static void requestTickets(String departureCity, String arrivalCity, String departureDate, WebDriver driver, String url, String language) throws ParseException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(url);

        selectCity("city_from", "ui-id-1", departureCity, driver, wait);
        selectCity("city_to", "ui-id-2", arrivalCity, driver, wait);

        selectDate(departureDate, driver, wait, language);

        driver.findElement(By.id("main_form_search_button")).click();
    }

    private static void selectCity(String inputCityId, String clickableElementId, String city, WebDriver driver, WebDriverWait wait) {
        WebElement inputCity = driver.findElement(By.id(inputCityId));
        Actions actions = new Actions(driver);
        actions.moveToElement(inputCity).doubleClick().build().perform();
        inputCity.sendKeys(city);
        wait.until(ExpectedConditions.elementToBeClickable(By.id(clickableElementId))).findElements(By.tagName("li")).getFirst().click();
    }

    private static void selectDate(String departureDate, WebDriver driver, WebDriverWait wait, String language) throws ParseException {
        WebElement dateFrom = driver.findElement(By.id("dateFrom"));
        dateFrom.click();

        SimpleDateFormat inputFormat = new SimpleDateFormat("dd.MM.yyyy");
        SimpleDateFormat outputMonthFormat = new SimpleDateFormat("MMMM", new Locale("uk"));
        SimpleDateFormat outputYearFormat = new SimpleDateFormat("yyyy", new Locale("uk", "en"));
        SimpleDateFormat outputDayFormat = new SimpleDateFormat("d", new Locale("uk", "en"));

        if (language.equals("eng")) outputMonthFormat = new SimpleDateFormat("MMMM", new Locale("en"));
        if (language.equals("eng")) outputYearFormat = new SimpleDateFormat("yyyy", new Locale("en"));
        if (language.equals("eng")) outputDayFormat = new SimpleDateFormat("d", new Locale("en"));

        Date inputDate = inputFormat.parse(departureDate);

        String requestMonth = outputMonthFormat.format(inputDate);
        String requestYear = outputYearFormat.format(inputDate);
        String requestDay = outputDayFormat.format(inputDate);

        String calendarMonth = driver.findElement(By.cssSelector("span.ui-datepicker-month")).getText();
        String calendarYear = driver.findElement(By.cssSelector("span.ui-datepicker-year")).getText();

        while (!(calendarMonth.equalsIgnoreCase(requestMonth) && calendarYear.equals(requestYear))) {
            wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//a[@data-handler=\"next\"]"))).click();

            calendarMonth = driver.findElement(By.cssSelector("span.ui-datepicker-month")).getText();
            calendarYear = driver.findElement(By.cssSelector("span.ui-datepicker-year")).getText();
        }

        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("a.ui-state-default")));
        WebElement element = driver.findElements(By.cssSelector("a.ui-state-default")).stream().filter(e -> e.getText().equals(requestDay)).findFirst().get();
        Actions actions = new Actions(driver);
        actions.moveToElement(element).click().build().perform();
    }

    private static BusTicket createTicket(WebElement webTicket, Route route, String price, String carrier, int totalMinutes, String date) {
        return BusTicket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .placeFrom(webTicket.findElement(By.cssSelector("div.departure")).findElement(By.cssSelector("a.text-g")).getText())
                .placeAt(webTicket.findElement(By.cssSelector("div.arrival")).findElement(By.cssSelector("a.text-g")).getText())
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .departureTime(webTicket.findElement(By.cssSelector("div.departure")).findElement(By.cssSelector("div.day_time")).findElements(By.tagName("span")).get(2).getText())
                .arrivalTime(webTicket.findElement(By.cssSelector("div.arrival")).findElement(By.cssSelector("div.day_time")).findElements(By.tagName("span")).get(2).getText())
                .arrivalDate(date)
                .carrier(carrier.toUpperCase()).build().addPrice(BusInfo.builder().price(new BigDecimal(price)).sourceWebsite(SiteConstants.INFOBUS).build());
    }

}

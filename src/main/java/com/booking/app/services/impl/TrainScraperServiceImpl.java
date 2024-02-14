package com.booking.app.services.impl;

import com.booking.app.config.LinkProps;
import com.booking.app.entity.BusTicket;
import com.booking.app.entity.Route;
import com.booking.app.entity.TrainComfortInfo;
import com.booking.app.entity.TrainTicket;
import com.booking.app.mapper.TrainMapper;
import com.booking.app.services.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.BooleanUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
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

@Service("train")
@RequiredArgsConstructor
@Log4j2
public class TrainScraperServiceImpl implements ScraperService {

    private final LinkProps linkProps;

    private final ChromeOptions options;

    private final TrainMapper trainMapper;

    private static final String DIV_TICKET = "div.trip-adaptive";

    private static final String DIV_TICKET_NOT_FOUND = "div.error.card";

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTickets(SseEmitter emitter, Route route, String language, Boolean doShow) throws ParseException, IOException {
        ChromeDriver driver = new ChromeDriver(options);
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver, determineBaseUrl(language), language);

        try {
            wait.until(ExpectedConditions.or(ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET_NOT_FOUND)), ExpectedConditions.presenceOfElementLocated(By.cssSelector(DIV_TICKET))));
            driver.findElement(By.cssSelector(DIV_TICKET));
        } catch (Exception e) {
            driver.quit();
            log.info("PROIZD TRAIN TICKETS IN scrapeTickets(): NOT FOUND");
            return CompletableFuture.completedFuture(false);
        }

        try {
            synchronized (driver) {
                driver.wait(5000);
            }
        } catch (InterruptedException e) {
        }

        List<WebElement> elements = driver.findElements(By.cssSelector(DIV_TICKET));

        log.info("PROIZD TRAIN TICKETS IN scrapeTickets(): " + elements.size());

        for (int i = 0; i < elements.size() && i < 150; i++) {
            TrainTicket ticket = scrapeTicketInfo(elements.get(i), route, language);
            if (route.getTickets().add(ticket) && BooleanUtils.isTrue(doShow)) {
                emitter.send(SseEmitter.event().name("Proizd train: ").data(trainMapper.toTrainTicketDto(ticket, language)));
            }
        }

        return CompletableFuture.completedFuture(true);
    }

    @Override
    public String determineBaseUrl(String language) {
        return switch (language) {
            case ("ua") -> linkProps.getProizdUaTrain();
            case ("eng") -> linkProps.getProizdEngTrain();
            default -> linkProps.getProizdUaTrain();
        };
    }


    @Override
    public CompletableFuture<Boolean> getBusTicket(SseEmitter emitter, BusTicket ticket, String language) {
        //implementation is not needed
        return null;
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

        String carrier = language.equals("eng") ? "" : "Укрзалізниця";


        List<WebElement> elements = element.findElements(By.cssSelector("div.carriage"));

        List<TrainComfortInfo> list = new LinkedList<>();

        for (WebElement webElement : elements) {

            String price = webElement.findElement(By.cssSelector("div.carriage__price ")).getText().replaceAll("[^\\d\\.]+", "");

            list.add(TrainComfortInfo.builder()
                    .comfort(webElement.findElement(By.cssSelector("span.carriage__type")).getText())
                    .price(new BigDecimal(price))
                    .link(webElement.findElement(By.cssSelector("a.btn")).getAttribute("href")).build());
        }

        return createTicket(element, route, totalMinutes, formattedDate, carrier, list);
    }

    private static TrainTicket createTicket(WebElement element, Route route, int totalMinutes, String formattedTime, String carrier, List<TrainComfortInfo> list) {
        return TrainTicket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .arrivalDate(formattedTime)
                .placeFrom(element.findElement(By.cssSelector("div.trip-item__route")).getText().replace(" —.+", ""))
                .placeAt(element.findElement(By.cssSelector("div.trip-item__route")).getText().replace(".+— ", ""))
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .departureTime(element.findElements(By.cssSelector("div.trip__time ")).get(0).getText())
                .arrivalTime(element.findElements(By.cssSelector("div.trip__time ")).get(1).getText())
                .carrier(carrier)
                .infoList(list)
                .build();
    }

    private static void requestTickets(String departureCity, String arrivalCity, String departureDate, ChromeDriver driver, String url, String language) throws ParseException {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(url);
        if (language.equals("ua"))
            selectCity(wait, departureCity, "//input[@placeholder='Звідки виїзд?']", driver);
        else
            selectCity(wait, departureCity, "//input[@placeholder='Departure station']", driver);

        if (language.equals("ua"))
            selectCity(wait, arrivalCity, "//input[@placeholder='Куди прямуєте?']", driver);
        else
            selectCity(wait, arrivalCity, "//input[@placeholder='Arrival station']", driver);

        selectDate(departureDate, driver, wait, language);

        driver.findElement(By.cssSelector("button.search-btn")).click();
    }

    private static void selectCity(WebDriverWait wait, String city, String inputXpath, ChromeDriver driver) {
        WebElement inputCity = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputXpath)));
        Actions actions = new Actions(driver);
        actions.moveToElement(inputCity).doubleClick().build().perform();
        inputCity.clear();
        inputCity.sendKeys(city);
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

        driver.findElement(By.cssSelector("div.calbody")).findElements(By.tagName("li")).stream().filter(element -> element.getText().equals(requestDay)).findFirst().get().click();
    }

}

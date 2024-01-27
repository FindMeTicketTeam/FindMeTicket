package com.booking.app.services.impl;

import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.entity.Route;
import com.booking.app.entity.Ticket;
import com.booking.app.mapper.TicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Log4j2
public class ProizdScrapeServiceImpl {

    private static final String proizdLink = "https://bus.proizd.ua/";
    private final TicketMapper ticketMapper;


    @Async
    public CompletableFuture<Boolean> scrapeTickets(RequestTicketsDTO requestTicketDTO, SseEmitter emitter, Route route) throws ParseException, IOException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);

        requestTickets(requestTicketDTO.getDepartureCity(), requestTicketDTO.getArrivalCity(), requestTicketDTO.getDepartureDate(), driver);

        try {
            synchronized (driver) {
                driver.wait(5000);
            }
        } catch (InterruptedException e) {
        }

        List<WebElement> elements = driver.findElements(By.cssSelector("div.trip"));

        log.info(elements.size());
        for (WebElement element : elements) {

            Ticket ticket = scrapeTicketInfo(element, route);

            if (route.getTickets().add(ticket)) {
                emitter.send(SseEmitter.event().name("ticket data: ").data(ticketMapper.toDto(ticket)));
            }
        }

        driver.quit();
        return CompletableFuture.completedFuture(true);
    }

    private Ticket scrapeTicketInfo(WebElement element, Route route) {

//        SimpleDateFormat ticketDate = new SimpleDateFormat("d MMMM u", new Locale("uk"));
//        SimpleDateFormat formatedTicketDate = new SimpleDateFormat("dd.MM, E", new Locale("uk"));


        String arrivalDate = element.findElements(By.cssSelector("div.trip__date")).get(1).getText();
        arrivalDate = arrivalDate.substring(4);


        DateTimeFormatter ticketDate = DateTimeFormatter.ofPattern("dd MMMM yyyy", new Locale("uk", "UA"));


        LocalDate date = LocalDate.parse(arrivalDate + " 2024", ticketDate);
//        Date date = ticketDate.parse(arrivalDate);

        ticketDate = DateTimeFormatter.ofPattern("d.MM, EE", new Locale("uk"));

        String formattedTime = date.format(ticketDate);


        String price = element.findElement(By.cssSelector("div.carriage-bus__price")).getText();
        price = price.substring(0, price.length() - 6);

        String travelTime = element.findElement(By.cssSelector("div.travel-time__value")).getText();
        travelTime = travelTime.replaceFirst("г", "год.");


        String[] parts = travelTime.split("[^\\d]+");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int totalMinutes = hours * 60 + minutes;


        Ticket ticket = Ticket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .price(BigDecimal.valueOf(Long.parseLong(price)))
                .placeFrom(element.findElements(By.cssSelector("div.trip__station-address")).get(0).getText())
                .placeAt(element.findElements(By.cssSelector("div.trip__station-address")).get(1).getText())
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .departureTime(element.findElements(By.cssSelector("div.trip__time")).get(0).getText())
                .arrivalTime(element.findElements(By.cssSelector("div.trip__time")).get(1).getText())
                .arrivalDate(formattedTime).build();

        return ticket;
    }


    @Async
    public CompletableFuture<Boolean> getTicket(SseEmitter emitter, Ticket ticket) throws IOException, ParseException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        options.addArguments("--headless");
        ChromeDriver driver = new ChromeDriver(options);


        Route route = ticket.getRoute();


        requestTickets(route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate(), driver);

        try {
            synchronized (driver) {
                driver.wait(5000);
            }
        } catch (InterruptedException e) {
        }

        List<WebElement> elements = driver.findElements(By.cssSelector("div.trip"));

        for (WebElement element : elements) {

            String priceString = element.findElement(By.cssSelector("div.carriage-bus__price")).getText();
            priceString = priceString.substring(0, priceString.length() - 6);

            String departureTime = element.findElements(By.cssSelector("div.trip__time")).get(0).getText();
            String arrivalTime = element.findElements(By.cssSelector("div.trip__time")).get(1).getText();

            if (ticket.getDepartureTime().equals(departureTime) &&
                    ticket.getArrivalTime().equals(arrivalTime) &&
                    ticket.getPrice().equals(BigDecimal.valueOf(Long.parseLong(priceString)))) {
                ticket.getUrls().setProizd(element.findElement(By.cssSelector("a.btn")).getAttribute("href"));
                break;
            }
        }

        if (ticket.getUrls().getProizd() != null) {
            emitter.send(SseEmitter.event().name("Proizd url:").data(ticket.getUrls().getProizd()));

        } else {
            emitter.send(SseEmitter.event().name("Proizd url:").data("no such url"));
        }

        driver.quit();
        return CompletableFuture.completedFuture(true);
    }

    private void requestTickets(String departureCity, String arrivalCity, String departureDate, ChromeDriver driver) throws ParseException {

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        driver.get(proizdLink);

        selectCity(wait, departureCity, "//input[@placeholder='Станція відправлення']");
        selectCity(wait, arrivalCity, "//input[@placeholder='Станція прибуття']");


        selectDate(departureDate, driver, wait);

        driver.findElement(By.cssSelector("button.btn.search-form__btn")).click();
    }

    private void selectCity(WebDriverWait wait, String city, String inputXpath) {
        WebElement inputCity = wait.until(ExpectedConditions.elementToBeClickable(By.xpath(inputXpath)));
        inputCity.click();
        inputCity.sendKeys(city);
        wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("li.station-item.active.ng-star-inserted"))).click();
    }

    private void selectDate(String departureDate, WebDriver driver, WebDriverWait wait) throws ParseException {

        while (true) {
            WebElement dateFrom = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("div.search-form__field.search-form__date")));
            dateFrom.click();

            try {
                synchronized (driver) {
                    driver.wait(100);
                }
            } catch (InterruptedException e) {
            }

            String calendarMonth = driver.findElement(By.cssSelector("li.calmonth")).getText();

            if (calendarMonth.length() != 0) break;
        }

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat outputMonthFormat = new SimpleDateFormat("MMMM", new Locale("uk"));
        SimpleDateFormat outputYearFormat = new SimpleDateFormat("yyyy", new Locale("uk"));
        SimpleDateFormat outputDayFormat = new SimpleDateFormat("d", new Locale("uk"));

        Date inputDate = inputFormat.parse(departureDate);

        String requestMonth = outputMonthFormat.format(inputDate);
        String requestYear = outputYearFormat.format(inputDate);
        String requestDay = outputDayFormat.format(inputDate);

        String calendarMonth = driver.findElement(By.cssSelector("li.calmonth")).getText();
        calendarMonth = calendarMonth.substring(0, calendarMonth.length() - 5);

        String calendarYear = driver.findElement(By.cssSelector("li.calmonth")).getText();
        calendarYear = calendarYear.substring(calendarYear.length() - 4);

        while (!(calendarMonth.toLowerCase().equals(requestMonth) && calendarYear.equals(requestYear))) {

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

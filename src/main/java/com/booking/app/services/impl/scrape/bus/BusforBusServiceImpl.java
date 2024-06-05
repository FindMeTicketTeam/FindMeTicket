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
import com.booking.app.repositories.TicketRepository;
import com.booking.app.services.ScraperService;
import com.booking.app.util.ExchangeRateUtils;
import com.booking.app.util.WebDriverFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.Range;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service("busforBusService")
@RequiredArgsConstructor
@Log4j2
public class BusforBusServiceImpl implements ScraperService {
    private final TicketRepository ticketRepository;

    private final LinkProps linkProps;

    private final BusMapper busMapper;

    private final WebDriverFactory webDriverFactory;

    private final BusTicketRepository repository;

    private static final String DIV_TICKET = "div.ticket";

    private static final String DIV_TICKET_NOT_FOUND = "div.Style__EmptyTitle-xljhz5-2.iBjiPF";

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTickets(SseEmitter emitter, Route route, String language, Boolean doShow, MutableBoolean emitterNotExpired) throws IOException {

        WebDriver driver = webDriverFactory.createInstance();

        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            String url = determineBaseUri(language);
            String fulfilledUrl = String.format(url, route.getDepartureCity(), route.getArrivalCity(), route.getDepartureDate());

            driver.get(fulfilledUrl);

            if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

            waitForTickets(driver);

            List<WebElement> tickets = driver.findElements(By.cssSelector(DIV_TICKET));

            processScrapedTickets(emitter, route, language, doShow, tickets, driver, wait, emitterNotExpired);
            return CompletableFuture.completedFuture(true);
        } catch (Exception e) {
            log.error("Error in BUSFOR BUS service: " + e.getMessage());
            return CompletableFuture.completedFuture(false);
        } finally {
            driver.quit();
        }
    }

    @Async
    @Override
    public CompletableFuture<Boolean> scrapeTicketUri(SseEmitter emitter, BusTicket ticket, String language, MutableBoolean emitterNotExpired) throws IOException {
        WebDriver driver = webDriverFactory.createInstance();

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        String departureCity = ticket.getRoute().getDepartureCity();
        String arrivalCity = ticket.getRoute().getArrivalCity();
        String departureDate = ticket.getRoute().getDepartureDate();

        String url = determineBaseUri(language);
        String fulfilledUrl = String.format(url, departureCity, arrivalCity, departureDate);

        driver.get(fulfilledUrl);
        if (!areTicketsPresent(wait, driver)) return CompletableFuture.completedFuture(false);

        waitForTickets(driver);

        List<WebElement> tickets = driver.findElements(By.cssSelector(DIV_TICKET));
        processTicketInfo(emitter, ticket, language, tickets, wait, driver, emitterNotExpired);

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

    private static void processTicketInfo(SseEmitter emitter, BusTicket ticket, String language, List<WebElement> tickets, WebDriverWait wait, WebDriver driver, MutableBoolean emitterNotExpired) throws IOException {
        log.info("Bus tickets on busfor: " + tickets.size());
        BigDecimal currentUAH = null;

        BusInfo priceInfo = ticket.getInfoList().stream().filter(t -> t.getSourceWebsite().equals(SiteConstants.BUSFOR_UA)).findFirst().get();

        if (language.equals("eng"))
            currentUAH = ExchangeRateUtils.getCurrentExchangeRate("PLN", "UAH");
        for (WebElement element : tickets) {


            List<WebElement> ticketInfo = element.findElements(By.cssSelector("div.Style__Item-yh63zd-7.kAreny"));

            WebElement departureInfo = ticketInfo.get(0);
            WebElement arrivalInfo = ticketInfo.get(1);

            String departureDateTime = departureInfo.findElement(By.cssSelector("div.Style__Time-sc-1n9rkhj-0.bmnWRj")).getText();
            String arrivalDateTime = arrivalInfo.findElement(By.cssSelector("div.Style__Time-sc-1n9rkhj-0.bmnWRj")).getText();

            BigDecimal price = new BigDecimal(element.findElement(By.cssSelector("span.price")).getText().replace(",", ".").trim());
            if (language.equals("eng")) price = currentUAH.multiply(price);
            BigDecimal difference = priceInfo.getPrice().subtract(price);
            Range<Integer> range = Range.between(-2, 2);

            if (range.contains(difference.intValue()) &&
                    ticket.getArrivalTime().equals(arrivalDateTime.substring(0, 5)) &&
                    ticket.getDepartureTime().equals(departureDateTime.substring(0, 5))) {

                WebElement button = wait.until(ExpectedConditions.elementToBeClickable(element.findElement(By.tagName("button")).findElement(By.tagName("span"))));
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", button);

                wait.until(ExpectedConditions.or(ExpectedConditions.urlContains("preorders"), (ExpectedConditions.urlContains("checkout"))));

                priceInfo.setLink(driver.getCurrentUrl());
                log.info("BUSFOR URL: " + driver.getCurrentUrl());
                break;
            }
        }

        if (priceInfo.getLink() != null) {
            if (emitterNotExpired.booleanValue()) {
                emitter.send(SseEmitter.event().name(SiteConstants.BUSFOR_UA).data(UrlAndPriceDTO.builder()
                        .price(priceInfo.getPrice())
                        .url(priceInfo.getLink())
                        .build()));
            }
        } else log.info("BUSFOR URL NOT FOUND");
    }

    private void processScrapedTickets(SseEmitter emitter, Route route, String language, Boolean doShow, List<WebElement> tickets, WebDriver driver, WebDriverWait wait, MutableBoolean emitterNotExpired) throws IOException {
        log.info("Bus tickets on busfor: " + tickets.size());
        BigDecimal currentUAH = null;
        if (language.equals("eng"))
            currentUAH = ExchangeRateUtils.getCurrentExchangeRate("PLN", "UAH");

        for (int i = 0; i < tickets.size() && i < 150; i++) {
            WebElement webTicket = driver.findElements(By.cssSelector(DIV_TICKET)).get(i);
            BusTicket scrapedTicket = scrapeTicketInfo(webTicket, route, currentUAH, language, wait);

            BusTicket busTicket = scrapedTicket;

            if (route.getTickets().add(scrapedTicket)) {
                if (BooleanUtils.isTrue(doShow) && emitterNotExpired.booleanValue())
                    emitter.send(SseEmitter.event().name("Busfor bus: ").data(busMapper.ticketToTicketDto(scrapedTicket, language)));

            } else
                scrapedTicket = ((BusTicket) route.getTickets().stream().filter(t -> t.equals(busTicket)).findFirst().get()).addPrice(busTicket.getInfoList().get(0));

            ticketRepository.save(scrapedTicket);
        }
    }

    private static String formatDate(String inputDate, String language) {
        DateTimeFormatter formatter;
        DateTimeFormatter resultFormatter;

        return switch (language) {
            case "ua" -> {
                formatter = DateTimeFormatter.ofPattern("d MMM u", new Locale("uk"));
                resultFormatter = DateTimeFormatter.ofPattern("dd.MM, EEE", new Locale("uk"));
                LocalDate date = LocalDate.parse(inputDate + " " + Year.now().getValue(), formatter);
                yield date.format(resultFormatter);
            }
            case "eng" -> {
                formatter = DateTimeFormatter.ofPattern("d MMM u", new Locale("en"));
                resultFormatter = DateTimeFormatter.ofPattern("dd.MM, EEE", new Locale("en"));
                LocalDate date = LocalDate.parse(inputDate + " " + Year.now().getValue(), formatter);
                yield date.format(resultFormatter);
            }
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
            log.info("Bus tickets on busfor: : NOT FOUND");
            return false;
        }
    }


    private static BusTicket scrapeTicketInfo(WebElement webTicket, Route route, BigDecimal currentRate, String language, WebDriverWait wait) {

        String carrier = webTicket.findElement(By.cssSelector("div.Style__Information-sc-13gvs4g-6.jBuzam > div.Style__Carrier-sc-13gvs4g-3.gUvIjh > span:nth-child(2)")).getText().toUpperCase();

        List<WebElement> ticketInfo = webTicket.findElements(By.cssSelector("div.Style__Item-yh63zd-7.kAreny"));

        WebElement departureInfo = ticketInfo.get(0);
        WebElement arrivalInfo = ticketInfo.get(1);

        String departureDateTime = departureInfo.findElement(By.cssSelector("div.Style__Time-sc-1n9rkhj-0.bmnWRj")).getText();
        String arrivalDateTime = arrivalInfo.findElement(By.cssSelector("div.Style__Time-sc-1n9rkhj-0.bmnWRj")).getText();
        String travelTime = webTicket.findElement(By.cssSelector("span.Style__TimeInRoad-yh63zd-0.btMUVs")).getText();
        travelTime = travelTime.substring(0, travelTime.length() - 9);

        String[] parts = travelTime.split("[^\\d]+");
        int hours = Integer.parseInt(parts[0]);
        int minutes = 0;
        if (parts.length == 2) {
            minutes = Integer.parseInt(parts[1]);
        }
        int totalMinutes = hours * 60 + minutes;

        BigDecimal price = new BigDecimal(webTicket.findElement(By.cssSelector("span.price")).getText().replace(",", ".").trim());
        String arrivalDate = formatDate(arrivalDateTime.substring(6), language);

        if (language.equals("eng"))
            price = currentRate.multiply(price).setScale(2, RoundingMode.HALF_UP);

        return createTicket(route, departureInfo, arrivalInfo, departureDateTime, arrivalDateTime.substring(0, 5), arrivalDate, totalMinutes, price, carrier);
    }

    private String determineBaseUri(String language) {
        return switch (language) {
            case ("ua") -> linkProps.getBusforUaBus();
            case ("eng") -> linkProps.getBusforEngBus();
            default -> throw new UndefinedLanguageException();
        };
    }

    private static BusTicket createTicket(Route route, WebElement departureInfo, WebElement
            arrivalInfo, String departureDateTime, String arrivalDateTime, String arrivalDate, int totalMinutes, BigDecimal price, String carrier) {
        return BusTicket.builder()
                .id(UUID.randomUUID())
                .route(route)
                .placeFrom(departureInfo.findElement(By.cssSelector("div.LinesEllipsis")).getText())
                .placeAt(arrivalInfo.findElement(By.cssSelector("div.LinesEllipsis")).getText())
                .departureTime(departureDateTime.substring(0, 5))
                .arrivalTime(arrivalDateTime)
                .arrivalDate(arrivalDate)
                .travelTime(BigDecimal.valueOf(totalMinutes))
                .carrier(carrier).build().addPrice(BusInfo.builder().price(price).sourceWebsite(SiteConstants.BUSFOR_UA).build());
    }

}

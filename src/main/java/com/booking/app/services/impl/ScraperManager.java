package com.booking.app.services.impl;

import com.booking.app.dto.RequestTicketsDTO;
import com.booking.app.dto.UrlAndPriceDTO;
import com.booking.app.entity.BusTicket;
import com.booking.app.entity.Route;
import com.booking.app.entity.Ticket;
import com.booking.app.entity.TrainTicket;
import com.booking.app.exception.exception.UndefinedLanguageException;
import com.booking.app.mapper.BusMapper;
import com.booking.app.mapper.TrainMapper;
import com.booking.app.repositories.BusTicketRepository;
import com.booking.app.repositories.RouteRepository;
import com.booking.app.repositories.TicketRepository;
import com.booking.app.repositories.TrainTicketRepository;
import com.booking.app.services.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static com.booking.app.constant.SiteConstants.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class ScraperManager {

    private final TrainTicketRepository trainTicketRepository;

    @Qualifier("busfor")
    private final ScraperService busforService;

    @Qualifier("infobus")
    private final ScraperService infobusService;

    @Qualifier("proizd")
    private final ScraperService proizdService;

    @Qualifier("train")
    private final ScraperService trainService;

    private final RouteRepository routeRepository;

    private final BusTicketRepository busTicketRepository;

    private final TicketRepository ticketRepository;

    private final BusMapper busMapper;

    private final TrainMapper trainMapper;

    @Async
    public CompletableFuture<Boolean> scrapeTickets(RequestTicketsDTO requestTicketDTO, SseEmitter emitter, String language) throws IOException, ParseException, UndefinedLanguageException {

        Route route = routeRepository.findByDepartureCityAndArrivalCityAndDepartureDate(requestTicketDTO.getDepartureCity(), requestTicketDTO.getArrivalCity(), requestTicketDTO.getDepartureDate());

        Route newRoute = null;
        if (route == null) {
            newRoute = createRoute(requestTicketDTO);

            List<CompletableFuture<Boolean>> completableFutureListBus = completableFutureListBuses(emitter, newRoute, language, requestTicketDTO.getBus(), requestTicketDTO.getTrain());
            CompletableFuture<Void> allOf = CompletableFuture.allOf(completableFutureListBus.toArray(CompletableFuture[]::new));
            try {
                allOf.join();
            } catch (CancellationException | CompletionException e) {
                log.error("Error in Scraper service, scrapeTickets() method: " + e.getMessage());
                emitter.completeWithError(e);
                return CompletableFuture.completedFuture(false);
            }
            if (completableFutureListBus.stream().anyMatch(t -> {
                try {
                    return t.get().equals(true);
                } catch (ExecutionException | InterruptedException e) {
                    log.warn("Interrupted while waiting for CompletableFuture result.", e);
                    /* Clean up whatever needs to be handled before interrupting  */
                    Thread.currentThread().interrupt();
                    return false;
                }
            })) {
                routeRepository.save(newRoute);
            }
        } else {
            for (Ticket ticket : route.getTickets()) {
                if (ticket instanceof BusTicket && requestTicketDTO.getBus()) {
                    emitter.send(SseEmitter.event().name("ticket data: ").data(busMapper.ticketToTicketDto((BusTicket) ticket, language)));
                }
                if (ticket instanceof TrainTicket && requestTicketDTO.getTrain()) {
                    emitter.send(SseEmitter.event().name("ticket data: ").data(trainMapper.toTrainTicketDto((TrainTicket) ticket, language)));
                }
            }
        }

        emitter.complete();

        return route == null && newRoute.getTickets().isEmpty() ?
                CompletableFuture.completedFuture(false) : CompletableFuture.completedFuture(true);
    }

    @Async
    public CompletableFuture<Boolean> getTicket(UUID id, SseEmitter emitter, String language) throws IOException, ParseException, UndefinedLanguageException {

        Ticket ticket = ticketRepository.findById(id).orElseGet(Ticket::new);

        return switch (ticket) {
            case BusTicket e -> sendBus(e, emitter, language);
            case TrainTicket e -> sendTrain(e, emitter, language);
            default -> {
                emitter.complete();
                yield CompletableFuture.completedFuture(false);
            }
        };

    }

    private CompletableFuture<Boolean> sendTrain(TrainTicket trainTicket, SseEmitter emitter, String language) throws IOException {

        emitter.send(SseEmitter.event().name("ticket info").data(trainMapper.toTrainTicketDto(trainTicket, language)));

        trainTicket.getInfoList().forEach(t -> {
            try {
                emitter.send(SseEmitter.event().name(PROIZD_UA).data(trainMapper.toTrainComfortInfoDTO(t)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        emitter.complete();
        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Boolean> sendBus(BusTicket busTicket, SseEmitter emitter, String language) throws IOException, ParseException, UndefinedLanguageException {

        emitter.send(SseEmitter.event().name("ticket info").data(busMapper.ticketToTicketDto(busTicket, language)));

        if (!busTicket.linksAreScraped()) {

            List<CompletableFuture<Boolean>> completableFutureListBus = new LinkedList<>();

            if (busTicket.getBusforPrice() != null)
                completableFutureListBus.add(busforService.getBusTicket(emitter, busTicket, language));
            if (busTicket.getInfobusPrice() != null)
                completableFutureListBus.add(infobusService.getBusTicket(emitter, busTicket, language));
            if (busTicket.getProizdPrice() != null)
                completableFutureListBus.add(proizdService.getBusTicket(emitter, busTicket, language));

            CompletableFuture<Void> allOf = CompletableFuture.allOf(completableFutureListBus.toArray((CompletableFuture[]::new)));

            try {
                allOf.join();
            } catch (CancellationException | CompletionException e) {
                log.error("Error in Scraper service, scrapeTickets() method: " + e.getMessage());
                emitter.completeWithError(e);
                return CompletableFuture.completedFuture(false);
            }

            busTicketRepository.save(busTicket);
        } else {
            if (busTicket.getProizdLink() != null) {
                emitter.send(SseEmitter.event().name(PROIZD_UA).data(UrlAndPriceDTO.builder()
                        .price(busTicket.getProizdPrice())
                        .url(busTicket.getProizdLink())
                        .build()));
                log.info("PROIZD URL IN single getTicket() INSTANTLY:" + busTicket.getProizdLink());
            }
            if (busTicket.getBusforLink() != null) {
                emitter.send(SseEmitter.event().name(BUSFOR_UA).data(UrlAndPriceDTO.builder()
                        .price(busTicket.getBusforPrice())
                        .url(busTicket.getBusforLink())
                        .build()));
                log.info("BUSFOR URL IN single getTicket() INSTANTLY: " + busTicket.getBusforLink());
            }
            if (busTicket.getInfobusLink() != null) {
                emitter.send(SseEmitter.event().name(INFOBUS).data(
                        UrlAndPriceDTO.builder()
                                .price(busTicket.getInfobusPrice())
                                .url(busTicket.getInfobusLink())
                                .build()));
                log.info("INFOBUS URL IN single getTicket() INSTANTLY:  " + busTicket.getInfobusLink());
            }

        }
        emitter.complete();
        return CompletableFuture.completedFuture(true);
    }

    private static Route createRoute(RequestTicketsDTO requestTicketDTO) {
        return Route.builder()
                .addingTime(LocalDateTime.now())
                .departureCity(requestTicketDTO.getDepartureCity())
                .arrivalCity(requestTicketDTO.getArrivalCity())
                .departureDate(requestTicketDTO.getDepartureDate())
                .build();
    }

    private List<CompletableFuture<Boolean>> completableFutureListBuses(SseEmitter emitter, Route newRoute, String language, Boolean doBus, Boolean doTrain) throws ParseException, IOException, UndefinedLanguageException {
        return Arrays.asList(
                infobusService.scrapeTickets(emitter, newRoute, language, doBus),
                proizdService.scrapeTickets(emitter, newRoute, language, doBus),
                busforService.scrapeTickets(emitter, newRoute, language, doBus),
                trainService.scrapeTickets(emitter, newRoute, language, doTrain)
        );
    }

}
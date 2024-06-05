package com.booking.app.services.impl.scrape;

import com.booking.app.dto.RequestTicketsDto;
import com.booking.app.dto.UrlAndPriceDTO;
import com.booking.app.entity.ticket.Route;
import com.booking.app.entity.ticket.Ticket;
import com.booking.app.entity.ticket.bus.BusTicket;
import com.booking.app.entity.ticket.train.TrainTicket;
import com.booking.app.mapper.BusMapper;
import com.booking.app.mapper.RouteMapper;
import com.booking.app.mapper.TrainMapper;
import com.booking.app.repositories.BusTicketRepository;
import com.booking.app.repositories.RouteRepository;
import com.booking.app.repositories.TicketRepository;
import com.booking.app.services.ScraperService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

import static com.booking.app.constant.SiteConstants.*;

@Service
@Log4j2
@RequiredArgsConstructor
public class ScraperManager {

    @Qualifier("busforBusService")
    private final ScraperService busforBusService;

    @Qualifier("infobusBusService")
    private final ScraperService infobusBusService;

    @Qualifier("proizdBusService")
    private final ScraperService proizdBusService;

    @Qualifier("proizdTrainService")
    private final ScraperService proizdTrainService;

    @Qualifier("gdtickets")
    private final ScraperService gdticketsBusService;

    private final RouteRepository routeRepository;

    private final BusTicketRepository busTicketRepository;

    private final TicketRepository ticketRepository;

    private final BusMapper busMapper;

    private final TrainMapper trainMapper;

    private final RouteMapper routeMapper;

    @Async
    public CompletableFuture<Boolean> findTickets(RequestTicketsDto requestTicketDTO, SseEmitter emitter, String language) throws IOException, ParseException {

        CompletableFuture<Boolean> result = sendTickets(requestTicketDTO, emitter, language);

        if (emitter != null) emitter.complete();

        return result;
    }

    private CompletableFuture<Boolean> sendTickets(RequestTicketsDto requestTicketDTO, SseEmitter emitter, String language) throws ParseException, IOException {

        Route route = routeRepository.findByDepartureCityAndArrivalCityAndDepartureDate(requestTicketDTO.getDepartureCity(), requestTicketDTO.getArrivalCity(), requestTicketDTO.getDepartureDate());

        MutableBoolean emitterNotExpired = new MutableBoolean(true);
        configureEmitter(emitter, emitterNotExpired);

        if (route == null) {
            return scrapeTickets(requestTicketDTO, emitter, language, emitterNotExpired);
        } else {
            return extractTickets(requestTicketDTO, emitter, language, route, emitterNotExpired);
        }
    }

    private CompletableFuture<Boolean> extractTickets(RequestTicketsDto requestTicketDTO, SseEmitter emitter, String language, Route route, MutableBoolean emitterNotExpired) throws IOException {
        if (route.getTickets().isEmpty()) return CompletableFuture.completedFuture(false);

        List<Ticket> tickets = ticketRepository.findByRouteId(route.getId());

        for (Ticket ticket : tickets) {
            if (emitterNotExpired.booleanValue()) {
                if (ticket instanceof BusTicket && requestTicketDTO.getBus()) {
                    emitter.send(SseEmitter.event().name("Ticket bus data: ").data(busMapper.ticketToTicketDto((BusTicket) ticket, language)));
                }
                if (ticket instanceof TrainTicket && requestTicketDTO.getTrain()) {
                    emitter.send(SseEmitter.event().name("Ticket train data: ").data(trainMapper.toTrainTicketDto((TrainTicket) ticket, language)));
                }
            }
        }
        return CompletableFuture.completedFuture(true);
    }

    private CompletableFuture<Boolean> scrapeTickets(RequestTicketsDto requestTicketDTO, SseEmitter emitter, String language, MutableBoolean emitterNotExpired) throws ParseException, IOException {

        Route route = routeMapper.toRoute(requestTicketDTO);
        routeRepository.save(route);

        List<CompletableFuture<Boolean>> completableFutureListBus = completableFutureListBuses(emitter, route, language, requestTicketDTO.getBus(), requestTicketDTO.getTrain(), emitterNotExpired);

        CompletableFuture<Void> allOf = CompletableFuture.allOf(completableFutureListBus.toArray(CompletableFuture[]::new));

        try {
            allOf.join();
        } catch (CancellationException | CompletionException e) {
            log.error("Error in Scraper service, scrapeTickets() method: " + e.getMessage());
            if (emitter != null) emitter.completeWithError(e);
            return CompletableFuture.completedFuture(false);
        }

        return CompletableFuture.completedFuture(completableFutureListBus.stream().map(t -> {
            try {
                return t.get();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).filter(t -> !t.booleanValue()).findFirst().isEmpty());

    }

    private void configureEmitter(SseEmitter emitter, MutableBoolean emitterNotExpired) {

        Runnable expire = () -> emitterNotExpired.setValue(false);
        if (Objects.nonNull(emitter)) {
            emitter.onCompletion(expire);
            emitter.onError(t -> expire.run());
            emitter.onTimeout(expire);
        }
    }

    @Async
    public CompletableFuture<Boolean> getTicket(UUID id, SseEmitter emitter, String language) throws
            IOException, ParseException {

        Ticket ticket = ticketRepository.findById(id).orElseGet(Ticket::new);

        MutableBoolean emitterNotExpired = new MutableBoolean(true);
        configureEmitter(emitter, emitterNotExpired);

        return switch (ticket) {
            case BusTicket e -> getBusTicket(e, emitter, language, emitterNotExpired);
            case TrainTicket e -> getTrainTicket(e, emitter, language);
            default -> {
                emitter.complete();
                yield CompletableFuture.completedFuture(false);
            }
        };

    }

    private CompletableFuture<Boolean> getTrainTicket(TrainTicket trainTicket, SseEmitter emitter, String language) throws
            IOException {

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

    private CompletableFuture<Boolean> getBusTicket(BusTicket busTicket, SseEmitter emitter, String language, MutableBoolean emitterNotExpired) throws
            IOException, ParseException {

        emitter.send(SseEmitter.event().name("ticket info").data(busMapper.ticketToTicketDto(busTicket, language)));

        if (!busTicket.linksAreScraped()) {

            CompletableFuture<Void> allOf = scrapeBusLink(busTicket, emitter, language, emitterNotExpired);

            try {
                allOf.join();
            } catch (CancellationException | CompletionException e) {
                log.error("Error in Scraper service, scrapeTickets() method: " + e.getMessage());
                emitter.completeWithError(e);
                return CompletableFuture.completedFuture(false);
            }

            busTicketRepository.save(busTicket);

        } else {
            extractBusPriceAndLink(busTicket, emitter);
        }

        if (emitter != null) {
            emitter.complete();
        }
        return CompletableFuture.completedFuture(true);
    }


    private CompletableFuture<Void> scrapeBusLink(BusTicket busTicket, SseEmitter emitter, String language, MutableBoolean emitterNotExpired) {

        List<CompletableFuture<Boolean>> completableFutureListBus = new LinkedList<>();

        busTicket.getInfoList().forEach(t -> {
            try {
                switch (t.getSourceWebsite()) {
                    case PROIZD_UA ->
                            completableFutureListBus.add(proizdBusService.scrapeTicketUri(emitter, busTicket, language, emitterNotExpired));
                    case BUSFOR_UA ->
                            completableFutureListBus.add(busforBusService.scrapeTicketUri(emitter, busTicket, language, emitterNotExpired));
                    case INFOBUS ->
                            completableFutureListBus.add(infobusBusService.scrapeTicketUri(emitter, busTicket, language, emitterNotExpired));
                }
            } catch (IOException | ParseException e) {
            }
        });

        return CompletableFuture.allOf(completableFutureListBus.toArray((CompletableFuture[]::new)));
    }


    private void extractBusPriceAndLink(BusTicket busTicket, SseEmitter emitter) {
        busTicket.getInfoList().forEach(t -> {
            try {
                emitter.send(SseEmitter.event().name(t.getSourceWebsite()).data(UrlAndPriceDTO.builder()
                        .price(t.getPrice())
                        .url(t.getLink())
                        .build()));
                log.info(t.getSourceWebsite() + " URL IN single getTicket() INSTANTLY:" + t.getLink());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }


    private List<CompletableFuture<Boolean>> completableFutureListBuses(SseEmitter emitter, Route newRoute, String
            language, Boolean doDisplay, Boolean doTrain, MutableBoolean emitterNotExpired) throws ParseException, IOException {
        return Arrays.asList(
                infobusBusService.scrapeTickets(emitter, newRoute, language, doDisplay, emitterNotExpired),
                proizdBusService.scrapeTickets(emitter, newRoute, language, doDisplay, emitterNotExpired),
                busforBusService.scrapeTickets(emitter, newRoute, language, doDisplay, emitterNotExpired),
//                  gdticketsBusService.scrapeTickets(emitter, newRoute, language, doDisplay)
                proizdTrainService.scrapeTickets(emitter, newRoute, language, doTrain, emitterNotExpired)
        );
    }

}

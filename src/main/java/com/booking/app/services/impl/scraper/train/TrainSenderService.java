package com.booking.app.services.impl.scraper.train;

import com.booking.app.constants.Website;
import com.booking.app.entities.ticket.train.TrainTicket;
import com.booking.app.mappers.TrainMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.io.UncheckedIOException;

@Service
@RequiredArgsConstructor
public class TrainSenderService {

    private final TrainMapper trainMapper;

    public void sendTicket(TrainTicket trainTicket, SseEmitter emitter, String language) throws IOException {
        emitter.send(SseEmitter.event().name("Ticket info").data(trainMapper.toTrainTicketDto(trainTicket, language)));

        trainTicket.getInfoList().forEach(t -> {
            try {
                emitter.send(SseEmitter.event().name(String.valueOf(Website.PROIZD)).data(trainMapper.toTrainComfortInfoDTO(t)));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

}
package com.booking.app.props;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("link")
@Setter
@Getter
public class LinkProps {
    private String busforUaBus;
    private String busforEngBus;

    private String infobusUaBus;
    private String infobusEngBus;

    private String infobusEngTrain;
    private String infobusUaTrain;

    private String proizdUaBus;
    private String proizdEngBus;

    private String proizdEngTrain;
    private String proizdUaTrain;

    private String ticketsUaTrain;
    private String ticketsEngTrain;

}

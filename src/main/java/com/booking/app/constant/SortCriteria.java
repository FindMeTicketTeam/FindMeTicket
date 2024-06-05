package com.booking.app.constant;

import lombok.Getter;

@Getter
public enum SortCriteria {

    PRICE("Price"),

    DEPARTURE_TIME("DepartureTime"),

    ARRIVAL_TIME("ArrivalTime"),

    TRAVEL_TIME("TravelTime");

    private final String criteria;

    SortCriteria(String criteria) {
        this.criteria = criteria;
    }

}

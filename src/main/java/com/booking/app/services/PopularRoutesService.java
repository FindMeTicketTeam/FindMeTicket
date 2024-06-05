package com.booking.app.services;

import java.io.IOException;
import java.text.ParseException;

public interface PopularRoutesService {

    /**
     * Finds popular routes and initiates ticket searches for these routes asynchronously.
     *
     * @throws IOException    if an I/O error occurs during the ticket search
     * @throws ParseException if there is an error in parsing the date
     */
    void findRoutes() throws IOException, ParseException;

}

CREATE TABLE trip_flight (
    trip_id UUID NOT NULL,
    flight_id UUID NOT NULL,

    CONSTRAINT pk_trip_flight
                         PRIMARY KEY (trip_id, flight_id),

    CONSTRAINT fk_trip_flight_trip
                         FOREIGN KEY (trip_id)
                         REFERENCES trip (id)
                         ON DELETE CASCADE,

    CONSTRAINT fk_trip_flight_flight
                         FOREIGN KEY (flight_id)
                         REFERENCES flights (id)
                         ON DELETE CASCADE
);

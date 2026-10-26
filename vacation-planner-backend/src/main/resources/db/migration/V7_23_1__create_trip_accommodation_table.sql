CREATE TABLE trip_accommodation (
                                    trip_id UUID NOT NULL,
                                    accommodation_id UUID NOT NULL,

                                    PRIMARY KEY (trip_id, accommodation_id),

                                    CONSTRAINT fk_trip_accommodation_trip
                                        FOREIGN KEY (trip_id)
                                            REFERENCES trip(id),

                                    CONSTRAINT fk_trip_accommodation_accommodation
                                        FOREIGN KEY (accommodation_id)
                                            REFERENCES accommodation(id)
);

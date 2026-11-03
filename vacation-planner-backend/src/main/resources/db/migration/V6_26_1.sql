CREATE TABLE trip_restaurant(
    id UUID PRIMARY KEY,
    trip_id UUID NOT NULL,
    restaurant_id UUID NOT NULL,
    visit_date DATE NOT NULL,
    visit_time TIME NOT NULL,

    CONSTRAINT fk_trip_restaurant_trip
        FOREIGN KEY (trip_id) REFERENCES trip(id),
    CONSTRAINT fk_trip_restaurant_restaurant
        FOREIGN KEY (restaurant_id) REFERENCES restaurant(id),
    CONSTRAINT uq_trip_restaurant_visit
        UNIQUE (trip_id, restaurant_id, visit_date, visit_time)
);

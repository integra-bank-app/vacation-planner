
create table flights (
                         id uuid primary key,
                         flight_number varchar(255) not null unique,
                         departure_airport_code varchar(255) not null,
                         arrival_airport_code varchar(255) not null,
                         departure_time timestamp with time zone not null,
                         arrival_time timestamp with time zone not null,
                         number_of_seats integer not null
);


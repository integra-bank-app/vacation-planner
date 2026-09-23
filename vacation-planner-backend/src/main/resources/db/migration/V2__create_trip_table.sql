CREATE TABLE trip (
                      id BIGSERIAL PRIMARY KEY,
                      destination VARCHAR(255) NOT NULL,
                      start_date DATE NOT NULL,
                      end_date DATE NOT NULL,
                      username VARCHAR(255) NOT NULL
);
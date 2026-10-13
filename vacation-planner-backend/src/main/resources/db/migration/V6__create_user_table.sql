CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       username VARCHAR(20) UNIQUE NOT NULL,
                       email VARCHAR(255) UNIQUE NOT NULL,
                       first_name VARCHAR(50) NOT NULL,
                       last_name VARCHAR(50) NOT NULL
);
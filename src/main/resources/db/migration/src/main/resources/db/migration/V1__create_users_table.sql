CREATE TABLE users (
                       id SERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       email VARCHAR(255) UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       birthDate TIMESTAMP WITH TIME ZONE NOT NULL,
                       height  DECIMAL NOT NULL,
                       weight  DECIMAL NOT NULL,
                       gender VARCHAR(255) NOT NULL
);
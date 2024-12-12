CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255),
    role VARCHAR(255),
    PRIMARY KEY (id)
    );

--Таблица с данными о погоде от одного сервиса
CREATE TABLE weather(
    id SERIAL PRIMARY KEY,
    serviceName VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    temperature DECIMAL(5,2) NOT NULL,
    metcast VARCHAR(255) NOT NULL,
    dateAndTime TIMESTAMP NOT NULL
);

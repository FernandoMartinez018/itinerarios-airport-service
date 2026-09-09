CREATE TABLE airports (
    id BIGSERIAL PRIMARY KEY,
    external_id VARCHAR(50) UNIQUE NOT NULL,
    iata_code VARCHAR(10) UNIQUE,
    icao_code VARCHAR(10) UNIQUE,
    name VARCHAR(150) NOT NULL,
    city VARCHAR(100) NOT NULL,
    department VARCHAR(100),
    latitude DECIMAL(10,7),
    longitude DECIMAL(10,7),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_airports_iata_code ON airports (iata_code);
CREATE INDEX idx_airports_city ON airports (city);

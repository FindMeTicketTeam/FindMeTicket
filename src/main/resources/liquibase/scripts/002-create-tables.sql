CREATE TABLE bus_info
(
    id            UUID NOT NULL,
    link          VARCHAR(1000),
    website       WEBSITE,
    price         DECIMAL(10, 2),
    bus_ticket_id UUID,
    CONSTRAINT pk_businfo PRIMARY KEY (id)
);

CREATE TABLE confirmation_code
(
    id          UUID                        NOT NULL,
    code        VARCHAR(255)                NOT NULL,
    expiry_time TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    CONSTRAINT pk_confirmationcode PRIMARY KEY (id)
);

CREATE TABLE review
(
    id          UUID    NOT NULL,
    review_text VARCHAR(1000),
    adding_date TIMESTAMP WITHOUT TIME ZONE,
    grade       INTEGER NOT NULL,
    user_id     UUID,
    CONSTRAINT pk_review PRIMARY KEY (id)
);

CREATE TABLE route
(
    id             UUID NOT NULL,
    departure_city VARCHAR(255),
    arrival_city   VARCHAR(255),
    departure_date date,
    adding_time    TIMESTAMP WITHOUT TIME ZONE,
    CONSTRAINT pk_route PRIMARY KEY (id)
);

CREATE TABLE search_history
(
    id             UUID NOT NULL,
    departure_city BIGINT,
    arrival_city   BIGINT,
    departure_date date,
    adding_time    TIMESTAMP WITHOUT TIME ZONE,
    type           TRANSPORT[],
    user_id        UUID,
    CONSTRAINT pk_searchhistory PRIMARY KEY (id)
);

CREATE TABLE ticket
(
    id                UUID NOT NULL,
    place_from        VARCHAR(255),
    place_at          VARCHAR(255),
    type              TRANSPORT,
    departure_time    time WITHOUT TIME ZONE,
    arrival_date_time TIMESTAMP WITHOUT TIME ZONE,
    travel_time       BIGINT,
    carrier           VARCHAR(255),
    route_id          UUID,
    CONSTRAINT pk_ticket PRIMARY KEY (id)
);

CREATE TABLE train_info
(
    id              UUID NOT NULL,
    link            VARCHAR(1000),
    website         WEBSITE,
    comfort         VARCHAR(255),
    price           DECIMAL(10, 2),
    train_ticket_id UUID,
    CONSTRAINT pk_traininfo PRIMARY KEY (id)
);

CREATE TABLE ukrainian_cities
(
    id       BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
    name_eng VARCHAR(255),
    country  VARCHAR(255),
    lon      DOUBLE PRECISION,
    lat      DOUBLE PRECISION,
    name_ua  VARCHAR(255),
    name_ru  VARCHAR(255),
    CONSTRAINT pk_ukrainiancities PRIMARY KEY (id)
);

CREATE TABLE "user"
(
    id                  UUID    NOT NULL,
    email               VARCHAR(255),
    password            VARCHAR(255),
    username            VARCHAR(255),
    registration_date   date,
    phone_number        VARCHAR(255),
    default_avatar      BYTEA,
    social_media_avatar VARCHAR(255),
    notification        BOOLEAN NOT NULL,
    expired             BOOLEAN,
    locked              BOOLEAN,
    credentials_expired BOOLEAN,
    enabled             BOOLEAN,
    providers           AUTH_PROVIDER[],
    roles               ROLE[],
    code_id             UUID,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

ALTER TABLE review
    ADD CONSTRAINT uc_review_user UNIQUE (user_id);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_code UNIQUE (code_id);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_email UNIQUE (email);

ALTER TABLE "user"
    ADD CONSTRAINT uc_user_phonenumber UNIQUE (phone_number);

CREATE UNIQUE INDEX idx_email ON "user" (email);

ALTER TABLE bus_info
    ADD CONSTRAINT FK_BUSINFO_ON_BUS_TICKET FOREIGN KEY (bus_ticket_id) REFERENCES ticket (id);

ALTER TABLE review
    ADD CONSTRAINT FK_REVIEW_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE search_history
    ADD CONSTRAINT FK_SEARCHHISTORY_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);

ALTER TABLE ticket
    ADD CONSTRAINT FK_TICKET_ON_ROUTE FOREIGN KEY (route_id) REFERENCES route (id);

ALTER TABLE train_info
    ADD CONSTRAINT FK_TRAININFO_ON_TRAIN_TICKET FOREIGN KEY (train_ticket_id) REFERENCES ticket (id);

ALTER TABLE "user"
    ADD CONSTRAINT FK_USER_ON_CODE FOREIGN KEY (code_id) REFERENCES confirmation_code (id);
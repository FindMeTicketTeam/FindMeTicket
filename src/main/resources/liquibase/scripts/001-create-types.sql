CREATE TYPE website AS ENUM ('PROIZD', 'BUSFOR', 'INFOBUS');
CREATE TYPE transport AS ENUM ('BUS', 'TRAIN', 'AIRPLANE', 'FERRY');
CREATE TYPE auth_provider AS ENUM ('BASIC', 'GOOGLE', 'FACEBOOK');
CREATE TYPE role AS ENUM ('USER', 'MANAGER', 'ADMIN');
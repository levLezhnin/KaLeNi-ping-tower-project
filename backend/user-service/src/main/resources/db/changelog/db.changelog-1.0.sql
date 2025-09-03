--liquibase formatted sql

--changeset lezhnin:1
create table users (
    id BIGSERIAL primary key,
    username TEXT not null unique,
    email TEXT not null unique,
    password_hash TEXT not null
);
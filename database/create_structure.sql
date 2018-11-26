CREATE DATABASE "numbers-searcher"
    WITH
    OWNER = postgres
    ENCODING = 'UTF8'
    LC_COLLATE = 'Russian_Russia.1251'
    LC_CTYPE = 'Russian_Russia.1251'
    TABLESPACE = pg_default
    CONNECTION LIMIT = -1;

CREATE USER searcher WITH
  LOGIN
  NOSUPERUSER
  INHERIT
  NOCREATEDB
  NOCREATEROLE
  NOREPLICATION;

CREATE TABLE public.request_history
(
    id uuid NOT NULL,
    code text COLLATE pg_catalog."default",
    "number" integer,
    filenames text COLLATE pg_catalog."default",
    error text COLLATE pg_catalog."default",
    CONSTRAINT request_history_pkey PRIMARY KEY (id)
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.request_history
    OWNER to searcher;
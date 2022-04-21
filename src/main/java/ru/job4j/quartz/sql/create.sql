create table if not exists post (
    id serial not null,
    name text,
    text text,
    link text not null,
    created timestamp,
    PRIMARY KEY (id, link)
);
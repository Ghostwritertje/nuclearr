create table removed
(
    id              INTEGER auto_increment primary key,
    name            varchar2(256) not null,
    seed_time       LONG,
    hardlinks       INTEGER,
    trackers        varchar2(256),
    transmission_id integer
);


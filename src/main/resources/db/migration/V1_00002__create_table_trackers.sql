create table tracker
(
    id         INTEGER auto_increment primary key,
    name       varchar2(256) not null,
    torrent_id INTEGER references torrent (id)
);


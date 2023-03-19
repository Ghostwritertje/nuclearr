create table torrent
(
    id INTEGER auto_increment primary key ,
    name varchar2 not null ,
    hash_name varchar2 not null unique,
    transmission_id integer not null
);

create table file_item
(
    id INTEGER auto_increment primary key ,
    path varchar2(512) not null unique ,
    hard_links integer
);

create table file_item_occurrence
(
    id INTEGER auto_increment primary key ,
    file_item_id INTEGER references file_item(id),
    torrent_id INTEGER references torrent(id)
);
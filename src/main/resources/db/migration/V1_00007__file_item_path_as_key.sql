drop table file_item_occurrence;
drop table file_item;

create table file_item
(
    id         INTEGER auto_increment primary key,
    path       varchar2(1025) not null unique,
    hard_links integer
);

create table file_item_occurrence
(
    id             INTEGER auto_increment primary key,
    file_item_path varchar2(1025) not null ,
    torrent_id     INTEGER references torrent (id)
);

create index i_file_item_path on file_item (path);
create index i_file_item_occurrence_path on file_item_occurrence (file_item_path);
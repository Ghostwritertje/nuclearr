Alter table removed
    alter column trackers varchar2(2048);

Alter table removed
    alter column name varchar2(2048);

create index i_removed_transmission_id on removed(transmission_id);
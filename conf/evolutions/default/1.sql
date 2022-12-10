# text_properties schema

# --- !Ups

create table text_properties(
    text_id bigint(4) not null,
    text varchar(255) not null,
    created_time datetime not null
) engine=innodb charset=utf8mb4;

# --- !Downs

drop table text_properties;
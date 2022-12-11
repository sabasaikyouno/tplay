# posted_properties schema

# --- !Ups

create table posted_properties(
    content_id bigint(4) not null auto_increment,
    content_type varchar(255) not null,
    content varchar(255) not null,
    created_time datetime not null,
    primary key (content_id)
) engine=innodb charset=utf8mb4;

# --- !Downs

drop table posted_properties;
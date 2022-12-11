# text_properties schema

# --- !Ups

create table text_properties(
    text_id bigint(4) not null auto_increment,
    text varchar(255) not null,
    created_time datetime not null,
    primary key (text_id)
) engine=innodb charset=utf8mb4;

create table image_properties(
    image_id bigint(4) not null auto_increment,
    image varchar(255) not null,
    created_time datetime not null,
    primary key (image_id)
) engine=innodb charset=utf8mb4;

# --- !Downs

drop table text_properties;

drop table image_properties;
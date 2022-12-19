# posted_properties schema

# --- !Ups

create table posted_properties(
    content_id bigint(4) not null,
    content_type varchar(255) not null,
    room_id varchar(255) not null,
    user_id varchar(255) not null,
    content varchar(255) not null,
    created_time datetime not null
) engine=innodb charset=utf8mb4;

create table room_properties(
    id bigint(4) not null auto_increment,
    room_id varchar(255) not null,
    user_id varchar(255) not null,
    title varchar(255) not null,
    view_count int(4) not null,
    content_type varchar(255) not null,
    primary key (id)
) engine=innodb charset=utf8mb4;

create table user_properties(
    user_id varchar(191) not null unique,
    password varchar(255) not null
) engine=innodb charset=utf8mb4;

create table tag_properties(
    room_id varchar(255) not null,
    tag varchar(255) not null
) engine=innodb charset=utf8mb4;

create table room_auth_properties (
    room_id varchar(255) not null,
    user_id varchar(191) not null
) engine=innodb charset=utf8mb4;

# --- !Downs

drop table posted_properties;

drop table room_properties;

drop table user_properties;

drop table tag_properties;
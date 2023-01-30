DROP TABLE member IF EXISTS;
CREATE TABLE member
(
    id int NOT NULL PRIMARY KEY,
    socialId varchar(255) not null,
    email varchar(255) not null,
    nickname varchar(255) not null,
    profileImage varchar(255),
    authority varchar(255) not null,
    primary key (id)
);
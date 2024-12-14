create table tbl_books (
    id varbinary(16) not null primary key,
    title varchar(100) unique,
    description varchar(100),
    isbn varchar(255),
    publisher varchar(255),
    author_id varbinary(16),
    user_id varbinary(16),
    user_name varchar(20),
    completed boolean default 0,
    created timestamp,
    updated timestamp
) engine=InnoDB;

create table tbl_authors (
    id varbinary(16) not null primary key,
    first_name varchar(255),
    last_name varchar(255),
    genre varchar(255),
    created timestamp,
    updated timestamp
) engine=InnoDB;

--create sequence author_seq start with 1 increment by 50 nocache;
--create sequence book_seq start with 1 increment by 50 nocache;
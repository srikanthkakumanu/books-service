create table tbl_authors (
    id uuid not null primary key,
    first_name varchar(255),
    last_name varchar(255),
    genre varchar(255),
    created timestamptz,
    updated timestamptz
);

create table tbl_books (
    id uuid not null primary key,
    title varchar(100) unique,
    description varchar(100),
    isbn varchar(255),
    publisher varchar(255),
    author_id uuid,
    user_id uuid,
    user_name varchar(20),
    completed boolean default false,
    created timestamptz,
    updated timestamptz
);

create index idx_tbl_books_author_id on tbl_books(author_id);
create index idx_tbl_books_user_id on tbl_books(user_id);
create index idx_tbl_books_isbn on tbl_books(isbn);

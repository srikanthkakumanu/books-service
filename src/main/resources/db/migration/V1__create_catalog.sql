-- Catalog schema. Run by the schema owner (booksadmin); the runtime user (theuser) receives data
-- access to these tables through the default privileges set when the database was created.

create table author (
    id         uuid         primary key,
    first_name varchar(100) not null,
    last_name  varchar(100),
    genre      varchar(100) not null,
    created_at timestamptz  not null,
    updated_at timestamptz  not null,
    version    bigint       not null default 0
);

create index idx_author_last_name on author (lower(last_name));
create index idx_author_genre on author (lower(genre));

create table book (
    id          uuid          primary key,
    title       varchar(200)  not null,
    description varchar(1000),
    isbn        varchar(13)   not null,
    publisher   varchar(200)  not null,
    author_id   uuid          not null,
    -- The platform user who owns the book (the user ID from user-service); null for catalog-owned books.
    owner_id    uuid,
    completed   boolean       not null default false,
    created_at  timestamptz   not null,
    updated_at  timestamptz   not null,
    version     bigint        not null default 0,
    constraint uq_book_isbn unique (isbn),
    constraint fk_book_author foreign key (author_id) references author (id)
);

create index idx_book_author on book (author_id);
create index idx_book_owner on book (owner_id);
create index idx_book_title on book (lower(title));

create table users(
    id serial primary key,
    external_id UUID default uuid_generate_v4(),
    datetime_added timestamp default now(),
    datetime_disabled timestamp default null,
    password text not null
);


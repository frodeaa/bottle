alter table bottles
    add column user_id integer references users(id);


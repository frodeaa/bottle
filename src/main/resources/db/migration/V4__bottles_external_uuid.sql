create extension if not exists "uuid-ossp";
alter table bottles
    add column external_id UUID default uuid_generate_v4();

update bottles set external_id=uuid_generate_v4()
    where external_id is null;

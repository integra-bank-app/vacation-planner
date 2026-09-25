create table restaurant (
    id UUID primary key,
    name varchar(50) not null,
    address varchar(100) not null,
    opening_hour time not null,
    closing_hour time not null
)
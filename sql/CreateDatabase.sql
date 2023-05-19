-- Don't forget to run CreateDatabase.sql from BotCommands

drop table if exists gasbarrel_version;

create table gasbarrel_version
(
    one_row bool primary key default true check (one_row),
    version text not null
);

insert into gasbarrel_version
values (true, '1.0'); -- Change in DatabaseSource.kt too

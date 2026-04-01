-- Multi-Datasource Configuration Table
drop table if exists sys_datasource;
create sequence sys_datasource_seq start with 100 increment by 1;
create table sys_datasource (
    datasource_id   bigint          not null,
    datasource_name varchar(100)    not null,
    datasource_key  varchar(50)     not null unique,
    db_type         varchar(20)     not null, -- H2, MySQL, PostgreSQL, Oracle, SQLServer
    url             varchar(500)    not null,
    username        varchar(100)    not null,
    password        varchar(500)    not null,
    driver_class    varchar(200)    not null,
    status          varchar(1)      default '0', -- 0=Active, 1=Inactive
    create_by       varchar(64)     default '',
    create_time     timestamp       default current_timestamp,
    update_by       varchar(64)     default '',
    update_time     timestamp       null,
    remark          varchar(500)    default '',
    primary key (datasource_id)
);
create index sys_datasource_key_idx on sys_datasource(datasource_key);
create index sys_datasource_status_idx on sys_datasource(status);

-- Test connection and store last test time
alter table sys_datasource add column last_test_time timestamp null;
alter table sys_datasource add column last_test_status varchar(1) default '0'; -- 0=Success, 1=Failed

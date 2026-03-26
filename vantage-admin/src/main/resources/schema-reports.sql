-- =====================================================
-- REPORT MODULE - Database Schema
-- =====================================================

-- Report Definition Table
drop table if exists sys_report;
create sequence sys_report_seq start with 100 increment by 1;
create table sys_report (
    report_id         bigint          not null,
    report_name       varchar(100)    not null,
    report_key        varchar(50)     not null unique,
    report_type       varchar(20)     default 'SQL', -- SQL, STORED_PROC
    datasource_key    varchar(50)     default 'master',
    sql_content       clob            not null,
    params_config     varchar(2000),  -- JSON config for parameters
    columns_config    varchar(4000),  -- JSON config for column mapping
    output_format     varchar(20)     default 'EXCEL', -- EXCEL, PDF, CSV, HTML
    status            varchar(1)      default '0', -- 0=Active, 1=Disabled
    create_by         varchar(64)     default '',
    create_time       timestamp       default current_timestamp,
    update_by         varchar(64)     default '',
    update_time       timestamp       null,
    remark            varchar(500)    default '',
    primary key (report_id)
);
create index sys_report_key_idx on sys_report (report_key);
create index sys_report_status_idx on sys_report (status);

-- Report Execution History
drop table if exists sys_report_exec;
create sequence sys_report_exec_seq start with 100 increment by 1;
create table sys_report_exec (
    exec_id           bigint          not null,
    report_id         bigint          not null,
    report_name       varchar(100),
    exec_time         timestamp       default current_timestamp,
    exec_params       varchar(2000),
    output_format     varchar(20),
    file_path         varchar(500),
    file_size         bigint,
    status            varchar(1)      default '0', -- 0=Success, 1=Failed
    error_msg         varchar(2000),
    exec_duration     bigint,
    create_by         varchar(64),
    primary key (exec_id)
);
create index sys_report_exec_report_idx on sys_report_exec (report_id);
create index sys_report_exec_time_idx on sys_report_exec (exec_time);

-- Email Template for Reports
drop table if exists sys_report_email;
create sequence sys_report_email_seq start with 100 increment by 1;
create table sys_report_email (
    email_id          bigint          not null,
    report_id         bigint          not null,
    email_subject     varchar(255)    not null,
    email_body        clob,
    attachment_name   varchar(100),
    recipient_emails  varchar(1000),  -- Comma-separated
    cc_emails         varchar(1000),
    send_on_success   boolean         default true,
    send_on_failure   boolean         default false,
    status            varchar(1)      default '0',
    create_by         varchar(64),
    create_time       timestamp       default current_timestamp,
    primary key (email_id)
);
create index sys_report_email_report_idx on sys_report_email (report_id);

-- Email SMTP Configuration Storage
CREATE TABLE IF NOT EXISTS sys_email_smtp_config (
    config_id           BIGINT          NOT NULL,
    smtp_host           VARCHAR(200)    NOT NULL,
    smtp_port           INT             DEFAULT 587,
    username            VARCHAR(200)    NOT NULL,
    password            VARCHAR(500)    NOT NULL,
    auth                VARCHAR(1)      DEFAULT '1',
    starttls_enable     VARCHAR(1)      DEFAULT '1',
    starttls_required   VARCHAR(1)      DEFAULT '1',
    ssl_enable          VARCHAR(1)      DEFAULT '0',
    timeout             INT             DEFAULT 5000,
    status              VARCHAR(1)      DEFAULT '0',
    create_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    update_time         TIMESTAMP       DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (config_id)
);

CREATE SEQUENCE IF NOT EXISTS sys_email_smtp_config_seq START WITH 100 INCREMENT BY 1;

-- Insert default config (disabled until user updates via UI)
MERGE INTO sys_email_smtp_config (config_id, smtp_host, smtp_port, username, password, auth, starttls_enable, starttls_required, ssl_enable, timeout, status)
VALUES (1, 'smtp.gmail.com', 587, '', '', '1', '1', '1', '0', 5000, '0');

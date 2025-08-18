CREATE TABLE {1}_log (
	{1}_log_id BIGINT AUTO INCREMENT,
    field_name VARCHAR(64) NOT NULL, -- 64 for MySQL, 128 for Oracle
    old_value VARCHAR(65535) NULL,
    new_value VARCHAR(65535) NULL,
    updated_by VARCHAR(50) NOT NULL,
    updated_dts DATETIME NOT NULL,
    PRIMARY KEY ({1}_log_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
    
SELECT tabid AS table_id,
       colno AS column_sequence,
       colname AS column_name,
       DECODE(pure_type, 
                       0, 'CHAR', 
                       1, 'SMALLINT', 
                       2, 'INTEGER',
                       3, 'FLOAT',
                       4, 'SMALLFLOAT',
                       5, 'DECIMAL',
                       6, 'SERIAL',
                       7, 'DATE',
                       8, 'MONEY',
                       9, 'NULL',
                      10, 'DATETIME',
                      11, 'BYTE',
                      12, 'TEXT',
                      13, 'VARCHAR',
                      14, 'INTERVAL',
                      15, 'NCHAR',
                      16, 'NVARCHAR',
                      17, 'INT8',
                      18, 'SERIAL8',
                      19, 'SET',
                      20, 'MULTISET',
                      21, 'LIST',
                      22, 'ROW (Unnamed)',
                      23, 'COLLECTION',
                      40, 'LVARCHAR',
                      41, 'BLOB/CLOB/BOOLEAN',
                      43, 'LVARCHAR (client-side only)',
                      45, 'BOOLEAN',
                      52, 'BIGINT',
                      53, 'BIGSERIAL',
                      2061, 'IDSSECURITYLABEL',
                      4118, 'ROW (Named)',
                      TO_CHAR(coltype)) -- AS column_type,
            ||
       CASE WHEN pure_type IN (10, 14) THEN 
                 ' ' || first_qual || ' TO ' || last_qual
            WHEN pure_type IN (1, 2, 3, 4, 6, 7, 17, 18, 52, 53) THEN
                 ''
            WHEN pure_type = 5 THEN
                 '(' || precision || ',' || scale || ')'
            ELSE 
                 '(' || TO_CHAR(collength) || ')' 
       END AS column_type,
       DECODE(not_null, 1, 'NOT NULL', 'NULL') AS null_constraint,
       (SELECT CASE WHEN INSTR(TRIM(default), ' ') > 0 THEN
                   TRIM(SUBSTR(default, INSTR(TRIM(default), ' ') + 1))
               ELSE default END
          FROM sysdefaults
         WHERE tabid = y.tabid
           AND colno = y.colno) AS column_default
  FROM (SELECT tabid,
               colno,
               colname,
               not_null,
               coltype,
               pure_type,
               collength,
               scale,
               precision,
               DECODE(TRUNC(scale / 16), 
                       0, 'YEAR',
                       2, 'MONTH', 
                       4, 'DAY',
                       6, 'HOUR',
                       8, 'MINUTE',
                      10, 'SECOND', 
                      11, 'FRACTION(1)',
                      12, 'FRACTION(2)',
                      13, 'FRACTION(3)',
                      14, 'FRACTION(4)',
                      15, 'FRACTION(5)',
                      '[UNKNOWN]') AS first_qual,
               DECODE(scale - (TRUNC(scale / 16) * 16), 
                       0, 'YEAR',
                       2, 'MONTH', 
                       4, 'DAY',
                       6, 'HOUR',
                       8, 'MINUTE',
                      10, 'SECOND', 
                      11, 'FRACTION(1)',
                      12, 'FRACTION(2)',
                      13, 'FRACTION(3)',
                      14, 'FRACTION(4)',
                      15, 'FRACTION(5)',
                      '[UNKNOWN]') AS last_qual
          FROM (SELECT tabid,
                       colno,
                       colname,
                       TRUNC((coltype - (TRUNC(coltype / 512) * 512)) / 256) AS not_null,
                       coltype,
                       coltype - (TRUNC(coltype / 256) * 256) AS pure_type,
                       collength,
                       collength - (TRUNC(collength / 256) * 256) AS scale, -- also datetime_qualifier
                       TRUNC(collength / 256) AS precision
                  FROM informix.syscolumns
                 WHERE tabid = TO_NUMBER({1})) x) y
 ORDER BY colno
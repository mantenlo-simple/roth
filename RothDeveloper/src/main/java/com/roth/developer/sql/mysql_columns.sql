SELECT CONCAT(table_catalog, '.', table_schema, '.', table_name) AS table_id,
       ordinal_position AS column_sequence,
       column_name,
       CASE WHEN extra = 'auto_increment' THEN
           CONCAT(UPPER(column_type), ' AUTO_INCREMENT')
       ELSE
           UPPER(column_type)
       END AS column_type,
       IF(is_nullable = 'YES', 'NULL', 'NOT NULL') AS null_constraint,
       column_default
  FROM information_schema.columns
 WHERE table_catalog = SUBSTRING_INDEX({1}, '.', 1)
   AND table_schema = SUBSTRING_INDEX(SUBSTRING_INDEX({1}, '.', 2), '.', -1)
   AND table_name = SUBSTRING_INDEX({1}, '.', -1)
 ORDER BY ordinal_position
SELECT CONCAT(table_catalog, '.', table_schema, '.', table_name) AS table_id,
       table_schema AS "schema",
       index_name,
       IF(non_unique = 0, 'UNIQUE', '') AS unique_constraint,
       IF(index_name = 'PRIMARY', 'Y', 'N') AS primary_key,
       GROUP_CONCAT(column_name ORDER BY seq_in_index SEPARATOR ', ') AS columns
  FROM information_schema.statistics
 WHERE table_catalog = SUBSTRING_INDEX({1}, '.', 1)
   AND table_schema = SUBSTRING_INDEX(SUBSTRING_INDEX({1}, '.', 2), '.', -1)
   AND table_name = SUBSTRING_INDEX({1}, '.', -1)
 GROUP BY 1, 2, 3, 4
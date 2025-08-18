SELECT CONCAT(u.table_catalog, '.', u.table_schema, '.', u.table_name) AS table_id, 
       u.constraint_name,
       GROUP_CONCAT(u.column_name ORDER BY u.ordinal_position SEPARATOR ', ') AS column_names,
	   u.referenced_table_schema AS referenced_schema,
       '[n/a]' AS referenced_constraint_name,
       u.referenced_table_name,
	   GROUP_CONCAT(u.referenced_column_name ORDER BY u.ordinal_position SEPARATOR ', ') AS referenced_column_names,
	   c.delete_rule
  FROM information_schema.key_column_usage u
  JOIN information_schema.referential_constraints c
    ON c.constraint_name = u.constraint_name
 WHERE u.table_catalog = SUBSTRING_INDEX({1}, '.', 1)
   AND u.table_schema = SUBSTRING_INDEX(SUBSTRING_INDEX({1}, '.', 2), '.', -1)
   AND u.table_name = SUBSTRING_INDEX({1}, '.', -1)
   AND u.referenced_table_name IS NOT NULL
 GROUP BY u.table_catalog, u.table_schema, u.table_name, u.constraint_name, u.referenced_table_schema, u.referenced_table_name, c.delete_rule
 ORDER BY table_id, column_names
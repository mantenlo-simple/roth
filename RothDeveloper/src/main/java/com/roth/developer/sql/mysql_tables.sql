SELECT CONCAT(table_catalog, '.', table_schema, '.', table_name) AS table_id, 
        table_schema AS "schema", 
        table_name, 
        create_time AS created 
   FROM information_schema.tables 
  WHERE table_schema NOT IN ('information_schema', 'performance_schema', 'mysql', 'sakila')
    AND table_schema = {1}
  ORDER BY table_schema, table_name
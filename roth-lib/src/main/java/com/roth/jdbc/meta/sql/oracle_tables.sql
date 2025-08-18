SELECT owner || '.' || table_name AS table_id, 
       LOWER(owner) AS "schema", 
       LOWER(table_name) AS table_name, 
       NULL AS created 
  FROM all_tables 
 WHERE LOWER(owner) = {1}
 ORDER BY owner, table_name
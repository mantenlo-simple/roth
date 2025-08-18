SELECT CONCAT(tc.table_catalog, '.', tc.table_schema, '.', tc.table_name) AS table_id,
	   tc.constraint_name, 
       STRING_AGG(kcu.column_name, ', ') AS column_names,
       ccu.table_schema AS referenced_schema,
	   '[n/a]' AS referenced_constraint_name,
       ccu.table_name AS referenced_table_name,
       STRING_AGG(ccu.column_name, ', ') AS referenced_column_names
  FROM information_schema.table_constraints AS tc 
  JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
   AND tc.table_schema = kcu.table_schema
  JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
   AND ccu.table_schema = tc.table_schema
 WHERE tc.constraint_type = 'FOREIGN KEY'
   AND tc.table_catalog = SPLIT_PART({1}, '.', 1)
   AND tc.table_schema = SPLIT_PART({1}, '.', 2)
   AND tc.table_name = SPLIT_PART({1}, '.', 3)
 GROUP BY 1, 2, 4, 5, 6
 ORDER BY table_id, column_names
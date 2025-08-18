SELECT CONCAT(SPLIT_PART({1}, '.', 1), '.', schemaname, '.', tablename) AS table_id,
       SPLIT_PART({1}, '.', 2) AS "schema",
       indexname AS index_name,
       CASE WHEN indexdef LIKE '% UNIQUE %' THEN 
	       'UNIQUE' 
	   ELSE 
	       ''
	   END AS unique_constraint,
       (SELECT MAX(CASE WHEN ix.indisprimary = 't' THEN 'Y' ELSE 'N' END)
		  FROM pg_index ix
		  JOIN pg_class t
			ON t.oid = ix.indrelid
		  JOIN pg_class i
			ON i.oid = ix.indexrelid
		  JOIN pg_attribute a
			ON a.attrelid = t.oid
		   AND a.attnum = ANY(ix.indkey)
		 WHERE t.relkind = 'r'
		   AND t.relname = pi.tablename
		   AND i.relname = pi.indexname) AS primary_key,
       SPLIT_PART(SPLIT_PART(indexdef, '(', 2), ')', 1) AS columns
  FROM pg_indexes pi
 WHERE schemaname = SPLIT_PART({1}, '.', 2)
   AND tablename = SPLIT_PART({1}, '.', 3)
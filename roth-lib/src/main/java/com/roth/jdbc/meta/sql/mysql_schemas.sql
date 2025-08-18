SELECT schema_name
  FROM information_schema.schemata
 WHERE schema_name NOT IN ('information_schema', 'performance_schema', 'mysql', 'sakila')
 ORDER BY schema_name
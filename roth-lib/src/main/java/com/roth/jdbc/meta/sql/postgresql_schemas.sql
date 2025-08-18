SELECT nspname AS schema
  FROM pg_catalog.pg_namespace
 WHERE nspname !~ '^pg_'
   AND nspname <> 'information_schema'
 ORDER BY 1
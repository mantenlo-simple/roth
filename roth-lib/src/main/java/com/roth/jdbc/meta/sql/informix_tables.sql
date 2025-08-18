SELECT tabid AS table_id,
       owner AS schema,
       tabname AS table_name,
       created
  FROM informix.systables
 WHERE tabtype = 'T'
   AND tabid >= 100
   AND owner = {1}
 ORDER BY owner, tabname
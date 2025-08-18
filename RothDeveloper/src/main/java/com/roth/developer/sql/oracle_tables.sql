SELECT owner || '.' || table_name AS table_id, 
       owner AS "schema", 
       table_name, 
       NULL AS created 
  FROM all_tables 
 WHERE owner NOT IN ('SYS', 'SYSTEM', 'ORDSYS', 'MDSYS', 'OUTLN', 'CTXSYS', 'XDB')
   AND owner = {1}
 ORDER BY owner, table_name
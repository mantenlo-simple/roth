SELECT username
  FROM dba_users
 WHERE initial_rsrc_consumer_group != 'SYS_GROUP'
 ORDER BY username
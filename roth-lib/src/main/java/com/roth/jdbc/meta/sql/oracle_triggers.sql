SELECT table_owner || '.' || table_name AS table_id, 
       owner || '.' || trigger_name AS trigger_id,
       LOWER(trigger_name)  AS trigger_name,
       trigger_body,
       trigger_type,
       triggering_event
  FROM all_triggers
 WHERE table_owner = SUBSTR({1}, 1, INSTR({1}, '.') - 1)
   AND table_name = SUBSTR({1}, INSTR({1}, '.', -1) + 1)
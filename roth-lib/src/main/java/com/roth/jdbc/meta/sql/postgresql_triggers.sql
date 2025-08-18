SELECT CONCAT(event_object_catalog, '.', event_object_schema, '.', event_object_table) AS table_id, 
       LOWER(CONCAT(trigger_catalog, '.', trigger_schema, '.', trigger_name)) AS trigger_id,
       LOWER(trigger_name) AS trigger_name,
       action_statement AS trigger_body,
       CONCAT(action_timing, ' EACH ', action_orientation) AS trigger_type,
       event_manipulation AS triggering_event
  FROM information_schema.triggers
 WHERE event_object_catalog = SPLIT_PART({1}, '.', 1)
   AND event_object_schema = SPLIT_PART({1}, '.', 2)
   AND event_object_table = SPLIT_PART({1}, '.', 3)
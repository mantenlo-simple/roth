SELECT *
  FROM schedule_event
 WHERE status = 'P' 
   AND event_submit_dts <= SYSDATE()
 ORDER BY event_submit_dts
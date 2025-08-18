SELECT *
  FROM schedule s
 WHERE status = 'A' 
   AND start_date <= SYSDATE() 
   AND (end_date IS NULL 
    OR  end_date > SYSDATE()) 
   AND start_time <= DATE_FORMAT(SYSDATE(), '%H:%i')
   AND (next_instance IS NULL 
    OR  next_instance <= SYSDATE())
   AND NOT EXISTS 
       (SELECT 1
          FROM schedule_event e
		 WHERE e.schedule_id = s.schedule_id
           AND status IN ('P', 'R'))
 ORDER BY next_instance
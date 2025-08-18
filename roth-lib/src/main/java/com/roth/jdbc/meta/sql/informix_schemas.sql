SELECT DISTINCT owner 
  FROM informix.systables 
 WHERE tabid >= 100 
 ORDER BY owner
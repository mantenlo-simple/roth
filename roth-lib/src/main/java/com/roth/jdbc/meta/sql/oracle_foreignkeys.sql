SELECT fk.owner || '.' || fk.table_name AS table_id, 
       LOWER(fk.constraint_name) AS constraint_name,
       (SELECT LOWER(LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_name))
		  FROM all_cons_columns
		 WHERE owner = fk.owner
		   AND constraint_name = fk.constraint_name) AS column_names,
	   LOWER(rc.owner) AS referenced_schema,
       LOWER(rc.constraint_name) AS referenced_constraint_name,
       LOWER(rc.table_name) AS referenced_table_name,
	   (SELECT LOWER(LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_name))
		  FROM all_cons_columns
		 WHERE owner = rc.owner
		   AND constraint_name = rc.constraint_name) AS referenced_column_names
  FROM all_constraints fk,
       all_constraints rc
 WHERE rc.owner = fk.r_owner
   AND rc.constraint_name = fk.r_constraint_name
   AND fk.constraint_type = 'R'
   AND fk.owner = SUBSTR({1}, 1, INSTR({1}, '.') - 1)
   AND fk.table_name = SUBSTR({1}, INSTR({1}, '.', -1) + 1)
 ORDER BY table_id, column_names
SELECT owner || '.' || table_name AS table_id, 
       column_id AS column_sequence, 
       LOWER(column_name) AS column_name,
       data_type || 
       CASE WHEN data_type = 'DATE' THEN '' 
            WHEN data_type = 'NUMBER' AND data_length = 22 AND data_precision IS NULL THEN '' 
            ELSE '(' || TO_CHAR(NVL(data_precision, data_length)) ||
			            CASE WHEN NVL(data_scale, 0) = 0 THEN '' ELSE ',' || TO_CHAR(data_scale) END || ')' 
            END AS column_type, 
       DECODE(nullable, 'N', 'NOT NULL', 'NULL') AS null_constraint, 
       data_default AS column_default 
  FROM all_tab_columns 
 WHERE owner = SUBSTR({1}, 1, INSTR({1}, '.') - 1)
   AND table_name = SUBSTR({1}, INSTR({1}, '.', -1) + 1)
 ORDER BY column_id
SELECT owner || '.' || table_name AS table_id, 
       column_id AS column_sequence, 
       column_name,
       data_type || 
       CASE WHEN data_type = 'DATE' THEN '' 
            WHEN data_type = 'NUMBER' AND data_length = 22 AND data_precision IS NULL THEN '' 
            ELSE '(' || TO_CHAR(NVL(data_precision, data_length)) || ')' 
            END AS column_type, 
       DECODE(nullable, 'N', 'NOT NULL', 'NULL') AS null_constraint, 
       data_default AS column_default 
  FROM all_tab_columns 
 WHERE owner = SUBSTRING_INDEX({1}, '.', 1)
   AND table_name = SUBSTRING_INDEX({1}, '.', -1) 
 ORDER BY column_id
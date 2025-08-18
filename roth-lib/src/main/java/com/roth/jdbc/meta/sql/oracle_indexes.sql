SELECT owner || '.' || table_name AS table_id, 
       LOWER(owner) AS schema, 
       LOWER(index_name) AS index_name, 
       DECODE(uniqueness, 'UNIQUE', 'UNIQUE', '') AS unique_constraint,
       NVL((SELECT DECODE(constraint_type, 'P', 'Y', 'N') 
              FROM all_constraints 
             WHERE owner = all_indexes.owner
               AND table_name = all_indexes.table_name
               AND constraint_name = all_indexes.index_name), 'N') AS primary_key,
       (SELECT LISTAGG(column_name, ', ') WITHIN GROUP (ORDER BY column_position) 
          FROM all_ind_columns 
         WHERE index_owner = all_indexes.owner 
           AND index_name = all_indexes.index_name) AS columns
  FROM all_indexes 
 WHERE owner = SUBSTR({1}, 1, INSTR({1}, '.') - 1)
   AND table_name = SUBSTR({1}, INSTR({1}, '.', -1) + 1)
 ORDER BY index_name
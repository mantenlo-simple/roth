SELECT owner || '.' || table_name AS table_id, 
       owner AS schema, 
       index_name, 
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
 WHERE owner = SUBSTRING_INDEX({1}, '.', 1)
   AND table_name = SUBSTRING_INDEX({1}, '.', -1) 
 ORDER BY index_name
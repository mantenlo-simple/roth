SELECT CONCAT(table_catalog, '.', table_schema, '.', table_name) AS table_id,
       ordinal_position AS column_sequence,
       column_name,
       CASE WHEN is_generated = 'YES' THEN
				CONCAT(UPPER(data_type), ' AUTO_INCREMENT')
	        WHEN UPPER(data_type) = 'CHARACTER VARYING' THEN
				CONCAT('VARCHAR(', character_maximum_length, ')')
			WHEN UPPER(data_type) = 'CHARACTER' THEN
			    CONCAT('CHAR(', character_maximum_length, ')')
			ELSE
				UPPER(data_type)
       END AS column_type,
       CASE WHEN is_nullable = 'YES' THEN 
	       'NULL' 
	   ELSE 
	       'NOT NULL' 
	   END AS null_constraint,
       column_default
  FROM information_schema.columns
 WHERE table_catalog = SPLIT_PART({1}, '.', 1)
   AND table_schema = SPLIT_PART({1}, '.', 2)
   AND table_name = SPLIT_PART({1}, '.', 3)
 ORDER BY ordinal_position
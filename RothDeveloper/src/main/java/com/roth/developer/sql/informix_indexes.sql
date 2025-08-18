SELECT tabid AS table_id,
       owner AS schema,
       idxname AS index_name,
       DECODE(idxtype, 'U', 'UNIQUE', '') AS unique_constraint,
       NVL((SELECT DECODE(constrtype, 'P', 'Y', 'N') 
                          FROM sysconstraints 
                         WHERE tabid = i.tabid 
                           AND idxname = i.idxname), 'N') AS primary_key,
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part1) ||
       CASE WHEN part2 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part2)
       ELSE '' END ||
       CASE WHEN part3 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part3)
       ELSE '' END ||
       CASE WHEN part4 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part4)
       ELSE '' END ||
       CASE WHEN part5 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part5)
       ELSE '' END ||
       CASE WHEN part6 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part6)
       ELSE '' END ||
       CASE WHEN part7 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part7)
       ELSE '' END ||
       CASE WHEN part8 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part8)
       ELSE '' END ||
       CASE WHEN part9 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part9)
       ELSE '' END ||
       CASE WHEN part10 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part10)
       ELSE '' END ||
       CASE WHEN part11 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part11)
       ELSE '' END ||
       CASE WHEN part12 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part12)
       ELSE '' END ||
       CASE WHEN part13 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part13)
       ELSE '' END ||
       CASE WHEN part14 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part14)
       ELSE '' END ||
       CASE WHEN part15 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part15)
       ELSE '' END ||
       CASE WHEN part16 != 0 THEN ', ' || 
       (SELECT colname FROM syscolumns WHERE tabid = i.tabid AND colno = i.part16)
       ELSE '' END AS columns
  FROM sysindexes i
 WHERE tabid = TO_NUMBER({1})
 ORDER BY idxname
/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
SELECT 'BOOK' AS item_type, 
        book_id,
        book_title AS title,
        NULL AS portlet_uri,
        sequence
   FROM book z
  WHERE parent_book_id = {1}
    AND EXISTS (SELECT p.book_id
                  FROM book b,
                       page p,
                       portlet_role pr
                 WHERE b.book_id = p.book_id
                   AND p.portlet_id = pr.portlet_id
                   AND b.lineage LIKE CONCAT('% ', z.book_id, ' %')
                   AND pr.role_name IN
                       (SELECT role_name
		                  FROM user_role ur,
		                       domain d
		                 WHERE d.domain_id = ur.domain_id
		                   AND d.domain_name = {2}
		                   AND (ur.userid = {3} 
		                    OR  ({3} != 'anonymous' 
		                   AND   ur.userid = 'authenticated'))
		                 UNION
		                SELECT DISTINCT role_name
		                  FROM group_role r
		                 WHERE EXISTS (SELECT g.lineage
						                 FROM `group` g,
						                      user_group u,
						                      domain d
						                WHERE u.group_id = g.group_id
						                  AND d.domain_id = u.domain_id
						                  AND d.domain_name = {2}
						                  AND INSTR(g.lineage, CONCAT(' ', r.group_id, ' ')) > 0
						                  AND (u.userid = {3}
						                   OR  ({3} != 'anonymous' 
						                  AND   u.userid = 'authenticated')))))
  UNION ALL
 SELECT 'PAGE' AS item_type,
        NULL AS book_id,
        p.page_title AS title,
        t.portlet_uri,
        sequence
   FROM page p,
        portlet t
  WHERE p.book_id = {1}
    AND t.portlet_id = p.portlet_id
    AND EXISTS (SELECT pr.portlet_id
                  FROM portlet_role pr
                 WHERE pr.portlet_id = t.portlet_id
                   AND pr.role_name IN
                       (SELECT ur.role_name
                          FROM user_role ur,
                               domain d
                         WHERE d.domain_id = ur.domain_id
                           AND d.domain_name = {2}
                           AND (ur.userid = {3} 
                            OR  ({3} != 'anonymous' 
                           AND   ur.userid = 'authenticated'))
                         UNION
                        SELECT DISTINCT role_name
                          FROM group_role r
                         WHERE EXISTS (SELECT g.lineage
				                         FROM `group` g,
				                              user_group u,
				                              domain d
				                        WHERE u.group_id = g.group_id
				                          AND d.domain_id = u.domain_id
				                          AND d.domain_name = {2}
				                          AND INSTR(g.lineage, CONCAT(' ', r.group_id, ' ')) > 0
				                          AND (u.userid = {3}
				                           OR  ({3} != 'anonymous' 
				                          AND   u.userid = 'authenticated')))))
  ORDER BY sequence
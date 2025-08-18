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
SELECT desktop_title,
       --CONCAT(desktop_uri, CASE WHEN INSTR(desktop_uri, '?') THEN '&' ELSE '?' END, '_pdtid=', desktop_id) AS desktop_uri,
       (SELECT CASE WHEN INSTR(p.portlet_uri, 'javascript:') THEN p.portlet_uri
                    ELSE CONCAT(p.portlet_uri, CASE WHEN INSTR(p.portlet_uri, '?') THEN '&' ELSE '?' END, '_pdtid=', z.desktop_id) END
		  FROM book b,
		       page pg,
		       portlet p,
		       portlet_role pr
		 WHERE pg.book_id = b.book_id
		   AND p.portlet_id = pg.portlet_id
		   AND pr.portlet_id = p.portlet_id
		   AND b.lineage LIKE CONCAT('% ', z.book_id, ' %')
		   AND pr.role_name IN 
		       (SELECT ur.role_name
		          FROM user_role ur,
		               user ud,
		               domain d
		         WHERE ud.userid = ur.userid
		           AND d.domain_id = ud.domain_id
		           AND d.domain_name = {1}
		           AND (ur.userid = {2} 
		            OR  ({2} != 'anonymous' 
		           AND   ur.userid = 'authenticated'))
		         UNION
		        SELECT DISTINCT role_name
		          FROM group_role r
		         WHERE EXISTS (SELECT g.lineage
		                         FROM `group` g,
		                              user_group u,
		                              user ud,
		                              domain d
		                        WHERE u.group_id = g.group_id
		                          AND ud.userid = u.userid
		                          AND d.domain_id = ud.domain_id
		                          AND d.domain_name = {1}
		                          AND INSTR(g.lineage, CONCAT(' ', r.group_id, ' ')) > 0
		                          AND (u.userid = {2}
		                           OR  ({2} != 'anonymous' 
		                          AND   u.userid = 'authenticated')))) 
		 ORDER BY b.lineage, b.sequence, pg.sequence 
		 LIMIT 1) AS desktop_uri,
       desktop_icon
  FROM desktop z
 WHERE desktop_id != 1
   AND (z.book_id IS NULL
    OR  EXISTS 
        (SELECT p.book_id
           FROM book b,
                page p,
                portlet_role pr
          WHERE b.book_id = p.book_id
            AND p.portlet_id = pr.portlet_id
            AND p.portlet_id > 0
            AND b.lineage LIKE CONCAT('% ', z.book_id, ' %')
            AND pr.role_name IN 
                (SELECT ur.role_name
                   FROM user_role ur,
                        user ud,
                        domain d
                  WHERE ud.userid = ur.userid
                    AND d.domain_id = ud.domain_id
                    AND d.domain_name = {1}
                    AND (ur.userid = {2} 
                     OR  ({2} != 'anonymous' 
                    AND   ur.userid = 'authenticated'))
                  UNION
                 SELECT DISTINCT role_name
                   FROM group_role r
                  WHERE EXISTS(SELECT g.lineage
				                 FROM `group` g,
				                      user_group u,
				                      user ud,
				                      domain d
				                WHERE u.group_id = g.group_id
				                  AND ud.userid = u.userid
				                  AND d.domain_id = ud.domain_id
				                  AND d.domain_name = {1}
				                  AND INSTR(g.lineage, CONCAT(' ', r.group_id, ' ')) > 0
				                  AND (u.userid = {2}
				                   OR  ({2} != 'anonymous' 
				                  AND   u.userid = 'authenticated'))))))
 ORDER BY desktop_title 
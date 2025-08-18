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
SELECT DISTINCT l.link_title,
       l.link_uri,
       l.link_icon
  FROM link l
 WHERE l.updated_by = {1}
    OR l.link_id IN
       (SELECT link_id
          FROM link_role lr
         WHERE lr.role_name IN
               (SELECT ur.role_name
                  FROM user_role ur,
                       user ud,
                       domain d
                 WHERE ud.userid = ur.userid
                   AND d.domain_id = ud.domain_id
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
                                      user ud,
                                      domain d
                                WHERE u.group_id = g.group_id
                                  AND ud.userid = u.userid
                                  AND d.domain_id = ud.domain_id
                                  AND d.domain_name = {2}
                                  AND INSTR(g.lineage, CONCAT(' ', r.group_id, ' ')) > 0
                                  AND (u.userid = {3}
                                   OR  ({3} != 'anonymous' 
                                  AND   u.userid = 'authenticated')))))
 ORDER BY l.link_title
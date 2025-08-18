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
SELECT ur.role_name 
  FROM user_role ur,
       domain d
 WHERE d.domain_id = ur.domain_id
   AND d.domain_name = {1}
   AND ur.userid = {2}
 UNION
SELECT DISTINCT role_name
  FROM group_role gr
 WHERE EXISTS (SELECT g.lineage
                 FROM user_group u,
                      domain d,
                      `group` g LEFT OUTER JOIN group_role r
                   ON g.group_id = r.group_id
                WHERE g.group_id = u.group_id
                  AND g.domain_id = d.domain_id
                  AND d.domain_name = {1}
                  AND u.userid = {2}
                  AND u.domain_id = d.domain_id
                  AND g.lineage LIKE CONCAT('% ', gr.group_id, ' %'))
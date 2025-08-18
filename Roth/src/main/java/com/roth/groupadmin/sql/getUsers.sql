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
SELECT u.userid,
       u.domain_id,
       u.name,
       u.password,
       u.created_by,
       u.updated_by,
       u.updated_dts,
       GROUP_CONCAT(g.group_name) AS group_name_list
  FROM user u LEFT OUTER JOIN user_group ug 
    ON ug.userid = u.userid AND ug.domain_id = u.domain_id
              LEFT OUTER JOIN `group` g 
    ON g.group_id = ug.group_id
 WHERE u.userid != 'anonymous'
   AND u.protect = 'N'
   AND u.domain_id = {2}
/*       (SELECT g.domain_id 
		  FROM user u,
			   user_group ug,
			   `group` g,
			   group_role r
		 WHERE u.userid = ug.userid
		   AND u.domain_id = ug.domain_id
		   AND ug.group_id = g.group_id
		   AND r.group_id = g.group_id
		   AND r.role_name = 'GroupAdmin'
		   AND u.userid = {1}
		   AND u.domain_id = {2})*/
 GROUP BY u.userid,
       u.domain_id,
       u.name,
       u.password,
       u.created_by,
       u.updated_by,
       u.updated_dts
 ORDER BY u.userid
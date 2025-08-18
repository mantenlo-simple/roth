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
       d.domain_id,
       d.domain_name,
       u.updated_by,
       u.updated_dts
  FROM domain d LEFT OUTER JOIN user u
    ON d.domain_id = u.domain_id
   AND u.userid = {1}
 WHERE d.domain_id IN 
       (SELECT g.domain_id 
		  FROM user u,
			   user_group ug,
			   `group` g,
			   group_role r
		 WHERE u.userid = ug.userid
		   AND ug.group_id = g.group_id
		   AND g.domain_id = g.domain_id
		   AND r.group_id = g.group_id
		   AND r.role_name = 'GroupAdmin'
		   AND u.userid = {2})
 ORDER BY d.domain_name
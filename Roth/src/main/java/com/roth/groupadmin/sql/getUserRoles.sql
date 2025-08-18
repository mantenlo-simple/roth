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
SELECT DISTINCT u.userid,
       u.domain_id,
       r.role_name,
       u.created_by,
       u.created_dts
  FROM (SELECT role_name
		  FROM group_role
		 WHERE role_name NOT IN ('GroupAdmin', 'InternalUser')
		   AND group_id IN ({sql: 1})) r LEFT OUTER JOIN user_role u
    ON r.role_name = u.role_name
   AND u.userid = {2}
   AND u.domain_id = {3}
 ORDER BY r.role_name
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
       {2} AS domain_id,
       r.role_name,
       u.created_by,
       u.created_dts
  FROM role r LEFT OUTER JOIN user_role u
    ON r.role_name = u.role_name
   AND u.userid = {1}
   AND u.domain_id = {2}
 WHERE r.role_name NOT IN ('Anonymous', 'Authenticated')
       {sql: 4}
 ORDER BY r.role_name
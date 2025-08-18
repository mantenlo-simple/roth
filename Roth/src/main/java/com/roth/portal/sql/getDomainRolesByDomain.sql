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
SELECT d.domain_id,
       r.role_name,
       d.created_by,
       d.created_dts
  FROM role r LEFT OUTER JOIN domain_role d
    ON r.role_name = d.role_name
   AND d.domain_id = {1}
 WHERE r.role_name NOT IN ('Anonymous', 'Authenticated')
 ORDER BY r.role_name
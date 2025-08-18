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
       r.role_name,
       r.created_by,
       r.created_dts
  FROM user u LEFT OUTER JOIN user_role r
    ON u.userid = r.userid
   AND u.domain_id = r.domain_id
   AND r.role_name = {3}
 WHERE u.userid != 'anonymous'
 ORDER BY u.userid
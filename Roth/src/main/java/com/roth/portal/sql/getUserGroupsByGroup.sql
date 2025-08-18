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
       g.group_id,
       g.created_by,
       g.created_dts
  FROM user u LEFT OUTER JOIN user_group g
    ON u.userid = g.userid
   AND u.domain_id = g.domain_id 
   AND g.group_id = {3}
 WHERE u.userid != 'anonymous'
   AND u.domain_id = {2}
 ORDER BY u.userid
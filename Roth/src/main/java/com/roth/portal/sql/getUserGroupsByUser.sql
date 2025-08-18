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
       g.domain_id,
       g.group_id,
       g.group_name,
       g.parent_group_id,
       g.lineage,
       u.created_by,
       u.created_dts
  FROM `group` g LEFT OUTER JOIN user_group u
    ON g.group_id = u.group_id
   AND g.domain_id = u.domain_id
   AND u.userid = {1}
 WHERE g.domain_id = {2}
 ORDER BY get_group_sort(g.group_id)
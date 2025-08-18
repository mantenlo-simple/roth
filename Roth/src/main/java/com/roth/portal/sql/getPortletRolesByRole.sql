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
SELECT p.portlet_name,
       p.portlet_id,
       r.role_name,
       r.created_by,
       r.created_dts
  FROM portlet p LEFT OUTER JOIN portlet_role r
    ON p.portlet_id = r.portlet_id
   AND r.role_name = {1}
 WHERE p.portlet_name != 'default'
 ORDER BY p.portlet_name
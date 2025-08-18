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
SELECT 'book' AS table_name, 
       book_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM book
UNION ALL 
SELECT 'desktop' AS table_name,
       desktop_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM desktop
UNION ALL
SELECT 'domain' AS table_name,
       domain_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM domain
UNION ALL
SELECT 'domain_property' AS table_name,
       property_name AS table_id,
       domain_id AS owner_id,
       updated_dts
  FROM domain_property
UNION ALL
SELECT 'group' AS table_name,
       group_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM `group`
UNION ALL
SELECT 'group_role' AS table_name,
       role_name AS table_id,
       group_id AS owner_id,
       updated_dts
  FROM group_role
UNION ALL
SELECT 'link' AS table_name,
       link_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM link
UNION ALL
SELECT 'link_role' AS table_name,
       role_name AS table_id,
       link_id AS owner_id,
       updated_dts
  FROM link_role
UNION ALL
SELECT 'page' AS table_name,
       portlet_id AS table_id,
       book_id AS owner_id,
       updated_dts
  FROM page
UNION ALL
SELECT 'page_property' AS table_name,
       property_name AS table_id,
       portlet_id || '.' || book_id AS owner_id,
       updated_dts
  FROM page_property
UNION ALL
SELECT 'portlet' AS table_name,
       portlet_id AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM portlet
UNION ALL
SELECT 'portlet_role' AS table_name,
       role_name AS table_id,
       portlet_id AS owner_id,
       updated_dts
  FROM portlet_role
UNION ALL
SELECT 'role' AS table_name,
       role_name AS table_id,
       NULL AS owner_id,
       updated_dts
  FROM role
UNION ALL
SELECT 'role_property' AS table_name,
       property_name AS table_id,
       role_name AS owner_id,
       updated_dts
  FROM role_property
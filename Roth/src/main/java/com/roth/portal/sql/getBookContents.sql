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
SELECT parent_book_id AS book_id,
       'book' AS table_name, 
       book_id AS table_id,
       book_name AS name, 
       book_title AS title, 
       sequence,
       updated_by,
       updated_dts
  FROM book
 WHERE parent_book_id = {1}
 UNION ALL
SELECT p.book_id,
       'portlet', 
       t.portlet_id,
       t.portlet_name,
       p.page_title,
       p.sequence,
       p.updated_by,
       p.updated_dts
  FROM portlet t,
       page p
 WHERE p.portlet_id = t.portlet_id
   AND p.book_id = {1}
 ORDER BY sequence
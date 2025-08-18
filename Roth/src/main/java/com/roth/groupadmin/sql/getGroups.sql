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
SELECT group_id, group_name
  FROM `group` x
 WHERE EXISTS (SELECT g.lineage
	             FROM `group` g,
	                  group_role r,
	                  user_group u
	            WHERE g.group_id = r.group_id
	              AND g.group_id = u.group_id
	              AND r.role_name = 'GroupAdmin'
	              AND u.userid = {1}
	              AND u.domain_id = {2}
	              AND g.lineage LIKE CONCAT('% ', x.group_id, ' %'))
	OR EXISTS (SELECT p.lineage
	             FROM `group` g,
	                  `group` p,
	                  group_role r,
	                  user_group u
	            WHERE p.group_id = g.parent_group_id
	              AND g.group_id = r.group_id
	              AND g.group_id = u.group_id
	              AND r.role_name = 'GroupAdmin'
	              AND u.userid = {1}
	              AND u.domain_id = {2}
	              AND x.lineage LIKE CONCAT(p.lineage, '%'))
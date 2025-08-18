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
package com.roth.jdbc.model;

public interface StateBean {
	/**
	 * Return true if the an INSERT should be performed, or false if an UPDATE should be performed.
	 * This should only return true once in the lifetime of the instance.  If true is returned, the
	 * data upon which the check is performed should be altered to result in false in subsequent calls.
	 * @return
	 */
	public boolean isNew();
}

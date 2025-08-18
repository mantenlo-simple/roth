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

public interface EnhancedBean extends StateBean {
	/**
	 * Copy all data from this bean into a new bean of the same type.
	 * Why do this?  Because copy constructors aren't polymorphic.
	 * @param source
	 */
	EnhancedBean copy();
	
	/**
	 * Merge data from a source bean into this bean.  This should avoid copying
	 * primary keys and audit columns.
	 * @param source
	 */
	void merge(EnhancedBean source);
}
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
package com.roth.base.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.roth.base.util.ValidateConstants;

/**
 * Defines validation for data.
 * @author James M. Payne
 *
 */
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Validate {
	// General
	boolean notNull() default false;
	
	// String Validation
	int strCase() default ValidateConstants.CASE_NORMAL;
	int minLength() default 0; // 0 => no minimum
	int maxLength() default 0; // 0 => no maximum
	
	// Numeric Validation
	boolean signed() default true;
	int precision() default -1; // -1 => no limit
	
	// Date Validation
	int dateResolution() default ValidateConstants.DATE_SECOND; // If java.sql.Date, then this is overridden by equivalent of DATE_DAY

	// Numerical and Date Validation
	String min() default ""; // Minimum value allowed; for numerical and date values; format evaluated against setter data type.
	String max() default ""; // Maximum value allowed; for numerical and date values; format evaluated against setter data type.
}
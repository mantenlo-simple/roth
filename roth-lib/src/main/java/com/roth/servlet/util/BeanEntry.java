package com.roth.servlet.util;

import com.roth.jdbc.model.StateBean;

public record BeanEntry(
	Class<? extends StateBean> beanClass,
	String beanName,
	String jndiName,
	String primaryKey,
	String parentKey,
	String primaryFilter,
	String parentFilter,
	String primarySetter
) {}
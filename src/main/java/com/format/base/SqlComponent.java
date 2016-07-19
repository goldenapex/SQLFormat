package com.format.base;

public abstract class SqlComponent {

	protected abstract void analysis(String sql);

	protected void ad(SqlComponent component) {
		throw new AppException(null);
	}

}

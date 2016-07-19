package com.format.sql;

import java.util.ArrayList;
import java.util.List;

import com.format.base.AppException;
import com.format.base.SqlComponent;
import com.format.common.Key;
import com.format.util.SqlAnalysUtils;
import com.format.util.StringUtils;

public class Column extends SqlComponent {

	private List<SqlComponent> subSelectSentence = null;

	private String colName = null;

	private String aliasColName = null;

	private Object value = null;

	private boolean isSubSentence = false;

	public Column(String column) {

		if (StringUtils.isEmpty(column)) {
			throw new AppException("error:cause column is empty! [" + column + "]");
		}


		if (!SqlAnalysUtils.isSelectSentence(column)) {
			// --------------------------------------------------------
			// 通常カラムの場合
			// --------------------------------------------------------
			String arr[] = column.split(Key.COMMA);

			if (arr == null || !(arr.length == 1 || arr.length == 2)) {
				throw new AppException("error:Illigal column. [" + column + "]");
			}

			if (arr[0] != null) {
				this.colName = arr[0];
			}

			if (arr.length == 2 && arr[1] != null) {
				this.aliasColName = arr[1];
			}

			return;

		} else {
			// --------------------------------------------------------
			// 副問い合わせ句の場合
			// --------------------------------------------------------
			if (this.subSelectSentence == null) {
				this.subSelectSentence = new ArrayList<SqlComponent>();
			}

			SelectSentence subSelectSentence = new SelectSentence(column);
			this.subSelectSentence.add(subSelectSentence);

		}
	}

	public void add(SqlComponent component) {
		this.subSelectSentence.add(component);
	}

	@Override
	protected void analysis(String sql) {
		// TODO 自動生成されたメソッド・スタブ

	}
}

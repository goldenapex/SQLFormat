package com.format.base;

import java.util.ArrayList;
import java.util.List;

import com.format.common.Key;
import com.format.sql.SelectSentence;
import com.format.util.SqlAnalysUtils;

public class SearchTable extends SqlComponent {

	private List<SqlComponent> subSelectSentence = null;

	private String tblName = null;

	private String aliasTblName = null;

	private boolean isSubSentence = false;



	public List<SqlComponent> getSubSelectSentence() {
		return subSelectSentence;
	}



	public String getTblName() {
		return tblName;
	}



	public String getAliasTblName() {
		return aliasTblName;
	}



	public boolean isSubSentence() {
		return isSubSentence;
	}

	public SearchTable(String tableWords) {
		if (tableWords == null) {
			throw new AppException("the table is null!");
		}

		// =====================================================
		// 通常カラムの場合
		// =====================================================
		if (!SqlAnalysUtils.isSelectSentence(tableWords)) {
			String arr[] = tableWords.split(Key.COMMA);

			if (arr == null || arr.length == 0 || arr.length > 2) {
				throw new AppException("[" + tableWords + "]:error column!");
			}

			if (arr[0] != null) {
				this.tblName = arr[0].trim();
			}

			if (arr.length == 2 && arr[1] != null) {
				this.aliasTblName = arr[1].trim();
			}

			return;
		}

		// =====================================================
		// 副問い合わせ句の場合
		// =====================================================
		if (this.subSelectSentence == null) {
			this.subSelectSentence = new ArrayList<SqlComponent>();
		}

		SelectSentence subSelectSentence =new SelectSentence(tableWords);
		this.subSelectSentence.add(subSelectSentence);

	}


	public SearchTable(String subSelect, String aliasTblName) {
		if (subSelect == null || aliasTblName == null) {
			throw new AppException("this table is null!");
		}

		// =====================================================
		// 副問い合わせ句
		// =====================================================
		if (this.subSelectSentence == null) {
			this.subSelectSentence = new ArrayList<SqlComponent>();
		}

		SelectSentence subSelectSentence =new SelectSentence(subSelect);
		this.subSelectSentence.add(subSelectSentence);

		this.aliasTblName = aliasTblName;
	}



	@Override
	protected void analysis(String sql) {
		// TODO 自動生成されたメソッド・スタブ

	}

}

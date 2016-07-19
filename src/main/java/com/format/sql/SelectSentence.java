package com.format.sql;

import java.util.ArrayList;
import java.util.List;

import com.format.base.SqlComponent;
import com.format.util.SqlAnalysUtils;

public class SelectSentence extends SqlComponent {

	private List<SqlComponent> selectColumns = new ArrayList<SqlComponent>();
	private List<SqlComponent> fromTables = new ArrayList<SqlComponent>();
	private List<SqlComponent> whereConditions = new ArrayList<SqlComponent>();
	private List<SqlComponent> groupByColumns = new ArrayList<SqlComponent>();
	private List<SqlComponent> orderByColumns = new ArrayList<SqlComponent>();

	public SelectSentence(String content) {
		analysis(content);
	}

	@Override
	protected void analysis(String sql) {

		StringBuffer selectWords = new StringBuffer();
		StringBuffer fromWords = new StringBuffer();
		StringBuffer whereWords = new StringBuffer();
		StringBuffer groupByWords = new StringBuffer();
		StringBuffer orderByWords = new StringBuffer();

		// select句を取得＆各カラムを初期化
		SqlAnalysUtils.disassembleSelectSentence(sql,  selectWords, fromWords, whereWords, groupByWords, orderByWords);

		SqlAnalysUtils.getAllColumnsBySelectWords(selectColumns, selectWords.toString());

		SqlAnalysUtils.getAllTablesByFromWords(fromTables, fromWords.toString());

		SqlAnalysUtils.getAllConditionsByWhereWords(whereConditions, whereWords.toString());
	}

}

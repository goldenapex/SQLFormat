package com.format.sql;

import java.util.ArrayList;
import java.util.List;

import com.format.base.AppException;
import com.format.base.ConditionPosition;
import com.format.base.SqlComponent;
import com.format.util.SqlAnalysUtils;
import com.format.util.StringUtils;

public class WhereCondition extends SqlComponent {

	private List<SqlComponent> subSelectSentence = null;

	private List<SqlComponent> subWhereCondition = null;

	private SqlComponent leftCondition = null;

	private SqlComponent rightCondition = null;

	private String nestedOperation = null;

	private String innerLogicOperation = null;

	private String andOrLogicOperation = null;

	private boolean isSubSentence = false;

	private boolean ishaveParenthesis = false;

	public WhereCondition(String leftConditionWords, String rightConditionWords, String comparisonOperation, String andOrOperation) {

		// paremter check.
		if (StringUtils.isEmpty(leftConditionWords)
				|| StringUtils.isEmpty(rightConditionWords)) {
			throw new AppException("the where condition is null!");
		}


	}

	public WhereCondition(String andOrLogicOperation, String condition) {

		// ====================================================
		// parameter check
		// ====================================================
		if (StringUtils.isEmpty(condition)) {
			throw new AppException("the condition is null!");
		}

		// ====================================================
		// 構文チェック
		// ====================================================
		ConditionPosition firstComparisonPos = SqlAnalysUtils.getFirstPositionByComparisonOperation(condition);
		if (firstComparisonPos == null) {
			throw new AppException("there is no any comparison on the condition!");
		}

		String leftCondition = StringUtils.getSubString(condition, 0, firstComparisonPos.getStart(), true);
		String rightCondition = StringUtils.getSubString(condition, 0, firstComparisonPos.getEnd(), true);

		WhereCondition whereCondition = new WhereCondition(leftCondition, rightCondition, firstComparisonPos.getLogicOperation(), null);

		// ====================================================
		// 副問い合わせ句
		// ====================================================
		if (this.subSelectSentence == null) {
			this.subSelectSentence = new ArrayList<SqlComponent>();
		}

		SelectSentence subSelectSentence = new SelectSentence(subSelect);
		this.subSelectSentence.add(subSelectSentence);
	}


	public void add(SqlComponent component) {
		this.subSelectSentence.add(component);
	}

	@Override
	protected void analysis(String sql) {

	}

}

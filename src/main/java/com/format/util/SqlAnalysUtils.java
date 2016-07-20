package com.format.util;

import java.util.ArrayList;
import java.util.List;

import com.format.base.AppException;
import com.format.base.ConditionPosition;
import com.format.base.SearchTable;
import com.format.base.SqlComponent;
import com.format.common.Key;
import com.format.sql.Column;

public class SqlAnalysUtils {

	public static enum AND_OR_OPERATION {
		AND(1,			"AND",		"AND"),
		OR(2,			"OR",		"OR");

		private int id;
		private String searchKey;
		private String displayKey;

		public int getId() {
			return this.id;
		}

		public String getSearchKey() {
			return this.searchKey;
		}

		public String getDisplayKey() {
			return this.displayKey;
		}

		private AND_OR_OPERATION(int id, String searchKey, String displayKey) {
			this.id = id;
			this.searchKey = searchKey;
			this.displayKey = displayKey;
		}
	}


	public static enum COMPARISION_OPERATION {
		EQUAL		(0		, "="				, "="			),
		NOT_EQUAL1	(1		, "!="				, "!="			),
		NOT_EQUAL2	(2		, "^="				, "^="			),
		NOT_EQUAL3	(3		, "<>"				, "<>"			),
		LARGE		(4		, ">"				, ">"			),
		LARGE_EQUAL	(5		, ">="				, ">="			),
		SMALL		(6		, "<"				, "<"			),
		SMALL_EQUAL	(7		, "<="				, "<="			),
		ANY			(8		, "ANY"				, "ANY"			),
		SOME		(9		, "SOME"			, "SOME"		),
		IN			(10		, "IN"				, "IN"			),
		NOT_IN		(11		, "NOT\\s+IN"		, "NOT IN"		),
		NOT_ANY		(12		, "NOT\\s+ANY"		, "NOT ANY"		),
		ALL			(13		, "ALL"				, "ALL"			),
		BETWEEN		(14		, "BETWEEN"			, "BETWEEN"		),
		NOT_BETWEEN	(15		, "NOT\\s+BETWEEN"	, "NOT BETWEEN"	),
		EXIST		(16		, "EXIST"			, "EXIST"		),
		NOT_EXIST	(17		, "NOT\\s+EXIST"	, "NOT EXIST"	),
		LIKE		(18		, "LIKE"			, "LIKE"		),
		NOT_LIKE	(19		, "NOT\\s+LIKE"		, "NOT LIKE"	),
		IS_NULL		(20		, "IS\\s+NULL"		, "IS NULL"		),
		NOT_NULL	(21		, "NOT\\s+NULL"		, "NOT NULL"	);


		private int id;
		private String searchKey;
		private String displayKey;

		public int getId() {
			return this.id;
		}

		public String getSearchKey() {
			return this.searchKey;
		}

		public String getDisplayKey() {
			return this.displayKey;
		}

		private COMPARISION_OPERATION(int id, String searchKey, String displayKey) {
			this.id = id;
			this.searchKey = searchKey;
			this.displayKey = displayKey;
		}
	}



	/**
	 * SELECT文にて、各要素を切り出す。
	 * @param content
	 * @param selectWords
	 * @param fromWords
	 * @param whereWords
	 * @param gropuByWords
	 * @param orderByWords
	 * @return
	 */
	public static boolean disassembleSelectSentence(
			String content,
			StringBuffer selectWords,
			StringBuffer fromWords,
			StringBuffer whereWords,
			StringBuffer gropuByWords,
			StringBuffer orderByWords) {

		// parameter check.
		if (StringUtils.isEmpty(content)) {
			return false;
		}


		content = content.trim();

		// get select words.
		int afterFromKeyIdx = getSelectWords(selectWords, content);

		// get from words.
		int afterWhereKeyIdx = getFromWords(fromWords, content, afterFromKeyIdx);

		// get where words.
		if (afterWhereKeyIdx > 0) {
			getWhereWords(whereWords, gropuByWords, orderByWords, content, afterWhereKeyIdx);
		}

		return true;
	}


	/**
	 * SELECT文にて、select句のみを切り出す。
	 * @param selectWords
	 * @param content
	 * @return
	 */
	private static int getSelectWords(StringBuffer selectWords, String content) {
		// =================================================
		// get select words.
		// =================================================
		List<Integer> selectIndexes = new ArrayList<Integer>();
		List<Integer> fromIndexes = new ArrayList<Integer>();

		StringUtils.getIndexesWithKey(selectIndexes, content, 0, Key.SELECT);
		StringUtils.getIndexesWithKey(fromIndexes, content, 0, Key.FROM);

		int fromCnt = 0;
		int selectCnt = 0;

		int startWithAfterFromIdx = 0;

		for (Integer fromIdx : fromIndexes) {
			selectCnt = 0;
			fromCnt++;

			for (Integer selectIdx : selectIndexes) {
				if (selectIdx > fromIdx) {
					break;
				}

				selectCnt++;
			}

			if (fromCnt == selectCnt) {
				selectWords.append(StringUtils.getSubString(content, Key.SELECT.length(), fromIdx, true));
				startWithAfterFromIdx = fromIdx + Key.FROM.length();
				break;
			}
		}

		return startWithAfterFromIdx;
	}



	/**
	 * SELECT文にて、FROM句のみを切り出す。
	 * @param fromWords
	 * @param content
	 * @param startWithAfterFromIdx
	 * @return
	 */
	private static int getFromWords(StringBuffer fromWords, String content, int startWithAfterFromIdx) {
		int startWithAfterWhereIdx = 0;

		// ====================================================
		// get from words.
		// ====================================================
		String afterFromKeyContent = StringUtils.getSubString(content, startWithAfterFromIdx, false);

		List<Integer> whereIndexes = new ArrayList<Integer>();
		List<Integer> openParenthesisIndexes = new ArrayList<Integer>();
		List<Integer> closeParenthesisIndexes = new ArrayList<Integer>();

		StringUtils.getIndexesWithKey(whereIndexes, afterFromKeyContent, 0, Key.WHERE);
		StringUtils.getIndexesWithKey(openParenthesisIndexes, afterFromKeyContent, 0, Key.OPEN_PARENTHESIS);
		StringUtils.getIndexesWithKey(closeParenthesisIndexes, afterFromKeyContent, 0, Key.CLOSE_PARENTHESIS);

		// ====================================================
		// from句後にwhere句無しの場合、副問い合わせ文を含むか含まないかと関係なく
		// select句以外はfrom句になる。
		// ====================================================
		if (whereIndexes.size() == 0) {
			fromWords.append(afterFromKeyContent);
		}

		// ====================================================
		// from句後にwhere句が続く、副問い合わせ文を含まない場合
		// ⇒whereキーの直前までが、from句となる。
		//
		// from句後にwhere句が続く、副問い合わせ文が含まれている場合、
		// ⇒括弧の組み合わせが成り立つwhereキーのまでのすべてが、from句となる。
		// ====================================================
		else {
			for (Integer whereIdx : whereIndexes) {
				int openParenthesisCnt = StringUtils.countTillDesignatedPosition(openParenthesisIndexes, whereIdx);
				int closeParenthesisCnt = StringUtils.countTillDesignatedPosition(closeParenthesisIndexes, whereIdx);

				// 括弧の組み合わせがちゃんとペアされている場合、whereキー直前まではfrom句となる。
				if (openParenthesisCnt == closeParenthesisCnt) {
					fromWords.append(StringUtils.getSubString(afterFromKeyContent, 0, whereIdx, true));
					startWithAfterWhereIdx = startWithAfterFromIdx + whereIdx + Key.WHERE.length();
					break;
				}
			}
		}

		return startWithAfterWhereIdx;
	}



	/**
	 *
	 * @param whereWords
	 * @param groupByWords
	 * @param orderByWords
	 * @param content
	 * @param afterWhereKeyIdx
	 * @return
	 */
	public static int getWhereWords(
			StringBuffer whereWords,
			StringBuffer groupByWords,
			StringBuffer orderByWords,
			String content,
			int afterWhereKeyIdx) {

		int afterGroupByKeyContent = 0;

		List<Integer> subSelectIndexes = new ArrayList<Integer>();
		List<Integer> openParenthesisIndexes = new ArrayList<Integer>();
		List<Integer> closeParenthesisIndexes = new ArrayList<Integer>();

		String afterWhereKeyContent = StringUtils.getSubString(content, afterWhereKeyIdx, false);

		int lastGroupByIdx = StringUtils.getLastIndexWithKey(afterWhereKeyContent, Key.GROUP_BY);
		int lastOrderByIdx = StringUtils.getLastIndexWithKey(afterWhereKeyContent, Key.ORDER_BY);

		StringUtils.getIndexesWithKey(subSelectIndexes, afterWhereKeyContent, 0, Key.SUB_SELECT);
		StringUtils.getIndexesWithKey(openParenthesisIndexes, afterWhereKeyContent, 0, Key.OPEN_PARENTHESIS);
		StringUtils.getIndexesWithKey(closeParenthesisIndexes, afterWhereKeyContent, 0, Key.CLOSE_PARENTHESIS);

		int mostNearSubSelctIdx = StringUtils.countTillDesignatedPosition(subSelectIndexes, lastGroupByIdx);
		int openParenthesisCntByGroup = StringUtils.countKeyInRegion(afterWhereKeyContent, Key.GROUP_BY, mostNearSubSelctIdx, lastGroupByIdx);
		int closeParenthesisCntByGroup = StringUtils.countKeyInRegion(afterWhereKeyContent, Key.ORDER_BY, mostNearSubSelctIdx, lastGroupByIdx);

		mostNearSubSelctIdx = StringUtils.countTillDesignatedPosition(subSelectIndexes, lastOrderByIdx);
		int openParenthesisCntByOrder = StringUtils.countKeyInRegion(afterWhereKeyContent, Key.GROUP_BY, mostNearSubSelctIdx, lastOrderByIdx);
		int closeParenthesisCntByOrder = StringUtils.countKeyInRegion(afterWhereKeyContent, Key.ORDER_BY, mostNearSubSelctIdx, lastOrderByIdx);

		// --------------------------------------
		// group by =　無し、 order by = 無し
		// --------------------------------------
		if (lastGroupByIdx == -1 && lastOrderByIdx == -1) {
			whereWords.append(afterWhereKeyContent);
		}
		// --------------------------------------
		// group by =　有り、 order by = 無し
		// --------------------------------------
		else if (lastGroupByIdx != -1 && lastOrderByIdx == -1) {
			// 副問い合わせ = 無し
			if (!isSelectSentence(afterWhereKeyContent)) {
				whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastGroupByIdx, true));
			}
			else {
				// 副問い合わせ中のgroup by句である
				if (openParenthesisCntByGroup == closeParenthesisCntByGroup) {
					whereWords.append(afterWhereKeyContent);
				}
				// 副問い合わせ外側のgroup by句である
				else {
					whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastGroupByIdx, true));
				}
			}

			groupByWords.append(StringUtils.getSubString(afterWhereKeyContent, lastGroupByIdx + Key.GROUP_BY.length(), true));
		}
		// --------------------------------------
		// group by =　無し、 order by = 有り
		// --------------------------------------
		else if (lastGroupByIdx == -1 && lastOrderByIdx != -1) {
			if (!isSelectSentence(afterWhereKeyContent)) {
				whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastOrderByIdx, true));
			}
			else {
				// 副問い合わせ中のgroup by句である
				if (openParenthesisCntByOrder == closeParenthesisCntByOrder) {
					whereWords.append(afterWhereKeyContent);
				}
				// 副問い合わせ外側のgroup by句である
				else {
					whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastOrderByIdx, true));
				}
			}

			orderByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.ORDER_BY, lastOrderByIdx), true));
		}
		// --------------------------------------
		// group by =　有り、 order by = 有り
		// --------------------------------------
		else {
			if (!isSelectSentence(afterWhereKeyContent)) {
				whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastGroupByIdx, true));
				groupByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.GROUP_BY, lastGroupByIdx), lastOrderByIdx, true));
				orderByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.ORDER_BY, lastOrderByIdx), lastOrderByIdx, true));
			}
			else {
				// group by句が副問い合わせ以外の場合
				if (openParenthesisCntByGroup == closeParenthesisCntByGroup) {
					whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastGroupByIdx, true));

					// group by句後にorder by句が続く場合
					if (lastOrderByIdx > lastGroupByIdx) {
						groupByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.GROUP_BY, lastGroupByIdx), lastOrderByIdx, true));
						groupByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.ORDER_BY, lastOrderByIdx), true));
					}
					else {
						groupByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.GROUP_BY, lastGroupByIdx), true));

					}
				}
				else {
					// group byが副問い合わせ中で、order byが副問い合わせ外側の場合
					if (openParenthesisCntByOrder == closeParenthesisCntByOrder) {
						whereWords.append(StringUtils.getSubString(afterWhereKeyContent, 0, lastOrderByIdx, true));
						orderByWords.append(StringUtils.getSubString(afterWhereKeyContent, StringUtils.getEndIndexOfKeyByPosition(afterWhereKeyContent, Key.ORDER_BY, lastOrderByIdx), true));
					}
					// order byとgroup by共に副問い合わせ中である場合
					else {
						whereWords.append(afterWhereKeyContent);
					}
				}
			}
		}

		return afterGroupByKeyContent;
	}


	/**
	 * SELECT文にて、select句のみを切り出す。
	 * @param sql
	 * @return
	 */
	public static String getSelect(String sql) {
		String select = "";

		List<Integer> selectIndexes = new ArrayList<Integer>();
		List<Integer> fromIndexes = new ArrayList<Integer>();
		List<Integer> whereIndexes = new ArrayList<Integer>();

		sql = sql.trim();

		StringUtils.getIndexesWithKey(selectIndexes, sql,  0 , Key.SELECT);
		StringUtils.getIndexesWithKey(fromIndexes, sql,  0 , Key.FROM);
		StringUtils.getIndexesWithKey(whereIndexes, sql,  0 , Key.WHERE);

		int selectCnt = 0;
		int fromCnt = 0;
		for (Integer fromIdx : fromIndexes) {
			fromCnt++;

			for (Integer selectIdx : selectIndexes) {
				selectCnt++;

				if (fromCnt == selectCnt) {
					select = sql.substring(Key.SELECT.length(), fromIdx);
					break;
				}
			}
		}

		return select;
	}



	public static void getAllColumnsBySelectWords(List<SqlComponent> selectColumns, String selectWords) {

		selectWords = selectWords.trim();

		// 副問い合わせ文がない場合、一回ですべてのカラムを切り取る
		if (!isHaveSubSelectSentence(selectWords)) {
			String[] arrColumn = selectWords.split(Key.COMMA);
			for (String item : arrColumn) {
				Column column = new Column(item);
				selectColumns.add(column);
			}

			return;
		}

		getAllColumnsHaveSubSelect(selectColumns, selectWords);
	}


	public static void getAllColumnsHaveSubSelect(List<SqlComponent> columns, String selectContent) {

		selectContent = selectContent.trim();
		int firstCommaIdx = selectContent.indexOf(Key.COMMA);

		String firstItem = "";


		if (firstCommaIdx != -1) {
			firstItem = StringUtils.getSubString(selectContent, 0, firstCommaIdx, true);
		}
		else {
			firstItem = selectContent;
		}

		// カンマで切り取った文言が通常カラムの場合
		Column column = null;
		if (!isHaveSubSelectSentence(firstItem)) {
			column = new Column(firstItem);
			columns.add(column);

			if (firstCommaIdx != -1) {
				selectContent = StringUtils.getSubString(selectContent, firstCommaIdx+1, true);
			}
			else {
				return;
			}

			getAllColumnsHaveSubSelect(columns, selectContent);
		}
		// カンマで切り取った文言が副問い合わせ文の場合
		else {
			StringBuffer subSelect = new StringBuffer();
			String nextColumnsContent = getSubSelect(subSelect, selectContent);
			column = new Column(subSelect.toString());
			columns.add(column);
			if (firstCommaIdx == -1) {
				return;
			}

			getAllColumnsHaveSubSelect(columns, nextColumnsContent);
		}

		if (firstCommaIdx == -1) {
			return;
		}
	}


	public static void getAllTablesByFromWords(List<SqlComponent> fromTables, String fromWords) {
		fromWords = fromWords.trim();

		// 副問い合わせ文がない場合、一回ですべてのテーブルを切り取る
		if (!isHaveSubSelectSentence(fromWords)) {
			String[] arrTable = fromWords.split(Key.COMMA);
			for (String item : arrTable) {
				SearchTable table = new SearchTable(item);
				fromTables.add(table);
			}

			return;
		}

		getAllTablesHaveSubSelect(fromTables, fromWords);
	}

	public static void getAllTablesHaveSubSelect(List<SqlComponent> fromTables, String fromWords) {
		fromWords = fromWords.trim();
		int firstCommaIdx = fromWords.indexOf(Key.COMMA);

		String firstItem = "";
		if (firstCommaIdx != -1) {
			firstItem = StringUtils.getSubString(fromWords, 0, firstCommaIdx, true);
		}
		else {
			firstItem = fromWords;
		}

		// カンマで切り取った文言が通常カラムの場合
		SearchTable searchTable = null;
		if (!isHaveSubSelectSentence(firstItem)) {
			searchTable = new SearchTable(firstItem);
			fromTables.add(searchTable);

			if (firstCommaIdx != -1) {
				fromWords = StringUtils.getSubString(fromWords, firstCommaIdx+1, true);
			}
			else {
				return;
			}

			getAllTablesHaveSubSelect(fromTables, fromWords);
		}
		else {
			StringBuffer subSelect = new StringBuffer();
			String nextColumnContent = getSubSelect(subSelect, fromWords);

			// 副問い合わせ文に仮テーブル名が付けている場合、即ち副問い合わせにカンマ以外の文字が続く場合
			String aliasTblName = "";
			if (nextColumnContent.length()>0 && nextColumnContent.indexOf(Key.COMMA)>0) {
				aliasTblName = StringUtils.getSubStringUntilKey(nextColumnContent, Key.COMMA, true);
				nextColumnContent = StringUtils.getSubStringAfterKey(nextColumnContent, Key.COMMA, true);
			}

			searchTable = new SearchTable(subSelect.toString(), aliasTblName);
			fromTables.add(searchTable);
			if (firstCommaIdx == -1) {
				return;
			}

			getAllTablesHaveSubSelect(fromTables, nextColumnContent);
		}

		if (firstCommaIdx == -1) {
			return;
		}
	}


	public static void getAllConditionsByWhereWords(List<SqlComponent> whereConditions, String whereWords) {

		whereWords = whereWords.trim();

		ConditionPosition firstAndOrPos = null;
		ConditionPosition firstComparisonPos = null;


		// ============================================
		// 最初に現れた論理演算子を元に、検索条件を分ける
		// ============================================
		// AND/OR論理演算子が尽きるまでループする
		firstAndOrPos = getFirstPositionByAndOrOperation(whereWords);

		// 条件式が一つしかない場合、条件式を比較演算子を元に左右式を分ける
		if (firstAndOrPos == null) {

			whereConditions.add(null, whereCondition);
			return;
		}
		// 条件式が複数ある場合
		else {

		}

		getAllTablesHaveSubSelect(whereConditions, whereWords);
	}


	public static String getSubSelect(StringBuffer subSelect, String subSelectContent) {

		String nextColumnsContent = "";

		// ==============================================
		// fromキー以降のＳＱＬ文から、ＳＱＬ文の末尾の位置を判定し、副問い合わせ文全体を切り取る
		// ==============================================
		List<Integer> openParenthesisIndexes = new ArrayList<Integer>();
		List<Integer> closeParenthesisIndexes = new ArrayList<Integer>();

		StringUtils.getIndexesWithKey(openParenthesisIndexes, subSelectContent, 0, Key.OPEN_PARENTHESIS);
		StringUtils.getIndexesWithKey(closeParenthesisIndexes, subSelectContent, 0, Key.CLOSE_PARENTHESIS);

		int openParenthesisCnt = 0;
		int closeParenthesisCnt = 0;
		for (Integer closeParenthesisIdx : closeParenthesisIndexes) {
			closeParenthesisCnt++;

			openParenthesisCnt = StringUtils.countTillDesignatedPosition(openParenthesisIndexes, closeParenthesisIdx);

			// 最初の副問い合わせ句を取得する（括弧は捨てる)
			if (openParenthesisCnt == closeParenthesisCnt) {
				int idxOfSubSelectEnding = StringUtils.getStartIndexOfKeyByPosition(subSelectContent,  Key.CLOSE_PARENTHESIS, closeParenthesisIdx);
				subSelect.append(StringUtils.getSubString(subSelectContent, StringUtils.getStartIndexOfKeyByPosition(subSelectContent, Key.SELECT, 0), idxOfSubSelectEnding, true));

				// -----------------------------------------
				// 副問い合わせ以降を取得
				// -----------------------------------------
				int idxOfSubSelectEnded = StringUtils.getEndIndexOfKeyByPosition(subSelectContent, Key.CLOSE_PARENTHESIS, closeParenthesisIdx);
				nextColumnsContent = StringUtils.getSubString(subSelectContent, idxOfSubSelectEnded, true);

				// カンマがある場合、カンマを捨てる
				if (nextColumnsContent.length() > 0 && nextColumnsContent.indexOf(Key.COMMA) == 0) {
					nextColumnsContent = StringUtils.getSubString(nextColumnsContent, 1, true);
				}

				break;
			}
		}

		return nextColumnsContent.trim();
	}


	public static List<Column> getColumns(String sql) {
		List<Column> columns = new ArrayList<Column>();

		List<Integer> openIndexes = new ArrayList<Integer>();
		List<Integer> closeIndexes = new ArrayList<Integer>();

		StringUtils.getIndexesWithKey(openIndexes, sql, 0, Key.OPEN_PARENTHESIS);
		StringUtils.getIndexesWithKey(closeIndexes, sql, 0, Key.CLOSE_PARENTHESIS);

		if (isSelectSentence(sql)) {

		}
		else {
			String[] arrColumns = sql.split(Key.COMMA);
		}

		return columns;
	}



	public static boolean isSelectSentence(String sql) {
		boolean bRet = false;
		if (StringUtils.getStartIndexOfKeyByPosition(sql, Key.SELECT, 0) != -1) {
			bRet = true;
		}

		return bRet;
	}


	public static boolean isHaveSubSelectSentence(String sql) {
		boolean bRet = false;

		if (StringUtils.getStartIndexOfKeyByPosition(sql, Key.SUB_SELECT, 0) != -1) {
			bRet = true;
		}

		return bRet;
	}


	public static boolean isHaveNestedConditionFully(String condition) {
		boolean bRet = false;

		if (StringUtils.getStartIndexOfKeyByPosition(condition, Key.SUB_SELECT, 0) != -1) {
			bRet = true;
		}

		return bRet;
	}


	/**
	 * whereの条件文にて、論理演算子を区切りとして、最初に現れた絞りの条件式を切り取る。なお、最初の条件式には論理演算子なしとする。
	 * @param conditionWords
	 * @return
	 */
	public static ConditionPosition getFirstPositionByAndOrOperation(String conditionWords) {
		ConditionPosition pos = null;

		int startIdx = -1;
		int endIdx = -1;

		for (AND_OR_OPERATION item : AND_OR_OPERATION.values()) {
			startIdx = StringUtils.getStartIndexOfKeyByPosition(conditionWords, item.getSearchKey(), 0);

			if (startIdx != -1) {

				// ANDキーがBETWEEN～AND比較演算子の一部である場合、スキップする
				if (AND_OR_OPERATION.AND.getSearchKey().equals(item.getSearchKey())
						&& StringUtils.getFirstPosByKeyRegion(conditionWords, COMPARISION_OPERATION.BETWEEN.getSearchKey(), 0, startIdx) != null) {
					continue;
				}

				endIdx = StringUtils.getEndIndexOfKeyByPosition(conditionWords, item.getSearchKey(), 0);
				pos = new ConditionPosition(item.getDisplayKey(), startIdx, endIdx);
				break;
			}
		}

		return pos;
	}


	public static ConditionPosition getFirstPositionByComparisonOperation(String conditionWords) throws AppException {

		ConditionPosition pos = null;

		int startIdx = -1;
		int endIdx = -1;

		for (COMPARISION_OPERATION item : COMPARISION_OPERATION.values()) {
			startIdx = StringUtils.getStartIndexOfKeyByPosition(conditionWords, item.getSearchKey(), 0);
			if (startIdx != -1) {

				// サブクエリに含まれているＡＮＤかＯＲかを判定
				int openParenthesisCnt = StringUtils.countKeyInRegion(conditionWords, Key.OPEN_PARENTHESIS, 0, startIdx);
				int closeParenthesisCnt = StringUtils.countKeyInRegion(conditionWords, Key.CLOSE_PARENTHESIS, 0, startIdx);

				if (openParenthesisCnt != closeParenthesisCnt) {
					continue;
				}

				endIdx = StringUtils.getEndIndexOfKeyByPosition(conditionWords, item.getSearchKey(), 0);
				pos = new ConditionPosition(item.getDisplayKey(), startIdx, endIdx);
				break;
			}
		}

		if (pos == null) {
			throw new AppException("thre is no any comparison operation in where condition!!! [" + conditionWords + "]");
		}

		return pos;

	}



	public static String getNestedCondition(String condition) {
		String nestedCondition = null;

		nestedCondition = StringUtils.getSubStringAfterKey(condition, Key.OPEN_PARENTHESIS, true);

		return nestedCondition;
	}



}

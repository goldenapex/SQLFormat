package com.format.base;

public class ConditionPosition extends Position {

	private String logicOperation = null;

	public String getLogicOperation() {
		return logicOperation;
	}

	public ConditionPosition(String logicOperation, int start, int end) {
		super(start, end);

		this.logicOperation = logicOperation;
	}

}

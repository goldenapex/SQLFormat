package com.format.base;

public class Position {

	private int start = -1;

	private int end = -1;

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

	public Position(int start, int end) {
		if (start >= end) {
			throw new AppException("error: cause start >= end. start[" + start + "], end[" + end + "]");
		}

		this.start = start;
		this.end = end;
	}

}

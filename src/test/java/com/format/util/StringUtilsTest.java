package com.format.util;

import org.junit.Assert;
import org.junit.Test;

import com.format.util.StringUtils;

public class StringUtilsTest {

	@Test
	public void test() {
		String data = "(123)";
		String expect = "123";


		String actual = StringUtils.trimParenthesis(data);

		Assert.assertEquals(expect, actual);
	}

}

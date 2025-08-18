package com.roth.base.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.roth.base.util.Data;
import com.roth.portal.model.UserProfile;
import com.roth.portal.model.UserProperty;

class DataTest {
	static final String NOT_YET_IMPLEMENTED = "Not yet implemented";
	static final String DEFAULT = "Default";
	static final String HELLO = "Hello";
	static final String HELLO_SLASH = "Hello/";
	
	@Test
	void testNewInstanceClassOfT() {
		try {
			UserProperty property = Data.newInstance(UserProperty.class);
			assertTrue(property instanceof UserProperty);
		} catch (IllegalArgumentException | SecurityException | InstantiationException | IllegalAccessException
				| InvocationTargetException | NoSuchMethodException e) {
			fail("Unable to invoke new instance.");
		}
	}

	@Disabled
	@Test
	void testNewInstanceClassOfTClassOfQArrayObjectArray() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testGetDeclaredMethod() {
		try {
			UserProperty a = new UserProperty();
			a.setUserid("test");
			Method method = Data.getDeclaredMethod(a.getClass(), "getUserId");
			assertEquals(method.invoke(a), "test");
		} catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			fail("Unable to get declared method.");
		}
	}

	
	@Disabled
	@Test
	void testGetContextEnv() {
		/* DO NOT REENABLE: This is not testable outside of a running Tomcat instance.
		   This is here as a placeholder to ensure someone doesn't try to test it. */
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testWriteTempFile() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testReadTeampFile() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testIsEmpty() {
		assertTrue(Data.isEmpty(""));
		assertTrue(Data.isEmpty(null));
		assertTrue(!Data.isEmpty(HELLO));
	}

	@Test
	void testEvl() {
		assertEquals(Data.evl(null, DEFAULT), DEFAULT);
		assertEquals(Data.evl("", DEFAULT), DEFAULT);
		assertEquals(Data.evl(HELLO, DEFAULT), HELLO);
	}

	@Test
	void testNvlString() {
		assertEquals(Data.nvl(null), "");
		assertEquals(Data.nvl(""), "");
		assertEquals(Data.nvl(HELLO), HELLO);
		assertEquals(Data.nvl(null, 25), 25);
		assertEquals(Data.nvl(null, "Blue", "String"), "Blue");
		assertEquals(Data.nvl(null, null, null, 27.5), 27.5);
		assertEquals(Data.nvl(null, null, null, null, 28L), 28L);
	}

	@Test
	void testNvlStringString() {
		assertEquals(Data.nvl(null, DEFAULT), DEFAULT);
		assertEquals(Data.nvl("", DEFAULT), "");
		assertEquals(Data.nvl(HELLO, DEFAULT), HELLO);
	}

	@Test
	void testNvlIntegerInteger() {
		Integer a = null;
		assertTrue(Data.nvl(a, 1) == 1);
		a = 4;
		assertTrue(Data.nvl(a, 1) == 4);
	}

	@Test
	void testNvlLongLong() {
		Long a = null;
		assertTrue(Data.nvl(a, 1L) == 1L);
		a = 4L;
		assertTrue(Data.nvl(a, 1L) == 4L);
	}

	@Test
	void testNvlFloatFloat() {
		Float a = null;
		assertTrue(Data.nvl(a, 1.2f) == 1.2f);
		a = 4.5f;
		assertTrue(Data.nvl(a, 1.2f) == 4.5f);
	}

	@Test
	void testNvlDoubleDouble() {
		Double a = null;
		assertTrue(Data.nvl(a, 1.2d) == 1.2d);
		a = 4.5d;
		assertTrue(Data.nvl(a, 1.2d) == 4.5d);
	}

	@Test
	void testNvlBigDecimalBigDecimal() {
		BigDecimal a = null;
		BigDecimal d = BigDecimal.valueOf(1.2);
		assertEquals(Data.nvl(a, d), d);
		a = BigDecimal.valueOf(4.5);
		assertEquals(Data.nvl(a, d), a);
	}

	@Test
	void testNvlDateDate() {
		Date a = null;
		Date d = new Date();
		assertEquals(Data.nvl(a, d), d);
		a = Data.strToDate("2010-10-01");
		assertEquals(Data.nvl(a, d), a);
	}

	@Test
	void testEnvl() {
		// assertEquals(Data.envl(null), null);
		assertEquals(Data.envl(""), null);
		assertEquals(Data.envl(HELLO), HELLO);
	}

	@Test
	void testEnforceEnd() {
		assertEquals(Data.enforceEnd(HELLO, "/"), HELLO_SLASH);
		assertEquals(Data.enforceEnd(HELLO_SLASH, "/"), HELLO_SLASH);
	}

	@Test
	void testTrim() {
		assertEquals(Data.trim(HELLO), HELLO);
		assertEquals(Data.trim(" Hello"), HELLO);
		assertEquals(Data.trim("Hello "), HELLO);
		assertEquals(Data.trim(" Hello "), HELLO);
		assertEquals(Data.trim(" Hello \n "), HELLO);
	}

	@Test
	void testPad() {
		assertEquals(Data.pad("", ' ', 3, Data.Pad.LEFT), "   ");
		assertEquals(Data.pad("a", ' ', 3, Data.Pad.LEFT), "  a");
		assertEquals(Data.pad("aa", ' ', 3, Data.Pad.LEFT), " aa");
		assertEquals(Data.pad("aaa", ' ', 3, Data.Pad.LEFT), "aaa");
		assertEquals(Data.pad("", '0', 3, Data.Pad.LEFT), "000");
		assertEquals(Data.pad("1", '0', 3, Data.Pad.LEFT), "001");
		assertEquals(Data.pad("11", '0', 3, Data.Pad.LEFT), "011");
		assertEquals(Data.pad("111", '0', 3, Data.Pad.LEFT), "111");
		assertEquals(Data.pad("", ' ', 3, Data.Pad.RIGHT), "   ");
		assertEquals(Data.pad("a", ' ', 3, Data.Pad.RIGHT), "a  ");
		assertEquals(Data.pad("aa", ' ', 3, Data.Pad.RIGHT), "aa ");
		assertEquals(Data.pad("aaa", ' ', 3, Data.Pad.RIGHT), "aaa");
		assertEquals(Data.pad("", '0', 3, Data.Pad.RIGHT), "000");
		assertEquals(Data.pad("1", '0', 3, Data.Pad.RIGHT), "100");
		assertEquals(Data.pad("11", '0', 3, Data.Pad.RIGHT), "110");
		assertEquals(Data.pad("111", '0', 3, Data.Pad.RIGHT), "111");
	}

	static final String ONE = "one";
	static final String TWO = "two";
	static final String THREE = "three";
	
	@Test
	void testInStringStringArray() {
		
		String[] a = {ONE, TWO, THREE};
		assertTrue(Data.in(ONE, a));
		assertTrue(Data.in(TWO, a));
		assertTrue(Data.in(THREE, a));
		assertFalse(Data.in("four", a));
		assertFalse(Data.in(null, a));
	}

	@Disabled
	@Test
	void testInDateDateArray() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testInIntegerIntegerArray() {
		Integer[] a = {1, 2, 3};
		assertTrue(Data.in(1, a));
		assertTrue(Data.in(2, a));
		assertTrue(Data.in(3, a));
		assertFalse(Data.in(4, a));
		assertFalse(Data.in(null, a));
	}

	@Test
	void testInLongLongArray() {
		Long[] a = {1L, 2L, 3L};
		assertTrue(Data.in(1L, a));
		assertTrue(Data.in(2L, a));
		assertTrue(Data.in(3L, a));
		assertFalse(Data.in(4L, a));
		assertFalse(Data.in(null, a));
	}

	@Test
	void testInFloatFloatArray() {
		Float[] a = {1f, 2f, 3f};
		assertTrue(Data.in(1f, a));
		assertTrue(Data.in(2f, a));
		assertTrue(Data.in(3f, a));
		assertFalse(Data.in(4f, a));
		assertFalse(Data.in(null, a));
	}

	@Test
	void testInDoubleDoubleArray() {
		Double[] a = {1d, 2d, 3d};
		assertTrue(Data.in(1d, a));
		assertTrue(Data.in(2d, a));
		assertTrue(Data.in(3d, a));
		assertFalse(Data.in(4d, a));
		assertFalse(Data.in(null, a));
	}

	@Disabled
	@Test
	void testInBigDecimalBigDecimalArray() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testInClassOfQClassOfQArray() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testSplitLF() {
		// Unix format test.
		String source = "one\ntwo\nthree";
		String[] output = Data.splitLF(source);
		assertEquals(output.length, 3);
		assertEquals(output[0], ONE);
		assertEquals(output[1], TWO);
		assertEquals(output[2], THREE);
		// Mac format test.
		source = "one\rtwo\rthree";
		output = Data.splitLF(source);
		assertEquals(output.length, 3);
		assertEquals(output[0], ONE);
		assertEquals(output[1], TWO);
		assertEquals(output[2], THREE);
		// Windows format test.
		source = "one\r\ntwo\r\nthree";
		output = Data.splitLF(source);
		assertEquals(output.length, 3);
		assertEquals(output[0], ONE);
		assertEquals(output[1], TWO);
		assertEquals(output[2], THREE);
	}

	@Test
	void testGet() {
		String[] a = {ONE, TWO, THREE};
		assertEquals(Data.get(a, 0), ONE);
		assertEquals(Data.get(a, 1), TWO);
		assertEquals(Data.get(a, 2), THREE);
		assertEquals(Data.get(a, 3), null);
	}

	@Test
	void testIsAlpha() {
		assertTrue(Data.isAlpha('a'));
		assertFalse(Data.isAlpha('1'));
		assertFalse(Data.isAlpha('!'));
	}

	@Test
	void testIsNumeric() {
		assertFalse(Data.isNumeric("a"));
		assertTrue(Data.isNumeric("123"));
		assertTrue(Data.isNumeric("123.456"));
		assertTrue(Data.isNumeric("-123"));
		assertTrue(Data.isNumeric("-123.456"));
		assertFalse(Data.isNumeric("1-23.456"));
		assertFalse(Data.isNumeric("-12.3.456"));
		assertFalse(Data.isNumeric("!"));
	}

	@Test
	void testGetULRegEx() {
		assertEquals(Data.getULRegEx("<blue/>"), "<[b|B][l|L][u|U][e|E]/>");
	}

	@Test
	void testUpcaseFirst() {
		assertEquals(Data.upcaseFirst(HELLO.toLowerCase()), HELLO);
		assertEquals(Data.upcaseFirst("goodBye"), "GoodBye");
	}

	@Test
	void testSizeOf() {
		String[] array = {ONE, TWO, THREE};
		assertTrue(Data.sizeOf(array) == 3);
		List<String> list = Arrays.asList(array);
		assertTrue(Data.sizeOf(list) == 3);
	}

	@Test
	void testItemOf() {
		String[] array = {ONE, TWO, THREE};
		assertEquals(Data.itemOf(array, 2), THREE);
		List<String> list = Arrays.asList(array);
		assertEquals(Data.itemOf(list, 2), THREE);
	}

	@Test
	void testFieldOf() {
		UserProfile bean = new UserProfile();
		bean.setUserid("test");
		try {
			assertEquals(Data.fieldOf(bean, "userid"), "test");
		} catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
			fail("Exception thrown during testFieldOf.");
		}
	}

	@Disabled
	@Test
	void testSqlEscapeStringBoolean() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testSqlEscapeStringStringBoolean() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testHtmlEscape() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testObj2StrObject() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testObj2StrObjectHashMapOfStringString() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testObj2StrObjectHashMapOfStringStringStringStringLocale() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testObj2SQLStrStringObjectBoolean() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testObj2SQLStrStringObject() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testCol2SQLStr() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testStr2SQLLike() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testDateToStrDateString() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testDateToStrDate() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testDtsToStr() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testDateToSQLStr() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testTimeToSQLStr() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testDtsToSQLStr() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testStrToDateStringString() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testStrToDateString() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Disabled
	@Test
	void testStrToDts() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testTruncDateDate() {
		try {
			assertEquals(Data.truncDate(new Date()), Data.expToDts("T", new Date()));
		} catch (ParseException e) {
			fail("Parse exception encountered while truncating date.");
		}
	}

	@Test
	void testTruncDateDateInt() {
		try {
			assertEquals(Data.truncDate(new Date(), Calendar.DATE), Data.expToDts("T", new Date()));
			assertEquals(Data.truncDate(new Date(), Calendar.MONTH), Data.strToDate(Data.dateToStr(new Date(), "yyyy-MM-01")));
			assertEquals(Data.truncDate(new Date(), Calendar.YEAR), Data.strToDate(Data.dateToStr(new Date(), "yyyy-01-01")));
		} catch (ParseException e) {
			fail("Parse exception encountered while truncating date.");
		}
	}

	@Test
	void testExpToDtsStringDate() {
		Date source = Data.strToDate("2010-10-01 00:00:00");
		Date comp1 = Data.strToDate("2018-05-12 12:32:54");
		Date comp2 = Data.strToDate("2018-05-12 00:00:00");
		Date comp3 = Data.strToDate("2018-05-01 00:00:00");
		Date comp4 = Data.strToDate("2018-01-01 00:00:00");
		Date comp5 = Data.strToDate("2018-06-01 00:00:00");
		Date comp6 = Data.strToDate("2018-05-12 12:32:00");
		try {
			assertEquals(Data.expToDts("N+8Y-5M+11D+12h+32m+54s", source), comp1);
			assertEquals(Data.expToDts("T", comp1), comp2);
			assertEquals(Data.expToDts("F", comp1), comp3);
			assertEquals(Data.expToDts("F1", comp1), comp4);
			assertEquals(Data.expToDts("F6", comp1), comp5);
			assertEquals(Data.expToDts("12:32:54", comp1), Date.from(Data.strToLocalDateTime("2000-01-01 12:32:54").atZone(ZoneId.systemDefault()).toInstant()));
			assertEquals(Data.expToDts("2018-05-12", comp1), comp2);
			assertEquals(Data.expToDts("2018-05-12 12:32", comp1), comp6);
			assertEquals(Data.expToDts("2018-05-12 12:32:54", comp1), comp1);
		} catch (ParseException e) {
			fail("ParseException occurred.");
		}
	}

	static final String DATETIME = "datetime";
	
	@Test
	void testExpToDtsStringDateHashMapOfStringString() {
		HashMap<String,String> map = new HashMap<>();
		map.put("date", "MM/dd/yyyy");
		map.put(DATETIME, "MM/dd/yyyy HH:mm:ss");
		
		Date source = Data.strToDate("10/01/2010 00:00:00", map.get(DATETIME));
		Date comp1 = Data.strToDate("05/12/2018 12:32:54", map.get(DATETIME));
		Date comp2 = Data.strToDate("05/12/2018 00:00:00", map.get(DATETIME));
		Date comp3 = Data.strToDate("05/01/2018 00:00:00", map.get(DATETIME));
		Date comp4 = Data.strToDate("01/01/2018 00:00:00", map.get(DATETIME));
		Date comp5 = Data.strToDate("06/01/2018 00:00:00", map.get(DATETIME));
		try {
			assertEquals(Data.expToDts("N+8Y-5M+11D+12h+32m+54s", source, map), comp1);
			assertEquals(Data.expToDts("T", comp1, map), comp2);
			assertEquals(Data.expToDts("F", comp1, map), comp3);
			assertEquals(Data.expToDts("F1", comp1, map), comp4);
			assertEquals(Data.expToDts("F6", comp1, map), comp5);
		} catch (ParseException e) {
			fail("ParseException occurred.");
		}
	}

	static final String N15623 = "15623";
	static final String N15_623 = "15.623";
	
	@Test
	void testIntegerToStr() {
		assertEquals(Data.integerToStr(15623), N15623);
		assertEquals(Data.integerToStr(null), null);
	}

	@Test
	void testStrToInteger() {
		assertTrue(Data.strToInteger(N15623) == 15623);
		assertEquals(Data.strToInteger(null), null);
	}

	@Test
	void testLongToStr() {
		assertEquals(Data.longToStr(15623L), N15623);
		assertEquals(Data.longToStr(null), null);
	}

	@Test
	void testStrToLong() {
		assertTrue(Data.strToLong(N15623) == 15623L);
		assertEquals(Data.strToLong(null), null);
	}

	@Test
	void testFloatToStr() {
		assertEquals(Data.floatToStr(15.623f), N15_623);
		assertEquals(Data.floatToStr(null), null);
	}

	@Test
	void testStrToFloat() {
		assertTrue(Data.strToFloat(N15_623) == 15.623f);
		assertEquals(Data.strToFloat(null), null);
	}

	@Test
	void testDoubleToStr() {
		assertEquals(Data.doubleToStr(15.623d), N15_623);
		assertEquals(Data.doubleToStr(null), null);
	}

	@Test
	void testStrToDouble() {
		assertTrue(Data.strToDouble(N15_623) == 15.623d);
		assertEquals(Data.strToDouble(null), null);
	}

	@Test
	void testBigDecimalToStr() {
		assertEquals(Data.bigDecimalToStr(BigDecimal.valueOf(15.623)), N15_623);
		assertEquals(Data.bigDecimalToStr(null), null);
	}

	@Test
	void testStrToBigDecimal() {
		assertEquals(Data.strToBigDecimal(N15_623), BigDecimal.valueOf(15.623));
		assertEquals(Data.strToBigDecimal(null), null);
	}

	@Disabled
	@Test
	void testObjToStrNvl() {
		fail(NOT_YET_IMPLEMENTED);
	}

	@Test
	void testJoinStringArrayString() {
		String[] source = {ONE, TWO, THREE};
		assertEquals(Data.join(source, ","), "one,two,three");
	}

	@Test
	void testJoinStringArrayStringBoolean() {
		String[] source = {ONE, TWO, "th_ree"};
		assertEquals("one,two,th_ree", Data.join(source, ",", false));
		assertEquals("{one},{two},{thRee}", Data.join(source, ",", true));
	}

	@Test
	void testExJoin() {
		String[] source = {"one", "two", "th_ree"};
		assertEquals("one = {one},two = {two},th_ree = {thRee}", Data.exJoin(source, ","));
	}

	@Test
	void testMinus() {
		String[] a = {ONE, TWO, THREE, "four", "five"};
		String[] b = {TWO, "four", "five"};
		String[] c = {ONE, THREE};
		assertTrue(Arrays.equals(c, Data.minus(a,  b)));
	}

	@Test
	void testHexString() {
		byte a = 123;
		assertEquals("7b", Data.hexString(a));
	}

	@Test
	void testToHexString() {
		assertEquals("3d6b", Data.toHexString(15723, 4));
		assertEquals("003d6b", Data.toHexString(15723, 6));
	}

	@Disabled
	@Test
	void testRandChar() {
		fail(NOT_YET_IMPLEMENTED);
	}

	static final String SHUFFLE_A = "abcdef";
	static final String SHUFFLE_B = "cdbeaf";
	static final String SHIFT_C = "bcdefg";
	
	@Test
	void testShuffle() {
		assertEquals(Data.shuffle(SHUFFLE_A, 1), SHUFFLE_B);
	}

	@Test
	void testUnshuffle() {
		assertEquals(Data.unshuffle(SHUFFLE_B, 1), SHUFFLE_A);
	}

	@Test
	void testShift() {
		assertEquals(Data.shift(SHUFFLE_A, 1), SHIFT_C);
		assertEquals(Data.shift(SHIFT_C, -1), SHUFFLE_A);
	}

	@Test
	void testEncryptDecrypt() {
		String a = HELLO.toLowerCase();
		String b = Data.encrypt(a, 521638);
		String c = Data.encrypt(a, 521638);
		String d = Data.encrypt(a, 47983);
		assertFalse(b.equals(c));
		assertFalse(b.equals(d));
		assertEquals(Data.decrypt(b, 521638), a);
		assertEquals(Data.decrypt(c, 521638), a);
		assertEquals(Data.decrypt(d, 47983), a);
	}
	
	@Test
	void testGetTextFile() {
		String file = Data.readTextFile(this.getClass(), "hello.txt");
		assertEquals(file, "Hola");
	}

	@Test
	void testSerializeDeserialize() {
		try {
			UserProperty a = new UserProperty();
			a.setUserid("test");
			String b = Data.serialize(a);
			assertFalse(b == null);
			a = Data.deserialize(b);
			assertEquals(a.getUserid(), "test");
		} catch (IOException | ClassNotFoundException e) {
			fail("Serialization or deserialization failed.");
		}
	}

	@Test
	void testParseDate() {
		HashMap<String,String> formats = new HashMap<>();
		formats.put("date", Data.ISO_DATE);
		formats.put(DATETIME, Data.ISO_DATETIME);
		formats.put("time", Data.ISO_TIME);
		try {
			assertEquals(Data.parseDate("1985-07-04 00:16:54", formats), Data.strToDts("1985-07-04 00:16:54"));
		} catch (ParseException e) {
			fail("Parse exception while testing parseDate.");
		}
	}

	@Test
	void testGetParamValue() {
		try {
			HashMap<String,String> formats = new HashMap<>();
			formats.put("date", Data.ISO_DATE);
			formats.put(DATETIME, Data.ISO_DATETIME);
			formats.put("time", Data.ISO_TIME);
			assertEquals(Data.getParamValue(null, HELLO.toLowerCase(), formats), null);
			assertEquals(Data.getParamValue(String.class, null, formats), null);
			assertEquals(Data.getParamValue(boolean.class, "true", formats), true);
			assertEquals(Data.getParamValue(byte.class, "26", formats), Byte.parseByte("26"));
			assertEquals(Data.getParamValue(double.class, "2.56", formats), 2.56d);
			assertEquals(Data.getParamValue(float.class, "2.56", formats), 2.56f);
			assertEquals(Data.getParamValue(int.class, "254", formats), 254);
			assertEquals(Data.getParamValue(long.class, "254", formats), 254L);
			assertEquals(Data.getParamValue(short.class, "125", formats), Short.parseShort("125"));
			assertEquals(Data.getParamValue(Boolean.class, "true", formats), true);
			assertEquals(Data.getParamValue(Byte.class, "26", formats), Byte.parseByte("26"));
			assertEquals(Data.getParamValue(Double.class, "2.56", formats), 2.56d);
			assertEquals(Data.getParamValue(Float.class, "2.56", formats), 2.56f);
			assertEquals(Data.getParamValue(Integer.class, "254", formats), 254);
			assertEquals(Data.getParamValue(Long.class, "254", formats), 254L);
			assertEquals(Data.getParamValue(Short.class, "125", formats), Short.parseShort("125"));
			assertEquals(Data.getParamValue(BigDecimal.class, "2.56", formats), new BigDecimal("2.56"));
			String tSample = "08:17:43";
			String dSample = "2019-07-24";
			String dtsSample = "2019-07-24 08:17:43";
			assertEquals(Data.getParamValue(Date.class, dtsSample, formats), Data.strToDts(dtsSample));
			assertEquals(Data.getParamValue(java.sql.Time.class, tSample, formats), new java.sql.Time(Data.strToDate(tSample, formats.get("time")).getTime()));
			assertEquals(Data.getParamValue(java.sql.Timestamp.class, dtsSample, formats), new java.sql.Timestamp(Data.strToDts(dtsSample).getTime()));
			assertEquals(Data.getParamValue(java.sql.Date.class, dSample, formats), new java.sql.Date(Data.strToDate(dSample, formats.get("date")).getTime()));
			assertEquals(Data.getParamValue(String.class, HELLO.toLowerCase(), formats), HELLO.toLowerCase());
			assertEquals(Data.getParamValue(LocalDate.class, dSample, formats), Data.strToLocalDate(dSample, formats.get("date")));
			assertEquals(Data.getParamValue(LocalTime.class, tSample, formats), Data.strToLocalTime(tSample, formats.get("time")));
			assertEquals(Data.getParamValue(LocalDateTime.class, dtsSample, formats), Data.strToLocalDateTime(dtsSample));
		} catch (ParseException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			fail("getParamValue failed with parse exception.");
		}
	}

	@Test
	void testParseEncodedParam() {
		String params = "one=two&three=four&five=six";
		assertEquals(Data.parseEncodedParam(params, ONE), TWO);
		assertEquals(Data.parseEncodedParam(params, THREE), "four");
		assertEquals(Data.parseEncodedParam(params, "five"), "six");
	}

	@Test
	void testDigest() {
		assertEquals(Data.digest(HELLO.toLowerCase(), "SHA-1"), "aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d");
		assertEquals(Data.digest(HELLO.toLowerCase(), "SHA-256"), "2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824");
		assertEquals(Data.digest(HELLO.toLowerCase(), "SHA-512"), "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca72323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043");
	}

	@Test
	void testSlowDigest() {
		assertEquals(Data.slowDigest(HELLO.toLowerCase(), "SHA-1", 1000), "fed178f198b1a8228c84b64449c98d881e68214f");
		assertEquals(Data.slowDigest(HELLO.toLowerCase(), "SHA-256", 1000), "9c9dae0965814a30aa242290e32e74e0e6f0058b8a8498d35535ef65b0f45cb5");
		assertEquals(Data.slowDigest(HELLO.toLowerCase(), "SHA-512", 1000), "d6f79394cb49bcd0cdbaa5ba695a0c3ed78b3fb33679de4365e3111a782a4916d7c32c7b7f2409141fc447e6e93cddd87311182ab0a1e416db23881aa0964501");
	}
	
	// The method getWebEnv is not testable outside of a running Tomcat container.

	@Test
	void testSanitize() {
		try {
			assertEquals(Data.sanitize(null, false), null);
			// The following test is impossible outside of a running web context.
			// String result = Data.sanitize("https://test.sample.com/ContextRoot/ServletName'extra", true);
			// assertEquals(result, "https://test.sample.com/ContextRoot/ServletName", result);
		}
		catch (MalformedURLException | FileNotFoundException e) {
			fail("sanitize failed with parse exception.");
		}
	}
	
	@Test
	void testStripHtml() {
		assertEquals(Data.stripHtml("<b>This</b> is <i>a</i> <span style=\"color: blue;\">sentence</span>."), "This is a sentence.");
	}
}

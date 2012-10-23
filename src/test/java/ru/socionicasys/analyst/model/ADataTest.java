package ru.socionicasys.analyst.model;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class ADataTest {
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testParseNull() throws Exception {
		AData.parseAData(null);
	}

	@Test
	public void testParseAspect() {
		AData data = AData.parseAData("ЧЛ;");
		assertEquals(data, new AData(AData.P, null, null, null, null, null, null));
	}

	@Test
	public void testParseWhitespace() {
		AData data = AData.parseAData(" ЧЛ;");
		assertEquals(data, new AData(AData.P, null, null, null, null, null, null));
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testParseInvalidAspect() throws Exception {
		AData.parseAData("ЖЭ;");
	}

	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testParseInvalidAddition() throws Exception {
		AData.parseAData("БЭ;Многодумность;");
	}

	@Test
	public void testParseDoubt() throws Exception {
		AData data = AData.parseAData("Фрагмент требует уточнения;");
		assertEquals(data, new AData(AData.DOUBT, null, null, null, null, null, null));
	}

	@Test
	public void testParseBlock() throws Exception {
		AData data = AData.parseAData("ЧЛ~БС;");
		assertEquals(data, new AData(AData.P, AData.S, null, null, null, AData.BLOCK, null));
	}

	@Test
	public void testParseBlockAndDim() throws Exception {
		AData data = AData.parseAData("ЧЛ~БИ;Маломерность;");
		assertEquals(data, new AData(AData.P, AData.T, null, AData.MALOMERNOST, null, AData.BLOCK, null));
	}

	@Test
	public void testParseBlockAndMV() throws Exception {
		AData data = AData.parseAData("БЭ~ЧС;Ментал");
		assertEquals(data, new AData(AData.R, AData.F, null, null, AData.MENTAL, AData.BLOCK, null));
	}

	@Test
	public void testParseJump() throws Exception {
		AData data = AData.parseAData("ЧЛ>БЭ;");
		assertEquals(data, new AData(AData.P, AData.R, null, null, null, AData.JUMP, null));
	}

	@Test
	public void testParseJumpAndDim() throws Exception {
		AData data = AData.parseAData("ЧС>БЭ;Одномерность;");
		assertEquals(data, new AData(AData.F, AData.R, null, AData.ODNOMERNOST, null, AData.JUMP, null));
	}

	@Test
	public void testParseDoubtAndDim() throws Exception {
		AData data = AData.parseAData("Фрагмент требует уточнения;Размерность НОРМА;");
		assertEquals(data, new AData(AData.DOUBT, null, null, AData.D2, null, null, null));
	}

	@Test
	public void testParseSign() throws Exception {
		AData data = AData.parseAData("БС;МИНУС;");
		assertEquals(data, new AData(AData.S, null, AData.MINUS, null, null, null, null));
	}

	@Test
	public void testParseDim() throws Exception {
		AData data = AData.parseAData("БЭ;Размерность СИТУАЦИЯ;");
		assertEquals(data, new AData(AData.R, null, null, AData.D3, null, null, null));
	}

	@Test
	public void testParseSignAndDim() throws Exception {
		AData data = AData.parseAData("ЧИ;ПЛЮС;Размерность ВРЕМЯ;");
		assertEquals(data, new AData(AData.I, null, AData.PLUS, AData.D4, null, null, null));
	}

	@Test
	public void testParseDimAndMV() throws Exception {
		AData data = AData.parseAData("ЧЭ;Размерность СИТУАЦИЯ;Витал");
		assertEquals(data, new AData(AData.E, null, null, AData.D3, AData.VITAL, null, null));
	}

	@Test
	public void testParseMV() throws Exception {
		AData data = AData.parseAData("БЭ;Ментал");
		assertEquals(data, new AData(AData.R, null, null, null, AData.MENTAL, null, null));
	}

	@Test
	public void testParseAspectFull() throws Exception {
		AData data = AData.parseAData("БЛ;ПЛЮС;Индивидуальность;Ментал");
		assertEquals(data, new AData(AData.L, null, AData.PLUS, AData.INDIVIDUALNOST, AData.MENTAL, null, null));
	}

	@Test
	public void testParseBlockFull() throws Exception {
		AData data = AData.parseAData("БС~ЧЭ;МИНУС;Многомерность;Витал");
		assertEquals(data, new AData(AData.S, AData.E, AData.MINUS, AData.MNOGOMERNOST, AData.VITAL, AData.BLOCK, null));
	}

	@Test
	public void testParseJumpFull() {
		AData data = AData.parseAData("ЧЭ>БС;ПЛЮС;Одномерность;Ментал");
		assertEquals(data, new AData(AData.E, AData.S, AData.PLUS, AData.ODNOMERNOST, AData.MENTAL, AData.JUMP, null));
	}

	@Test
	public void testInvalidAspect() throws Exception {
		AData data = new AData("ЗЛ", null, null, null, null, null, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testInvalidSecondAspect() throws Exception {
		AData data = new AData(AData.L, "РС", null, null, null, null, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testInvalidSign() throws Exception {
		AData data = new AData(AData.L, AData.F, "*", null, null, null, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testInvalidDimension() throws Exception {
		AData data = new AData(AData.L, AData.F, null, "Разномерность", null, null, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testInvalidMV() throws Exception {
		AData data = new AData(AData.L, AData.F, null, null, "Супер-Эгоизм", null, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testInvalidModifier() throws Exception {
		AData data = new AData(AData.L, AData.F, null, null, null, "КУСОК", null);
		assertFalse(data.isValid());
	}

	@Test
	public void testIncompleteJumpIsInvalid() throws Exception {
		AData data = new AData(AData.L, null, null, null, null, AData.JUMP, null);
		assertFalse(data.isValid());
	}

	@Test
	public void testIncompleteDataIsClearlyIdentified() throws Exception {
		AData data = new AData(AData.L, null, null, null, null, AData.JUMP, null);
		assertEquals(data.toString(), "(неполная отметка)");
	}

	@Test
	public void testCreation() throws Exception {
		String comment = "Комментарий";
		AData data = new AData(AData.P, AData.S, AData.PLUS, AData.D3, AData.MENTAL, AData.BLOCK, comment);
		assertEquals(data.getAspect(), AData.P);
		assertEquals(data.getSecondAspect(), AData.S);
		assertEquals(data.getSign(), AData.PLUS);
		assertEquals(data.getDimension(), AData.D3);
		assertEquals(data.getMV(), AData.MENTAL);
		assertEquals(data.getModifier(), AData.BLOCK);
		assertEquals(data.getComment(), comment);

		String newComment = "Новый комментарий";
		data.setComment(newComment);
		assertEquals(data.getComment(), newComment);
	}

	@Test
	public void testNullComment() throws Exception {
		AData data = new AData(AData.P, null, null, null, null, null, null);
		assertEquals(data.getComment(), "");
		data.setComment(null);
		assertEquals(data.getComment(), "");
	}

	@SuppressWarnings({"EqualsBetweenInconvertibleTypes", "LiteralAsArgToStringEquals"})
	@Test
	public void testEquals() throws Exception {
		AData data = new AData(AData.R, null, null, null, null, null, null);
		assertTrue(data.equals(data));
		assertFalse(data.equals("Строка"));
	}
}

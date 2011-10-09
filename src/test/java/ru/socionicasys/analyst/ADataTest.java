package ru.socionicasys.analyst;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ADataTest {
	@Test
	public void testParseAspect() {
		AData data = AData.parseAData("ЧЛ;");
		Assert.assertEquals(data, new AData(AData.P, null, null, null, null, null, null));
	}

	@Test
	public void testParseInvalidAspect() throws Exception {
		AData data = AData.parseAData("ЖЭ;");
		Assert.assertNull(data);
	}

	@Test
	public void testParseInvalidAddition() throws Exception {
		AData data = AData.parseAData("БЭ;Многодумность;");
		Assert.assertEquals(data, new AData(AData.R, null, null, null, null, null, null));
	}

	@Test
	public void testParseDoubt() throws Exception {
		AData data = AData.parseAData("Фрагмент требует уточнения;");
		Assert.assertEquals(data, new AData(AData.DOUBT, null, null, null, null, null, null));
	}

	@Test
	public void testParseBlock() throws Exception {
		AData data = AData.parseAData("ЧЛ~БС;");
		Assert.assertEquals(data, new AData(AData.P, AData.S, null, null, null, AData.BLOCK, null));
	}

	@Test
	public void testParseBlockAndDim() throws Exception {
		AData data = AData.parseAData("ЧЛ~БИ;Маломерность;");
		Assert.assertEquals(data, new AData(AData.P, AData.T, null, AData.MALOMERNOST, null, AData.BLOCK, null));
	}

	@Test
	public void testParseBlockAndMV() throws Exception {
		AData data = AData.parseAData("БЭ~ЧС;Ментал");
		Assert.assertEquals(data, new AData(AData.R, AData.F, null, null, AData.MENTAL, AData.BLOCK, null));
	}

	@Test
	public void testParseJump() throws Exception {
		AData data = AData.parseAData("ЧЛ>БЭ;");
		Assert.assertEquals(data, new AData(AData.P, AData.R, null, null, null, AData.JUMP, null));
	}

	@Test
	public void testParseJumpAndDim() throws Exception {
		AData data = AData.parseAData("ЧС>БЭ;Одномерность;");
		Assert.assertEquals(data, new AData(AData.F, AData.R, null, AData.ODNOMERNOST, null, AData.JUMP, null));
	}

	@Test
	public void testParseDoubtAndDim() throws Exception {
		AData data = AData.parseAData("Фрагмент требует уточнения;Размерность НОРМА;");
		Assert.assertEquals(data, new AData(AData.DOUBT, null, null, AData.D2, null, null, null));
	}

	@Test
	public void testParseSign() throws Exception {
		AData data = AData.parseAData("БС;МИНУС;");
		Assert.assertEquals(data, new AData(AData.S, null, AData.MINUS, null, null, null, null));
	}

	@Test
	public void testParseDim() throws Exception {
		AData data = AData.parseAData("БЭ;Размерность СИТУАЦИЯ;");
		Assert.assertEquals(data, new AData(AData.R, null, null, AData.D3, null, null, null));
	}

	@Test
	public void testParseSignAndDim() throws Exception {
		AData data = AData.parseAData("ЧИ;ПЛЮС;Размерность ВРЕМЯ;");
		Assert.assertEquals(data, new AData(AData.I, null, AData.PLUS, AData.D4, null, null, null));
	}

	@Test
	public void testParseDimAndMV() throws Exception {
		AData data = AData.parseAData("ЧЭ;Размерность СИТУАЦИЯ;Витал");
		Assert.assertEquals(data, new AData(AData.E, null, null, AData.D3, AData.VITAL, null, null));
	}

	@Test
	public void testParseMV() throws Exception {
		AData data = AData.parseAData("БЭ;Ментал");
		Assert.assertEquals(data, new AData(AData.R, null, null, null, AData.MENTAL, null, null));
	}

	@Test
	public void testParseAspectFull() throws Exception {
		AData data = AData.parseAData("БЛ;ПЛЮС;Индивидуальность;Ментал");
		Assert.assertEquals(data, new AData(AData.L, null, AData.PLUS, AData.INDIVIDUALNOST, AData.MENTAL, null, null));
	}

	@Test
	public void testParseBlockFull() throws Exception {
		AData data = AData.parseAData("БС~ЧЭ;МИНУС;Многомерность;Витал");
		Assert.assertEquals(data, new AData(AData.S, AData.E, AData.MINUS, AData.MNOGOMERNOST, AData.VITAL, AData.BLOCK, null));
	}

	@Test
	public void testParseJumpFull() {
		AData data = AData.parseAData("ЧЭ>БС;ПЛЮС;Одномерность;Ментал");
		Assert.assertEquals(data, new AData(AData.E, AData.S, AData.PLUS, AData.ODNOMERNOST, AData.MENTAL, AData.JUMP, null));
	}
}

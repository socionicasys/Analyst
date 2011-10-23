package ru.socionicasys.analyst.types;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Locale;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertFalse;

public class AspectTest {
	@BeforeMethod
	public void setUp() throws Exception {
		// Класс Aspect locale-зависимый, создаем предсказуемое окружение
		Locale.setDefault(new Locale("ru"));
	}

	@Test
	public void testGetAbbreviation() throws Exception {
		assertEquals(Aspect.P.getAbbreviation(), "ЧЛ");
		assertEquals(Aspect.L.getAbbreviation(), "БЛ");
		assertEquals(Aspect.F.getAbbreviation(), "ЧС");
		assertEquals(Aspect.S.getAbbreviation(), "БС");
		assertEquals(Aspect.E.getAbbreviation(), "ЧЭ");
		assertEquals(Aspect.R.getAbbreviation(), "БЭ");
		assertEquals(Aspect.I.getAbbreviation(), "ЧИ");
		assertEquals(Aspect.T.getAbbreviation(), "БИ");
	}

	@Test
	public void testIsBlockWith() throws Exception {
		assertTrue(Aspect.P.isBlockWith(Aspect.S));
		assertFalse(Aspect.R.isBlockWith(Aspect.T));
	}

	@Test
	public void testByAbbreviation() throws Exception {
		assertEquals(Aspect.byAbbreviation("ЧЛ"), Aspect.P);
		assertEquals(Aspect.byAbbreviation("БЛ"), Aspect.L);
		assertEquals(Aspect.byAbbreviation("ЧС"), Aspect.F);
		assertEquals(Aspect.byAbbreviation("БС"), Aspect.S);
		assertEquals(Aspect.byAbbreviation("ЧЭ"), Aspect.E);
		assertEquals(Aspect.byAbbreviation("БЭ"), Aspect.R);
		assertEquals(Aspect.byAbbreviation("ЧИ"), Aspect.I);
		assertEquals(Aspect.byAbbreviation("БИ"), Aspect.T);
	}
}

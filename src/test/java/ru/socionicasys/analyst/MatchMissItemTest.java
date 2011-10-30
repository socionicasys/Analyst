package ru.socionicasys.analyst;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.types.Sociotype;

import static org.testng.Assert.assertEquals;

public class MatchMissItemTest {
	/**
	 * Tested {@link MatchMissItem}
	 */
	private MatchMissItem item;

	/**
	 * A markup data which matches {@link #item}
	 */
	private AData matchingData;

	/**
	 * A markup data which doesn't match {@link #item}
	 */
	private AData nonMatchingData;

	private static final float DELTA = 0.0001f;

	@BeforeClass
	public void setUpData() throws Exception {
		matchingData = new AData(AData.I, null, null, AData.MALOMERNOST, AData.VITAL, null, null);
		nonMatchingData = new AData(AData.R, AData.F, null, AData.MALOMERNOST, null, AData.BLOCK, null);
	}

	@BeforeMethod
	public void setUpItem() throws Exception {
		item = new MatchMissItem(Sociotype.SLI);
	}

	@Test
	public void testEmptyMatch() throws Exception {
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getMatchCoefficient(), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testReset() throws Exception {
		item.addData(matchingData);
		item.addData(nonMatchingData);
		item.reset();
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getMatchCoefficient(), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testMatch() throws Exception {
		item.addData(matchingData);
		assertEquals(item.getMatchCount(), 1);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getMatchCoefficient(), Float.POSITIVE_INFINITY, DELTA);
	}

	@Test
	public void testMiss() throws Exception {
		item.addData(nonMatchingData);
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 1);
		assertEquals(item.getMatchCoefficient(), 0f, DELTA);
	}

	@Test
	public void testMatchAndMiss() throws Exception {
		item.addData(matchingData);
		item.addData(nonMatchingData);
		assertEquals(item.getMatchCount(), 1);
		assertEquals(item.getMissCount(), 1);
		assertEquals(item.getMatchCoefficient(), 1.0f, DELTA);
	}
}

package ru.socionicasys.analyst;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.socionicasys.analyst.predicates.BlockPredicate;
import ru.socionicasys.analyst.predicates.LowDimensionPredicate;
import ru.socionicasys.analyst.predicates.Predicate;
import ru.socionicasys.analyst.predicates.VitalPredicate;
import ru.socionicasys.analyst.types.Aspect;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.ArrayList;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

public class MatchMissItemTest {
	/**
	 * Tested {@link MatchMissItem}
	 */
	private MatchMissItem item;

	/**
	 * A markup data which matches {@link #item}
	 */
	private Collection<Predicate> matchingPredicates;

	/**
	 * A markup data which doesn't match {@link #item}
	 */
	private Collection<Predicate> nonMatchingPredicates;

	private static final float DELTA = 0.0001f;

	@BeforeClass
	public void setUpData() throws Exception {
		matchingPredicates = new ArrayList<Predicate>();
		matchingPredicates.add(new LowDimensionPredicate(Aspect.I));
		matchingPredicates.add(new VitalPredicate(Aspect.I));

		nonMatchingPredicates = new ArrayList<Predicate>();
		nonMatchingPredicates.add(new LowDimensionPredicate(Aspect.R));
		nonMatchingPredicates.add(new BlockPredicate(Aspect.R, Aspect.F));
	}

	@BeforeMethod
	public void setUpItem() throws Exception {
		item = new MatchMissItem(Sociotype.SLI);
	}

	@Test
	public void testEmptyMatch() throws Exception {
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getRawCoefficient(), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testReset() throws Exception {
		item.addData(matchingPredicates);
		item.addData(nonMatchingPredicates);
		item.reset();
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getRawCoefficient(), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testMatch() throws Exception {
		item.addData(matchingPredicates);
		assertEquals(item.getMatchCount(), 1);
		assertEquals(item.getMissCount(), 0);
		assertEquals(item.getRawCoefficient(), Float.POSITIVE_INFINITY, DELTA);
	}

	@Test
	public void testMiss() throws Exception {
		item.addData(nonMatchingPredicates);
		assertEquals(item.getMatchCount(), 0);
		assertEquals(item.getMissCount(), 1);
		assertEquals(item.getRawCoefficient(), 0f, DELTA);
	}

	@Test
	public void testMatchAndMiss() throws Exception {
		item.addData(matchingPredicates);
		item.addData(nonMatchingPredicates);
		assertEquals(item.getMatchCount(), 1);
		assertEquals(item.getMissCount(), 1);
		assertEquals(item.getRawCoefficient(), 1.0f, DELTA);
	}

	@Test
	public void testInitialScale() throws Exception {
		item.setScale(10.0f);
		assertEquals(item.getScaledCoefficient(), Float.POSITIVE_INFINITY);
	}

	@Test
	public void testScale() throws Exception {
		item.addData(matchingPredicates);
		item.addData(nonMatchingPredicates);
		item.setScale(10.0f);
		assertEquals(item.getScaledCoefficient(), 10.0f, DELTA);
	}

	@Test
	public void testZeroScaleMatch() throws Exception {
		item.addData(matchingPredicates);
		item.setScale(0.0f);
		assertEquals(item.getScaledCoefficient(), 1.0f, DELTA);
	}

	@Test
	public void testZeroScaleMiss() throws Exception {
		item.addData(matchingPredicates);
		item.addData(nonMatchingPredicates);
		item.setScale(0.0f);
		assertEquals(item.getScaledCoefficient(), 0.0f, DELTA);
	}
}

package ru.socionicasys.analyst;

import mockit.Mocked;
import mockit.NonStrictExpectations;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.socionicasys.analyst.model.AData;
import ru.socionicasys.analyst.types.Sociotype;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static ru.socionicasys.analyst.types.Sociotype.*;

@SuppressWarnings({"OverlyBroadThrowsClause", "ResultOfMethodCallIgnored"})
public class MatchMissModelTest {
	/**
	 * A match-miss model being tested
	 */
	private MatchMissModel model;

	@Mocked
	private ADocument document;

	private static final float DELTA = 0.0001f;

	@BeforeMethod
	public void setUp() throws Exception {
		model = new MatchMissModel();

		// В рамках этого теста все экземпляры DocumentSection считаются не равными
		new NonStrictExpectations() {
			DocumentSection documentSection;
			{
				documentSection.equals(any);
				result = false;
			}
		};
	}

	@Test
	public void testInitialState() throws Exception {
		for (Sociotype sociotype : Sociotype.values()) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 0);
			assertEquals(item.getMissCount(), 0);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
	}

	@Test
	public void testEmptyDocument() throws Exception {
		final Map<DocumentSection, AData> dataMap = Collections.emptyMap();
		new NonStrictExpectations() {{
			document.getADataMap();
			result = dataMap;
		}};
		model.aDocumentChanged(document);
		for (Sociotype sociotype : Sociotype.values()) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 0);
			assertEquals(item.getMissCount(), 0);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
	}

	@Test
	public void testSingleAspectMarkup() throws Exception {
		final Map<DocumentSection, AData> dataMap = Collections.singletonMap(
				new DocumentSection(document, 0, 0),
				new AData(AData.P, null, null, null, null, null, null));
		new NonStrictExpectations() {{
			document.getADataMap();
			returns(dataMap);
		}};
		model.aDocumentChanged(document);
		for (Sociotype sociotype : Sociotype.values()) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 1);
			assertEquals(item.getMissCount(), 0);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
	}

	@Test
	public void testSingleMarkup() throws Exception {
		final Map<DocumentSection, AData> dataMap = Collections.singletonMap(
				new DocumentSection(document, 0, 0),
				new AData(AData.P, null, null, AData.MNOGOMERNOST, null, null, null));
		new NonStrictExpectations() {{
			document.getADataMap();
			returns(dataMap);
		}};
		model.aDocumentChanged(document);
		for (Sociotype sociotype : Arrays.asList(LIE, LII, LSE, LSI, ILE, ILI, SLE, SLI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 1);
			assertEquals(item.getMissCount(), 0);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
		for (Sociotype sociotype : Arrays.asList(EIE, EII, ESE, ESI, IEE, IEI, SEE, SEI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 0);
			assertEquals(item.getMissCount(), 1);
			assertEquals(item.getMatchCoefficient(), 0f, DELTA);
		}
	}

	@Test
	public void testFullMatch() throws Exception {
		final Map<DocumentSection, AData> dataMap = new HashMap<DocumentSection, AData>();
		dataMap.put(
				new DocumentSection(document, 0, 0),
				new AData(AData.R, null, null, AData.MALOMERNOST, null, null, null));
		dataMap.put(
				new DocumentSection(document, 0, 0),
				new AData(AData.R, null, AData.MINUS, null, null, null, null));
		new NonStrictExpectations() {{
			document.getADataMap();
			returns(dataMap);
		}};
		model.aDocumentChanged(document);
		for (Sociotype sociotype : Arrays.asList(LII, LSE, ILE, SLI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 2);
			assertEquals(item.getMissCount(), 0);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
		for (Sociotype sociotype : Arrays.asList(LIE, LSI, ILI, SLE, EIE, IEI, SEE, ESI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 1);
			assertEquals(item.getMissCount(), 1);
			assertEquals(item.getMatchCoefficient(), 0f, DELTA);
		}
		for (Sociotype sociotype : Arrays.asList(EII, ESE, IEE, SEI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 0);
			assertEquals(item.getMissCount(), 2);
			assertEquals(item.getMatchCoefficient(), 0f, DELTA);
		}
	}

	@Test
	public void testPartialMatch() throws Exception {
		final Map<DocumentSection, AData> dataMap = new HashMap<DocumentSection, AData>();
		dataMap.put(
				new DocumentSection(document, 0, 0),
				new AData(AData.R, null, null, AData.MNOGOMERNOST, null, null, null));
		dataMap.put(
				new DocumentSection(document, 0, 0),
				new AData(AData.P, null, null, AData.MNOGOMERNOST, null, null, null));
		dataMap.put(
				new DocumentSection(document, 0, 0),
				new AData(AData.I, null, AData.MINUS, null, null, null, null));
		new NonStrictExpectations() {{
			document.getADataMap();
			returns(dataMap);
		}};
		model.aDocumentChanged(document);
		for (Sociotype sociotype : Arrays.asList(LIE, LSI, ILI, SLE, EII, ESE, IEE, SEI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 2);
			assertEquals(item.getMissCount(), 1);
			assertEquals(item.getMatchCoefficient(), 1f, DELTA);
		}
		for (Sociotype sociotype : Arrays.asList(LII, LSE, ILE, SLI, EIE, IEI, SEE, ESI)) {
			MatchMissItem item = model.get(sociotype);
			assertEquals(item.getMatchCount(), 1);
			assertEquals(item.getMissCount(), 2);
			assertEquals(item.getMatchCoefficient(), 0.25f, DELTA);
		}
	}
}

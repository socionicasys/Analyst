package ru.socionicasys.analyst;

import org.testng.annotations.Test;
import ru.socionicasys.analyst.model.AData;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings({"OverlyBroadThrowsClause", "ThrowInsideCatchBlockWhichIgnoresCaughtException"})
public class LegacyHtmlReaderTest {
	@Test(expectedExceptions = FileNotFoundException.class)
	public void testLoadNonexistant() throws Throwable {
		File tempFile = File.createTempFile("nonexistant", ".htm");
		assertTrue(tempFile.delete());
		assertFalse(tempFile.exists());

		throwNestedException(new LegacyHtmlReader(tempFile));
	}

	@Test(expectedExceptions = StringIndexOutOfBoundsException.class)
	public void testLoadEmpty() throws Throwable {
		checkInvalidTestFile("empty.htm");
	}

	@Test(expectedExceptions = StringIndexOutOfBoundsException.class)
	public void testLoadInvalidProperties() throws Throwable {
		checkInvalidTestFile("invalid-properties.htm");
	}

	@Test
	public void testLoadProperties() throws Exception {
		ADocument document = loadTestFile("valid.htm");
		Dictionary<Object,Object> properties = document.getDocumentProperties();

		assertEquals(properties.get(ADocument.TitleProperty), "Тестовый документ");
		assertEquals(properties.get(ADocument.EXPERT_PROPERTY), "Первый Эксперт; Второй Эксперт");
		assertEquals(properties.get(ADocument.CLIENT_PROPERTY), "Кто-то");
		assertEquals(properties.get(ADocument.DATE_PROPERTY), "23.10.2011");
		assertEquals(properties.get(ADocument.COMMENT_PROPERTY), "Документ для тестирования Аналитика");
	}

	@Test
	public void testLoadMarkup() throws Exception {
		ADocument document = loadTestFile("valid.htm");
		Map<DocumentSection, AData> dataMap = document.getADataMap();

		assertEquals(dataMap.size(), 17);

		assertEquals(
				dataMap.get(new DocumentSection(document, 11, 21)),
				new AData(AData.P, null, null, null, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 27, 38)),
				new AData(AData.L, AData.F, null, null, null, AData.BLOCK, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 39, 46)),
				new AData(AData.P, null, null, null, null, null, "Снова ЧЛ?"));
		assertEquals(
				dataMap.get(new DocumentSection(document, 58, 73)),
				new AData(AData.F, AData.S, null, null, null, AData.JUMP, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 86, 115)),
				new AData(AData.S, null, AData.MINUS, null, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 166, 176)),
				new AData(AData.I, null, null, AData.MALOMERNOST, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 239, 242)),
				new AData(AData.L, null, AData.PLUS, null, AData.VITAL, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 273, 295)),
				new AData(AData.DOUBT, null, null, null, null, null, "Непонятная фундаментальная ошибка"));
		assertEquals(
				dataMap.get(new DocumentSection(document, 467, 515)),
				new AData(AData.R, null, AData.MINUS, AData.D1, AData.SUPERID, null, "еще комментарий"));
		assertEquals(
				dataMap.get(new DocumentSection(document, 658, 673)),
				new AData(AData.F, null, null, AData.MNOGOMERNOST, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 864, 875)),
				new AData(AData.E, null, null, AData.INDIVIDUALNOST, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 965, 973)),
				new AData(AData.R, null, null, AData.D2, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 1081, 1118)),
				new AData(AData.S, null, null, AData.D4, null, null, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 1256, 1283)),
				new AData(AData.F, AData.L, AData.PLUS, AData.MNOGOMERNOST, AData.VITAL, AData.BLOCK, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 1371, 1389)),
				new AData(AData.R, AData.P, null, null, null, AData.JUMP, ""));
		assertEquals(
				dataMap.get(new DocumentSection(document, 1510, 1515)),
				new AData(AData.I, AData.T, null, null, null, AData.JUMP, ""));
	}

	/**
	 * Загружает документ из ресурсов приложения.
	 *
	 * @param fileName имя файла для загрузки
	 * @return загруженный документ
	 * @throws URISyntaxException
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	@SuppressWarnings("JavaDoc")
	private ADocument loadTestFile(String fileName) throws URISyntaxException, InterruptedException, ExecutionException {
		URL fileUrl = getClass().getResource(String.format("LegacyHtmlReader/%s", fileName));
		File file = new File(fileUrl.toURI());
		LegacyHtmlReader reader = new LegacyHtmlReader(file);
		reader.execute();
		return reader.get();
	}

	/**
	 * Загружает тестовый файл с некорректной разметкой и выбрасывает исключение,
	 * возникшее во время загрузки.
	 *
	 * @param fileName имя тестового файла (без имени пакета)
	 * @throws Throwable исключение, возникшее при загрузке документа
	 */
	private void checkInvalidTestFile(String fileName) throws Throwable {
		URL invalidFileUrl = getClass().getResource(String.format("LegacyHtmlReader/%s", fileName));
		File invalidFile = new File(invalidFileUrl.toURI());
		throwNestedException(new LegacyHtmlReader(invalidFile));
	}

	/**
	 * Ожидает загрузки документа и выбрасывает исключение, полученное при загрузке.
	 *
	 * @param reader загрузчик документа
	 * @throws Throwable исключение, возникшее при загрузке документа
	 */
	private static void throwNestedException(LegacyHtmlReader reader) throws Throwable {
		reader.execute();
		try {
			reader.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}
}

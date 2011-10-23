package ru.socionicasys.analyst;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

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

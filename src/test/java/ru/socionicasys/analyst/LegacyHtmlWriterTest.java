package ru.socionicasys.analyst;

import mockit.Cascading;
import mockit.NonStrictExpectations;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import ru.socionicasys.analyst.service.VersionInfo;

import java.io.*;
import java.util.Locale;

import static org.testng.Assert.assertEquals;

@SuppressWarnings("OverlyBroadThrowsClause")
public class LegacyHtmlWriterTest {
	@Cascading
	private AnalystWindow analystWindow;

	@BeforeClass
	public void setUp() throws Exception {
		Locale.setDefault(new Locale("ru"));
	}

	@BeforeMethod
	public void commonExpectations() throws Exception {
		new NonStrictExpectations() {
			@SuppressWarnings("UnusedDeclaration")
			VersionInfo versionInfo;
			{
				VersionInfo.getApplicationName(); result = "Информационный анализ";
				VersionInfo.getVersion(); result = "2.0-unittest";

				analystWindow.getNavigeTree().getReport(); result = "";
			}
		};
	}

	@Test
	public void testEmptyDocument() throws Exception {
		File tempFile = File.createTempFile("empty", ".htm");
		tempFile.deleteOnExit();

		LegacyHtmlWriter writer = new LegacyHtmlWriter(analystWindow, new ADocument(), tempFile);
		writer.execute();
		writer.get();
		
		checkFilesEqual(tempFile, "empty.htm");
	}

	/**
	 * Проверяет, что содержимое файла соответствует внутреннему ресурсу с заданным именем.
	 *
	 * @param testedFile проверяемый файл
	 * @param sourceFileName имя внутреннего ресурса-образца
	 * @throws IOException ошибка при открытии или чтении файла
	 */
	private void checkFilesEqual(File testedFile, String sourceFileName) throws IOException {
		InputStream sourceStream = getClass().getResourceAsStream(String.format("LegacyHtmlWriter/%s", sourceFileName));
		FileInputStream testedStream = new FileInputStream(testedFile);
		assertEquals(readStream(testedStream), readStream(sourceStream));
		sourceStream.close();
		testedStream.close();
	}

	/**
	 * Считывает поток в байтовый массив.
	 *
	 * @param stream поток для преобразования
	 * @return байтовый массив с содержимым потока
	 * @throws IOException ошибка чтения из потока
	 */
	private static byte[] readStream(InputStream stream) throws IOException {
		ByteArrayOutputStream bufferStream = new ByteArrayOutputStream();
		final int bufferSize = 16 * 1024;
		byte[] buffer = new byte[bufferSize];
		int readBytes;

		while ((readBytes = stream.read(buffer, 0, bufferSize)) != -1) {
			bufferStream.write(buffer, 0, readBytes);
		}

		bufferStream.flush();
		bufferStream.close();
		return bufferStream.toByteArray();
	}
}

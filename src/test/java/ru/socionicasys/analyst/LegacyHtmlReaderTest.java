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
		
		LegacyHtmlReader reader = new LegacyHtmlReader(tempFile);
		reader.execute();
		try {
			reader.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}

	@Test(expectedExceptions = StringIndexOutOfBoundsException.class)
	public void testLoadEmpty() throws Throwable {
		URL invalidFileUrl = getClass().getResource("LegacyHtmlReader/empty.htm");
		File invalidFile = new File(invalidFileUrl.toURI());

		LegacyHtmlReader reader = new LegacyHtmlReader(invalidFile);
		reader.execute();
		try {
			reader.get();
		} catch (ExecutionException e) {
			throw e.getCause();
		}
	}
}

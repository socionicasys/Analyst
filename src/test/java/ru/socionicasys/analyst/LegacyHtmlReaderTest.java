package ru.socionicasys.analyst;

import org.testng.annotations.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.ExecutionException;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

@SuppressWarnings({"OverlyBroadThrowsClause", "ThrowInsideCatchBlockWhichIgnoresCaughtException"})
public class LegacyHtmlReaderTest {
	@Test(expectedExceptions = FileNotFoundException.class)
	public void testLoadInvalid() throws Throwable {
		File tempFile = File.createTempFile("invalid", ".txt");
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
}

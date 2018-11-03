package com.kncept.junit.reporter.html;

import static com.kncept.junit.reporter.TestReportProcessor.xmlTestFile;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class TestHTMLReportProcessorTest {

	@Test
	public void xmlFileTesterIsValid() throws IOException {
		File validTestFile = File.createTempFile("TEST-", ".xml");
		File inalidTestFile = File.createTempFile("non-", ".xml");
		assertTrue(xmlTestFile(validTestFile));
		assertFalse(xmlTestFile(inalidTestFile));
		validTestFile.delete();
		validTestFile.mkdir();
		// now its INVALID because its a directory (not really defined...)
		assertFalse(xmlTestFile(inalidTestFile));

	}
}

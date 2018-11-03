package com.kncept.junit.reporter.xml;

import com.kncept.junit.reporter.domain.TestCase;

import java.util.LinkedHashMap;
import java.util.List;

public interface TestSuite {

	public String name();

	public LinkedHashMap<String, String> systemProperties();

	public LinkedHashMap<String, String> testsuiteProperties();

	public List<TestCase> testcases();

	public List<String> sysOut();

	public List<String> sysErr();

}

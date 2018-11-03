package com.kncept.junit.reporter.xml;

import static com.kncept.junit.reporter.domain.TestCaseStatus.Errored;
import static com.kncept.junit.reporter.domain.TestCaseStatus.Failed;
import static com.kncept.junit.reporter.domain.TestCaseStatus.Passed;
import static com.kncept.junit.reporter.domain.TestCaseStatus.Skipped;

import com.kncept.junit.reporter.domain.TestCase;
import com.kncept.junit.reporter.domain.TestCaseStatus;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class Junit4DomReader implements TestSuite {

	private final String testResultSetName;
	private final LinkedHashMap<String, String> systemProperties = new LinkedHashMap<>();
	private final LinkedHashMap<String, String> testsuiteProperties = new LinkedHashMap<>();
	private final List<TestCase> testcases = new ArrayList<>();
	private final List<String> sysOut = new ArrayList<>();
	private final List<String> sysErr = new ArrayList<>();

	public Junit4DomReader(String testResultSetName, InputStream in)
			throws ParserConfigurationException, SAXException, IOException {
		this.testResultSetName = testResultSetName;
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(in);

		NamedNodeMap testsuiteAttrs = doc.getDocumentElement().getAttributes();
		if (testsuiteAttrs != null)
			for (int i = 0; i < testsuiteAttrs.getLength(); i++) {
				Node attr = testsuiteAttrs.item(i);
				testsuiteProperties.put(attr.getNodeName(), attr.getNodeValue());
			}

		NodeList nl = doc.getElementsByTagName("property");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);
			systemProperties.put(attr(node, "name"), attr(node, "value"));
		}

		sysOut().addAll(handleTextNode(child(doc.getDocumentElement(), "system-out")));
		sysErr().addAll(handleTextNode(child(doc.getDocumentElement(), "system-err")));

		nl = doc.getElementsByTagName("testcase");
		for (int i = 0; i < nl.getLength(); i++) {
			Node node = nl.item(i);

			TestCaseStatus status = Passed; // default output is success.

			String unsuccessfulMessage = null;
			String stackTrace = null;

			Node statusNode = child(node, "skipped");
//			<skipped><![CDATA[public void com.kncept.junit.reporter.J5T2Test.skippedTest() is @Disabled]]></skipped>
			if (statusNode != null) {
				status = Skipped;
				unsuccessfulMessage = attr(statusNode, "message");
				stackTrace = statusNode.getTextContent();
			}

			statusNode = child(node, "failure");
//			<failure message="Failure Message passed into Assertions.fail" type="org.opentest4j.AssertionFailedError"><![CDATA[org.opentest4j.AssertionFailedError: Failure Message passed into Assertions.fail
			if (statusNode != null) {
				status = Failed;
				unsuccessfulMessage = attr(statusNode, "message");
				stackTrace = statusNode.getTextContent();
			}

			statusNode = child(node, "error");
//			N.B. "message" is optional
//			<error message="RuntimeException message" type="java.lang.RuntimeException"><![CDATA[java.lang.RuntimeException: RuntimeException message
			if (statusNode != null) {
				status = Errored;

				String message = attr(statusNode, "message");
				String type = attr(statusNode, "type");
				if (message != null && type != null)
					unsuccessfulMessage = type + ": " + message;
				else if (type != null)
					unsuccessfulMessage = type;
				stackTrace = statusNode.getTextContent();
			}

			TestCase testCase = new TestCase(attr(node, "name"), attr(node, "classname"),
					new BigDecimal(attr(node, "time")), status);

			testCase.setUnsuccessfulMessage(unsuccessfulMessage);
			testCase.setStackTrace(stackTrace);
			testCase.getSystemOut().addAll(handleTextNode(child(node, "system-out")));
			testCase.getSystemErr().addAll(handleTextNode(child(node, "system-err")));
			testcases.add(testCase);
		}

	}

	private String attr(Node node, String name) {
		Node item = node.getAttributes().getNamedItem(name);
		return item == null ? null : item.getTextContent();
	}

	private Node child(Node parent, String name) {
		NodeList nl = parent.getChildNodes();
		for (int i = 0; i < nl.getLength(); i++) {
			Node child = nl.item(i);
			if (name.equals(child.getNodeName()))
				return child;
		}
		return null;
	}

	private List<String> handleTextNode(Node node) throws IOException {
		List<String> lines = new ArrayList<>();
		if (node != null) {
			NodeList nl = node.getChildNodes();
			for (int i = 0; i < nl.getLength(); i++) {
				Node child = nl.item(i);

				// switch to BufferedReader rather than string split to handle unix/windows
				// cross platform rendering
				BufferedReader bIn = new BufferedReader(new StringReader(child.getTextContent()));
				String line = bIn.readLine();
				while (line != null) {
					lines.add(line);
					line = bIn.readLine();
				}

			}
		}
		return lines;
	}

	@Override
	public String name() {
		return testResultSetName;
	}

	@Override
	public LinkedHashMap<String, String> systemProperties() {
		return systemProperties;
	}

	@Override
	public LinkedHashMap<String, String> testsuiteProperties() {
		return testsuiteProperties;
	}

	@Override
	public List<TestCase> testcases() {
		return testcases;
	}

	@Override
	public List<String> sysOut() {
		return sysOut;
	}

	@Override
	public List<String> sysErr() {
		return sysErr;
	}

}

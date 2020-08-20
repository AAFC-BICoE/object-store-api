package ca.gc.aafc.objectstore.api;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ca.gc.aafc.objectstore.api.entities.License;

public class SupportedLicensesConfigurationTest {
	
	private SupportedLicensesConfiguration config;
	private LinkedHashMap<String, LinkedList<String>> testToLicenses;
	
	@Before
	public void setup() {
		testToLicenses = new LinkedHashMap<>();
		LinkedList<String> testTitles = new LinkedList<>();
		testTitles.add("en: test");
		testTitles.add("fr: test");
		LinkedList<String> testURL = new LinkedList<>();
		testURL.add("test.url.com");
		testToLicenses.put("license-1.url", testURL);
		testToLicenses.put("license-1.titles", testTitles);
		testToLicenses.put("license-2.url", testURL);
		testToLicenses.put("license-2.titles", testTitles);
		testToLicenses.put("license-3.url", testURL);
		testToLicenses.put("license-3.titles", testTitles);
		
		config = new SupportedLicensesConfiguration();
	}
	
	@Test
	public void parseYamlInput_OnValidInput_NoAssertionErrors() {
		config.setLicenses(testToLicenses);
		List<License> licenses = config.getLicenses();
		assertEquals(3, licenses.size());
		for(int i = 0; i < 3; i++) {
			License license = licenses.get(i);
			assertEquals("license-" + Integer.toString(i + 1), license.getName());
			assertEquals("test.url.com", license.getUrl());
			assertEquals(2, license.getTitles().size());
		}
	}
}
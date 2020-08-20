package ca.gc.aafc.objectstore.api.entities;

import java.util.ArrayList;
import java.util.List;

public class License {
	
	private String name;
	private String url;
	private List<String> titles =  new ArrayList<>();
	
	/* index constants for titles */
	public static int LANG = 0;
	public static int NAME = 1;
	
	public License() {}
	
	public License(String name, String url, List<String> titles) {
		this.setName(name);
		this.setUrl(url);
		this.setTitles(titles);		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<String> getTitles() {
		return titles;
	}

	public void setTitles(List<String> titles) {
		this.titles = titles;
	}
	
	public void addTitle(String title) {
		titles.add(title);
	}
}
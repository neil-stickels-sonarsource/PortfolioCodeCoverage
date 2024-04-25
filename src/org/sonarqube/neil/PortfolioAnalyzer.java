package org.sonarqube.neil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
/**
 * This class is where the connections to SonarQube are made, and where the calculations take place.
 * The primary method exposed here is analyzePortfolio which will connect to the specified portfolio
 * to find all of the projects within that portfolio.  Then it will get the metrics from each project
 * in that portfolio specific to the branch specified for code coverage on both overall and new code.
 */
public class PortfolioAnalyzer {
	
	private final String token;
	private final String hostURL;
	private final String portfolioName;
	private final String branchName;
	
	private int totalLinesOfCode = 0;
	private int newLinesOfCode = 0;
	private int totalLinesCovered = 0;
	private int newLinesCovered = 0;
	
	public PortfolioAnalyzer(String token, String url, String portfolioName, String branchName)
	{
		this.token = token;
		if(url.endsWith("/"))
			url = url.substring(0, url.length()-1);
		this.hostURL = url;
		this.portfolioName = portfolioName;
		this.branchName = branchName;
	}
	
	/**
	 * Primary method used to do the analysis.  This calls two internal methods:
	 * 		1. getProjectsForPortfolio - used to get all of the projects within the portfolio specified
	 * 		2. getCoverageForProjects - used to get the coverage metrics for the projects found in 1
	 */
	public void analyzePortfolio()
	{
		List<String> projects = getProjectsForPortfolio();
		getCoverageForProjects(projects);
	}

	private void getCoverageForProjects(List<String> projects) {
		try {
			for(String project: projects)
			{
				String thisBranchName = branchName;
				//if(project.equals("demo:mono-maven"))
				//	thisBranchName = "master";
				URL url = new URL(hostURL+"/api/measures/component?component="+project+
						"&branch="+thisBranchName+"&metricKeys=coverage,lines_to_cover,new_coverage,new_lines_to_cover");
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestProperty("Authorization", "Bearer "+token);
				conn.setRequestMethod("GET");
				if(conn.getResponseCode() != 200)
				{
					System.out.println(url+" returned "+conn.getResponseCode());
					throw new RuntimeException("getCoverageForProjects failed! HTTP error code "+conn.getResponseCode());
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				StringBuilder response = new StringBuilder();
				String line;
				while((line = br.readLine()) != null)
					response.append(line);
				conn.disconnect();
				JSONObject json = new JSONObject(response.toString());
				JSONObject component = json.getJSONObject("component");
				JSONArray measures = component.getJSONArray("measures");
				ProjectData data = new ProjectData();
				for(int i = 0; i < measures.length(); i++)
				{
					JSONObject measure = measures.getJSONObject(i);
					String metric = measure.getString("metric");
					if(metric.equals("lines_to_cover"))
					{
						data.setTotalLines(measure.getInt("value"));
					} else if(metric.equals("coverage"))
					{
						data.setTotalCoverage(measure.getDouble("value"));
					} else
					{
						JSONObject period = measure.getJSONObject("period");
						if(metric.equals("new_lines_to_cover"))
							data.setNewLines(period.getInt("value"));
						else if(metric.equals("new_coverage"))
							data.setNewCoverage(period.getDouble("value"));

					}
				}
				totalLinesOfCode+=data.getTotalLines();
				newLinesOfCode+=data.getNewLines();
				totalLinesCovered+=data.getTotalLinesCovered();
				newLinesCovered+=data.getNewLinesCovered();
				
			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}				
		
	}

	private List<String> getProjectsForPortfolio() {
		List<String> projects = new ArrayList<>();
		try {
			URL url = new URL(hostURL+"/api/components/tree?component="+portfolioName);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestProperty("Authorization", "Bearer "+token);
			conn.setRequestMethod("GET");
			if(conn.getResponseCode() != 200)
			{
				System.out.println(url+" returned "+conn.getResponseCode());
				throw new RuntimeException("getProjectsForPortolio failed! HTTP error code "+conn.getResponseCode());
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			StringBuilder response = new StringBuilder();
			String line;
			while((line = br.readLine()) != null)
				response.append(line);
			conn.disconnect();
			JSONObject json = new JSONObject(response.toString());
			JSONArray projectsArray = json.getJSONArray("components");
			for(int i = 0; i < projectsArray.length(); i++)
			{
				JSONObject component = projectsArray.getJSONObject(i);
				projects.add(component.getString("refKey"));
			}

			
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return projects;
	}
	
	public int getTotalLinesOfCode() {
		return totalLinesOfCode;
	}

	public int getNewLinesOfCode() {
		return newLinesOfCode;
	}

	public int getTotalLinesCovered() {
		return totalLinesCovered;
	}

	public int getNewLinesCovered() {
		return newLinesCovered;
	}

	/**
	 * internal class used to hold the coverage data for each project being analyzed
	 */
	class ProjectData
	{
		int totalLines;
		int newLines;
		double totalCoverage;
		double newCoverage;
		
		public ProjectData()
		{
			
		}

		public int getTotalLines() {
			return totalLines;
		}

		public void setTotalLines(int totalLines) {
			this.totalLines = totalLines;
		}

		public int getNewLines() {
			return newLines;
		}

		public void setNewLines(int newLines) {
			this.newLines = newLines;
		}

		public double getTotalCoverage() {
			return totalCoverage;
		}

		public void setTotalCoverage(double totalCoverage) {
			this.totalCoverage = totalCoverage;
		}

		public double getNewCoverage() {
			return newCoverage;
		}

		public void setNewCoverage(double newCoverage) {
			this.newCoverage = newCoverage;
		}
		
		public int getTotalLinesCovered() {
			return (int)(totalLines*(totalCoverage/100));
		}
		
		public int getNewLinesCovered() {
			return (int)(newLines*(newCoverage/100));
		}
		
		
	}

}

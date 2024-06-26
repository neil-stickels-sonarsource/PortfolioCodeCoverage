package org.sonarqube.neil;

/**
 * The main entry point for the jar file.  As mentioned in the expected usage, there are 4 parameters:
 * 		1 - The token used to connect to Sonar (recommended this token is generated by someone with Admin access)
 * 		2 - The base URL of your SonarQube instance (for example: https://nautilus.sonarqube.org)
 * 		3 - The name of the portfolio to analyze
 *      4 - The branch to use for coverage analysis
 *      
 * Once this is initiated, this will connect to the specified portfolio, find all of the projects in that
 * portfolio, and then get the new and overall code coverage metrics for each of those projects using the 
 * branch specified.  These will be compiled and then the combined code coverage for overall code and new
 * code will be sent to System.out and shown on the command line.
 * 
 * Note: the count of the lines in the code coverage will be different than the overall lines of code in
 * the project.  The number of lines in this output is using the total number of lines that should be
 * covered with unit tests.
 * 
 */
public class EntryClass {
	

	public static void main(String[] args) 
	{
		if(args.length != 4)
		{
			System.out.println("Expected usage: java -jar PortfolioCodeCoverage.jar {1} {2} {3} {4}\nwhere: \n\t {1} is your sonar token,\n\t {2} is your SonarQube URL,\n\t {3} is the name of the portfolio to analyze,\n\t {4} is the branch to use");
			System.exit(0);
		}
		String token = args[0]; // user token to login to Sonar API
		String url = args[1]; // base URL for your SonarQube instance
		String portfolio = args[2]; // portfolio to analyze
		String branch = args[3]; // branch to analyze
		
		PortfolioAnalyzer pa = new PortfolioAnalyzer(token, url, portfolio, branch);
		pa.analyzePortfolio();
		double totalCoverage = 100d*(double)pa.getTotalLinesCovered()/(double)pa.getTotalLinesOfCode();
		System.out.println("Total Coverage (Overall Code): "+pa.getTotalLinesCovered()+"/"+pa.getTotalLinesOfCode()+"="+totalCoverage);
		double newCoverage = 100d*(double)pa.getNewLinesCovered()/(double)pa.getNewLinesOfCode();
		if(pa.getNewLinesOfCode() == 0)
			newCoverage = 0d;
		System.out.println("New Coverage (New Code): "+pa.getNewLinesCovered()+"/"+pa.getNewLinesOfCode()+"="+newCoverage);
	}





}

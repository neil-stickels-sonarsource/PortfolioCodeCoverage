# PortfolioCodeCoverage

Helper to call the SonarQube API and calculate code coverage for overall and new code for all of the projects within a portfolio. A compiled executable jar file is provided, PortfolioCodeCoverage.jar.  To run this:

java -jar PortfolioCodeCoverage.jar arg1 arg2 arg3 arg4

where:

arg1 is your Sonar Token to access the API (this user should have administrator access to SonarQube or you will likely get 403 errors)
arg2 is your base URL to your SonarQube instance
arg3 is the name of the portfolio you want to analyze
arg4 is the name of the branch to use when calculating coverage



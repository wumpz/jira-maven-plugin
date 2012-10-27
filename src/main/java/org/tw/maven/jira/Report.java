package org.tw.maven.jira;

/**
 * Reportdefinition for a single JQL request.
 *
 * @author tw
 */
public final class Report {

	private String jql = "project = '{project}' AND status in (Resolved, Closed) AND fixVersion = '{fixversion}'";
	private String document = "{issues}";
	private String issue = "[{key}] {summary}";
	private int maxIssues = 100;

	/**
	 * Document template. {issues} is placeholder for the issue list.
	 * 
	 * @parameter
	 * @return
	 */
	public String getDocument() {
		return document;
	}

	public void setDocument(String document) {
		this.document = document;
	}

	/**
	 * Issue template.
	 *
	 * @return
	 */
	public String getIssue() {
		return issue;
	}

	public void setIssue(String issue) {
		this.issue = issue;
	}

	/**
	 * JQL - Query for issue report.
	 *
	 * @return
	 */
	public String getJql() {
		return jql;
	}

	public void setJql(String jql) {
		this.jql = jql;
	}

	/**
	 * Maximum Issues queried by this report.
	 *
	 * @return
	 */
	public int getMaxIssues() {
		return maxIssues;
	}

	public void setMaxIssues(int maxIssues) {
		this.maxIssues = maxIssues;
	}
}

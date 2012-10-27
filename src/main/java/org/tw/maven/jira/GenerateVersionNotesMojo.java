package org.tw.maven.jira;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteIssue;

/**
 * Goal that generate Jira-Issue reports. It is possible to create a report including
 * several JQL - issue lists. There is the possibility to specify special template parts.
 * The report is build up the form
 * <pre>
 * headerText
 *	  report1Header
 *		  issue list 1
 *    report1Footer
 *	  report2Header
 *		  issue list 2
 *    report2Footer
 * footerText
 * </pre>
 *
 * @goal generate-version-notes
 * @phase deploy
 *
 * @author tw
 */
public class GenerateVersionNotesMojo extends AbstractJiraVersionMojo {

	/**
	 * JIRA Report definitions. The definition follows the report tag.
	 * <pre>
	 * {@code 
	 * <report>
	 *   <jql>Jira JQL query.</jql>
	 *   <document>Subreporttemplate</document>
	 *	 <issue>Issuetemplate</issue>
	 *   <maxIssues>maximum amount of issues to query</maxIssues>
	 * </report>
	 * }
	 * </pre>
	 *
	 * The JQL query can replace the macros {project} and {fixversion}. The standard
	 * value is 
	 * {@code 
	 * project = '{project}' AND status in (Resolved, Closed) AND fixVersion = '{fixversion}'
	 * }
	 * 
	 * The document knows the macro {issues} where it puts the result from
	 * the JQL query.
	 * 
	 * The issue template knows the macros {key}, {summary}, {description}, {reporter},
	 * {asignee}.
	 * 
	 * @parameter
	 */
	Report[] reports;
	
	/**
	 * Version to read from.
	 *
	 * @parameter expression="${notesVersion}"
	 */
	String notesVersion;
	
	/**
	 * Report output file.
	 *
	 * @parameter expression="${targetFile}"
	 * default-value="${project.build.directory}/versionreport.txt"
	 * @required
	 */
	File targetFile;
	/**
	 * Headertext for reports document.
	 *
	 * @parameter expression="${headerText}"
	 */
	String headerText;
	/**
	 * Footertext for reports document.
	 *
	 * @parameter expression="${afterText}"
	 */
	String footerText;

	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {
		if (targetFile == null) {
			log.warn("No targetFile specified. Ignoring.");
			return;
		}

		if (notesVersion == null) {
			notesVersion = this.getJiraVersionName();
		}

		if (reports == null) {
			reports = new Report[]{new Report()};
		}

		PrintWriter ps = null;
		try {
			OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(targetFile, true));
			ps = new PrintWriter(writer);
			if (headerText != null) {
				ps.println(headerText);
			}

			for (Report report : reports) {
				String jql = report.getJql().replace("{project}", strvalue(getJiraProjectKey())).
						replace("{fixversion}", notesVersion);
				log.info("JIRA JQL Query: " + jql);

				RemoteIssue[] issues = jiraService.getIssuesFromJqlSearch(loginToken, jql, report.
						getMaxIssues());
				StringBuilder builder = new StringBuilder();
				for (RemoteIssue issue : issues) {
					builder.append(report.getIssue().replace("{key}", strvalue(issue.getKey())).
							replace("{summary}", strvalue(issue.getSummary())).replace("{description}", strvalue(issue.
							getDescription())).
							replace("{reporter}", strvalue(issue.getReporter())).replace("{asignee}", strvalue(issue.
							getAssignee())));
					builder.append("\n");
				}

				ps.println(report.getDocument().replace("{issues}", builder.toString()));
			}

			if (footerText != null) {
				ps.println(footerText);
			}
		} catch (FileNotFoundException ex) {
			log.warn("JIRA report file could not be created: " + targetFile);
		} finally {
			if (ps != null) {
				ps.close();
			}
		}


	}

	private String strvalue(String value) {
		return value == null ? "" : value;
	}

	public void setFooterText(String footerText) {
		this.footerText = footerText;
	}

	public void setHeaderText(String headerText) {
		this.headerText = headerText;
	}

	public File getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(File targetFile) {
		this.targetFile = targetFile;
	}

	public void setNotesVersion(String notesVersion) {
		this.notesVersion = notesVersion;
	}

	Report[] getReports() {
		return reports;
	}

	void setReports(Report[] reports) {
		this.reports = reports;
	}
}

package org.tw.maven.jira;

import java.io.File;
import java.rmi.RemoteException;
import org.apache.maven.wagon.util.FileUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemoteIssue;

public class GenerateVersionNotesMojoTest {

	private static final RemoteIssue[] ISSUES = new RemoteIssue[2];

	static {
		RemoteIssue issue = new RemoteIssue();
		issue.setAssignee("tw");
		issue.setKey("MISC-123");
		issue.setSummary("this is an issue");
		issue.setDescription("with a long description");
		ISSUES[0] = issue;
		
		issue = new RemoteIssue();
		issue.setAssignee("tw");
		issue.setKey("MISC-126");
		issue.setSummary("this is an issue (2)");
		issue.setDescription("with a long description (2)");
		ISSUES[1] = issue;
		
	}
	
	private static final String JIRA_LOGIN_TOKEN = "TEST_TOKEN";
	private GenerateVersionNotesMojo jiraVersionNotes;
	private JiraSoapService jiraStub;
	 
	@Before
	public void setUp() throws Exception {
		jiraVersionNotes = new GenerateVersionNotesMojo();
		jiraVersionNotes.setJiraUser("user");
		jiraVersionNotes.setJiraPassword("password");
		jiraVersionNotes.setJiraProjectKey("TEST");

		//jiraStub = mock(JiraSoapService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
		jiraStub = mock(JiraSoapService.class);
		this.jiraVersionNotes.jiraService = jiraStub;
	}

	@Test
	public void testDoExecute() throws Exception {
		File file = FileUtils.getFile("target/versionnotes.txt");
		file.delete();
		
		jiraVersionNotes.setNotesVersion("1.1");
		jiraVersionNotes.setTargetFile(new File("target/versionnotes.txt"));
		doLogin();
		when(jiraStub.getIssuesFromJqlSearch(anyString(), anyString(), anyInt())).thenReturn(ISSUES);
		doLogout();
		jiraVersionNotes.execute();
		assertTrue("Release Notes not generated",file.exists());
		assertEquals("file size does not match",62, file.length());
	}
	
	@Test
	public void testDoExecute2() throws Exception {
		//clean up test files
		File file = FileUtils.getFile("target/versionnotes2.txt");
		file.delete();
		
		jiraVersionNotes.setNotesVersion("1.1");
		jiraVersionNotes.setTargetFile(new File("target/versionnotes2.txt"));
		
		Report report1 = new Report();
		report1.setDocument("test {issues} testende");
		
		Report report2 = new Report();
		report2.setDocument("test2 {issues} testende2");
		
		jiraVersionNotes.setReports(new Report[] {report1,report2});
		jiraVersionNotes.setHeaderText("starting ");
		jiraVersionNotes.setFooterText(" finished");

		doLogin();
		when(jiraStub.getIssuesFromJqlSearch(anyString(), anyString(), anyInt())).thenReturn(ISSUES);
		when(jiraStub.getIssuesFromJqlSearch(anyString(), anyString(), anyInt())).thenReturn(ISSUES);
		doLogout();
		jiraVersionNotes.execute();
		
		assertTrue("Release Notes not generated",file.exists());
		assertEquals("file size does not match", 176, file.length());
	}
	
	/**
	 * Set up logout mock behavior
	 *
	 * @throws RemoteException
	 */
	private void doLogout() throws RemoteException {
		when(jiraStub.logout(JIRA_LOGIN_TOKEN)).thenReturn(Boolean.TRUE);
	}

	/**
	 * Set up login mock behavior
	 */
	private void doLogin() throws RemoteException,
			RemoteAuthenticationException {
		when(jiraStub.login("user", "password")).thenReturn(JIRA_LOGIN_TOKEN);
	}
}

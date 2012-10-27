package org.tw.maven.jira;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemoteVersion;

/**
 *
 * @author toben
 */
public class ArchiveVersionMojoTest {
	private static final String JIRA_LOGIN_TOKEN = "TEST_TOKEN";
	private static final RemoteVersion[] VERSIONS = new RemoteVersion[20];
	
	static {
		for (int i = 0; i < 10; i++) {
			VERSIONS[i] = new RemoteVersion(String.valueOf(i), String.valueOf(i) + ".0", false, null, false, null);
		}

		for (int i = 0; i < 10; i++) {
			VERSIONS[10 + i] = new RemoteVersion(String.valueOf(i) + "00", String.valueOf(i) + ".1", false, null, false, null);
		}
	}
	
	private ArchiveVersionMojo jiraArchiveVersionMojo;
	private JiraSoapService jiraStub;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraArchiveVersionMojo = new ArchiveVersionMojo();
		jiraArchiveVersionMojo.setJiraUser("user");
		jiraArchiveVersionMojo.setJiraPassword("password");
		jiraArchiveVersionMojo.setJiraProjectKey("TEST");

		//jiraStub = mock(JiraSoapService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
		jiraStub = mock(JiraSoapService.class);
		this.jiraArchiveVersionMojo.jiraService = jiraStub;
	}
	
	@Test
	public void testExecuteWithReleaseVersion() throws Exception {
		RemoteVersion ARCHIVE_VERSION = VERSIONS[3];
		jiraArchiveVersionMojo.archiveVersion = "3.0";
		ARCHIVE_VERSION.setArchived(false);
		assertFalse(ARCHIVE_VERSION.isArchived());
		doLogin();
		when(jiraStub.getVersions(JIRA_LOGIN_TOKEN, jiraArchiveVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);
		doLogout();
	
		jiraArchiveVersionMojo.execute();
		verify(jiraStub).archiveVersion(JIRA_LOGIN_TOKEN, jiraArchiveVersionMojo.getJiraProjectKey(), "3.0", true);
		assertTrue(ARCHIVE_VERSION.isArchived());
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
		when(jiraStub.login("user", "password")).thenReturn(
				JIRA_LOGIN_TOKEN);
	}
}

/**
 *
 */
package org.tw.maven.jira;

import java.rmi.RemoteException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.internal.debugging.VerboseMockInvocationLogger;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemoteVersion;

/**
 * JUnit test case for Jira version MOJO
 *
 * @author tw
 *
 */
public class CreateNewVersionMojoTest {

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
	private CreateNewVersionMojo jiraVersionMojo;
	private JiraSoapService jiraStub;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraVersionMojo = new CreateNewVersionMojo();
		jiraVersionMojo.setJiraUser("user");
		jiraVersionMojo.setJiraPassword("password");
		jiraVersionMojo.setJiraProjectKey("TEST");

		//jiraStub = mock(JiraSoapService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
		jiraStub = mock(JiraSoapService.class);
		this.jiraVersionMojo.jiraService = jiraStub;
	}

	/**
	 * Test method for {@link CreateNewVersionMojo#execute()}
	 *
	 * @throws Exception
	 */
	@Test
	public void testExecuteWithNewDevVersion() throws Exception {
		jiraVersionMojo.actualVersion = "11.0";
		doLogin();

		when(
				jiraStub.getVersions(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);


		when(jiraStub.addVersion(JIRA_LOGIN_TOKEN, jiraVersionMojo.getJiraProjectKey(),
				new RemoteVersion(null, jiraVersionMojo.actualVersion, false, null, false, null))).thenReturn(VERSIONS[0]);
		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub).addVersion(JIRA_LOGIN_TOKEN, jiraVersionMojo.getJiraProjectKey(),
				new RemoteVersion(null, jiraVersionMojo.actualVersion, false, null, false, null));
	}

	@Test
	public void testExecuteWithNewDevVersionIncludingQualifierAndSnapshot() throws Exception {
		jiraVersionMojo.actualVersion = "5.0-beta-2-SNAPSHOT";
		doLogin();

		when(
				jiraStub.getVersions(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);


		when(
				jiraStub.addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"5.0-beta-2", false,
				null, false, null))).thenReturn(VERSIONS[0]);
		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub).addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"5.0-beta-2", false,
				null, false, null));
	}

	@Test
	public void testExecuteWithNewDevVersionAndUseFinalNameForVersionSetToTrue() throws Exception {
		jiraVersionMojo.actualVersion = "5.0-beta-2-SNAPSHOT";
		jiraVersionMojo.finalNameUsedForVersion = true;
		jiraVersionMojo.finalName = "my-component-5.0-beta-2-SNAPSHOT";
		doLogin();

		when(
				jiraStub.getVersions(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);

		when(
				jiraStub.addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"my-momponent-5.0-beta-2", false,
				null, false, null))).thenReturn(VERSIONS[0]);
		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub).addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"my-component-5.0-beta-2", false,
				null, false, null));
	}
	
	@Test
	public void testExecuteWithNewDevVersionAndUseFinalNameForVersionSetToTrue2() throws Exception {
		jiraVersionMojo.actualVersion = "5.0-beta-2-SNAPSHOT";
		jiraVersionMojo.finalNameUsedForVersion = true;
		jiraVersionMojo.finalName = "my-component-5.0 beta-2-SNAPSHOT";
		doLogin();

		when(
				jiraStub.getVersions(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);

		when(
				jiraStub.addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"my-momponent-5.0 beta-2", false,
				null, false, null))).thenReturn(VERSIONS[0]);
		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub).addVersion(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey(), new RemoteVersion(null,
				"my-component-5.0 beta-2", false,
				null, false, null));
	}

	/**
	 * Test method for {@link ReleaseVersionMojo#execute()}
	 *
	 * @throws Exception
	 */
	@Test
	public void testExecuteWithExistentDevVersion() throws Exception {
		jiraVersionMojo.actualVersion = "2.0";
		doLogin();

		when(jiraStub.getVersions(JIRA_LOGIN_TOKEN, jiraVersionMojo.getJiraProjectKey())).thenReturn(
				VERSIONS);

		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub, never()).addVersion(anyString(), anyString(), any(RemoteVersion.class));
	}

	@Test
	public void testExecuteWithNewDevVersion_SkipCreation() throws Exception {
		jiraVersionMojo.actualVersion = "5.0";
		jiraVersionMojo.setJiraSkipCreation(true);

		doLogin();

		when(
				jiraStub.getVersions(JIRA_LOGIN_TOKEN,
				jiraVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);

		doLogout();

		jiraVersionMojo.execute();

		verify(jiraStub, never()).addVersion(anyString(), anyString(), any(RemoteVersion.class));
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

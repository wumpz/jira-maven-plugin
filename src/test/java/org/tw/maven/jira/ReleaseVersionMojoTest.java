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
 * ReleaseVersionMojo Test
 *
 * @author tw
 *
 */
public class ReleaseVersionMojoTest {

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
	
	private ReleaseVersionMojo jiraReleaseVersionMojo;
	private JiraSoapService jiraStub;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		this.jiraReleaseVersionMojo = new ReleaseVersionMojo();
		jiraReleaseVersionMojo.setJiraUser("user");
		jiraReleaseVersionMojo.setJiraPassword("password");
		jiraReleaseVersionMojo.setJiraProjectKey("TEST");

		//jiraStub = mock(JiraSoapService.class, withSettings().invocationListeners(new VerboseMockInvocationLogger()));
		jiraStub = mock(JiraSoapService.class);
		this.jiraReleaseVersionMojo.jiraService = jiraStub;
	}

	@Test
	public void testDiscoverJiraWSURL() throws MalformedURLException {
		jiraReleaseVersionMojo.setJiraURL("http://tester.org");

		URL actual = jiraReleaseVersionMojo.buildJiraSoapURL();
		String expected = jiraReleaseVersionMojo.getJiraURL()
				+ AbstractJiraMojo.JIRA_SOAP_URL_SUFFIX;
		assertEquals("JIRA WS URL does not match", expected, actual.toString());
	}
	
	@Test
	public void testDiscoverWithProjectKey() throws Exception {
		String expected = "http://tester.org/jira" + AbstractJiraMojo.JIRA_SOAP_URL_SUFFIX;
		jiraReleaseVersionMojo.setJiraURL("http://tester.org/jira/browse/TESTPRJ");
		URL actual = jiraReleaseVersionMojo.buildJiraSoapURL();
		assertEquals(expected, actual.toString());
		assertEquals("TESTPRJ", jiraReleaseVersionMojo.getJiraProjectKey());
	}

	@Test
	public void testDiscoverWithoutProjectKey() throws Exception {
		jiraReleaseVersionMojo.setJiraProjectKey("TESTPRJ");
		String expected = "http://tester.org/jira" + AbstractJiraMojo.JIRA_SOAP_URL_SUFFIX;
		jiraReleaseVersionMojo.setJiraURL("http://tester.org/jira");
		URL actual = jiraReleaseVersionMojo.buildJiraSoapURL();
		assertEquals(expected, actual.toString());
		assertEquals("TESTPRJ", jiraReleaseVersionMojo.getJiraProjectKey());
	}

	@Test
	public void testExecuteWithReleaseVersion() throws Exception {
		RemoteVersion RELEASED_VERSION = VERSIONS[3];
		jiraReleaseVersionMojo.releaseVersion = "3.0";
		RELEASED_VERSION.setReleased(false);
		assertFalse(RELEASED_VERSION.isReleased());
		doLogin();
		when(jiraStub.getVersions(JIRA_LOGIN_TOKEN, jiraReleaseVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);
		doLogout();
	
		jiraReleaseVersionMojo.execute();
		verify(jiraStub).releaseVersion(JIRA_LOGIN_TOKEN, jiraReleaseVersionMojo.getJiraProjectKey(), RELEASED_VERSION);
		assertTrue(RELEASED_VERSION.isReleased());
	}
	
	@Test
	public void testExecuteWithoutReleaseVersion() throws Exception {
		jiraReleaseVersionMojo.actualVersion = "5.0-beta-2-SNAPSHOT";
		jiraReleaseVersionMojo.finalNameUsedForVersion = true;
		jiraReleaseVersionMojo.finalName = "5.0";
		
		RemoteVersion RELEASED_VERSION = VERSIONS[5];
		
		RELEASED_VERSION.setReleased(false);
		assertFalse(RELEASED_VERSION.isReleased());
		doLogin();
		when(jiraStub.getVersions(JIRA_LOGIN_TOKEN, jiraReleaseVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);
		doLogout();
	
		jiraReleaseVersionMojo.execute();
		verify(jiraStub).releaseVersion(JIRA_LOGIN_TOKEN, jiraReleaseVersionMojo.getJiraProjectKey(), RELEASED_VERSION);
		assertTrue(RELEASED_VERSION.isReleased());
	}
	
	@Test
	public void testExecuteWithoutReleaseVersionSnapshot() throws Exception {
		jiraReleaseVersionMojo.actualVersion = "5.0-beta-2-SNAPSHOT";
		
		RemoteVersion RELEASED_VERSION = VERSIONS[5];
		
		RELEASED_VERSION.setReleased(false);
		assertFalse(RELEASED_VERSION.isReleased());
		doLogin();
		when(jiraStub.getVersions(JIRA_LOGIN_TOKEN, jiraReleaseVersionMojo.getJiraProjectKey())).thenReturn(VERSIONS);
		doLogout();
	
		jiraReleaseVersionMojo.execute();
		verify(jiraStub,never()).releaseVersion(anyString(),anyString(), any(RemoteVersion.class));
		assertFalse(RELEASED_VERSION.isReleased());
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

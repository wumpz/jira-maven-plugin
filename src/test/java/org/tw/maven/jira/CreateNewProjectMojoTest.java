package org.tw.maven.jira;

import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteAuthenticationException;
import org.swift.common.soap.jira.RemotePermissionScheme;
import org.swift.common.soap.jira.RemoteProject;
import java.rmi.RemoteException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.BeforeClass;

import static org.mockito.Mockito.*;
import org.swift.common.soap.jira.RemoteScheme;

/**
 *
 * @author tw
 */
public class CreateNewProjectMojoTest {

	private static final String JIRA_LOGIN_TOKEN = "TEST_TOKEN";
	private CreateNewProjectMojo jiraProjectMojo;
	private JiraSoapService jiraStub;
	private static final RemotePermissionScheme[] permissionSchemes = {
		new RemotePermissionScheme("test", 1000L, "test2", "permission", null),
		new RemotePermissionScheme("default", 0L, "default", "permission", null)};
	private static final RemoteProject project = new RemoteProject("1", "testproject", "testproject", null, "TP", "tw", null, null, null, null);

	public CreateNewProjectMojoTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}

	@Before
	public void setUp() {
		jiraProjectMojo = new CreateNewProjectMojo();
		jiraProjectMojo.setJiraUser("user");
		jiraProjectMojo.setJiraPassword("password");

		jiraStub = mock(JiraSoapService.class);
		jiraProjectMojo.jiraService = jiraStub;
	}

	@After
	public void tearDown() {
	}

	/**
	 * Test of getProjectName method, of class CreateNewProjectMojo.
	 */
	@Test
	public void testAddNewProject() throws RemoteException, MojoExecutionException, MojoFailureException {
		jiraProjectMojo.setProjectName("testproject");
		jiraProjectMojo.setJiraProjectKey("TP");
		jiraProjectMojo.setProjectLeader("tw");

		doLogin();

		when(jiraStub.getProjectByKey(JIRA_LOGIN_TOKEN, jiraProjectMojo.getJiraProjectKey())).thenThrow(new RemoteException());
		when(jiraStub.getPermissionSchemes(JIRA_LOGIN_TOKEN)).thenReturn(permissionSchemes);
		when(jiraStub.createProject(JIRA_LOGIN_TOKEN, "TP", "testproject", "testproject", null, "tw", permissionSchemes[1], null, null)).thenReturn(project);

		doLogout();

		jiraProjectMojo.execute();
		
		verify(jiraStub).createProject(JIRA_LOGIN_TOKEN, "TP", "testproject", "testproject", null, "tw", permissionSchemes[1], null, null);
	}

	@Test
	public void testAddNewProjectExistsAlready() throws RemoteException, MojoExecutionException, MojoFailureException {
		jiraProjectMojo.setProjectName("testproject");
		jiraProjectMojo.setJiraProjectKey("TP");
		jiraProjectMojo.setProjectLeader("tw");

		doLogin();

		when(jiraStub.getProjectByKey(JIRA_LOGIN_TOKEN, jiraProjectMojo.getJiraProjectKey())).thenReturn(project);

		doLogout();

		jiraProjectMojo.execute();
		
		verify(jiraStub).getProjectByKey(JIRA_LOGIN_TOKEN, jiraProjectMojo.getJiraProjectKey());
	}
	
	@Test
	public void testAddNewProjectSkipCreation() throws RemoteException, MojoExecutionException, MojoFailureException {
		jiraProjectMojo.setProjectName("testproject");
		jiraProjectMojo.setJiraProjectKey("TP");
		jiraProjectMojo.setProjectLeader("tw");
		jiraProjectMojo.setJiraSkipCreation(true);

		doLogin();

		when(jiraStub.getProjectByKey(JIRA_LOGIN_TOKEN, jiraProjectMojo.getJiraProjectKey())).thenThrow(new RemoteException());
		when(jiraStub.getPermissionSchemes(JIRA_LOGIN_TOKEN)).thenReturn(permissionSchemes);

		doLogout();

		jiraProjectMojo.execute();
		
		verify(jiraStub,never()).createProject(anyString(), anyString(), anyString(), anyString(), anyString(), anyString(),any(RemotePermissionScheme.class),any(RemoteScheme.class),any(RemoteScheme.class));
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

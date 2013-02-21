package org.tw.maven.jira;

import java.rmi.RemoteException;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemotePermissionScheme;
import org.swift.common.soap.jira.RemoteProject;

/**
 * Goal that creates a new project in JIRA. This goal assigns the default permission
 * scheme of jira and the project leader of this new project. If a project exists with 
 * the given name, then nothing is written.
 * 
 * @goal create-new-project
 * @phase deploy
 * @author tw
 */
public class CreateNewProjectMojo  extends AbstractJiraMojo {

	/**
	 * Name of project to be created. It defaults to artifactid.
	 * 
	 * @parameter expression="${projectName}" default-value="${project.artifactId}"
	 * @required
	 */
	private String projectName;

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(String projectName) {
		this.projectName = projectName;
	}
	
	/**
	 * Leader of the new project. This is the JIRA login of the project leader.
	 * 
	 * @parameter expression="${projectLeader}"
	 * @required
	 */
	private String projectLeader;

	public String getProjectLeader() {
		return projectLeader;
	}

	public void setProjectLeader(String projectLeader) {
		this.projectLeader = projectLeader;
	}
	
	/**
	 * Get the default permission scheme for this JIRA instance.
	 * @param loginToken
	 * @return
	 * @throws RemoteException 
	 */
	private RemotePermissionScheme getDefaultPermissionScheme(String loginToken) throws RemoteException {
		RemotePermissionScheme[] perms = jiraService.getPermissionSchemes(loginToken);
		for (RemotePermissionScheme scheme : perms) {
			if (scheme.getId()==0)
				return scheme;
		}
		return null;
	} 
	
	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken) throws RemoteException {
		RemoteProject project = null;
		
		if (getJiraProjectKey()==null) {
			log.info("no JIRA project key specified: execution of create-new-project skipped");
			return;
		}
		
		
		try {
			project = jiraService.getProjectByKey(loginToken,getJiraProjectKey());
		} catch (Exception ex) {
			log.info(String.format("JIRA: could not search for project %s", getJiraProjectKey()));
		} 
		
		if (project==null) {
			RemotePermissionScheme permissionScheme = getDefaultPermissionScheme(loginToken);
			
			if (permissionScheme!=null) {
				if (!isJiraSkipCreation()) {
					project = jiraService.createProject(loginToken, getJiraProjectKey(), projectName, projectName, null, projectLeader, permissionScheme, null, null);
					log.info(String.format("JIRA: project %s was created with id %s",project.getKey(),project.getId()));
				} else {
					log.info(String.format("JIRA: would have created project %s",getJiraProjectKey()));
				}
			}
			else {
				log.warn(String.format("could not get default permission scheme from JIRA."));
			}
		}
		else
		{
			log.warn(String.format("project %s already exists in JIRA.", getJiraProjectKey()));
		}
	}
	
}

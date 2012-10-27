package org.tw.maven.jira;

import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteVersion;

/**
 * Goal that creates a version in a JIRA project. The project name must be specified. 
 * The version is taken from the actual artifact version. The plugin removes any 
 * -SNAPSHOT part of it, so snapshot and release version names are equal. The release 
 * of a JIRA version is done by releasing it there. If the specified version is already
 * existing in JIRA nothing is done.
 *
 * @goal create-new-version
 * @phase deploy
 *
 * @author tw
 */
public class CreateNewVersionMojo extends AbstractJiraVersionMojo {

	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {;

		if (getJiraProjectKey() == null) {
			log.info("no JIRA project key specified: execution of create-new-version skipped");
			return;
		}

		RemoteVersion[] versions = jiraService.getVersions(loginToken, getJiraProjectKey());
		String newDevVersion = getJiraVersionName();
		
		boolean versionExists = isVersionAlreadyPresent(versions,newDevVersion);
		
		if (!versionExists) {
			if (!isJiraSkipCreation()) {
				RemoteVersion newVersion = new RemoteVersion();
				log.debug("New Development version in JIRA is: "
						+ newDevVersion);
				newVersion.setName(newDevVersion);
				jiraService.addVersion(loginToken, getJiraProjectKey(),
						newVersion);

				log.info("Version created in JIRA for project key "
						+ getJiraProjectKey() + " : " + newDevVersion);
			} else {
				log.info(String.format("would have created version '%s' for project key %s in JIRA", newDevVersion, getJiraProjectKey()));
			}
		} else {
			log.warn(String.format(
					"Version %s is already created in JIRA. Nothing to do.",
					newDevVersion));
		}
	}
}

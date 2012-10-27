package org.tw.maven.jira;

import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteVersion;

/**
 * Archive a version in JIRA. The project must be specified. The plugin looks for
 * the matching version. If there is a version, it will be archived. 
 *
 * @goal archive-version
 * @phase deploy
 *
 * @author tw
 */
public class ArchiveVersionMojo  extends AbstractJiraVersionMojo {

	/**
	 * Archive Version
	 *
	 * @parameter expression="${archiveVersion}"
	 */
	String archiveVersion;

	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {

		if (archiveVersion == null) {
			archiveVersion = getVersionName();
		}

		RemoteVersion[] versions = jiraService.getVersions(loginToken,
				getJiraProjectKey());

		if (archiveVersion != null) {
			log.info("JIRA: archiving Version " + this.archiveVersion);

			boolean foundVersion = false;
			for (RemoteVersion remoteVersion : versions) {
				if (archiveVersion.equalsIgnoreCase(remoteVersion.getName())) {
					if (!remoteVersion.isArchived()) {
						if (!isJiraSkipCreation()) {
							remoteVersion.setArchived(true);
							jiraService.archiveVersion(loginToken, getJiraProjectKey(),
									archiveVersion,true);
							log.info(
									"JIRA: Version " + remoteVersion.getName()
									+ " archived for project " + getJiraProjectKey());
						} else {
							log.info(
									"JIRA: Version " + remoteVersion.getName()
									+ " would have been archived for project " + getJiraProjectKey());
						}
						foundVersion = true;
						break;
					} else {
						getLog().info("Version " + archiveVersion + " already archived in project " + getJiraProjectKey());
					}
				}
			}

			if (!foundVersion) {
				getLog().info("Version " + archiveVersion + " not found in project " + getJiraProjectKey());
			}
		}
	}
}

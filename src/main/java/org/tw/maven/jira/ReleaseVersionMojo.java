package org.tw.maven.jira;

import java.util.Calendar;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.RemoteVersion;

/**
 * Release a version in JIRA. The project must be specified. The plugin looks for
 * the matching version. If there is a version, it will be released. The plugin does nothing
 * for maven snapshot versions.
 *
 * @goal release-version
 * @phase deploy
 *
 * @author tw
 */
public class ReleaseVersionMojo extends AbstractJiraVersionMojo {

	/**
	 * Released Version
	 *
	 * @parameter expression="${releaseVersion}"
	 */
	String releaseVersion;

	@Override
	public void doExecute(JiraSoapService jiraService, String loginToken)
			throws Exception {

		if (releaseVersion == null) {
			releaseVersion = getVersionName();
		}

		if (releaseVersion.endsWith("-SNAPSHOT")) {
			log.warn("JIRA: releasing Version " + this.releaseVersion + " not possible. Must be a release version.");
			return;
		}

		RemoteVersion[] versions = jiraService.getVersions(loginToken,
				getJiraProjectKey());

		if (releaseVersion != null) {
			log.info("JIRA: releasing Version " + this.releaseVersion);

			boolean foundVersion = false;
			for (RemoteVersion remoteReleasedVersion : versions) {
				if (releaseVersion.equalsIgnoreCase(remoteReleasedVersion.getName())) {
					if (!remoteReleasedVersion.isReleased()) {
						if (!isJiraSkipCreation()) {
							remoteReleasedVersion.setReleased(true);
							remoteReleasedVersion.setReleaseDate(Calendar.getInstance());
							jiraService.releaseVersion(loginToken, getJiraProjectKey(),
									remoteReleasedVersion);
							log.info(
									"JIRA: Version " + remoteReleasedVersion.getName()
									+ " released for project " + getJiraProjectKey());
						} else {
							log.info(
									"JIRA: Version " + remoteReleasedVersion.getName()
									+ " would have been released for project " + getJiraProjectKey());
						}
						foundVersion = true;
						break;
					} else {
						getLog().info("Version " + releaseVersion + " already released in project " + getJiraProjectKey());
					}
				}
			}

			if (!foundVersion) {
				getLog().info("Version " + releaseVersion + " not found in project " + getJiraProjectKey());
			}
		}
	}
}

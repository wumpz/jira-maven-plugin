package org.tw.maven.jira;

import org.swift.common.soap.jira.RemoteVersion;

/**
 * Base class for actions regarding version management. It simplifies the configuration
 * of version specific settings.
 * @author tw
 */
public abstract class AbstractJiraVersionMojo extends AbstractJiraMojo{
	/**
	 * Actual version name to use.
	 *
	 * @parameter expression="${developmentVersion}" default-value="${project.version}"
	 * @required
	 */
	String actualVersion;
	
	/**
	 * @parameter default-value="${project.build.finalName}"
	 */
	String finalName;
	
	/**
	 * Whether the final name is to be used for the version; defaults to false.
	 *
	 * @parameter expression="${finalNameUsedForVersion}"
	 */
	boolean finalNameUsedForVersion;
	
	/**
	 * Returns actual version name. This one is stripped from '-SNAPSHOT' suffix.
	 * @return 
	 */
	String getJiraVersionName() {
		return getVersionName().replace("-SNAPSHOT", "");
	}
	
	/**
	 * Returns the actual version name including '-SNAPSHOT' if its a snapshot version.
	 * @return 
	 */
	String getVersionName() {
		if (finalNameUsedForVersion) {
			return finalName;
		} else {
			return actualVersion;
		}
	}
	
	/**
	 * Check if version is already present on array
	 *
	 * @param versions
	 * @param versionName
	 * @return
	 */
	boolean isVersionAlreadyPresent(RemoteVersion[] versions,
			String versionName) {
		boolean versionExists = false;
		if (versions != null) {
			for (RemoteVersion remoteVersion : versions) {
				if (remoteVersion.getName().equalsIgnoreCase(versionName)) {
					versionExists = true;
					break;
				}
			}
		}
		return versionExists;
	}
}

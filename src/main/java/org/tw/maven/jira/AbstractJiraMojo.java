package org.tw.maven.jira;

import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.rpc.ServiceException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.swift.common.soap.jira.JiraSoapService;
import org.swift.common.soap.jira.JiraSoapServiceServiceLocator;

/**
 * Base class to connect to Jira using SOAP - Service.
 * 
 * @author tw
 * 
 */
public abstract class AbstractJiraMojo extends AbstractMojo {

	/**
	 * JIRA SOAP Url Suffix
	 */
	public static final String JIRA_SOAP_URL_SUFFIX = "/rpc/soap/jirasoapservice-v2";

	/**
	 * Server's id in settings.xml to look up username and password.
	 * 
	 * @parameter expression="${settingsXmlKey}"
	 */
	private String settingsXmlKey;
	
	/**
	 * @parameter expression="${settings}"
	 * @required
	 * @readonly
	 */
	Settings settings;

	/**
	 * JIRA Installation URL. If not set, it will use the
	 * project.issueManagement.url parameter.
	 * 
	 * @parameter expression="${jiraURL}" default-value="${project.issueManagement.url}"
	 * @required
	 */
	private String jiraURL;

	/**
	 * JIRA username to connect to Jira.
	 * 
	 * @parameter expression="${jiraUser}" default-value="${scmUsername}"
	 */
	private String jiraUser;

	/**
	 * JIRA password to connect to Jira.
	 * 
	 * @parameter expression="${jiraPassword}" default-value="${scmPassword}"
	 */
	private String jiraPassword;

	/**
	 * JIRA Project Key. If this one has no value, then create-new-project or create-new-version
	 * does not fail, but writes a log message.
	 * 
	 * @parameter expression="${jiraProjectKey}"
	 */
	private String jiraProjectKey;
	
	/**
	 * Skip creation of JIRA entities.
	 * 
	 * @parameter expression="${jiraSkipCreation}" default-value="false"
	 */
	private boolean jiraSkipCreation;
	
	/**
	 * Allow to correct JIRA project key. Removes "-" and "." which are common in artifactIds.
	 * 
	 * @parameter expression="${jiraCorrectProjectKey}" default-value="false"
	 */
	private boolean jiraCorrectProjectKey;

	/**
	 * Returns if this plugin is enabled for this context
	 * @parameter expression="${skip}"
	 */
	protected boolean skip;
	
	/**
	 * Jira SOAP Service Implementation.
	 */
	protected transient JiraSoapService jiraService;
	
	public boolean isJiraCorrectProjectKey() {
		return jiraCorrectProjectKey;
	}

	public void setJiraCorrectProjectKey(boolean jiraCorrectProjectKey) {
		this.jiraCorrectProjectKey = jiraCorrectProjectKey;
	}

	public String getJiraPassword() {
		return jiraPassword;
	}

	public void setJiraPassword(String jiraPassword) {
		this.jiraPassword = jiraPassword;
	}

	public String getJiraProjectKey() {
		return jiraProjectKey;
	}

	public void setJiraProjectKey(String jiraProjectKey) {
		this.jiraProjectKey = jiraProjectKey;
	}

	public boolean isJiraSkipCreation() {
		return jiraSkipCreation;
	}

	public void setJiraSkipCreation(boolean jiraSkipCreation) {
		this.jiraSkipCreation = jiraSkipCreation;
	}

	public String getJiraURL() {
		return jiraURL;
	}

	public void setJiraURL(String jiraURL) {
		this.jiraURL = jiraURL;
	}

	public String getJiraUser() {
		return jiraUser;
	}

	public void setJiraUser(String jiraUser) {
		this.jiraUser = jiraUser;
	}

	public Settings getSettings() {
		return settings;
	}

	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	public String getSettingsKey() {
		return settingsXmlKey;
	}

	public void setSettingsKey(String settingsKey) {
		this.settingsXmlKey = settingsKey;
	}

	public boolean isSkip() {
		return skip;
	}

	public void setSkip(boolean skip) {
		this.skip = skip;
	}
	
	
	/**
	 * Returns the stub needed to invoke the WebService
	 * 
	 * @return
	 * @throws MalformedURLException
	 * @throws ServiceException
	 */
	protected JiraSoapService getJiraSoapService() throws MalformedURLException,
			ServiceException {
		if (jiraService == null) {
			JiraSoapServiceServiceLocator locator = new JiraSoapServiceServiceLocator();
			jiraService = locator.getJirasoapserviceV2(buildJiraSoapURL());
		} 
		return jiraService;
	}

	/**
	 * Builds JIRA Soap Sevice URL from settings.
	 * 
	 * @return JIRA Web Service URL
	 * 
	 */
	URL buildJiraSoapURL() throws MalformedURLException {
		String url;
		if (jiraURL == null) {
			return null;
		}
		if (jiraURL.endsWith(JIRA_SOAP_URL_SUFFIX)) {
			url = jiraURL;
		} else {
			int projectIdx = jiraURL.indexOf("/browse");
			if (projectIdx > -1) {
				int lastPath = jiraURL.indexOf("/", projectIdx + 8);
				if (lastPath == -1) {
					lastPath = jiraURL.length();
				}
				jiraProjectKey = jiraURL.substring(projectIdx + 8,lastPath);
				url = jiraURL.substring(0,projectIdx) + JIRA_SOAP_URL_SUFFIX;
			} else {
				url = jiraURL + JIRA_SOAP_URL_SUFFIX;
			}
		}
		return new URL(url);
	}	

	/**
	 * Load username password from settings.xml if user has not set them in JVM
	 * properties
	 */
	void loadUserInfoFromSettings() {
		if (settingsXmlKey == null) {
			settingsXmlKey = jiraURL;
		}
		if ((jiraUser == null || jiraPassword == null) && (settings != null)) {
			Server server = settings.getServer(this.settingsXmlKey);

			if (server != null) {
				if (jiraUser == null) {
					jiraUser = server.getUsername();
				}

				if (jiraPassword == null) {
					jiraPassword = server.getPassword();
				}
			}
		}
	}
	
	/**
	 * Corrects JiraProjectKey to be valid.
	 * 
	 * @param jiraPrjKey
	 * @return 
	 */
	public static String getCorrectedJiraProjectKey(String jiraPrjKey) {
		return jiraPrjKey==null?jiraPrjKey:jiraPrjKey.replaceAll("[\\-.0-9]", "").toUpperCase();
	}
	
	/**
	 * Maven logging entry point.
	 */
	protected Log log;	
	
	@Override
	public final void execute() throws MojoExecutionException, MojoFailureException {
		log = getLog();
		
		if (isSkip()) {
			log.info("plugin " + getClass().getSimpleName() + " skipped");
			return;
		}
		
		if (isJiraCorrectProjectKey()) {
			jiraProjectKey = getCorrectedJiraProjectKey(jiraProjectKey);
		}
		
		try {
			JiraSoapService jiraSoapService = getJiraSoapService();
			loadUserInfoFromSettings();
			String loginToken = jiraSoapService.login(jiraUser, jiraPassword);
			log.debug("JIRA login token=" + loginToken);
			try {
				doExecute(jiraSoapService, loginToken);
			} finally {
				jiraSoapService.logout(loginToken);
				log.debug("JIRA logout");
			}
		} catch (Exception e) {
			log.error("error executing " + getClass().getSimpleName() + " (does not interrupt build)", e);
		}
	}
	
	public abstract void doExecute(JiraSoapService jiraService, String loginToken) throws Exception;
}
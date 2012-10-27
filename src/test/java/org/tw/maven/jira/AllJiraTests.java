package org.tw.maven.jira;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@SuiteClasses( { CreateNewVersionMojoTest.class, ReleaseVersionMojoTest.class,
		CreateNewProjectMojoTest.class, AbstractJiraMojoTest.class })
@RunWith(Suite.class)
public class AllJiraTests {

}

package org.tw.maven.jira;

import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.swift.common.soap.jira.JiraSoapService;

/**
 *
 * @author tw
 */
public class AbstractJiraMojoTest {
	
	public AbstractJiraMojoTest() {
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
	}

	@AfterClass
	public static void tearDownClass() throws Exception {
	}
	
	@Before
	public void setUp() {
	}
	
	@After
	public void tearDown() {
	}

	/**
	 * Test of setJiraCorrectProjectKey method, of class AbstractJiraMojo.
	 */
	@Test
	public void testSetJiraCorrectProjectKey() {
		assertEquals("COMMONS", AbstractJiraMojo.getCorrectedJiraProjectKey("COMMONS"));
		assertEquals("COMMONS", AbstractJiraMojo.getCorrectedJiraProjectKey("commons"));
		assertEquals("COMMONSTEST", AbstractJiraMojo.getCorrectedJiraProjectKey("commons.test"));
		assertEquals("COMMONSTEST", AbstractJiraMojo.getCorrectedJiraProjectKey("commons-test"));
		assertEquals("COMMONSTEST", AbstractJiraMojo.getCorrectedJiraProjectKey("commons1234t3e4s4t2"));
	}

	public class AbstractJiraMojoImpl extends AbstractJiraMojo {

		public void doExecute(JiraSoapService jiraService, String loginToken) throws Exception {
		}
	}
}

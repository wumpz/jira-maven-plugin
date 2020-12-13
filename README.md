# Jira Maven Plugin

First of all. If you dont be sure the plugins configuration is correct, then simply
set jiraSkipCreation to true (look at the example at the bottom). The plugin will
write out to the log what it would have done, but does in fact nothing else.

## Why should I use it?

If you manage your development using Jira then you probably have some kind of synchronization
between your softwares version and the corresponding version in Jira. This plugin helps you to
minimize your configuration overhead. It simply creates new versions in Jira corresponding to
your softwares version and releases this version if you release your software. The same works
even for new Jira projects.

After realizing it is not possible to give components in Jira a different versions in one
project my solution to this problem is to construct a version name out of my software components name and
its version. Therefore I have more open versions in one project beeing developed in parallel.

e.g. versions: Component_A_1.1 Component_B_2.3

Using this plugin I had only to deploy or release my software to maintain my Jira project.
Nice. Isn't it? 
   

## Example Configuration

This is an example configuration. You do not need a special profile, when all versions 
should be created in Jira. 

```xml
<plugin>
	<groupId>org.tw</groupId>
	<artifactId>jira-maven-plugin</artifactId>
	<version>2.0</version>
	<!-- <inherited>false</inherited> -->
	<configuration>
		<settingsXmlKey>jira</settingsXmlKey>
		<projectLeader>tw</projectLeader>
		<jiraURL>http://xxx/jira</jiraURL>
		<jiraSkipCreation>false</jiraSkipCreation>
		<jiraCorrectProjectKey>true</jiraCorrectProjectKey>
		<finalNameUsedForVersion>true</finalNameUsedForVersion>
		<finalName>${project.artifactId} ${project.version}</finalName>
		<reports>
			<!-- this is a standard report -->
			<report />
		</reports>
	</configuration>
	<executions>
		<execution>
			<phase>deploy</phase>
			<goals>
			<!-- <goal>create-new-project</goal>  -->
			<goal>create-new-version</goal>
			<!-- <goal>generate-version-notes</goal> -->
			<goal>release-version</goal>
			</goals>
		</execution>
	</executions>
</plugin>
```

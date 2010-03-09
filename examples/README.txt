Running JBehave examples.

JDK required: 1.5+
Maven (http://maven.apache.org) required: 2.1+

By default, the examples are meant to be run as part of the JBehave build:

svn co https://svn.codehaus.org/jbehave/trunk/core 
cd core
mvn clean install -Pexamples,gui 
(gui includes the non-headless examples)

Alternatively, to run the examples in standalone mode, add to your ~/.m2/settings.xml the following profile:

<profile>
  <id>codehaus-snapshots</id>
  <repositories>
    <repository>
      <id>codehaus-snapshots</id>
      <url>http://snapshots.repository.codehaus.org</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
      <releases>
        <enabled>false</enabled>
      </releases>
    </repository>
  </repositories>
  <pluginRepositories>
    <pluginRepository>
      <id>codehaus-snapshots</id>
      <url>http://snapshots.repository.codehaus.org</url>
      <snapshots>
        <enabled>true</enabled>
      </snapshots>
      <releases>
        <enabled>false</enabled>
      </releases>
    </pluginRepository>
  </pluginRepositories>
</profile>

and build with additional profile from examples:

svn co https://svn.codehaus.org/jbehave/trunk/core/examples
cd examples
mvn clean install -Pcodehaus-snapshots

Using the codehaus-snapshots profile will enable mvn to get the latest snapshot of the parent POM for the examples.

You only need to use the codehaus-snapshots profile once, because once retrieved the parent POM will be cached in your local repo.

 
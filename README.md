[![Build Status](https://travis-ci.org/jbehave/jbehave-core.png)](https://travis-ci.org/jbehave/jbehave-core)

# JBehave

JBehave is a BDD framework for Java and Groovy, mirrored [at Github](https://github.com/jbehave/jbehave-core), definitive repo [at Codehaus](http://xircles.codehaus.org/projects/jbehave).

<img src="http://jbehave.org/reference/preview/images/jbehave-logo.png" alt="JBehave logo" align="right" />

## Using

Canonical information for JBehave:

1. [Web Site](http://jbehave.org).
2. [Stable Reference](http://jbehave.org/reference/stable/).
3. [User mail-list](http://xircles.codehaus.org/lists/user@jbehave.codehaus.org)
4. [Search Maven](http://search.maven.org/#search|ga|1|jbehave)

## Contributing and Developing

Please report issues, feature requests on the Codehaus [JIRA](http://jira.codehaus.org/browse/JBEHAVE) or discuss them on the
[dev mail-list](http://xircles.codehaus.org/lists/dev@jbehave.codehaus.org).

Keep an eye on the  [Travis CI](http://travis-ci.org/jbehave/jbehave-core) server for JBehave builds.

### JDK

JDK version required: 

1.7 or above to build (tested with Oracle JDK on different platforms)

The target runtime version is still 1.5 or above.

### Maven 

[Maven](http://maven.apache.org) version required to build: 3.0 or above.

### Encoding

Configure IDE to use UTF-8 for all files
Configure Maven by adding "-Dfile.encoding=UTF-8" to $MAVEN_OPTS

### IDE Integration

Maven is supported in Intellij IDEA out-of-the-box
Maven is supported in Eclipse via [m2e plugin](http://eclipse.org/m2e), included out-of-the-box in some Eclipse distributions.
Eclipse users may also want to load the ides/eclipse/lifecycle-mapping-metadata.xml or ignore the m2e lifecycle mappings manually.

### Building

The first time you run the Maven build, do:

    mvn install -s settings.xml

After that, it is necessary to only do the following:

    mvn install

### Maven Build Profiles

- default: builds all releasable modules
- examples: builds all headless examples
- gui: builds examples that require a GUI (i.e. non-headless) mode (separated as they do not run on CI.
- nt: no-test, builds skipping unit-test behaviors

#### Maven Build Profiles used during release cycle

- reporting: builds reports
- distribution: builds distribution (documentation)

Note:  profiles are additive and the default profile is always active.

### Example Profile Usages

#### Build Core and all Examples

    mvn install -Pexamples

#### Build with Reporting and Distribution

    mvn install -Preporting,distribution

#### Building a Release with Maven

    mvn release:prepare -Preporting,distribution
    mvn release:perform -Preporting,distribution

## Related JBehave projects

See also: 

- [jbehave-web](jbehave-web) web extensions to JBehave
- [jbehave-tutorial](jbehave-tutorial) for an example of JBehave testing of a real web application.

## License

See LICENSE.txt in the source root (BSD).

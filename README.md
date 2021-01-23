[![Build Status](https://github.com/jbehave/jbehave-core/workflows/JBehave%20CI/badge.svg)](https://github.com/jbehave/jbehave-core/actions?query=workflow%3A%22JBehave+CI%22)


# JBehave

JBehave is a BDD framework for Java and all JVM languages (Groovy, Kotlin, Ruby, Scala).

<img src="http://jbehave.org/reference/preview/images/jbehave-logo.png" alt="JBehave logo" align="right" />

## Using

Canonical information for JBehave:

1. [Web Site](http://jbehave.org).
2. [Stable Reference](http://jbehave.org/reference/stable/).
3. [User mailing list](http://jbehave.org/mailing-lists.html)
4. [Search Maven](http://search.maven.org/#search|ga|1|jbehave)

## Contributing and Developing

Please report issues, feature requests on [JIRA](http://jbehave.org/issue-tracking.html) or discuss them on the
[dev mailing list](http://jbehave.org/mailing-lists.html).

Keep an eye on the  [Github Actions](https://github.com/jbehave/jbehave-core/actions?query=workflow%3A%22JBehave+CI%22) server for JBehave builds.

### JDK Version

At compile-time, JDK 11 is required to build JBehave modules

At runtime, end users can use JDK 8 or above in their own projects that use JBehave for testing.

### Maven 

[Maven](http://maven.apache.org) version required to build: 3.6 or above 
(while tested with 3.6.x it may also work with previous 3.x versions)

Some additional setup may be required to build the 

### Encoding

Configure IDE to use UTF-8 for all files
Configure Maven by adding "-Dfile.encoding=UTF-8" to $MAVEN_OPTS

### IDE Integration

Maven is supported in all major IDEs, including Intellij IDEA and Eclipse.

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

- [jbehave-pom](https://github.com/jbehave/jbehave-pom) JBehave POM
- [jbehave-web](https://github.com/jbehave/jbehave-web) web extensions to JBehave
- [jbehave-osgi](https://github.com/jbehave/jbehave-osgi) OSGi extensions to JBehave
- [jbehave-eclipse](https://github.com/jbehave/jbehave-eclipse) Eclipse integration for JBehave
- [jbehave-tutorial](https://github.com/jbehave/jbehave-tutorial) for an example of JBehave testing of a real web application.

## License

See LICENSE.txt in the source root (BSD).

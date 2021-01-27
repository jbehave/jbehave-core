Running JBehave examples.

JDK required: 11+
Maven (http://maven.apache.org) required: 3.6+

By default, the examples are meant to be run as part of the JBehave build, as the examples use the latest version of the JBehave code:

git clone git://github.com/jbehave/jbehave-core.git
cd jbehave-core
mvn clean install -Pexamples

Alternatively, to run the examples in standalone mode, i.e. using the latest snaphots published:
 
cd jbehave-core/examples
mvn clean install -Psnapshots -s ../settings.xml

Using the snapshots profile will enable mvn to get the latest snapshot of the parent POM for the examples.

You only need to use the snapshots profile once.

Once you've cached the parent POM, you can run the examples using a fixed version of JBehave:

mvn clean install -Djbehave.version=5.0

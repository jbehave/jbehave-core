<#ftl strip_whitespace=true>
<?xml version="1.0" encoding="UTF-8"?>
<testsuite xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report.xsd"
          name="${testsuite.name}" time="${testsuite.time}"
          tests="${testsuite.tests}" errors="${testsuite.errors}" skipped="${testsuite.skipped}" failures="${testsuite.failures}">
<properties>
<#assign properties=testsuite.getProperties()>
<#list properties.keySet() as name>
<#assign value = properties.get(name)>
<property name="${name?xml}" value="${value?xml}"/>
</#list>
</properties>
<#list testsuite.getTestCases() as testcase>
<testcase name="${testcase.name}" classname="${testcase.classname}" time="${testcase.time}"><#if testcase.hasFailure()><#assign failure = testcase.getFailure()><failure message="${failure.message}" type="${failure.type}">${failure.stackTrace}</failure></#if></testcase>
</#list>
</testsuite>
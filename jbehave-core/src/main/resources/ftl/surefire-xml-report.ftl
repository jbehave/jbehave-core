<#ftl strip_whitespace=true>
<#macro renderTime time><#if time != 0><#assign asSecs=time/1000>${asSecs?string['0.###']}<#else>${time}</#if></#macro>
<?xml version="1.0" encoding="UTF-8"?>
<testsuite xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:noNamespaceSchemaLocation="https://maven.apache.org/surefire/maven-surefire-plugin/xsd/surefire-test-report.xsd"
          name="${testsuite.name}" time="<@renderTime testsuite.getTime()/>"
          tests="${testsuite.tests}" errors="${testsuite.errors}" skipped="${testsuite.skipped}" failures="${testsuite.failures}">
<properties>
<#assign properties=testsuite.getProperties()>
<#list properties.keySet() as name>
<#assign value = properties.get(name)>
<property name="${name?xml}" value="${value?xml}"/>
</#list>
</properties>
<#list testsuite.getTestCases() as testcase>
<testcase name="${testcase.name}" classname="${testcase.classname}" time="<@renderTime testcase.getTime()/>"><#if testcase.hasFailure()><#assign failure = testcase.getFailure()><failure message="${failure.message}" type="${failure.type}">${failure.stackTrace}</failure></#if></testcase>
</#list>
</testsuite>
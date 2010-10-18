<#ftl strip_whitespace=true>
<#macro renderStat stats name class=""><#assign value = stats.get(name)!0><#if (value != 0)><span class="${class}">${value}</span><#else>${value}</#if></#macro>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
        "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html>
<head>
<title>JBehave Reports</title>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1" />
<style type="text/css" media="all">
@import url( "./style/jbehave-reports.css" );
</style>
</head>

<body>
<div id="banner"><img src="images/jbehave-logo.png" alt="jbehave" />
<div class="clear"></div>
</div>

<div class="reports">

<h2>Story Reports</h2>

<table>
<colgroup class="story"></colgroup>
<colgroup span="3" class="scenarios"></colgroup>
<colgroup span="6" class="steps"></colgroup>
<colgroup class="view"></colgroup>
<tr>
    <th>Story</th>
    <th colspan="3">Scenarios</th>
    <th colspan="6">Steps</th>
    <th>View</th>
</tr>
<tr>
    <th></th>
    <th>Total</th>
    <th>Successful</th>
    <th>Failed</th>
    <th>Total</th>
    <th>Successful</th>
    <th>Pending</th>
    <th>Not Performed</th>
    <th>Failed</th>
    <th>Ignored</th>
    <th></th>
</tr>
<#assign reportNames = reportsTable.getReportNames()>
<#list reportNames as name>
<#assign report = reportsTable.getReport(name)>
<#if name != "Totals">
<tr>
<td class="story">${report.name}</td>
<#assign stats = report.getStats()>
<td>
<@renderStat stats "scenarios"/> 
</td>
<td>
<@renderStat stats "scenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "scenariosFailed" "failed"/>
</td>
<td>
<@renderStat stats "steps" />
</td>
<td>
<@renderStat stats "stepsSuccessful" "successful"/>
</td>
<td>
<@renderStat stats "stepsPending" "pending"/>
</td>
<td>
<@renderStat stats "stepsNotPerformed" "notPerformed" />
</td>
<td>
<@renderStat stats "stepsFailed" "failed"/>
</td>
<td>
<@renderStat stats "stepsIgnorable" "ignorable"/>
</td>
<td>
<#assign filesByFormat = report.filesByFormat>
<#list filesByFormat.keySet() as format><#assign file = filesByFormat.get(format)><a href="${file.name}">${format}</a><#if format_has_next>|</#if></#list>
</td>
</tr>
</#if>
</#list>
<tr class="totals">
<td>Totals</td>
<#assign stats = reportsTable.getReport("Totals").getStats()>
<td>
<@renderStat stats "scenarios"/> 
</td>
<td>
<@renderStat stats "scenariosSuccessful" "successful"/> 
</td>
<td>
<@renderStat stats "scenariosFailed" "failed"/>
</td>
<td>
<@renderStat stats "steps" />
</td>
<td>
<@renderStat stats "stepsSuccessful" "successful"/>
</td>
<td>
<@renderStat stats "stepsPending" "pending"/>
</td>
<td>
<@renderStat stats "stepsNotPerformed" "notPerformed" />
</td>
<td>
<@renderStat stats "stepsFailed" "failed"/>
</td>
<td>
<@renderStat stats "stepsIgnorable" "ignorable"/>
</td>
<td>
</td>
</tr>
</table>
<br />
</div>

<div class="clear"></div>
<div id="footer">
<div class="left">Generated at ${date?string("dd/MM/yyyy HH:mm:ss")}</div>
<div class="right">JBehave &#169; 2003-2010</div>
<div class="clear"></div>
</div>

</body>

</html>

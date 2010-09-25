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
<tr>
    <th>Story</th>
    <th>Scenarios</th>
    <th>Failed Scenarios</th>
    <th>Steps</th>
    <th>Successful</th>
    <th>Pending</th>
    <th>Not Performed</th>
    <th>Failed</th>
    <th>Ignorable</th>
    <th>View</th>
</tr>
<#list reports as report>
<#assign filesByFormat = report.filesByFormat>
<tr>
<td class="story">${report.name}</td>
<#assign stats = report.getStats()>
<td>
<@renderStat stats "scenarios" "successful"/> 
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
<td><#list filesByFormat.keySet() as format><#assign file = filesByFormat.get(format)><a href="${file.name}">${format}</a><#if format_has_next>|</#if></#list></td>
</tr>
</#list>
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

<!--  SyntaxHighlighter resources:  should be included at end of body -->
<link rel="stylesheet" type="text/css" href="./style/sh-2.1.364/shCore.css"/>
<link rel="stylesheet" type="text/css" href="./style/sh-2.1.364/shThemeRDark.css"/>
<script language="javascript" src="./js/sh-2.1.364/shCore.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushBash.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushCss.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushDiff.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushGroovy.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushJava.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushJScript.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushPlain.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushPython.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushRuby.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushScala.js"></script>
<script language="javascript" src="./js/sh-2.1.364/shBrushXml.js"></script>
<script language="javascript" src="./js/shBrushBdd.js"></script>
<script type="text/javascript">
	SyntaxHighlighter.config.clipboardSwf = './js/sh-2.1.364/clipboard.swf';
    SyntaxHighlighter.defaults['gutter'] = false;
    SyntaxHighlighter.defaults['toolbar'] = true;    
	SyntaxHighlighter.all();
</script>

</html>

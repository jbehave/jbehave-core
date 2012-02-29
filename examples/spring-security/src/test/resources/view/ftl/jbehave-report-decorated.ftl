<#ftl strip_whitespace=true>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
<head>
  <title>${name}</title>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
  <link rel="stylesheet" href="styles/jbehave-reports.css" type="text/css" />
  <link rel="stylesheet" href="styles/shCore.css" type="text/css" />
  <link rel="stylesheet" href="styles/shThemeDefault.css" type="text/css" />
  <script src="scripts/shCore.js" type="text/javascript"></script>
  <script src="scripts/shBrushXml.js" type="text/javascript"></script>
  <script src="scripts/shBrushPlain.js" type="text/javascript"></script>
</head>
<body>
<#if format == "html">
${body}
<#else>
<#assign brushFormat = format> 
<#if format == "stats"><#assign brushFormat = "plain"> </#if>
<#if format == "txt"><#assign brushFormat = "text"> </#if>
<script type="syntaxhighlighter" class="brush: ${brushFormat}"><![CDATA[
${body}
]]></script>
</#if>
<script type="text/javascript">
  SyntaxHighlighter.all()
</script>
</body>
</html>

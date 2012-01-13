<#ftl strip_whitespace=true>
<html>
<head>
<title>${name}</title>
<style type="text/css" media="all">
@import url( "./style/jbehave-core.css" );
</style>
</head>
<body>
<#if format == "html">
${body}
<#else>
<#assign brushFormat = format> <#if format == "stats"><#assign brushFormat = "plain"> </#if>
<script type="syntaxhighlighter" class="brush: ${brushFormat}"><#if format != "txt" || !body.contains("</script>")><![CDATA[
${body}
]]><#else>
${body?html}
</#if></script>
</#if>
</body>
<#include "./sh.ftl">
</html>

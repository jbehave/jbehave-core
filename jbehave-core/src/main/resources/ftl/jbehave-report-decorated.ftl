<#ftl strip_whitespace=true>
<html>
<head>
<title>${name}</title>
<style type="text/css" media="all">
@import url( "./style/jbehave-reports.css" );
</style>
</head>
<body>
<#if format == "html">
${body}
<#else>
<#assign brushFormat = format> <#if format == "stats"><#assign brushFormat = "plain"> </#if>
<script type="syntaxhighlighter" class="brush: ${brushFormat}"><![CDATA[
${body}
]]></script>
</#if>
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
<script language="javascript" src="./js/shBrushTxt.js"></script>
<script type="text/javascript">
	SyntaxHighlighter.config.clipboardSwf = './js/sh-2.1.364/clipboard.swf';
    SyntaxHighlighter.defaults['gutter'] = false;
    SyntaxHighlighter.defaults['toolbar'] = true;    
	SyntaxHighlighter.all();
</script>
</html>

/**
 * SyntaxHighlighter brush for txt reports
 * 
 * Adapted from shBrushJava.
 */
SyntaxHighlighter.brushes.Txt = function()
{
	var keywords =	'Narrative Given When Then And Scenario GivenScenarios Examples Example PENDING NOT PERFORMED FAILED';
	
	this.regexList = [
		{ regex: new RegExp(this.getKeywords(keywords), 'gm'),		css: 'keyword' }		// txt keyword
		];

	this.forHtmlScript(SyntaxHighlighter.regexLib.aspScriptTags);
};

SyntaxHighlighter.brushes.Txt.prototype	= new SyntaxHighlighter.Highlighter();
SyntaxHighlighter.brushes.Txt.aliases		= ['txt'];

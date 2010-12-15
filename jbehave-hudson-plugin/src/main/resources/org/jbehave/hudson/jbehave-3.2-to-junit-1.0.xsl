<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="xml" indent="yes" />
	
	<xsl:variable name="storyTitle">
	    	<xsl:choose>
	    		<xsl:when test="string(//story/@title)">
	    			<xsl:value-of select="//story/@title" />
	    		</xsl:when>
	    		<xsl:otherwise>
	    			<xsl:value-of select="//story/@path" />
	    		</xsl:otherwise>
	    	</xsl:choose>
	 </xsl:variable>
	
	<xsl:template match="/">
		<xsl:call-template name="scenario">
			<xsl:with-param name="failures" select="0"/>
			<xsl:with-param name="ignores" select="0"/>
			<xsl:with-param name="totalTests" select="0"/>
			<xsl:with-param name="element" select="//scenario"/>
		</xsl:call-template>
	</xsl:template>
	
	<xsl:template match="step[@outcome='failed']">
		<failure message="{text()}"><xsl:value-of select="//failure"/></failure>
	</xsl:template>
	
	<xsl:template match="step[@outcome='pending']">
		<skipped message="{@keyword}: {text()}"/>
	</xsl:template>
	
	<xsl:template name="scenario">
		<xsl:param name="failures"/>
		<xsl:param name="ignores"/>
		<xsl:param name="totalTests"/>
		<xsl:param name="element"/>
			
		<xsl:choose>
			<xsl:when test="boolean($element/examples)">
				 <xsl:call-template name="examples">
					<xsl:with-param name="failures" select="$failures"/>
					<xsl:with-param name="ignores" select="$ignores"/>
					<xsl:with-param name="totalTests" select="$totalTests"/>
					<xsl:with-param name="element" select="$element/examples[1]"/>
				</xsl:call-template>				 
			</xsl:when>			
			<xsl:otherwise>
				<!-- calculate failures and pending (ignores) -->
				<xsl:variable name="numberOfFails" select="$failures + count($element[boolean(step/@outcome='failed')])" />
				<xsl:variable name="numberSkipped" select="$ignores + count($element[boolean(step/@outcome[.='pending']) and not(step/@outcome='failed')])" />
				
				<xsl:choose>
					<xsl:when test="boolean(count($element/following-sibling::scenario) > 0)">				
						<xsl:call-template name="scenario">
							<xsl:with-param name="failures" select="$numberOfFails"/>
							<xsl:with-param name="ignores" select="$numberSkipped"/>
							<xsl:with-param name="totalTests" select="$totalTests + 1"/>
							<xsl:with-param name="element" select="$element/following-sibling::scenario[0]"/>
						</xsl:call-template>				 
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="reportBody">
							<xsl:with-param name="numberOfFails" select="$numberOfFails"/>
							<xsl:with-param name="numberSkipped" select="$numberSkipped"/>
							<xsl:with-param name="testCount" select="$totalTests + 1"/>
							<xsl:with-param name="element" select="$element"/>
						</xsl:call-template>
					</xsl:otherwise>
				</xsl:choose>				
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="examples">
		<xsl:param name="failures"/>
		<xsl:param name="ignores"/>
		<xsl:param name="totalTests"/>
		<xsl:param name="element"/>
			
		<xsl:choose>
			<xsl:when test="boolean(count($element[1]/example) > 0)">
				 <xsl:call-template name="example">
					<xsl:with-param name="failures" select="$failures"/>
					<xsl:with-param name="ignores" select="$ignores"/>
					<xsl:with-param name="totalTests" select="$totalTests"/>
					<xsl:with-param name="element" select="$element[1]/example"/>
				</xsl:call-template>				 
			</xsl:when>
			<xsl:when test="boolean(count($element/ancestor::scenario/following-sibling::scenario) > 0)">
				<xsl:call-template name="scenario">
					<xsl:with-param name="failures" select="$failures"/>
					<xsl:with-param name="ignores" select="$ignores"/>
					<xsl:with-param name="totalTests" select="$totalTests"/>
					<xsl:with-param name="element" select="$element[1]/ancestor::scenario/following-sibling::scenario[0]"/>
				</xsl:call-template>				 
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="reportBody">
					<xsl:with-param name="numberOfFails" select="$failures"/>
					<xsl:with-param name="numberSkipped" select="$ignores"/>
					<xsl:with-param name="testCount" select="$totalTests"/>
					<xsl:with-param name="element" select="$element"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="example">
		<xsl:param name="failures"/>
		<xsl:param name="ignores"/>
		<xsl:param name="totalTests"/>
		<xsl:param name="element"/>
		
		<xsl:variable name="testCount" select="$totalTests + 1"/>
		<!-- calculate failures and pending (ignores) -->
		
		<xsl:variable name="numberOfFails">
			<xsl:choose>
				<xsl:when test="boolean($element/following-sibling::step[@outcome='failed'][generate-id(preceding-sibling::example[1]) = generate-id($element) ])">
					<xsl:value-of select="$failures + 1"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$failures"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		<xsl:variable name="numberSkipped">
			<xsl:choose>
				<xsl:when test="boolean($element/following-sibling::step[@outcome='failed'][generate-id(preceding-sibling::example[1]) = generate-id($element) ])">
					<xsl:value-of select="$ignores"/>
				</xsl:when>
				<xsl:when test="boolean($element/following-sibling::step[@outcome='pending'][generate-id(preceding-sibling::example[1]) = generate-id($element) ])">
					<xsl:value-of select="$ignores + 1"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$ignores"/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:variable>
		
		
		<xsl:choose>
			<xsl:when test="boolean(count($element/following-sibling::example) > 0)">
				 <xsl:call-template name="example">
					<xsl:with-param name="failures" select="$numberOfFails"/>
					<xsl:with-param name="ignores" select="$numberSkipped"/>
					<xsl:with-param name="totalTests" select="$testCount"/>
					<xsl:with-param name="element" select="$element[1]/following-sibling::example"/>
				</xsl:call-template>				 
			</xsl:when>
			<xsl:when test="boolean(count($element/ancestor::scenario/following-sibling::scenario) > 0)">
				<xsl:call-template name="scenario">
					<xsl:with-param name="failures" select="$numberOfFails"/>
					<xsl:with-param name="ignores" select="$numberSkipped"/>
					<xsl:with-param name="totalTests" select="$testCount"/>
					<xsl:with-param name="element" select="$element/ancestor::scenario/following-sibling::scenario"/>
				</xsl:call-template>				 
			</xsl:when>
			<xsl:otherwise>
				<xsl:call-template name="reportBody">
					<xsl:with-param name="numberOfFails" select="$numberOfFails"/>
					<xsl:with-param name="numberSkipped" select="$numberSkipped"/>
					<xsl:with-param name="testCount" select="$testCount"/>
					<xsl:with-param name="element" select="$element"/>
				</xsl:call-template>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	<xsl:template name="reportBody">
		<xsl:param name="numberOfFails"/>
		<xsl:param name="numberSkipped"/>
		<xsl:param name="testCount"/>
		<xsl:param name="element"/>
		
		<testsuite name="{$storyTitle}" tests="{$testCount}" time="0" failures="{$numberOfFails}" errors="0" skipped="{$numberSkipped}">
			
			<xsl:for-each select="//scenario[not(ancestor::scenario)]">
			<xsl:variable name="scenarioName" select="@title" />
			<xsl:choose>
				<xsl:when test="boolean(child::examples)">
					<xsl:for-each select="child::examples/example">
					<xsl:variable name="parameters" select="." />
						<xsl:choose>
							<xsl:when test="boolean(following-sibling::step[@outcome='failed'][generate-id(preceding-sibling::example[1]) = generate-id(current()) ])">
								<testcase name="{$scenarioName} {$parameters}">
									<xsl:apply-templates select="following::step[@outcome='failed'][1]"/>		
								</testcase>
							</xsl:when>
							<xsl:when test="boolean(following-sibling::step[@outcome='pending'][generate-id(preceding-sibling::example[1]) = generate-id(current()) ])">
								<testcase name="{$scenarioName} {$parameters}">
									<xsl:apply-templates select="following::step[@outcome='pending'][1]"/>
								</testcase>
							</xsl:when>
							<xsl:otherwise>
								<testcase name="{$scenarioName} {$parameters}"/>
							</xsl:otherwise>
						</xsl:choose>
						
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<testcase name="{$scenarioName}">
					<xsl:choose>
						<xsl:when test="boolean(descendant::step[@outcome='failed'])">
							<xsl:apply-templates select="descendant::step[@outcome='failed']"/>
						</xsl:when>
						<xsl:when test="boolean(descendant::step[@outcome='pending'])">
							<xsl:apply-templates select="descendant::step[@outcome='pending']"/>
						</xsl:when>
					</xsl:choose>
					</testcase>			
				</xsl:otherwise>
			</xsl:choose>			
		</xsl:for-each>
		</testsuite>
	</xsl:template>
</xsl:stylesheet>
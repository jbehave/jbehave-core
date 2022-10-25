<#ftl strip_whitespace=true>
<#macro renderMultiline text>${text?replace("\n", "<br/>")}</#macro>
<#macro renderMeta meta>
<meta>
<#assign metaProperties=meta.getProperties()>
<#list metaProperties.keySet() as name>
<#assign property = metaProperties.get(name)>
<property keyword="${keywords.metaProperty}" name="${name?xml}" value="${property?xml}"/>
</#list>
</meta>
</#macro>
<#macro renderNarrative narrative>
<narrative keyword="${keywords.narrative}">
<#assign isAlternative=narrative.isAlternative()?string>
<#if isAlternative == "true">
<asA keyword="${keywords.asA}">${narrative.asA}</asA>
<iWantTo keyword="${keywords.iWantTo}">${narrative.iWantTo}</iWantTo>
<soThat keyword="${keywords.soThat}">${narrative.soThat}</soThat>
<#else>
<inOrderTo keyword="${keywords.inOrderTo}">${narrative.inOrderTo}</inOrderTo>
<asA keyword="${keywords.asA}">${narrative.asA}</asA>
<iWantTo keyword="${keywords.iWantTo}">${narrative.iWantTo}</iWantTo>
</#if>
</narrative>
</#macro>
<#macro renderGivenStories givenStories>
<givenStories keyword="${keywords.givenStories}">
<#list givenStories.getStories() as givenStory>
<givenStory<#if givenStory.hasAnchor()> parameters="${givenStory.parameters}"</#if>>${givenStory.path}</givenStory>
</#list>
</givenStories>
</#macro>
<#macro renderScope scope><#if scope == 'SCENARIO'>${keywords.scopeScenario}<#elseif scope == 'STORY'>${keywords.scopeStory}</#if></#macro>
<#macro renderLifecycle lifecycle>
<lifecycle keyword="${keywords.lifecycle}">
<#if lifecycle.hasBeforeSteps()>
<before keyword="${keywords.before}">
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getBeforeSteps(scope)>
<#if !stepsByScope.isEmpty()>
<scope keyword="${keywords.scope}" value="<@renderScope scope/>">
<#list stepsByScope as step>
<step>${step?xml}</step>
</#list>
</scope>
</#if>
</#list>
</before>
</#if>
<#if lifecycle.hasAfterSteps()>
<after keyword="${keywords.after}">
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getAfterSteps(scope)>
<#if !stepsByScope.isEmpty()>
<scope keyword="${keywords.scope}" value="<@renderScope scope/>">
<#list lifecycle.getOutcomes() as outcome>
<outcome keyword="${keywords.outcome}" value="${outcome}">
<#assign metaFilter=lifecycle.getMetaFilter(outcome)>
<#if !metaFilter.isEmpty()><#assign metaFilterAsString=metaFilter.asString()><metaFilter keyword="${keywords.metaFilter}">${metaFilterAsString}</metaFilter></#if>
<#list lifecycle.getAfterSteps(scope, outcome) as step>
<step>${step?xml}</step>
</#list>
</outcome>
</#list><!-- outcome -->
</scope>
</#if>
</#list><!-- scope -->
</after>
</#if>
</lifecycle>
</#macro>
<#macro renderTable table>
<#assign rows=table.getRows()>
<#assign headers=table.getHeaders()>
<table>
<headers>
<#list headers as header>
<header>${header?xml}</header>
</#list>
</headers>
<#list rows as row>
<row>
<#list headers as header>
<#assign cell=row.get(header)>
<value>${cell?xml}</value>
</#list>
</row>
</#list>
</table>
</#macro>
<#macro renderVerbatim verbatim>
<#assign content=verbatim.getContent()>
<verbatim>${content?xml}</verbatim>
</#macro>
<#macro renderOutcomes table>
<#assign outcomes=table.getOutcomes()>
<#assign fields=table.getOutcomeFields()>
<outcomes>
<fields>
<#list fields as field>
<field>${field?xml}</field>
</#list>
</fields>
<#list outcomes as outcome>
<#assign isVerified=outcome.isVerified()?string>
<#if isVerified == "true"> <#assign verified="verified"><#else><#assign verified="notVerified"></#if>
<outcome>
<value>${outcome.description?xml}</value><value><@renderOutcomeValue outcome.getValue() table/></value><value>${outcome.matcher?xml}</value><value><#if isVerified == "true">${keywords.yes}<#else>${keywords.no}</#if></value>
</outcome>
</#list>
</outcomes>
</#macro>
<#macro renderOutcomeValue value table><#if value?is_date><#assign format=table.getFormat('java.util.Date')>${value?string(format)}<#elseif value?is_number><#assign format=table.getFormat('java.lang.Number')><#setting number_format="${format}">${value?c}<#elseif value?is_boolean><#assign format=table.getFormat('java.lang.Boolean')><#setting boolean_format="${format}">${value?c}<#else>${value?xml}</#if></#macro>
<#macro renderStep step>
<#assign formattedStep = step.getFormattedStep(EscapeMode.XML, "<parameter>{0}</parameter>")>
<step outcome="${step.outcome}">
${formattedStep}<#if step.getTable()??> <parameter><@renderTable step.getTable()/></parameter></#if><#if step.getVerbatim()??> <parameter><@renderVerbatim step.getVerbatim()/></parameter></#if>
<#if step.getFailure()??> <failure>${step.failureCause?xml}</failure></#if><#if step.getPendingMethod()??> <pendingMethod>${step.pendingMethod?xml}</pendingMethod></#if><#if step.getOutcomes()??><@renderOutcomes step.getOutcomes()/></#if></step>
</#macro>

<story path="${story.path}" title="${story.description?xml}">
<#if story.getMeta()??><@renderMeta story.getMeta()/></#if>
<#if story.getNarrative()??><@renderNarrative story.getNarrative()/></#if>
<#if story.getLifecycle()??><@renderLifecycle story.getLifecycle()/></#if>
<#if !story.getBeforeSteps().isEmpty()>
<before keyword="${keywords.before}">
<#list story.getBeforeSteps() as step>
<@renderStep step/>
</#list>
</before>
</#if>
<#assign scenarios = story.getScenarios()>
<#list scenarios as scenario>
<scenario keyword="${keywords.scenario}" title="${scenario.title?xml}">
<#if scenario.getMeta()??><@renderMeta scenario.getMeta()/></#if>
<#if scenario.getGivenStories()??><@renderGivenStories scenario.getGivenStories()/></#if>
<#if scenario.getExamplesTable()??>
<examples keyword="${keywords.examplesTable}">
<#list scenario.getExamplesSteps() as step>
<step>${step?xml}</step>
</#list>
<@renderTable scenario.getExamplesTable()/>
</examples>
<#if scenario.getExamples()??>
<#list scenario.getExamples() as example>
<example keyword="${keywords.examplesTableRow}">${example?xml}</example>
<#assign steps = scenario.getStepsByExample(example)>
<#list steps as step>
<@renderStep step/>
</#list>
</#list>
</#if>
<#else> 
<#assign steps = scenario.getSteps()>
<#list steps as step>
<@renderStep step/>
</#list>
</#if>
</scenario>
</#list>
<#if !story.getAfterSteps().isEmpty()>
<after keyword="${keywords.after}">
<#list story.getAfterSteps() as step>
<@renderStep step/>
</#list>
</after>
</#if>
<#if story.isCancelled()?string == 'true'>
<cancelled keyword="${keywords.storyCancelled}" durationKeyword="${keywords.duration}" durationInSecs="${story.storyDuration.durationInSecs}"/>
</#if>
</story>


<#ftl strip_whitespace=true>
<#macro renderMeta meta>
"meta":<#assign metaProperties=meta.getProperties()>
[<#list metaProperties.keySet() as name><#assign property = metaProperties.get(name)>
{"keyword": "${keywords.metaProperty}", "name": "${name?xml}", "value": "${property?xml}"}<#if name_has_next>,</#if>
</#list>]
</#macro>
<#macro renderNarrative narrative>
"narrative": {
"keyword": "${keywords.narrative}",
<#assign isAlternative=narrative.isAlternative()?string>
<#if isAlternative == "true">
"asA":  {"keyword": "${keywords.asA}", "value": "${narrative.asA}"},
"iWantTo":  {"keyword": "${keywords.iWantTo}", "value": "${narrative.iWantTo}"},
"soThat":  {"keyword": "${keywords.soThat}", "value": "${narrative.soThat}"}
<#else>
"inOrderTo":  {"keyword": "${keywords.inOrderTo}", "value": "${narrative.inOrderTo}"},
"asA":  {"keyword": "${keywords.asA}", "value": "${narrative.asA}"},
"iWantTo":  {"keyword": "${keywords.iWantTo}", "value": "${narrative.iWantTo}"}
</#if>
}
</#macro>
<#macro renderGivenStories givenStories>
"givenStories": {
"keyword": "${keywords.givenStories}",
"givenStories": [
<#list givenStories.getStories() as givenStory>
{<#if givenStory.hasAnchor()> "parameters": "${givenStory.parameters}", </#if> "path": "${givenStory.path}"}<#if givenStory_has_next>,</#if>
</#list>
]
}
</#macro>
<#macro renderScope scope><#if scope == 'SCENARIO'>${keywords.scopeScenario}<#elseif scope == 'STORY'>${keywords.scopeStory}</#if></#macro>
<#macro renderLifecycle lifecycle>
"lifecycle": { "keyword": "${keywords.lifecycle}"
<#if lifecycle.hasBeforeSteps()>
,"before": {
"keyword": "${keywords.before}", "scopes": [
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getBeforeSteps(scope)>
<#if !stepsByScope.isEmpty()>{"keyword": "${keywords.scope}", "value": "<@renderScope scope/>", "steps": [
<#list stepsByScope as step>
"${step?json_string}"<#if step_has_next>, </#if>
</#list>
]}<#if scope_has_next>,</#if>
</#if>
</#list>
]}
</#if>
<#if lifecycle.hasAfterSteps()>
,"after": {
"keyword": "${keywords.after}", "scopes": [
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getAfterSteps(scope)>
<#if !stepsByScope.isEmpty()>{ "keyword": "${keywords.scope}", "value": "<@renderScope scope/>", "outcomes": [
<#list lifecycle.getOutcomes() as outcome>
{ "keyword": "${keywords.outcome}", "value": "${outcome}",
<#assign metaFilter=lifecycle.getMetaFilter(outcome)>
<#if !metaFilter.isEmpty()><#assign metaFilterAsString=metaFilter.asString()>"metaFilter": "keyword": "${keywords.metaFilter}", "value": "${metaFilterAsString}"</#if>
"steps": [
<#list lifecycle.getAfterSteps(scope, outcome) as step>
"${step?json_string}"<#if step_has_next>, </#if>
</#list>
]}<#if outcome_has_next>, </#if>
</#list>
]}<#if scope_has_next>,</#if>
</#if>
</#list>
]}
</#if>
}
</#macro>
<#macro renderTable table>
<#assign rows=table.getRows()>
<#assign headers=table.getHeaders()>
{
"names":
[
<#list headers as header>
"${header?json_string}"<#if header_has_next>,</#if>
</#list>
]
,"values":
[
<#list rows as row>
[<#list headers as header><#assign cell=row.get(header)>"${cell?json_string}"<#if header_has_next>,</#if></#list>]<#if row_has_next>,</#if>
</#list>
]
}
</#macro>
<#macro renderOutcomes table>
<#assign outcomes=table.getOutcomes()>
<#assign fields=table.getOutcomeFields()>
"outcomes": {
"fields":
[
<#list fields as field>
"${field?json_string}"<#if field_has_next>,</#if>
</#list>
],
"outcomes": [
<#list outcomes as outcome>
<#assign isVerified=outcome.isVerified()?string>
<#if isVerified == "true"> <#assign verified="verified"><#else><#assign verified="notVerified"></#if>
{"description": "${outcome.description?json_string}", "value": "<@renderOutcomeValue outcome.getValue() table.getDateFormat()/>", "matcher": "${outcome.matcher?json_string}", "verified": "<#if isVerified == 'true'>${keywords.yes}<#else>${keywords.no}</#if>"}<#if outcome_has_next>,</#if>
</#list>
]
}
</#macro>
<#macro renderVerbatim verbatim>
"verbatim": {
"content": "${verbatim.content}"
}
</#macro>
<#macro renderOutcomeValue value dateFormat><#if value?is_date>${value?string(dateFormat)}<#else>${value?json_string}</#if></#macro>
<#macro renderStep step><#assign formattedStep = step.getFormattedStep(EscapeMode.JSON, "(({0}))")>
{"outcome": "${step.outcome}", "step": "${formattedStep}"<#if step.getTable()??>, "parameter": <@renderTable step.getTable()/></#if><#if step.getVerbatim()??>, "parameter": <@renderVerbatim step.getVerbatim()/></#if><#if step.getFailure()??>, "failure": "${step.failureCause?json_string}"</#if><#if step.getOutcomes()??>,<@renderOutcomes step.getOutcomes()/></#if>}
</#macro>
{
"path": "${story.path}",
"title": "${story.description?json_string}"
<#if story.getMeta()??>,<@renderMeta story.getMeta()/></#if>
<#if story.getNarrative()??>, <@renderNarrative story.getNarrative()/></#if>
<#if story.getLifecycle()??>, <@renderLifecycle story.getLifecycle()/></#if>
<#if !story.getBeforeSteps().isEmpty()>
,"before": {"keyword": "${keywords.before}", "steps":
[<#list story.getBeforeSteps() as step>
<@renderStep step/><#if step_has_next>,</#if>
</#list>
]}
</#if>
<#assign scenarios = story.getScenarios()>
,"scenarios": [
<#list scenarios as scenario>
{"keyword": "${keywords.scenario}", "title": "${scenario.title?json_string}"
<#if scenario.getMeta()??>, <@renderMeta scenario.getMeta()/></#if>
<#if scenario.getGivenStories()??>, <@renderGivenStories scenario.getGivenStories()/></#if>
<#if scenario.getExamplesTable()??>, "examples": {"keyword": "${keywords.examplesTable}",
<#list scenario.getExamplesSteps() as step>
"step": "${step?json_string}"<#if step_has_next>,</#if>
</#list>, "parameters": <@renderTable scenario.getExamplesTable()/>
}
<#if scenario.getExamples()??>, "examples": [
<#list scenario.getExamples() as example>
{"keyword": "${keywords.examplesTableRow}", "value": "${example?json_string}"
<#assign steps = scenario.getStepsByExample(example)>
,"steps": [
<#list steps as step>
<@renderStep step/><#if step_has_next>,</#if>
</#list>
]
}<#if example_has_next>,</#if>
</#list>
]
</#if>
<#else>
<#assign steps = scenario.getSteps()>,"steps": [
<#list steps as step>
<@renderStep step/><#if step_has_next>,</#if>
</#list>
]
</#if>
}<#if scenario_has_next>,</#if>
</#list>
]
<#if !story.getAfterSteps().isEmpty()>
,"after": {"keyword": "${keywords.after}", "steps":
<#list story.getAfterSteps() as step>
<@renderStep step/><#if step_has_next>,</#if>
</#list>
}
</#if>
<#if story.isCancelled()?string == 'true'>
,"cancelled": {"keyword": "${keywords.storyCancelled}", "durationKeyword": "${keywords.duration}", "durationInSecs": "${story.storyDuration.durationInSecs}"}
</#if>
<#if story.getPendingMethods()??>
,"pendingMethods": [
<#list story.getPendingMethods() as method>
"${method?json_string}"<#if method_has_next>,</#if>
</#list>
]
</#if>
}

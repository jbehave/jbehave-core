<#ftl strip_whitespace=true>
<#macro renderMultiline text>${text?html?replace("\n", "<br/>")}</#macro>
<#macro renderMeta meta>
<div class="meta">
<div class="keyword">${keywords.meta}</div>
<#assign metaProperties=meta.getProperties()>
<#list metaProperties.keySet() as name>
<#assign property = metaProperties.get(name)>
<div class="property">${keywords.metaProperty}${name?html} ${property?html}</div>
</#list>
</div>
</#macro>
<#macro renderNarrative narrative>
<div class="narrative"><h2>${keywords.narrative}</h2>
<#assign isAlternative=narrative.isAlternative()?string>
<#if isAlternative == "true">
<div class="element asA"><span class="keyword asA">${keywords.asA}</span> ${narrative.asA}</div>
<div class="element iWantTo"><span class="keyword iWantTo">${keywords.iWantTo}</span> ${narrative.iWantTo}</div>
<div class="element soThat"><span class="keyword soThat">${keywords.soThat}</span> ${narrative.soThat}</div>
<#else>
<div class="element inOrderTo"><span class="keyword inOrderTo">${keywords.inOrderTo}</span> ${narrative.inOrderTo}</div>
<div class="element asA"><span class="keyword asA">${keywords.asA}</span> ${narrative.asA}</div>
<div class="element iWantTo"><span class="keyword iWantTo">${keywords.iWantTo}</span> ${narrative.iWantTo}</div>
</#if>
</div>
</#macro>
<#macro renderGivenStories givenStories>
<div class="givenStories">
<div class="keyword">${keywords.givenStories}</div>
<#list givenStories.getStories() as givenStory>
<div class="givenStory">${givenStory.path}</div>
</#list>
</div>
</#macro>
<#macro renderScope scope><#if scope == 'SCENARIO'>${keywords.scopeScenario}<#elseif scope == 'STORY'>${keywords.scopeStory}</#if></#macro>
<#macro renderLifecycle lifecycle>
<div class="lifecycle"><h2>${keywords.lifecycle}</h2>
<#if lifecycle.hasBeforeSteps()>
<div class="before"><h3>${keywords.before}</h3>
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getBeforeSteps(scope)>
<#if !stepsByScope.isEmpty()>
<div class="scope"><h3>${keywords.scope} <@renderScope scope/></h3>
<#list stepsByScope as step>
<div class="step">${step?html}</div>
</#list> <!-- step -->
</div> <!-- scope -->
</#if>
</#list> <!-- scope -->
</div> <!-- before -->
</#if>
<#if lifecycle.hasAfterSteps()>
<div class="after"><h3>${keywords.after}</h3>
<#list lifecycle.getScopes() as scope>
<#assign stepsByScope=lifecycle.getAfterSteps(scope)>
<#if !stepsByScope.isEmpty()>
<div class="scope"><h3>${keywords.scope} <@renderScope scope/></h3>
<#list lifecycle.getOutcomes() as outcome>
<div class="outcome">
${keywords.outcome} ${outcome}
<#assign metaFilter=lifecycle.getMetaFilter(outcome)>
<#if !metaFilter.isEmpty()><#assign metaFilterAsString=metaFilter.asString()><div class="metaFilter step">${keywords.metaFilter} ${metaFilterAsString}</div></#if>
<#list lifecycle.getAfterSteps(scope, outcome) as step>
<div class="step">${step?html}</div>
</#list> <!-- step -->
</div>
</#list> <!-- outcome -->
</div>
</#if>
</#list> <!-- scope -->
</div> <!-- after -->
</#if>
</div>
</#macro>
<#macro renderTable table>
<#assign rows=table.getRows()>
<#assign headers=table.getHeaders()>
<table>
<thead><tr>
<#list headers as header>
<th>${header?html}</th>
</#list>
</tr></thead>
<tbody>
<#list rows as row>
<tr>
<#list headers as header>
<#assign cell=row.get(header)>
<td>${cell?html}</td>
</#list>
</tr>
</#list>
</tbody>
</table>
</#macro>
<#macro renderOutcomes table>
<#assign outcomes=table.getOutcomes()>
<#assign fields=table.getOutcomeFields()>
<table>
<thead><tr>
<#list fields as field>
<th>${field?html}</th>
</#list>
</tr></thead>
<tbody>
<#list outcomes as outcome>
<#assign isVerified=outcome.isVerified()?string>
<#if isVerified == "true"> <#assign verified="verified"><#else><#assign verified="notVerified"></#if>
<tr class="${verified}">
<td>${outcome.description?html}</td><td><@renderOutcomeValue outcome.getValue() table/></td><td>${outcome.matcher?html}</td><td><#if isVerified == "true">${keywords.yes}<#else>${keywords.no}</#if></td>
</tr>
</#list>
</tbody>
</table>
</#macro>
<#macro renderVerbatim verbatim>
<pre>${verbatim.content}</pre>
</#macro>
<#macro renderOutcomeValue value table><#if value?is_date><#assign format=table.getFormat('java.util.Date')>${value?string(format)}<#elseif value?is_number><#assign format=table.getFormat('java.lang.Number')><#setting number_format="${format}">${value?c}<#elseif value?is_boolean><#assign format=table.getFormat('java.lang.Boolean')><#setting boolean_format="${format}">${value?c}<#else>${value?html}</#if></#macro>
<#macro renderStep step>
<#assign formattedStep = step.getFormattedStep(EscapeMode.HTML, "<span class=\"step parameter\">{0}</span>")>
<div class="step ${step.outcome}">${formattedStep}<#if step.getTable()??> <span class="step parameter"><@renderTable step.getTable()/></span></#if><#if step.getVerbatim()??> <span class="step parameter"><@renderVerbatim step.getVerbatim()/></span></#if><@renderStepOutcome step.getOutcome()/></div>
<#if step.getFailure()??><pre class="failure">${step.failureCause?html}</pre></#if><#if step.getPendingMethod()??><pre class="pendingMethod">${step.pendingMethod?html}</pre></#if>
<#if step.getOutcomes()??>
<div class="outcomes"><@renderOutcomes step.getOutcomes()/>
<#if step.getOutcomesFailureCause()??><pre class="failure">${step.outcomesFailureCause?html}</pre></#if>
</div>
</#if>
</#macro>
<#macro renderStepOutcome outcome><#if outcome=="pending"><span class="keyword ${outcome}">(${keywords.pending})</span></#if><#if outcome=="failed"><span class="keyword ${outcome}">(${keywords.failed})</span></#if><#if outcome=="notPerformed"><span class="keyword ${outcome}">(${keywords.notPerformed})</span></#if></#macro>

<html>
<body>
<div class="story"><h1><@renderMultiline story.getDescription()/></h1>
<div class="path">${story.path}</div>
<#if story.getMeta()??><@renderMeta story.getMeta()/></#if>
<#if story.getNarrative()??><@renderNarrative story.getNarrative()/></#if>
<#if story.getLifecycle()??><@renderLifecycle story.getLifecycle()/></#if>
<#if !story.getBeforeSteps().isEmpty()>
<div class="before">
<h3>${keywords.before}</h3>
<#list story.getBeforeSteps() as step>
<@renderStep step/>
</#list>
</div> <!-- before -->
</#if>
<#assign scenarios = story.getScenarios()>
<#list scenarios as scenario>
<div class="scenario"><h2>${keywords.scenario} <@renderMultiline scenario.getTitle()/></h2>
<#if scenario.getMeta()??><@renderMeta scenario.getMeta()/></#if>
<#if scenario.getGivenStories()??><@renderGivenStories scenario.getGivenStories()/></#if>
<#if scenario.getExamplesTable()??>
<div class="examples"><h3>${keywords.examplesTable}</h3>
<#list scenario.getExamplesSteps() as step>
<div class="step">${step?html}</div>
</#list>
<@renderTable scenario.getExamplesTable()/>
</div>  <!-- end examples -->
<#if scenario.getExamples()??>
<#list scenario.getExamples() as example>
<h3 class="example">${keywords.examplesTableRow} ${example?html}</h3>
<#assign steps = scenario.getStepsByExample(example)>
<#list steps as step>
<@renderStep step/>
</#list>
</#list>
</#if>
<#else> <!-- normal scenario steps -->
<#assign steps = scenario.getSteps()>
<#list steps as step>
<@renderStep step/>
</#list>
</#if>
</div> <!-- end scenario -->
</#list>
<#if !story.getAfterSteps().isEmpty()>
<div class="after">
<h3>${keywords.after}</h3>
<#list story.getAfterSteps() as step>
<@renderStep step/>
</#list>
</div> <!-- after -->
</#if>
<#if story.isCancelled()?string == 'true'>
<div class="cancelled">${keywords.storyCancelled} (${keywords.duration} ${story.storyDuration.durationInSecs} s)</div>
</#if>
</div> <!-- end story -->
</body>
</html>


<#-- stand-alone -->
<#if (!metricView??)>
    <#include "../common/style.ftl">
    <#include "../common/script.ftl">
</#if>

<script src="/assets/js/thirdeye.metric.timeseries.js"></script>

<#assign dimensions = (metricView.view.dimensionValues)!dimensionValues>
<#include "../common/dimension-header.ftl">

<div id="metric-time-series-area">
    <div id="metric-time-series-placeholder"></div>
    <div id="metric-time-series-tooltip"></div>
</div>

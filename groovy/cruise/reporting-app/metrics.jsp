<%@page import="net.sourceforge.cruisecontrol.*, net.sourceforge.cruisecontrol.chart.*"%>
<%@taglib uri='WEB-INF/lib/cewolf.jar' prefix='cewolf' %>
<%@ taglib uri="/WEB-INF/cruisecontrol-jsp11.tld" prefix="cruisecontrol"%>

<cruisecontrol:buildInfo />

<table>
  <tr><td>Number of Build Attempts</td><td><%=build_info.size() %></td></tr>
  <tr><td>Number of Broken Builds</td><td><%=build_info.getNumBrokenBuilds() %></td></tr>
  <tr><td>Number of Successful Builds</td><td><%=build_info.getNumSuccessfulBuilds() %></td></tr>
</table>

<hr />
<jsp:useBean id="pieData" class="net.sourceforge.cruisecontrol.chart.PieChartData" />
<cewolf:chart id="pie" title="Breakdown of build types" type="pie" >
    <cewolf:data>
        <cewolf:producer id="pieData">
          <cewolf:param name="buildInfo" value="<%=build_info%>" />
        </cewolf:producer>
    </cewolf:data>
</cewolf:chart>
<cewolf:img chartid="pie" renderer="cewolf" width="400" height="300"/>

<hr />
<jsp:useBean id="chartData" class="net.sourceforge.cruisecontrol.chart.TimeChartData" />
<cewolf:chart id="chart" title="Breakdown of build types" type="timeseries"  xaxislabel="date" yaxislabel="time">
    <cewolf:data>
        <cewolf:producer id="chartData">
          <cewolf:param name="buildInfo" value="<%=build_info%>" />
        </cewolf:producer>
    </cewolf:data>
    <cewolf:chartpostprocessor id="chartData" />
</cewolf:chart>
<cewolf:img chartid="chart" renderer="cewolf" width="400" height="300"/>


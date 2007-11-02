<%@ page import="java.io.File,
                 java.util.Arrays,
                 java.util.Calendar"%>
 <%--********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 600
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************--%>
<jsp:useBean id="statusHelper" scope="page" class="net.sourceforge.cruisecontrol.StatusHelper" />
<%
    String singleProjectMode = application.getInitParameter("singleProject");
    if (Boolean.valueOf(singleProjectMode).booleanValue()) {
       %><jsp:forward page="buildresults" /><%
        return;
    }

    StringBuffer reportTime = new StringBuffer();
    Calendar now = Calendar.getInstance();
    reportTime.append(now.get(Calendar.HOUR_OF_DAY));
    reportTime.append(":");
    String minutes = String.valueOf(now.get(Calendar.MINUTE));
    if (minutes.length() == 1) {
        minutes = 0 + minutes;
    }
    reportTime.append(minutes);

    boolean autoRefresh = "true".equals(request.getParameter("auto_refresh"));
%>
<html>
<head>
  <title>CruiseControl Status Page</title>
  <base href="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()%><%=request.getContextPath()%>/" />
  <link type="text/css" rel="stylesheet" href="css/cruisecontrol.css"/>
  <%
     if (autoRefresh) { 
  %>
  <META HTTP-EQUIV="Refresh" CONTENT="10">
  <%
     }
  %>
</head>
<body background="images/bluebg.gif" topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">
<p>&nbsp;</p>

<h1 class="white" align="center">CruiseControl Status Page</h1>

<table align="center" border="0" cellpadding="0" cellspacing="0" width="70%">
<tfoot>
  <tr><td class="link">listing generated at <%=reportTime.toString()%></td></tr>
</tfoot>
<tbody>
<tr><td align="right">
  <%
     if (autoRefresh) {
  %>
    <a class="white" href="?auto_refresh=false">Turn autorefresh off</a>
  <%
     } else {
  %>
    <a class="white" href="?auto_refresh=true">Turn autorefresh on</a>
  <%
     }
  %>
  </td></tr>
  <tr><td>&nbsp;</td></tr>
  <tr><td bgcolor="#FFFFFF"><img border="0" src="images/bluestripestop.gif"></td></tr>
  <tr><td><table class="index" width="100%">
<%
   String logDirPath = application.getInitParameter("logDir");
   if (logDirPath == null) {
       %><tr><td>You need to provide a value for the context parameter <code>&quot;logDir&quot;</code></td></tr><%
   } else {
       java.io.File logDir = new java.io.File(logDirPath);
       if (logDir.isDirectory() == false) {
           %><tr><td>Context parameter logDir needs to be set to a directory. Currently set to &quot;<%=logDirPath%>&quot;</td></tr><%
       } else {
           String[] projectDirs = logDir.list(new java.io.FilenameFilter() {
               public boolean accept(File dir, String name) {
                   return (new File(dir, name).isDirectory());
               }
           });

           if (projectDirs.length == 0) {
               %><tr><td>no project directories found under <%=logDirPath%></td></tr><%
           }
           else {
%>    <thead class="index-header">
      <tr>
        <td>Project</td>
        <td align="center">Last build result</td>
        <td align="center">Last build time</td>
        <td align="center">Last successful build time</td>
        <td align="center">Last label</td>
    </tr>
    </thead>
    <tbody>
 <%
               Arrays.sort(projectDirs);
             for (int i = 0; i < projectDirs.length; i++) {
                   String project = projectDirs[i];
                   File projectDir = new File(logDir, project);
                   statusHelper.setProjectDirectory(projectDir);
                 final String result = statusHelper.getLastBuildResult();
         %>        <tr><td><a href="buildresults/<%=project%>"><%=project%></a></td><%
                 %><td class="index-<%=result%>" align="center"><%=result%></td><%
                 %><td align="center"><%=statusHelper.getLastBuildTimeString(request.getLocale())%></td><%
                 %><td align="center"><%=statusHelper.getLastSuccessfulBuildTimeString(request.getLocale())%></td><%
                 %><td><%=statusHelper.getLastSuccessfulBuildLabel()%></td>
                   </tr>
 <%
               }
         %>    </tbody>
<%
           }
       }
   }
%></table></td></tr>
  <tr><td bgcolor="#FFFFFF"><img border="0" src="images/bluestripesbottom.gif"></td></tr>
  <tr><td>&nbsp;</td></tr>
</tbody>
</table>
</body>
</html>


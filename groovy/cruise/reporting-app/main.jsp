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
<%@page contentType="text/html"%>
<%@ taglib uri="/WEB-INF/cruisecontrol-jsp11.tld" prefix="cruisecontrol"%>
<html>
<head>
  <title>groovy: CruiseControl Build Results</title>
  <base href="<%=request.getScheme()%>://<%=request.getServerName()%>:<%=request.getServerPort()%><%=request.getContextPath()%>/" />
  <link type="text/css" rel="stylesheet" href="css/cruisecontrol.css"/>
</head>
<body topmargin="0" leftmargin="0" marginheight="0" marginwidth="0">
  <table border="0" align="center" cellpadding="0" cellspacing="0" width="100%">
    <tr><td colspan="2" class="host">
        <div class="canoo">hosted by<br><a href="http://www.canoo.com"><img border="0" height="20" width="112" src="images/canoo_rgb_pos.gif"></a>
        </div></td></tr>
    <tr>
      <td valign="top" class="navigation">
        <%@ include file="navigation.jsp" %>
      </td>
      <td valign="top">
        &nbsp;<br>
        <cruisecontrol:tabsheet>
          <tr>
            <td bgcolor="white" >
              <cruisecontrol:tab name="buildResults" label="Build Results" >
                <%@ include file="buildresults.jsp" %>
              </cruisecontrol:tab>

              <cruisecontrol:tab name="testResults" label="Test Results" >
                <%@ include file="testdetails.jsp" %>
              </cruisecontrol:tab>

              <cruisecontrol:tab name="xmlLogFile" label="XML Log File" >
                <%@ include file="xmllog.jsp" %>
              </cruisecontrol:tab>

              <cruisecontrol:tab name="metrics" label="Metrics" >
                <%@ include file="metrics.jsp" %>
              </cruisecontrol:tab>

              <cruisecontrol:tab name="controlPanel" label="Control Panel" >
                <%@ include file="controlpanel.jsp" %>
              </cruisecontrol:tab>
            </td>
          </tr>
        </cruisecontrol:tabsheet>
      </td>
    </tr>
  </table>
</body>
</html>

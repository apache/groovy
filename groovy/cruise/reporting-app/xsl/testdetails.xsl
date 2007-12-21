<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
<!--********************************************************************************
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
 ********************************************************************************-->
<xsl:output method="html"/>
<xsl:decimal-format decimal-separator="." grouping-separator="," />

<!-- ================================================================== -->
<!-- Write a package level report                                       -->
<!-- It creates a table with values from the document:                  -->
<!-- Name | Tests | Errors | Failures | Time                            -->
<!-- ================================================================== -->
<xsl:template match="cruisecontrol" priority="1">
    <script type="text/javascript" language="JavaScript">
        var TestCases = new Array();
        var SystemOut = new Array();
        var SystemErr = new Array();
        var Problem = new Array();
        var cur;
      <!--xsl:apply-templates select="//testsuite" mode="js.props" /-->
      <xsl:apply-templates select="//testsuite" mode="js.props" />
    </script>
    <script type="text/javascript" language="JavaScript"><![CDATA[
        function displayProperties (name) {
          var win = window.open('','JUnitSystemProperties','scrollbars=1,resizable=1');
          var doc = win.document.open();
          doc.write("<html><head><title>Properties of " + name + "</title>");
          doc.write("<style>")
          doc.write("body {font:normal 68% verdana,arial,helvetica; color:#000000; }");
          doc.write("table tr td, table tr th { font-size: 68%; }");
          doc.write("table.properties { border-collapse:collapse; border-left:solid 1 #cccccc; border-top:solid 1 #cccccc; padding:5px; }");
          doc.write("table.properties th { text-align:left; border-right:solid 1 #cccccc; border-bottom:solid 1 #cccccc; background-color:#eeeeee; }");
          doc.write("table.properties td { font:normal; text-align:left; border-right:solid 1 #cccccc; border-bottom:solid 1 #cccccc; background-color:#fffffff; }");
          doc.write("h3 { margin-bottom: 0.5em; font: bold 115% verdana,arial,helvetica }");
          doc.write("</style>");
          doc.write("</head><body>");
          doc.write("<h3>Properties of " + name + "</h3>");
          doc.write("<div align=\"right\"><a href=\"javascript:window.close();\">Close</a></div>");
          doc.write("<table class='properties'>");
          doc.write("<tr><th>Name</th><th>Value</th></tr>");
          for (prop in TestCases[name]) {
            doc.write("<tr><th>" + prop + "</th><td>" + TestCases[name][prop] + "</td></tr>");
          }
          doc.write("</table>");
          doc.write("</body></html>");
          doc.close();
          win.focus();
        }
    ]]>  
    </script>
    <script type="text/javascript" language="JavaScript"><![CDATA[
    function displayMessage(name) {
        var win = window.open('','Message','scrollbars=1,resizable=1');
        var doc = win.document.open();
        doc.write("<html><head><title>Message</title></head>");
        doc.write("<body><pre>");
        doc.write(Problem[name]);
        doc.write("</pre></body></html>");
        doc.close();
        win.focus();
    }
    ]]>
    </script>
    <script type="text/javascript" language="JavaScript"><![CDATA[
    function displayOut(name) {
        var win = window.open('','Out','scrollbars=1,resizable=1');
        var doc = win.document.open();
        doc.write("<html><head><title>Out</title></head>");
        doc.write("<body><pre>");
        doc.write(SystemOut[name]);
        doc.write("</pre></body></html>");
        doc.close();
        win.focus();
    }
    ]]>
    </script>
    <script type="text/javascript" language="JavaScript"><![CDATA[
    function displayErr(name) {
        var win = window.open('','Err','scrollbars=1,resizable=1');
        var doc = win.document.open();
        doc.write("<html><head><title>Err</title></head>");
        doc.write("<body><pre>");
        doc.write(SystemErr[name]);
        doc.write("</pre></body></html>");
        doc.close();
        win.focus();
    }
    ]]>
    </script>
    <table border="0" cellspacing="0" width="100%">
    <xsl:call-template name="table.header" />
    <xsl:for-each select="//testsuite">
        <xsl:sort select="count(testcase/error)" data-type="number" order="descending" />
        <xsl:sort select="count(testcase/failure)" data-type="number" order="descending" />
        <xsl:sort select="@package"/>
        <xsl:sort select="@name"/>

        <xsl:call-template name="print.class" />
        <xsl:apply-templates select="." mode="print.test" />
        <xsl:call-template name="print.properties" />
    </xsl:for-each>
    </table>
</xsl:template>
    
<xsl:template match="system-out|system-err" mode="print.test"/>

<xsl:template match="testcase" mode="print.test">
    <tr>
        <xsl:attribute name="class">
            <xsl:choose>
                <xsl:when test="error">unittests-error</xsl:when>
                <xsl:when test="failure">unittests-error</xsl:when>
                <xsl:otherwise>unittests-data</xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
        <td />
        <td colspan="2">
            <xsl:value-of select="@name"/>
        </td>
        <td>
            <xsl:choose>
                <xsl:when test="error">
                    <a>
                        <xsl:attribute name="href">javascript:displayMessage('<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>.<xsl:value-of select='@name'/>');</xsl:attribute>
                        Error &#187;
                    </a>
                </xsl:when>
                <xsl:when test="failure">
                    <a>
                        <xsl:attribute name="href">javascript:displayMessage('<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>.<xsl:value-of select='@name'/>');</xsl:attribute>
                        Failure &#187;
                    </a>
                </xsl:when>
                <xsl:otherwise>Success</xsl:otherwise>
            </xsl:choose>
        </td>
        <xsl:if test="not(failure|error)">
            <td>
                <xsl:value-of select="format-number(@time,'0.000')"/>
            </td>
        </xsl:if>
    </tr>
</xsl:template>

<xsl:template name="table.header" >
    <colgroup>
        <col width="10%"></col>
        <col width="45%"></col>
        <col width="25%"></col>
        <col width="10%"></col>
        <col width="10%"></col>
    </colgroup>
    <tr valign="top" class="unittests-sectionheader" align="left" >
        <th colspan="3">Name</th>
        <th>Status</th>
        <th nowrap="nowrap">Time(s)</th>
    </tr>
</xsl:template>

<xsl:template name="print.properties" >
    <tr class="unittests-data">
        <td colspan="2">
            <a>
                <xsl:attribute name="href">javascript:displayProperties('<xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>');</xsl:attribute>
                Properties &#187;
            </a>
        </td>
        <td>
            <xsl:if test="system-out/text()">
              <a style="float:center" >
                <xsl:attribute name="href">javascript:displayOut('<xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>');</xsl:attribute>
                System.out &#187;
              </a>
            </xsl:if>
        </td>
        <td>
            <xsl:if test="system-err/text()">
              <a>
                <xsl:attribute name="href">javascript:displayErr('<xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>');</xsl:attribute>
                System.err &#187;
              </a>
            </xsl:if>
        </td>
    </tr>
</xsl:template>

<xsl:template name="print.class" >
    <tr>
        <xsl:attribute name="class">
            <xsl:choose>
                <xsl:when test="testcase/error">unittests-error</xsl:when>
                <xsl:when test="testcase/failure">unittests-error</xsl:when>
                <xsl:otherwise>unittests-data</xsl:otherwise>
            </xsl:choose>
        </xsl:attribute>
        <td colspan="5"><xsl:value-of select="@package"/>.<xsl:value-of select="@name"/></td>
    </tr>
</xsl:template>

<!--
  Write output and err into a JavaScript data structure.
  This is based on the original idea by Erik Hatcher (ehatcher@apache.org)
-->
<!--xsl:template match="system-out" mode="js.props" >
    SystemOut['<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>'] = '<xsl:call-template name="JS-escape"><xsl:with-param select="." name="string"/></xsl:call-template>';
</xsl:template>

<xsl:template match="system-err" mode="js.props" >
    SystemErr['<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>'] = '<xsl:call-template name="JS-escape"><xsl:with-param select="." name="string"/></xsl:call-template>';
</xsl:template-->

<xsl:template match="error|failure" mode="js.props" >
    Problem['<xsl:value-of select="../../@package"/>.<xsl:value-of select="../../@name"/>.<xsl:value-of select="../@name"/>'] = '<xsl:call-template name="JS-escape"><xsl:with-param select="." name="string"/></xsl:call-template>';
</xsl:template>

<!--
  Write properties into a JavaScript data structure.
  This is based on the original idea by Erik Hatcher (ehatcher@apache.org)
-->
<xsl:template match="properties" mode="js.props" >
    cur = TestCases['<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>'] = new Array();
    <xsl:for-each select="property">
        <xsl:sort select="@name"/>
        cur['<xsl:value-of select="@name"/>'] = '<xsl:call-template name="JS-escape"><xsl:with-param name="string" select="@value"/></xsl:call-template>';
    </xsl:for-each>
</xsl:template>
    
<xsl:template name="JS-escape">
    <xsl:param name="string"/>		
    <xsl:variable name="CR" select="'&#xD;'"/>					
    <xsl:variable name="LF" select="'&#xA;'"/>
    <xsl:variable name="CRLF" select="concat($CR, $LF)"/>			
    <xsl:choose>
        <!-- crlf -->
        <xsl:when test="contains($string,$CRLF)">
            <xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-before($string,$CRLF)"/>
            </xsl:call-template><br/><xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-after($string,$CRLF)"/>
            </xsl:call-template>
        </xsl:when>
        <!-- carriage return -->
        <xsl:when test="contains($string,$CR)">
            <xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-before($string,$CR)"/>
            </xsl:call-template><br/><xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-after($string,$CR)"/>
            </xsl:call-template>
        </xsl:when>
        <!-- line feed -->
        <xsl:when test="contains($string,$LF)">
            <xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-before($string,$LF)"/>
            </xsl:call-template><br/><xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-after($string,$LF)"/>
            </xsl:call-template>
        </xsl:when>
        <!-- single quote -->
        <xsl:when test="contains($string,&quot;'&quot;)">
            <xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-before($string,&quot;'&quot;)"/>
            </xsl:call-template>\&apos;<xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-after($string,&quot;'&quot;)"/>
            </xsl:call-template>
        </xsl:when>
        <!-- escape -->
        <xsl:when test="contains($string,'\')">
            <xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-before($string,'\')"/>
            </xsl:call-template>\\<xsl:call-template name="JS-escape">
                <xsl:with-param name="string" select="substring-after($string,'\')"/>
            </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="$string"/>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

</xsl:stylesheet>

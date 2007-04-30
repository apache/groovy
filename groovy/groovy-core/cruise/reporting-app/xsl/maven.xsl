<?xml version="1.0"?>
<!--********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2003, ThoughtWorks, Inc.
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
<xsl:stylesheet
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/TR/html4/strict.dtd" >

    <xsl:output method="html"/>

    <xsl:variable name="mavengoal" select="/cruisecontrol/build//mavengoal"/>

    <xsl:template match="/">

        <xsl:variable name="maven.messages" select="$mavengoal/message"/>
        <xsl:variable name="maven.error.messages" select="$mavengoal/message[@priority='error']"/>
        <xsl:variable name="maven.warn.messages" select="$mavengoal/message[@priority='warn']"/>
        <xsl:variable name="maven.info.messages" select="$mavengoal/message[@priority='info']"/>

        <xsl:if test="count($maven.messages) > 0">
            <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">
                 <!-- Style download notifications first -->
                 <tr class="compile-sectionheader">
                     <td>Initial Messages</td>
                 </tr>
                 <tr>
                     <td>
                         <xsl:apply-templates select="cruisecontrol/build/message"/>
                     </td>
                 </tr>
                 <xsl:apply-templates select="$mavengoal"/>
            </table>
        </xsl:if>
    </xsl:template>

    <xsl:template match="mavengoal">
       <tr class="compile-sectionheader">
       		<td>
            	<xsl:value-of select="@name"/>
            </td>
       </tr>
       <tr>
       		<td>
            	<xsl:apply-templates select="./message"/>
            </td>
       </tr>
    </xsl:template>

    <xsl:template match="message[@priority='error']">
    	  <span class="compile-error-data">
        <xsl:value-of select="text()"/><xsl:text disable-output-escaping="yes"><![CDATA[<br/>]]></xsl:text>
        </span>
    </xsl:template>

    <xsl:template match="message[@priority='warn']">
    	  <span class="compile-data">
        <xsl:value-of select="text()"/><xsl:text disable-output-escaping="yes"><![CDATA[<br/>]]></xsl:text>
        </span>
    </xsl:template>

    <xsl:template match="message[@priority='info']">
    	  <span class="compile-data">
        <xsl:value-of select="text()"/><xsl:text disable-output-escaping="yes"><![CDATA[<br/>]]></xsl:text>
        </span>
    </xsl:template>

</xsl:stylesheet>

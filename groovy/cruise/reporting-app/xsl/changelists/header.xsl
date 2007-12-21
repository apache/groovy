<?xml version="1.0"?>
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
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
    xmlns:lxslt="http://xml.apache.org/xslt">

    <xsl:output method="html"/>

    <xsl:template match="/">
        <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">

            <xsl:if test="cruisecontrol/build/@error">
                <tr><td class="header-title">BUILD FAILED</td></tr>
                <tr><td class="header-data">
                    <span class="header-label">Error Message:&#160;</span>
                    <xsl:value-of select="cruisecontrol/build/@error"/>
                </td></tr>
            </xsl:if>

            <xsl:if test="not (cruisecontrol/build/@error)">
                <tr><td class="header-title">BUILD COMPLETE&#160;-&#160;
                    <xsl:value-of select="cruisecontrol/info/property[@name='label']/@value"/>
                </td></tr>
            </xsl:if>

            <tr><td class="header-data">
                <span class="header-label">Date of build:&#160;</span>
                <xsl:value-of select="cruisecontrol/info/property[@name='lastbuild']/@value"/>
            </td></tr>
            <tr><td class="header-data">
                <span class="header-label">Time to build:&#160;</span>
                <xsl:value-of select="cruisecontrol/build/@time"/>
            </td></tr>
            <tr>
                <td class="header-data">
                    <span class="header-label">Last changed:&#160;</span>
                    <xsl:value-of select="cruisecontrol/modifications/changelist/@dateOfSubmission"/>
                </td>
            </tr>
            <tr>
                <td class="header-data">
                    <span class="header-label">Last log entry:&#160;</span>
                    <xsl:value-of select="cruisecontrol/modifications/changelist/description"/>
                </td>
            </tr>
        </table>
    </xsl:template>

</xsl:stylesheet>

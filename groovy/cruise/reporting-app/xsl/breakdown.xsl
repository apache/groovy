<?xml version="1.0"?>
<!--
 * Copyright (c) 2007, ASERT Consulting Pty Ltd
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
 *     + Neither the name of ASERT nor the
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
 *-->
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
        xmlns="http://www.w3.org/TR/html4/strict.dtd">

    <xsl:output method="html"/>

    <xsl:variable name="longtasks" select="/cruisecontrol/build//target[contains(@time,'minute') or number(substring-before(@time,' ')) > 30]"/>

    <xsl:template match="/">
        <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">
            <tr>
                <td class="breakdown-sectionheader">
                    &#160;Target Time Breakdown
                </td>
            </tr>
            <tr><td class="breakdown-data">
                <ul>
                    <xsl:for-each select="$longtasks">
                        <li style="padding-left:{count(ancestor::target)*20}px;">
                            <xsl:value-of select="concat(@name, ' (', @time, ')')"/>
                        </li>
                    </xsl:for-each>
                </ul>
            </td></tr>
        </table>
    </xsl:template>

</xsl:stylesheet>

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
    version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://www.w3.org/TR/html4/strict.dtd" >

    <xsl:output method="html"/>

    <xsl:variable name="tasklist" select="/cruisecontrol/build//target/task"/>
    <xsl:variable name="javac.tasklist" select="$tasklist[@name='Javac'] | $tasklist[@name='javac'] | $tasklist[@name='compilewithwalls']"/>
    <xsl:variable name="ejbjar.tasklist" select="$tasklist[@name='EjbJar'] | $tasklist[@name='ejbjar']"/>

    <xsl:template match="/">

        <xsl:variable name="javac.error.messages" select="$javac.tasklist/message[@priority='error'][text() != '']"/>
        <xsl:variable name="javac.warn.messages" select="$javac.tasklist/message[@priority='warn'][text() != '']"/>
        <xsl:variable name="ejbjar.error.messages" select="$ejbjar.tasklist/message[@priority='error'][text() != '']"/>
        <xsl:variable name="ejbjar.warn.messages" select="$ejbjar.tasklist/message[@priority='warn'][text() != '']"/>
        <xsl:variable name="total.errorMessage.count" select="count($javac.warn.messages) + count($ejbjar.warn.messages) + count($javac.error.messages) + count($ejbjar.error.messages)"/>

        <xsl:if test="$total.errorMessage.count > 0">
            <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">
                <tr>
                    <!-- NOTE: total.errorMessage.count is actually the number of lines of error
                     messages. This accurately represents the number of errors ONLY if the Ant property
                     build.compiler.emacs is set to "true" -->
                    <td class="compile-sectionheader">
                        &#160;Errors/Warnings: (<xsl:value-of select="$total.errorMessage.count"/>)
                    </td>
                </tr>
                <xsl:if test="count($javac.error.messages) > 0">
                    <tr>
                        <td>
                           <pre class="compile-error-data">
                            <xsl:apply-templates select="$javac.error.messages"/>
                           </pre>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:if test="count($javac.warn.messages) > 0">
                    <tr>
                        <td>
                           <pre class="compile-data">
                            <xsl:apply-templates select="$javac.warn.messages"/>
                           </pre>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:if test="count($ejbjar.error.messages) > 0">
                    <tr>
                        <td>
                           <pre class="compile-error-data">
                            <xsl:apply-templates select="$ejbjar.error.messages"/>
                           </pre>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:if test="count($ejbjar.warn.messages) > 0">
                    <tr>
                        <td>
                           <pre class="compile-warn-data">
                            <xsl:apply-templates select="$ejbjar.warn.messages"/>
                           </pre>
                        </td>
                    </tr>
                </xsl:if>
            </table>
        </xsl:if>

    </xsl:template>

    <xsl:template match="message[@priority='error']">
        <xsl:value-of select="text()"/>
        <xsl:if test="count(./../message[@priority='error']) != position()">
            <br class="none"/>
        </xsl:if>
    </xsl:template>

    <xsl:template match="message[@priority='warn']">
        <xsl:value-of select="text()"/><br class="none"/>
    </xsl:template>

</xsl:stylesheet>

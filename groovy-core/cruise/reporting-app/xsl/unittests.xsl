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

    <xsl:variable name="testsuite.list" select="//testsuite"/>
    <xsl:variable name="testsuite.error.count" select="count($testsuite.list/error)"/>
    <xsl:variable name="testcase.list" select="$testsuite.list/testcase"/>
    <xsl:variable name="testcase.error.list" select="$testcase.list/error"/>
    <xsl:variable name="testcase.failure.list" select="$testcase.list/failure"/>
    <xsl:variable name="totalErrorsAndFailures" select="count($testcase.error.list) + count($testcase.failure.list) + $testsuite.error.count"/>

    <xsl:template match="/">
        <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">

            <!-- Unit Tests -->
            <tr>
                <td class="unittests-sectionheader" colspan="4">
                   &#160;Unit Tests: (<xsl:value-of select="count($testcase.list)"/>)
                </td>
            </tr>

            <xsl:choose>
                <xsl:when test="count($testsuite.list) = 0">
                    <tr>
                        <td colspan="2" class="unittests-data">
                            No Tests Run
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" class="unittests-error">
                            This project doesn't have any tests
                        </td>
                    </tr>
                </xsl:when>

                <xsl:when test="$totalErrorsAndFailures = 0">
                    <tr>
                        <td colspan="2" class="unittests-data">
                            All Tests Passed
                        </td>
                    </tr>
                </xsl:when>
            </xsl:choose>
            <tr>
              <td>
                 <table align="center" cellpadding="2" cellspacing="0" border="0" width="98%">
                    <xsl:apply-templates select="$testcase.error.list"/>
                    <xsl:apply-templates select="$testcase.failure.list"/>
                 </table>
              </td>
            </tr>
            <tr/>
            <tr><td colspan="2">&#160;</td></tr>

            <xsl:if test="$totalErrorsAndFailures > 0">

              <tr>
                <td class="unittests-sectionheader" colspan="4">
                    &#160;Unit Test Error Details:&#160;(<xsl:value-of select="$totalErrorsAndFailures"/>)
                </td>
              </tr>

              <!-- (PENDING) Why doesn't this work if set up as variables up top? -->
              <xsl:call-template name="testdetail">
                <xsl:with-param name="detailnodes" select="//testsuite/testcase[.//error]"/>
              </xsl:call-template>

              <xsl:call-template name="testdetail">
                <xsl:with-param name="detailnodes" select="//testsuite/testcase[.//failure]"/>
              </xsl:call-template>


              <tr><td colspan="2">&#160;</td></tr>
            </xsl:if>


        </table>
    </xsl:template>

    <!-- UnitTest Errors -->
    <xsl:template match="error">
        <tr>
            <xsl:if test="position() mod 2 = 0">
                <xsl:attribute name="class">unittests-oddrow</xsl:attribute>
            </xsl:if>

            <td class="unittests-data">
                error
            </td>
            <td class="unittests-data" width="40%">
                <xsl:value-of select="../@name"/>
            </td>
            <td class="unittests-data" width="40%">
                <xsl:value-of select="..//..//@name"/>
            </td>
        </tr>
    </xsl:template>

    <!-- UnitTest Failures -->
    <xsl:template match="failure">
        <tr>
            <xsl:if test="($testsuite.error.count + position()) mod 2 = 0">
                <xsl:attribute name="class">unittests-oddrow</xsl:attribute>
            </xsl:if>

            <td class="unittests-data">
                failure
            </td>
            <td class="unittests-data" width="40%">
                <xsl:value-of select="../@name"/>
            </td>
            <td class="unittests-data" width="40%">
                <xsl:value-of select="..//..//@name"/>
            </td>
        </tr>
    </xsl:template>

    <!-- UnitTest Errors And Failures Detail Template -->
    <xsl:template name="testdetail">
      <xsl:param name="detailnodes"/>

      <xsl:for-each select="$detailnodes">

        <tr>
            <td colspan="2" class="unittests-data">
                Test:&#160;<xsl:value-of select="@name"/>
            </td>
        </tr>
        <tr>
            <td colspan="2" class="unittests-data">
                Class:&#160;<xsl:value-of select="..//@name"/>
            </td>
        </tr>

        <xsl:if test="error">
            <tr>
                <td colspan="2" class="unittests-data">
                    Type: <xsl:value-of select="error/@type" />
                </td>
            </tr>
        <tr>
            <td colspan="2" class="unittests-data">
                Message: <xsl:value-of select="error/@message" />
            </td>
        </tr>

        <tr>
            <td colspan="2" class="unittests-error">
                <PRE>
                    <xsl:call-template name="br-replace">
                        <xsl:with-param name="word" select="error" />
                    </xsl:call-template>
                </PRE>
            </td>
        </tr>
        </xsl:if>

        <xsl:if test="failure">
        <tr>
            <td colspan="2" class="unittests-data">
                Type: <xsl:value-of select="failure/@type" />
            </td>
        </tr>
        <tr>
            <td colspan="2" class="unittests-data">
                Message: <xsl:value-of select="failure/@message" />
            </td>
        </tr>

        <tr>
            <td colspan="2" class="unittests-failure">
                <pre>
                    <xsl:call-template name="br-replace">
                        <xsl:with-param name="word" select="failure"/>
                    </xsl:call-template>
                </pre>
            </td>
        </tr>
        </xsl:if>

      </xsl:for-each>
    </xsl:template>

    <xsl:template name="br-replace">
        <xsl:param name="word"/>
<!-- </xsl:text> on next line on purpose to get newline -->
<xsl:variable name="cr"><xsl:text>
</xsl:text></xsl:variable>
        <xsl:choose>
            <xsl:when test="contains($word,$cr)">
                <xsl:value-of select="substring-before($word,$cr)"/>
                <br/>
                <xsl:call-template name="br-replace">
                <xsl:with-param name="word" select="substring-after($word,$cr)"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$word"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


</xsl:stylesheet>

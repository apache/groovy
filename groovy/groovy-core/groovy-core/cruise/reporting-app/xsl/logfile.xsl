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
<xsl:stylesheet version="1.0"
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output omit-xml-declaration="yes" method="html"/>

  <xsl:param name="use-empty-syntax" select="true()"/>
  <xsl:param name="exclude-unused-prefixes" select="true()"/>

  <xsl:param name="start-tag-start"     select="'&lt;'"/>
  <xsl:param name="start-tag-end"       select="'>'"/>
  <xsl:param name="empty-tag-end"       select="'/>'"/>
  <xsl:param name="end-tag-start"       select="'&lt;/'"/>
  <xsl:param name="end-tag-end"         select="'>'"/>
  <xsl:param name="space"               select="' '"/>
  <xsl:param name="ns-decl"             select="'xmlns'"/>
  <xsl:param name="colon"               select="':'"/>
  <xsl:param name="equals"              select="'='"/>
  <xsl:param name="attribute-delimiter" select="'&quot;'"/>
  <xsl:param name="comment-start"       select="'&lt;!--'"/>
  <xsl:param name="comment-end"         select="'-->'"/>
  <xsl:param name="pi-start"            select="'&lt;?'"/>
  <xsl:param name="pi-end"              select="'?>'"/>

  <xsl:template name="xml-to-string">
    <xsl:param name="node-set" select="."/>
    <xsl:apply-templates select="$node-set" mode="xml-to-string">
      <xsl:with-param name="depth" select="1"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="/" name="xml-to-string-root-rule">
    <xsl:call-template name="xml-to-string"/>
  </xsl:template>

  <xsl:template match="/" mode="xml-to-string">
    <xsl:param name="depth"/>
    <xsl:apply-templates mode="xml-to-string">
      <xsl:with-param name="depth" select="$depth"/>
    </xsl:apply-templates>
  </xsl:template>

  <xsl:template match="*" mode="xml-to-string">
    <xsl:param name="depth"/>
    <xsl:variable name="element" select="."/>
    <xsl:value-of select="$start-tag-start"/>
    <xsl:call-template name="element-name">
      <xsl:with-param name="text" select="name()"/>
    </xsl:call-template>
    <xsl:apply-templates select="@*" mode="xml-to-string"/>
    <xsl:for-each select="namespace::*">
      <xsl:call-template name="process-namespace-node">
        <xsl:with-param name="element" select="$element"/>
        <xsl:with-param name="depth" select="$depth"/>
      </xsl:call-template>
    </xsl:for-each>
    <xsl:choose>
      <xsl:when test="node() or not($use-empty-syntax)">
        <xsl:value-of select="$start-tag-end"/>
        <xsl:apply-templates mode="xml-to-string">
          <xsl:with-param name="depth" select="$depth + 1"/>
        </xsl:apply-templates>
        <xsl:value-of select="$end-tag-start"/>
        <xsl:call-template name="element-name">
          <xsl:with-param name="text" select="name()"/>
        </xsl:call-template>
        <xsl:value-of select="$end-tag-end"/>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$empty-tag-end"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="process-namespace-node">
    <xsl:param name="element"/>
    <xsl:param name="depth"/>
    <xsl:variable name="declaredAbove">
      <xsl:call-template name="isDeclaredAbove">
        <xsl:with-param name="depth" select="$depth - 1"/>
        <xsl:with-param name="element" select="$element/.."/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:if test="(not($exclude-unused-prefixes) or ($element | $element//@* | $element//*)[namespace-uri()=current()]) and not(string($declaredAbove)) and name()!='xml'">
      <xsl:value-of select="$space"/>
      <xsl:value-of select="$ns-decl"/>
      <xsl:if test="name()">
        <xsl:value-of select="$colon"/>
        <xsl:call-template name="ns-prefix">
          <xsl:with-param name="text" select="name()"/>
        </xsl:call-template>
      </xsl:if>
      <xsl:value-of select="$equals"/>
      <xsl:value-of select="$attribute-delimiter"/>
      <xsl:call-template name="ns-uri">
        <xsl:with-param name="text" select="string(.)"/>
      </xsl:call-template>
      <xsl:value-of select="$attribute-delimiter"/>
    </xsl:if>
  </xsl:template>

  <xsl:template name="isDeclaredAbove">
    <xsl:param name="element"/>
    <xsl:param name="depth"/>
    <xsl:if test="$depth > 0">
      <xsl:choose>
        <xsl:when test="$element/namespace::*[name(.)=name(current()) and .=current()]">1</xsl:when>
        <xsl:when test="$element/namespace::*[name(.)=name(current())]"/>
        <xsl:otherwise>
          <xsl:call-template name="isDeclaredAbove">
            <xsl:with-param name="depth" select="$depth - 1"/>
            <xsl:with-param name="element" select="$element/.."/>
          </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:if>
  </xsl:template>

  <xsl:template match="@*" mode="xml-to-string">
    <xsl:value-of select="$space"/>
    <xsl:call-template name="attribute-name">
      <xsl:with-param name="text" select="name()"/>
    </xsl:call-template>
    <xsl:value-of select="$equals"/>
    <xsl:value-of select="$attribute-delimiter"/>
    <xsl:call-template name="attribute-value">
      <xsl:with-param name="text" select="string(.)"/>
    </xsl:call-template>
    <xsl:value-of select="$attribute-delimiter"/>
  </xsl:template>

  <xsl:template match="comment()" mode="xml-to-string">
    <xsl:value-of select="$comment-start"/>
    <xsl:call-template name="comment-text">
      <xsl:with-param name="text" select="string(.)"/>
    </xsl:call-template>
    <xsl:value-of select="$comment-end"/>
  </xsl:template>

  <xsl:template match="processing-instruction()" mode="xml-to-string">
    <xsl:value-of select="$pi-start"/>
    <xsl:call-template name="pi-target">
      <xsl:with-param name="text" select="name()"/>
    </xsl:call-template>
    <xsl:value-of select="$space"/>
    <xsl:call-template name="pi-text">
      <xsl:with-param name="text" select="string(.)"/>
    </xsl:call-template>
    <xsl:value-of select="$pi-end"/>
  </xsl:template>

  <xsl:template match="text()" mode="xml-to-string">
    <xsl:call-template name="text-content">
      <xsl:with-param name="text" select="string(.)"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="element-name">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="attribute-name">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="attribute-value">
    <xsl:param name="text"/>
    <xsl:variable name="escaped-markup">
      <xsl:call-template name="escape-markup-characters">
        <xsl:with-param name="text" select="$text"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:choose>
      <xsl:when test="$attribute-delimiter = &quot;'&quot;">
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="$escaped-markup"/>
          <xsl:with-param name="replace" select="&quot;'&quot;"/>
          <xsl:with-param name="with" select="'&amp;apos;'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:when test="$attribute-delimiter = '&quot;'">
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="$escaped-markup"/>
          <xsl:with-param name="replace" select="'&quot;'"/>
          <xsl:with-param name="with" select="'&amp;quot;'"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="$escaped-markup"/>
          <xsl:with-param name="replace" select="$attribute-delimiter"/>
          <xsl:with-param name="with" select="''"/>
        </xsl:call-template>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="ns-prefix">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="ns-uri">
    <xsl:param name="text"/>
    <xsl:call-template name="attribute-value">
      <xsl:with-param name="text" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="text-content">
    <xsl:param name="text"/>
    <xsl:call-template name="escape-markup-characters">
      <xsl:with-param name="text" select="$text"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="pi-target">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="pi-text">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="comment-text">
    <xsl:param name="text"/>
    <xsl:value-of select="$text"/>
  </xsl:template>

  <xsl:template name="escape-markup-characters">
    <xsl:param name="text"/>
    <xsl:variable name="ampEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$text"/>
        <xsl:with-param name="replace" select="'&amp;'"/>
        <xsl:with-param name="with" select="'&amp;amp;'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:variable name="ltEscaped">
      <xsl:call-template name="replace-string">
        <xsl:with-param name="text" select="$ampEscaped"/>
        <xsl:with-param name="replace" select="'&lt;'"/>
        <xsl:with-param name="with" select="'&amp;lt;'"/>
      </xsl:call-template>
    </xsl:variable>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$ltEscaped"/>
      <xsl:with-param name="replace" select="']]>'"/>
      <xsl:with-param name="with" select="']]&amp;gt;'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:variable name="stringText" select="string($text)"/>
    <xsl:choose>
      <xsl:when test="contains($stringText,$replace)">
        <xsl:value-of select="substring-before($stringText,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($stringText,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$stringText"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>

<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns="http://maven.apache.org/POM/4.0.0"
    exclude-result-prefixes="#default" version="1.0">

    <!-- rename artifactId -->
    <xsl:template match="/*[local-name() = 'project']/*[local-name() = 'artifactId']">
        <artifactId>groovy-all-jdk14</artifactId>
    </xsl:template>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>

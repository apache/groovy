<?xml version="1.0"?>
<!DOCTYPE xsl:stylesheet [
	<!ENTITY space "&#32;">
	<!ENTITY nbsp "&#160;">
	<!ENTITY bullet "&#8226;">]>

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
   xmlns:xslt="http://xml.apache.org/xslt"
	>
	<xsl:output method="html"/>

	<!-- collects all task nodes with parent target into the variable $tasklist -->
	<xsl:variable name="tasklist" select="//task"/>

	<!-- collects all task nodes from $tasklist with attribute name="Javac" etc. -->
	<xsl:variable name="javac.tasklist" select="$tasklist[@name='javac']"/>
	<xsl:variable name="ejbjar.tasklist" select="$tasklist[@name='ejbjar']"/>
	<xsl:variable name="jar.tasklist" select="$tasklist[@name='jar']"/>
	<xsl:variable name="war.tasklist" select="$tasklist[@name='war']"/>

	<!-- count elements in sublists -->
	<xsl:variable name="dist.count" select="count($jar.tasklist) + count($war.tasklist)"/>

	<!-- collect all testsuite nodes in build, regardless of depth -->
	<xsl:variable name="testsuite.list" select="//testsuite"/>
	<!-- count error nodes directly under testsuite -->
	<xsl:variable name="testsuite.error.count" select="count($testsuite.list/error)"/>

	<xsl:variable name="testcase.list" select="$testsuite.list/testcase"/>
	<xsl:variable name="testcase.error.list" select="$testcase.list/error"/>
	<xsl:variable name="testcase.failure.list" select="$testcase.list/failure"/>
	<xsl:variable name="totalErrorsAndFailures" select="count($testcase.error.list) + count($testcase.failure.list)"/>

	<!-- 1: /build/modifications, 2: /cruisecontrol/modifications -->
	<xsl:variable name="modification.list" select="//modifications/modification"/>
	<!-- collects all the modification sets -->
	<xsl:key name="modificationSet" match="//modifications/modification" use="concat(user,':',comment)"/>

	<!-- long template for the whole page, this ensures sequence -->
	<xsl:template match="/">
		<!-- Header Part -->

		<xsl:choose>
			<xsl:when test="//build/@error"> <!-- build tag contains attribute error -->
				<h1>BUILD FAILED</h1>
			</xsl:when>
			<xsl:otherwise>
				<h1>
					<xsl:text>BUILD COMPLETE -&space;</xsl:text>
					<!-- support CruiseControl 1.0 and 2.0 -->
					<xsl:value-of select="build/label | //info/property[@name='label']/@value"/>
				</h1>
			</xsl:otherwise>
		</xsl:choose>

		<table>
			<tr>
				<th>Date of build</th>
				<td>
					<!-- support CruiseControl 1.0 and 2.0 -->
					<xsl:value-of select="build/today | //info/property[@name='builddate']/@value"/>
				</td>
			</tr>
			<tr>
				<th>Time to build</th>
				<td>
					<xsl:value-of select="//build/@time"/>
				</td>
			</tr>
			<tr>
				<th>Last changed</th>
				<td>
					<xsl:value-of select="//modifications/modification/date"/>
				</td>
			</tr>
			<tr>
				<th>Last log entry</th>
				<td>
					<xsl:value-of select="//modifications/modification/comment"/>
				</td>
			</tr>
		</table>

		<xsl:if test="//build/@error">
			<h2>Ant Error Message</h2>
			<pre>
				<xsl:value-of select="//build/@error"/>
			</pre>
			<pre>
				<xsl:value-of select="//stacktrace"/>
			</pre>
		</xsl:if>

		<!-- Compilation Messages -->

		<xsl:variable name="javac.warn.messages" select="$javac.tasklist/message[@priority='warn']"/>
		<xsl:variable name="ejbjar.warn.messages" select="$ejbjar.tasklist/message[@priority='warn']"/>
		<xsl:variable name="total.errorMessage.count"
		              select="count($javac.warn.messages) + count($ejbjar.warn.messages)"/>

		<!-- NOTE: total.errorMessage.count is actually the number of lines of error
									  messages. This accurately represents the number of errors ONLY if the Ant property
									  build.compiler.emacs is set to "true" -->
		<xsl:if test="$total.errorMessage.count > 0">
			<h2>
				<xsl:text>Error-/Warning- Lines:&space;</xsl:text>
				<xsl:value-of select="$total.errorMessage.count"/>
			</h2>

			<pre>
				<xsl:apply-templates select="$javac.warn.messages"/>
			</pre>
		</xsl:if>

		<!-- Unit Tests -->
		<xsl:variable name="unit.passed" select="count($testcase.list)-$totalErrorsAndFailures"/>

		<h2>Unit Tests</h2>
		<p>
			<xsl:text>Test cases:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($testcase.list)"/>
			</b>
			<xsl:text>, passed:&space;</xsl:text>
			<b>
				<xsl:value-of select="$unit.passed"/>
			</b>
			<xsl:text>, failures:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($testcase.failure.list)"/>
			</b>
			<xsl:text>, errors:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($testcase.error.list)"/>
			</b>
			<xsl:text>.</xsl:text>
		</p>

		<xsl:call-template name="colorBar">
			<xsl:with-param name="success.count" select="$unit.passed"/>
			<xsl:with-param name="failed.count" select="$totalErrorsAndFailures"/>
			<xsl:with-param name="total.count" select="count($testcase.list)"/>
			<xsl:with-param name="tableID">utests</xsl:with-param>
		</xsl:call-template>

		<xsl:if test="count($testcase.error.list) > 0">
			<h3>Errors</h3>
			<ul>
				<xsl:apply-templates select="$testcase.error.list"/>
			</ul>
		</xsl:if>

		<xsl:if test="count($testcase.failure.list) > 0">
			<h3>Failures</h3>
			<ul>
				<xsl:apply-templates select="$testcase.failure.list"/>
			</ul>
		</xsl:if>

		<xsl:if test="$totalErrorsAndFailures > 0">
			<h3>
				<xsl:text>Unit Test Error Details:&space;(</xsl:text>
				<xsl:value-of select="$totalErrorsAndFailures"/>
				<xsl:text>)</xsl:text>
			</h3>
			<xsl:apply-templates select="//testsuite/testcase[.//error]"/>
			<xsl:apply-templates select="//testsuite/testcase[.//failure]"/>
		</xsl:if>

		<!-- Functional Tests -->

		<!-- functional testing vars -->
		<xsl:variable name="cases" select="//summary/testresult"/>
		<xsl:variable name="steps" select="//summary/testresult/results/step"/>
		<xsl:variable name="passed" select="$cases[@successful='yes']"/>
		<xsl:variable name="failed" select="$cases[@successful='no']"/>

		<h2>Functional Tests</h2>
		<p>
			<xsl:text>Szenarios:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($cases)"/>
			</b>
			<xsl:text>, passed:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($passed)"/>
			</b>
			<xsl:text>, failed:&space;</xsl:text>
			<b>
				<xsl:value-of select="count($failed)"/>
			</b>
			<xsl:text>, including&space;</xsl:text>
			<b>
				<xsl:value-of select="count($steps)"/>
			</b>
			<xsl:text>&space;single steps.</xsl:text>
		</p>

		<xsl:call-template name="colorBar">
			<xsl:with-param name="success.count" select="count($passed)"/>
			<xsl:with-param name="failed.count" select="count($failed)"/>
			<xsl:with-param name="total.count" select="count($cases)"/>
			<xsl:with-param name="tableID">ftests</xsl:with-param>
		</xsl:call-template>

		<h2>Modifications</h2>
		<p>
			<xsl:value-of select="count($modification.list)"/>
			<xsl:text>&space;modifications since last build.</xsl:text>
		</p>

		<xsl:for-each select="$modification.list[count(.|key('modificationSet',concat(user,':',comment))[1])=1]">
			<h3>
				<xsl:value-of select="user"/>
				<xsl:text>:&space;</xsl:text>
				<xsl:value-of select="comment"/>
				<xsl:text>&space;(</xsl:text>
				<xsl:value-of select="date"/>
				<xsl:text>)</xsl:text>
			</h3>
			<ul>
				<xsl:apply-templates select="key('modificationSet',concat(user,':',comment))"/>
			</ul>
		</xsl:for-each>


		<xsl:if test="$dist.count > 0">
			<h2>Deployments</h2>
			<p>
				<xsl:value-of select="$dist.count"/>
				<xsl:text>&space;files deployed by this build.</xsl:text>
			</p>

			<ul>
				<xsl:apply-templates select="$jar.tasklist | $war.tasklist"/>
			</ul>
		</xsl:if>

	</xsl:template>

	<xsl:template name="colorBar">
		<xsl:param name="success.count"/>
		<xsl:param name="failed.count"/>
		<xsl:param name="total.count"/>
		<xsl:param name="tableID"/>

		<table bgcolor="white" cellspacing="0" cellpadding="0">
			<xsl:attribute name="id">
				<xsl:value-of select="$tableID"/>
			</xsl:attribute>
			<tr>
				<xsl:if test="$success.count > 0">
					<td bgcolor="green">
						<xsl:attribute name="width">
							<xsl:value-of select="concat($success.count * 100 div $total.count, '%')"/>
						</xsl:attribute>
						&nbsp;
					</td>
				</xsl:if>

				<xsl:if test="$failed.count > 0">
					<td bgcolor="#FFFFA0">
						<xsl:attribute name="width">
							<xsl:value-of select="concat($failed.count * 100 div $total.count, '%')"/>
						</xsl:attribute>
						&nbsp;
					</td>
				</xsl:if>

				<xsl:if test="($total.count = 0) or ($total.count > ($success.count + $failed.count))">
					<td>&nbsp;</td>
				</xsl:if>
			</tr>
		</table>
	</xsl:template>


	<!-- UnitTest Errors/Failures -->
	<xsl:template match="error|failure">
		<li>
			<xsl:call-template name="colorOddEvenRow"/>
			<xsl:value-of select="../@name"/>
		</li>
	</xsl:template>

	<!-- UnitTest Errors And Failures Detail Template -->
	<xsl:template match="//testsuite/testcase">
		<h4>
			<xsl:text>Test:&space;</xsl:text>
			<xsl:value-of select="@name"/>
		</h4>

		<p>
			<xsl:text>Type:</xsl:text>
			<xsl:value-of select="node()/@type"/>
		</p>
		<p>
			<xsl:text>Message:</xsl:text>
			<xsl:value-of select="node()/@message"/>
		</p>

		<PRE>
			<xsl:value-of select="node()"/>
		</PRE>
	</xsl:template>

	<!-- Compilation Error Details -->
	<xsl:template match="message[@priority='warn']">
		<xsl:value-of select="text()"/>
		<br/>
	</xsl:template>
	<!-- Test Execution Stacktrace -->
	<xsl:template match="stacktrace">
		<pre>
			<xsl:value-of select="text()"/>
		</pre>
	</xsl:template>

	<!-- Modifications template -->
	<xsl:template match="modification[file/filename]">
		<!-- CC 2.0 -->
		<li>
			<xsl:call-template name="colorOddEvenRow"/>
			<xsl:call-template name="modificationType">
				<xsl:with-param name="type" select="file/@action"/>
			</xsl:call-template>

			<xsl:value-of select="file/project"/>
			<xsl:if test="string-length(normalize-space(file/project))">
				<xsl:text>/</xsl:text>
			</xsl:if>
			<xsl:value-of select="file/filename"/>
		</li>
	</xsl:template>

	<xsl:template match="modification">
		<!-- CC 1.0 -->
		<li>
			<xsl:call-template name="colorOddEvenRow"/>
			<xsl:call-template name="modificationType">
				<xsl:with-param name="type" select="@type"/>
			</xsl:call-template>

			<xsl:value-of select="filename"/>
		</li>
	</xsl:template>

	<xsl:template name="modificationType">
		<xsl:param name="type"/>

		<span class="modifificationType">
			<xsl:choose>
				<xsl:when test="$type='modified'">&gt;</xsl:when>
				<xsl:when test="$type='added'">+</xsl:when>
				<xsl:when test="$type='deleted'">-</xsl:when>
				<xsl:otherwise><xsl:value-of select="$type"/></xsl:otherwise>
			</xsl:choose>
		</span>

		&nbsp;
	</xsl:template>

	<!-- jar and war template -->
	<xsl:template match="task[translate(string(@name),'jJwW','')='ar']">
		<li>
			<xsl:call-template name="colorOddEvenRow"/>
			<xsl:value-of select="message"/>
		</li>
	</xsl:template>

	<xsl:template name="colorOddEvenRow">
		<xsl:attribute name="class">
			<xsl:choose>
				<xsl:when test="position() mod 2 = 0">evenrow</xsl:when>
				<xsl:otherwise>oddrow</xsl:otherwise>
			</xsl:choose>
		</xsl:attribute>
	</xsl:template>
</xsl:stylesheet>
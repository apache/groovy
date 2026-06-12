/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.text.markup;

import java.util.Locale;

/**
 * Configuration options for the {@link groovy.text.markup.MarkupTemplateEngine markup template engine}.
 */
public class TemplateConfiguration {

    private String declarationEncoding;
    private boolean expandEmptyElements;
    private boolean useDoubleQuotes;
    private String newLineString = System.lineSeparator();
    private boolean autoEscape = false;
    private boolean autoIndent = false;
    private String autoIndentString = DelegatingIndentWriter.SPACES;
    private boolean autoNewLine = false;
    private Class<? extends BaseTemplate> baseTemplateClass = BaseTemplate.class;
    private Locale locale = Locale.getDefault();
    private boolean cacheTemplates = true;

    /**
     * Creates a configuration instance with default markup template settings.
     */
    public TemplateConfiguration() {
    }

    /**
     * Creates a configuration by copying values from another instance.
     *
     * @param that configuration to copy
     */
    public TemplateConfiguration(TemplateConfiguration that) {
        this.declarationEncoding = that.declarationEncoding;
        this.expandEmptyElements = that.expandEmptyElements;
        this.useDoubleQuotes = that.useDoubleQuotes;
        this.newLineString = that.newLineString;
        this.autoEscape = that.autoEscape;
        this.autoIndent = that.autoIndent;
        this.autoIndentString = that.autoIndentString;
        this.autoNewLine = that.autoNewLine;
        this.baseTemplateClass = that.baseTemplateClass;
        this.locale = that.locale;
    }

    /**
     * @return the encoding used in the declaration header
     */
    public String getDeclarationEncoding() {
        return declarationEncoding;
    }

    /**
     * Set the encoding used to write the declaration header. Note that it is the responsibility of
     * the user to ensure that it matches the writer encoding.
     * @param declarationEncoding encoding to be used in the declaration string
     */
    public void setDeclarationEncoding(final String declarationEncoding) {
        this.declarationEncoding = declarationEncoding;
    }

    /**
     * @return whether elements without body should be written in the short form (ex: &lt;br/&gt;) or
     * in an expanded form (ex: &lt;br&gt;&lt;/br&gt;)
     */
    public boolean isExpandEmptyElements() {
        return expandEmptyElements;
    }

    /**
     * Sets whether empty elements should be expanded into explicit opening and closing tags.
     *
     * @param expandEmptyElements {@code true} to render {@code <tag></tag>} instead of {@code <tag/>}
     */
    public void setExpandEmptyElements(final boolean expandEmptyElements) {
        this.expandEmptyElements = expandEmptyElements;
    }

    /**
     * @return true if attributes should use double quotes instead of single quotes
     */
    public boolean isUseDoubleQuotes() {
        return useDoubleQuotes;
    }

    /**
     * Sets whether attributes should be rendered with double quotes.
     *
     * @param useDoubleQuotes {@code true} to use double quotes around attribute values
     */
    public void setUseDoubleQuotes(final boolean useDoubleQuotes) {
        this.useDoubleQuotes = useDoubleQuotes;
    }

    /**
     * Returns the line separator inserted by {@link BaseTemplate#newLine()}.
     *
     * @return the configured line separator
     */
    public String getNewLineString() {
        return newLineString;
    }

    /**
     * Sets the line separator inserted by {@link BaseTemplate#newLine()}.
     *
     * @param newLineString line separator to emit for explicit and automatic new lines
     */
    public void setNewLineString(final String newLineString) {
        this.newLineString = newLineString;
    }

    /**
     * @return true if variables in the model which are assignable to {@link java.lang.CharSequence} should be
     * automatically escaped.
     */
    public boolean isAutoEscape() {
        return autoEscape;
    }

    /**
     * Set to true if you want variables in the model which are assignable to {@link java.lang.CharSequence} to
     * be escaped automatically in templates. If this flag is set to true and that you want a value not to be
     * automatically escaped, then you need to use <i>${unescaped.variable}</i> instead of <i>$variable</i>
     * @param autoEscape value if the autoEscape flag
     */
    public void setAutoEscape(final boolean autoEscape) {
        this.autoEscape = autoEscape;
    }

    /**
     * @return true if the template engine should handle indents automatically
     */
    public boolean isAutoIndent() {
        return autoIndent;
    }

    /**
     * Set this to true if you want the template engine to render indents automatically. In that case,
     * the supplied writer is wrapped into a {@link groovy.text.markup.DelegatingIndentWriter} and indents
     * are inserted after each call to newLine.
     * @param autoIndent the auto-indent flag
     */
    public void setAutoIndent(final boolean autoIndent) {
        this.autoIndent = autoIndent;
    }

    /**
     * Returns the indentation unit inserted when automatic indentation is enabled.
     *
     * @return the indentation unit string
     */
    public String getAutoIndentString() {
        return autoIndentString;
    }

    /**
     * Sets the indentation unit inserted when automatic indentation is enabled.
     *
     * @param autoIndentString indentation unit to write for each nesting level
     */
    public void setAutoIndentString(final String autoIndentString) {
        this.autoIndentString = autoIndentString;
    }

    /**
     * Indicates whether source layout should automatically insert line separators.
     *
     * @return {@code true} if builder blocks automatically emit new lines
     */
    public boolean isAutoNewLine() {
        return autoNewLine;
    }

    /**
     * Sets whether source layout should automatically insert line separators.
     *
     * @param autoNewLine {@code true} to insert new lines based on builder block layout
     */
    public void setAutoNewLine(final boolean autoNewLine) {
        this.autoNewLine = autoNewLine;
    }

    /**
     * Returns the base class used for generated template scripts.
     *
     * @return the configured base template class
     */
    public Class<? extends BaseTemplate> getBaseTemplateClass() {
        return baseTemplateClass;
    }

    /**
     * Set the template base class. You can use a distinct template class to provide more
     * statically available data to your templates.
     *
     * @param baseTemplateClass a class extending {@link groovy.text.markup.BaseTemplate}
     */
    public void setBaseTemplateClass(final Class<? extends BaseTemplate> baseTemplateClass) {
        this.baseTemplateClass = baseTemplateClass;
    }

    /**
     * Returns the locale used when resolving localized templates.
     *
     * @return the configured template locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets the locale used when resolving localized templates.
     *
     * @param locale locale to prefer when loading localized template resources
     */
    public void setLocale(final Locale locale) {
        this.locale = locale;
    }

    /**
     * Indicates whether templates resolved from external resources are cached.
     *
     * @return {@code true} if template compilation results may be reused
     */
    public boolean isCacheTemplates() {
        return cacheTemplates;
    }

    /**
     * If cache is enabled, then templates are compiled once for each source (URL or File). It is recommended to keep
     * this flag to true unless you are in development mode and want automatic reloading of templates.
     * @param cacheTemplates should templates be cached
     */
    public void setCacheTemplates(final boolean cacheTemplates) {
        this.cacheTemplates = cacheTemplates;
    }
}

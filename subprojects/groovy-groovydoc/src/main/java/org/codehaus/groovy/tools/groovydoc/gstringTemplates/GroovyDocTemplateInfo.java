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
package org.codehaus.groovy.tools.groovydoc.gstringTemplates;

/**
 * Defines the default GString template paths used by GroovyDoc for generating HTML documentation.
 */
public class GroovyDocTemplateInfo {
    private static final String TEMPLATE_BASEDIR = "org/codehaus/groovy/tools/groovydoc/gstringTemplates/";
    private static final String DOCGEN_BASEDIR = "org/apache/groovy/docgenerator/";
    /**
     * Template paths for top-level (root) documentation pages such as the index, all-classes
     * frame, and deprecated list.
     */
    public static final String[] DEFAULT_DOC_TEMPLATES = new String[]{ // top level templates
            TEMPLATE_BASEDIR + "topLevel/index.html",
            TEMPLATE_BASEDIR + "topLevel/overview-frame.html", // needs all package names
            TEMPLATE_BASEDIR + "topLevel/allclasses-frame.html", // needs all packages / class names
            TEMPLATE_BASEDIR + "topLevel/overview-summary.html", // needs all packages
            TEMPLATE_BASEDIR + "topLevel/help-doc.html",
            TEMPLATE_BASEDIR + "topLevel/index-all.html",
            TEMPLATE_BASEDIR + "topLevel/deprecated-list.html",
            TEMPLATE_BASEDIR + "topLevel/overview-tree.html",
            TEMPLATE_BASEDIR + "topLevel/stylesheet.css", // copy default one, may override later
            TEMPLATE_BASEDIR + "topLevel/inherit.gif",
            DOCGEN_BASEDIR   + "groovy.ico",
            // GROOVY-11938 stage 4: Prism.js for client-side syntax highlighting of
            // {@snippet} bodies and fenced Markdown code blocks. These files are
            // always copied so an opt-in flag ({@code -syntaxHighlighter=prism})
            // can enable highlighting at any time without re-running with a
            // different template set.
            TEMPLATE_BASEDIR + "topLevel/prism.js",
            TEMPLATE_BASEDIR + "topLevel/prism.min.css",
            TEMPLATE_BASEDIR + "topLevel/prism-dark.min.css",
            TEMPLATE_BASEDIR + "topLevel/prism-csv.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-groovy.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-java.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-javadoclike.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-javascript.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-json.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-markdown.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-properties.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-regex.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-sql.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-toml.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-xml-doc.min.js",
            TEMPLATE_BASEDIR + "topLevel/prism-yaml.min.js",
    };
    /**
     * Template paths for package-level documentation pages.
     */
    public static final String[] DEFAULT_PACKAGE_TEMPLATES = new String[]{ // package level templates
            TEMPLATE_BASEDIR + "packageLevel/package-frame.html",
            TEMPLATE_BASEDIR + "packageLevel/package-summary.html",
            TEMPLATE_BASEDIR + "packageLevel/package-tree.html"
    };
    /**
     * Template paths for class-level documentation pages.
     */
    public static final String[] DEFAULT_CLASS_TEMPLATES = new String[]{ // class level templates
            TEMPLATE_BASEDIR + "classLevel/classDocName.html"
    };
}

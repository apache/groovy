/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools.groovydoc.gstringTemplates;

public class GroovyDocTemplateInfo {
    private static final String TEMPLATE_BASEDIR = "org/codehaus/groovy/tools/groovydoc/gstringTemplates/";
    private static final String DOCGEN_BASEDIR = "org/codehaus/groovy/tools/";
    public static final String[] DEFAULT_DOC_TEMPLATES = new String[]{ // top level templates
            TEMPLATE_BASEDIR + "topLevel/index.html",
            TEMPLATE_BASEDIR + "topLevel/overview-frame.html", // needs all package names
            TEMPLATE_BASEDIR + "topLevel/allclasses-frame.html", // needs all packages / class names
            TEMPLATE_BASEDIR + "topLevel/overview-summary.html", // needs all packages
            TEMPLATE_BASEDIR + "topLevel/help-doc.html",
            TEMPLATE_BASEDIR + "topLevel/index-all.html",
            TEMPLATE_BASEDIR + "topLevel/deprecated-list.html",
            TEMPLATE_BASEDIR + "topLevel/stylesheet.css", // copy default one, may override later
            TEMPLATE_BASEDIR + "topLevel/inherit.gif",
            DOCGEN_BASEDIR + "groovy.ico",
    };
    public static final String[] DEFAULT_PACKAGE_TEMPLATES = new String[]{ // package level templates
            TEMPLATE_BASEDIR + "packageLevel/package-frame.html",
            TEMPLATE_BASEDIR + "packageLevel/package-summary.html"
    };
    public static final String[] DEFAULT_CLASS_TEMPLATES = new String[]{ // class level templates
            TEMPLATE_BASEDIR + "classLevel/classDocName.html"
    };
}

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

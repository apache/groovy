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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import org.codehaus.groovy.tools.groovydoc.FileOutputTool;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;
import org.codehaus.groovy.tools.groovydoc.LinkArgument;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/**
 * Access to the GroovyDoc tool from Ant.
 */
public class Groovydoc extends Task {
    private final LoggingHelper log = new LoggingHelper(this);

    private Path sourcePath;
    private File destDir;
    private final List<String> packageNames;
    private final List<String> excludePackageNames;
    private String windowTitle = "Groovy Documentation";
    private String docTitle = "Groovy Documentation";
    private String footer = "Groovy Documentation";
    private String header = "Groovy Documentation";
    private Boolean privateScope;
    private Boolean protectedScope;
    private Boolean packageScope;
    private Boolean publicScope;
    private Boolean author;
    private Boolean processScripts;
    private Boolean includeMainForScripts;
    private boolean useDefaultExcludes;
    private boolean includeNoSourcePackages;
    private Boolean noTimestamp;
    private Boolean noVersionStamp;
    private final List<DirSet> packageSets;
    private final List<String> sourceFilesToDoc;
    private final List<LinkArgument> links = new ArrayList<>();
    private File overviewFile;
    private File styleSheetFile;
    // dev note: update javadoc comment for #setExtensions(String) if updating below
    private String extensions = ".java:.groovy:.gv:.gvy:.gsh";

    private String charset;
    private String fileEncoding;

    public Groovydoc() {
        packageNames = new ArrayList<>();
        excludePackageNames = new ArrayList<>();
        packageSets = new ArrayList<DirSet>();
        sourceFilesToDoc = new ArrayList<>();
        privateScope = false;
        protectedScope = false;
        publicScope = false;
        packageScope = false;
        useDefaultExcludes = true;
        includeNoSourcePackages = false;
        author = true;
        processScripts = true;
        includeMainForScripts = true;
        noTimestamp = false;
        noVersionStamp = false;
    }

    /**
     * Specify where to find source file
     *
     * @param src a Path instance containing the various source directories.
     */
    public void setSourcepath(Path src) {
        if (sourcePath == null) {
            sourcePath = src;
        } else {
            sourcePath.append(src);
        }
    }

    /**
     * Set the directory where the Groovydoc output will be generated.
     *
     * @param dir the destination directory.
     */
    public void setDestdir(File dir) {
        destDir = dir;
        // todo: maybe tell groovydoc to use file output
    }

    /**
     * If set to false, author will not be displayed.
     * Currently not used.
     *
     * @param author new value
     */
    public void setAuthor(boolean author) {
        this.author = author;
    }

    /**
     * If set to true, hidden timestamp will not appear within generated HTML.
     *
     * @param noTimestamp new value
     */
    public void setNoTimestamp(boolean noTimestamp) {
        this.noTimestamp = noTimestamp;
    }

    /**
     * If set to true, hidden version stamp will not appear within generated HTML.
     *
     * @param noVersionStamp new value
     */
    public void setNoVersionStamp(boolean noVersionStamp) {
        this.noVersionStamp = noVersionStamp;
    }

    /**
     * If set to false, Scripts will not be processed.
     * Defaults to true.
     *
     * @param processScripts new value
     */
    public void setProcessScripts(boolean processScripts) {
        this.processScripts = processScripts;
    }

    /**
     * If set to false, 'public static void main' method will not be displayed.
     * Defaults to true. Ignored when not processing Scripts.
     *
     * @param includeMainForScripts new value
     */
    public void setIncludeMainForScripts(boolean includeMainForScripts) {
        this.includeMainForScripts = includeMainForScripts;
    }

    /**
     * A colon-separated list of filename extensions to look for when searching for files to process in a given directory.
     * Default value: <code>.java:.groovy:.gv:.gvy:.gsh</code>
     *
     * @param extensions new value
     */
    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    /**
     * Set the package names to be processed.
     *
     * @param packages a comma separated list of packages specs
     *                 (may be wildcarded).
     */
    public void setPackagenames(String packages) {
        StringTokenizer tok = new StringTokenizer(packages, ",");
        while (tok.hasMoreTokens()) {
            String packageName = tok.nextToken();
            packageNames.add(packageName);
        }
    }

    public void setUse(boolean b) {
        //ignore as 'use external file' irrelevant with groovydoc :-)
    }

    /**
     * Set the title to be placed in the HTML &lt;title&gt; tag of the
     * generated documentation.
     *
     * @param title the window title to use.
     */
    public void setWindowtitle(String title) {
        windowTitle = title;
    }

    /**
     * Set the title for the overview page.
     *
     * @param htmlTitle the html to use for the title.
     */
    public void setDoctitle(String htmlTitle) {
        docTitle = htmlTitle;
    }

    /**
     * Specify the file containing the overview to be included in the generated documentation.
     *
     * @param file the overview file
     */
    public void setOverview(File file) {
        overviewFile = file;
    }

    /**
     * Indicates the access mode or scope of interest: one of public, protected, package, or private.
     * Package scoped access is ignored for fields of Groovy classes where they correspond to properties.
     *
     * @param access one of public, protected, package, or private
     */
    public void setAccess(String access) {
        if ("public".equals(access)) publicScope = true;
        else if ("protected".equals(access)) protectedScope = true;
        else if ("package".equals(access)) packageScope = true;
        else if ("private".equals(access)) privateScope = true;
    }

    /**
     * Indicate whether all classes and
     * members are to be included in the scope processed.
     *
     * @param b true if scope is to be private level.
     */
    public void setPrivate(boolean b) {
        privateScope = b;
    }

    /**
     * Indicate whether only public classes and members are to be included in the scope processed.
     *
     * @param b true if scope only includes public level classes and members
     */
    public void setPublic(boolean b) {
        publicScope = b;
    }

    /**
     * Indicate whether only protected and public classes and members are to be included in the scope processed.
     *
     * @param b true if scope includes protected level classes and members
     */
    public void setProtected(boolean b) {
        protectedScope = b;
    }

    /**
     * Indicate whether only package, protected and public classes and members are to be included in the scope processed.
     * Package scoped access is ignored for fields of Groovy classes where they correspond to properties.
     *
     * @param b true if scope includes package level classes and members
     */
    public void setPackage(boolean b) {
        packageScope = b;
    }

    /**
     * Set the footer to place at the bottom of each generated html page.
     *
     * @param footer the footer value
     */
    public void setFooter(String footer) {
        this.footer = footer;
    }

    /**
     * Specifies the header text to be placed at the top of each output file.
     * The header will be placed to the right of the upper navigation bar.
     * It may contain HTML tags and white space, though if it does, it must
     * be enclosed in quotes. Any internal quotation marks within the header
     * may have to be escaped.
     *
     * @param header the header value
     */
    public void setHeader(String header) {
        this.header = header;
    }

    /**
     * Specifies the charset to be used in the templates, i.e.&#160;the value output within:
     * &lt;meta http-equiv="Content-Type" content="text/html; charset=<em>charset</em>"&gt;.
     *
     * @param charset the charset value
     */
    public void setCharset(String charset) {
        this.charset = charset;
    }

    /**
     * Specifies the file encoding to be used for generated files. If <em>fileEncoding</em> is missing,
     * the <em>charset</em> encoding will be used for writing the files. If <em>fileEncoding</em> and
     * <em>charset</em> are missing, the file encoding will default to <em>Charset.defaultCharset()</em>.
     *
     * @param fileEncoding the file encoding
     */
    public void setFileEncoding(String fileEncoding) {
        this.fileEncoding = fileEncoding;
    }

    /**
     * Specifies a stylesheet file to use. If not specified,
     * a default one will be generated for you.
     *
     * @param styleSheetFile the css stylesheet file to use
     */
    public void setStyleSheetFile(File styleSheetFile) {
        this.styleSheetFile = styleSheetFile;
    }

    /**
     * Add the directories matched by the nested dirsets to the resulting
     * packages list and the base directories of the dirsets to the Path.
     * It also handles the packages and excludepackages attributes and
     * elements.
     *
     * @param resultantPackages a list to which we add the packages found
     * @param sourcePath        a path to which we add each basedir found
     * @since 1.5
     */
    private void parsePackages(List<String> resultantPackages, Path sourcePath) {
        List<String> addedPackages = new ArrayList<>();
        List<DirSet> dirSets = new ArrayList<DirSet>(packageSets);

        // for each sourcePath entry, add a directoryset with includes
        // taken from packagenames attribute and nested package
        // elements and excludes taken from excludepackages attribute
        // and nested excludepackage elements
        if (this.sourcePath != null) {
            PatternSet ps = new PatternSet();
            if (!packageNames.isEmpty()) {
                for (String pn : packageNames) {
                    String pkg = pn.replace('.', '/');
                    if (pkg.endsWith("*")) {
                        pkg += "*";
                    }
                    ps.createInclude().setName(pkg);
                }
            } else {
                ps.createInclude().setName("**");
            }

            for (String epn : excludePackageNames) {
                String pkg = epn.replace('.', '/');
                if (pkg.endsWith("*")) {
                    pkg += "*";
                }
                ps.createExclude().setName(pkg);
            }

            String[] pathElements = this.sourcePath.list();
            for (String pathElement : pathElements) {
                File dir = new File(pathElement);
                if (dir.isDirectory()) {
                    DirSet ds = new DirSet();
                    ds.setDefaultexcludes(useDefaultExcludes);
                    ds.setDir(dir);
                    ds.createPatternSet().addConfiguredPatternset(ps);
                    dirSets.add(ds);
                } else {
                    log.warn("Skipping " + pathElement + " since it is no directory.");
                }
            }
        }

        for (DirSet ds : dirSets) {
            File baseDir = ds.getDir(getProject());
            log.debug("scanning " + baseDir + " for packages.");
            DirectoryScanner dsc = ds.getDirectoryScanner(getProject());
            String[] dirs = dsc.getIncludedDirectories();
            boolean containsPackages = false;
            for (String dir : dirs) {
                // are there any groovy or java files in this directory?
                File pd = new File(baseDir, dir);
                String[] files = pd.list((dir1, name) -> {
                    if (!includeNoSourcePackages
                            && name.equals("package.html")) return true;
                    final StringTokenizer tokenizer = new StringTokenizer(extensions, ":");
                    while (tokenizer.hasMoreTokens()) {
                        String ext = tokenizer.nextToken();
                        if (name.endsWith(ext)) return true;
                    }
                    return false;
                });

                if (files != null) {
                    for (String filename : files) {
                        sourceFilesToDoc.add(dir + File.separator + filename);
                    }
                    if (files.length > 0) {
                        if (dir.isEmpty()) {
                            log.warn(baseDir
                                    + " contains source files in the default package,"
                                    + " you must specify them as source files not packages.");
                        } else {
                            containsPackages = true;
                            String pn = dir.replace(File.separatorChar, '.');
                            if (!addedPackages.contains(pn)) {
                                addedPackages.add(pn);
                                resultantPackages.add(pn);
                            }
                        }
                    }
                }
            }
            if (containsPackages) {
                // We don't need to care for duplicates here,
                // Path.list does it for us.
                sourcePath.createPathElement().setLocation(baseDir);
            } else {
                log.verbose(baseDir + " doesn't contain any packages, dropping it.");
            }
        }
    }

    @Override
    public void execute() throws BuildException {
        List<String> packagesToDoc = new ArrayList<>();
        Path sourceDirs = new Path(getProject());
        Properties properties = new Properties();
        properties.setProperty("windowTitle", windowTitle);
        properties.setProperty("docTitle", docTitle);
        properties.setProperty("footer", footer);
        properties.setProperty("header", header);
        checkScopeProperties(properties);
        properties.setProperty("publicScope", publicScope.toString());
        properties.setProperty("protectedScope", protectedScope.toString());
        properties.setProperty("packageScope", packageScope.toString());
        properties.setProperty("privateScope", privateScope.toString());
        properties.setProperty("author", author.toString());
        properties.setProperty("processScripts", processScripts.toString());
        properties.setProperty("includeMainForScripts", includeMainForScripts.toString());
        properties.setProperty("overviewFile", overviewFile != null ? overviewFile.getAbsolutePath() : "");
        properties.setProperty("charset", charset != null ? charset : "");
        properties.setProperty("fileEncoding", fileEncoding != null ? fileEncoding : "");
        properties.setProperty("timestamp", Boolean.valueOf(!noTimestamp).toString());
        properties.setProperty("versionStamp", Boolean.valueOf(!noVersionStamp).toString());

        if (sourcePath != null) {
            sourceDirs.addExisting(sourcePath);
        }
        parsePackages(packagesToDoc, sourceDirs);

        GroovyDocTool htmlTool = new GroovyDocTool(
                new ClasspathResourceManager(), // we're gonna get the default templates out of the dist jar file
                sourcePath.list(),
                getDocTemplates(),
                getPackageTemplates(),
                getClassTemplates(),
                links,
                properties
        );

        try {
            htmlTool.add(sourceFilesToDoc);
            FileOutputTool output = new FileOutputTool();
            htmlTool.renderToOutput(output, destDir.getCanonicalPath()); // TODO push destDir through APIs?
        } catch (Exception e) {
            e.printStackTrace();
        }
        // try to override the default stylesheet with custom specified one if needed
        if (styleSheetFile != null) {
            try {
                String css = ResourceGroovyMethods.getText(styleSheetFile);
                File outfile = new File(destDir, "stylesheet.css");
                ResourceGroovyMethods.setText(outfile, css);
            } catch (IOException e) {
                System.out.println("Warning: Unable to copy specified stylesheet '" + styleSheetFile.getAbsolutePath() +
                        "'. Using default stylesheet instead. Due to: " + e.getMessage());
            }
        }
    }

    private void checkScopeProperties(Properties properties) {
        // make protected the default scope and check for invalid duplication
        int scopeCount = 0;
        if (packageScope) scopeCount++;
        if (privateScope) scopeCount++;
        if (protectedScope) scopeCount++;
        if (publicScope) scopeCount++;
        if (scopeCount == 0) {
            protectedScope = true;
        } else if (scopeCount > 1) {
            throw new BuildException("More than one of public, private, package, or protected scopes specified.");
        }
    }

    /**
     * Create link to Javadoc/GroovyDoc output at the given URL.
     *
     * @return link argument to configure
     */
    public LinkArgument createLink() {
        LinkArgument result = new LinkArgument();
        links.add(result);
        return result;
    }

    /**
     * Creates and returns an array of package template classpath entries.
     * <p>
     * This method is meant to be overridden by custom GroovyDoc implementations, using custom package templates.
     *
     * @return an array of package templates, whereas each entry is resolved as classpath entry, defaults to
     * {@link GroovyDocTemplateInfo#DEFAULT_PACKAGE_TEMPLATES}.
     */
    protected String[] getPackageTemplates() {
        return GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES;
    }

    /**
     * Creates and returns an array of doc template classpath entries.
     * <p>
     * This method is meant to be overridden by custom GroovyDoc implementations, using custom doc templates.
     *
     * @return an array of doc templates, whereas each entry is resolved as classpath entry, defaults to
     * {@link GroovyDocTemplateInfo#DEFAULT_DOC_TEMPLATES}.
     */
    protected String[] getDocTemplates() {
        return GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES;
    }

    /**
     * Creates and returns an array of class template classpath entries.
     * <p>
     * This method is meant to be overridden by custom GroovyDoc implementations, using custom class templates.
     *
     * @return an array of class templates, whereas each entry is resolved as classpath entry, defaults to
     * {@link GroovyDocTemplateInfo#DEFAULT_CLASS_TEMPLATES}.
     */
    protected String[] getClassTemplates() {
        return GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES;
    }
}

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
package org.codehaus.groovy.tools.groovydoc;

import org.apache.groovy.groovydoc.tools.GroovyDocUtil;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.antlr4.GroovyDocParser;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.Logger.Level.WARNING;

/*
 *  todo: order methods alphabetically (implement compareTo enough?)
 */
public class GroovyRootDocBuilder {
    private static final System.Logger LOGGER = System.getLogger(GroovyRootDocBuilder.class.getName());
    private final Logger log = Logger.create(GroovyRootDocBuilder.class);
    private static final char FS = '/';
    private final List<LinkArgument> links;
    private final String[] sourcepaths;
    private final SimpleGroovyRootDoc rootDoc;
    private final Properties properties;
    private int errorCount;

    @Deprecated
    public GroovyRootDocBuilder(GroovyDocTool tool, String[] sourcepaths, List<LinkArgument> links, Properties properties) {
        this(sourcepaths, links, properties);
    }

    public GroovyRootDocBuilder(String[] sourcepaths, List<LinkArgument> links, Properties properties) {
        this.sourcepaths = sourcepaths == null ? null : Arrays.copyOf(sourcepaths, sourcepaths.length);
        this.links = links;
        this.rootDoc = new SimpleGroovyRootDoc("root");
        // GROOVY-11938: propagate for render-time snippet-file resolution.
        this.rootDoc.setSourcepaths(this.sourcepaths);
        this.properties = properties;
    }

    public void buildTree(List<String> filenames) throws IOException {
        setOverview();

        List<File> sourcepathFiles = new ArrayList<>();
        if (sourcepaths != null) {
            for (String sourcepath : sourcepaths) {
                sourcepathFiles.add(new File(sourcepath).getAbsoluteFile());
            }
        }

        for (String filename : filenames) {
            File srcFile = new File(filename);
            if (srcFile.exists()) {
                processFile(filename, srcFile, true);
                continue;
            }
            for (File spath : sourcepathFiles) {
                srcFile = new File(spath, filename);
                if (srcFile.exists()) {
                    processFile(filename, srcFile, false);
                    break;
                }
            }
        }
    }

    private void setOverview() {
        String path = properties.getProperty("overviewFile");
        if (path != null && !path.isEmpty()) {
            try {
                String content = ResourceGroovyMethods.getText(new File(path));
                calcThenSetOverviewDescription(content);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Unable to load overview file: {0}", e.getMessage());
            }
        }
    }

    private void processFile(String filename, File srcFile, boolean isAbsolute) throws IOException {
        String src = ResourceGroovyMethods.getText(srcFile);
        String relPackage = GroovyDocUtil.getPath(filename).replace('\\', FS);
        String packagePath = isAbsolute ? "DefaultPackage" : relPackage;
        String file = GroovyDocUtil.getFile(filename);
        SimpleGroovyPackageDoc packageDoc = null;
        if (!isAbsolute) {
            packageDoc = (SimpleGroovyPackageDoc) rootDoc.packageNamed(packagePath);
        }
        // todo: this might not work correctly for absolute paths
        if (filename.endsWith("package.html") || filename.endsWith("package-info.java") || filename.endsWith("package-info.groovy")) {
            if (packageDoc == null) {
                packageDoc = new SimpleGroovyPackageDoc(relPackage);
                packagePath = relPackage;
            }
            processPackageInfo(src, filename, packageDoc);
            rootDoc.put(packagePath, packageDoc);
            return;
        }
        try {
            GroovyDocParserI docParser = new GroovyDocParser(links, properties);
            Map<String, GroovyClassDoc> classDocs = docParser.getClassDocsFromSingleSource(packagePath, file, src);
            rootDoc.putAllClasses(classDocs);
            if (isAbsolute) {
                Iterator<Map.Entry<String, GroovyClassDoc>> iterator = classDocs.entrySet().iterator();
                if (iterator.hasNext()) {
                    final Map.Entry<String, GroovyClassDoc> docEntry = iterator.next();
                    String fullPath = docEntry.getValue().getFullPathName();
                    int slash = fullPath.lastIndexOf(FS);
                    if (slash > 0) packagePath = fullPath.substring(0, slash);
                    packageDoc = (SimpleGroovyPackageDoc) rootDoc.packageNamed(packagePath);
                }
            }
            if (packageDoc == null) {
                packageDoc = new SimpleGroovyPackageDoc(packagePath);
            }
            packageDoc.putAll(classDocs);
            rootDoc.put(packagePath, packageDoc);
        } catch (RuntimeException e) {
            errorCount++;
            e.printStackTrace(System.err);
            log.error("ignored due to parsing exception: " + filename + " [" + e.getMessage() + "]");
            log.debug("ignored due to parsing exception: " + filename + " [" + e.getMessage() + "]", e);
        }
    }

    public int getErrorCount() {
        return errorCount;
    }

    /* package private */ void processPackageInfo(String src, String filename, SimpleGroovyPackageDoc packageDoc) {
        String relPath = packageDoc.getRelativeRootPath();
        String description = calcThenSetPackageDescription(src, filename, relPath);
        packageDoc.setDescription(description);
        // get same description but with paths relative to root
        String altDescription = calcThenSetPackageDescription(src, filename, "");
        calcThenSetSummary(altDescription, packageDoc);
    }

    private String calcThenSetPackageDescription(String src, String filename, String relPath) {
        String description;
        if (filename.endsWith(".html")) {
            description = scrubOffExcessiveTags(src);
            description = pruneTagFromFront(description, "p");
            description = pruneTagFromEnd(description, "/p");
        } else {
            description = trimPackageAndComments(src);
        }
        description = replaceTags(description, relPath);
        return description;
    }

    // GROOVY-11939: package-info tag processing goes through the same single-pass
    // tokenizer as class-level docs, just with a different rendering profile.
    private String replaceTags(String orig, String relPath) {
        String result = orig.replaceAll("(?m)^\\s*\\*", "");
        return TagRenderer.render(result, links, relPath, rootDoc, null, TagRenderer.PACKAGE_LEVEL);
    }

    private static void calcThenSetSummary(String src, SimpleGroovyPackageDoc packageDoc) {
        packageDoc.setSummary(SimpleGroovyDoc.calculateFirstSentence(src));
    }

    private void calcThenSetOverviewDescription(String src) {
        String description = scrubOffExcessiveTags(src);
        rootDoc.setDescription(description);
    }

    private static String trimPackageAndComments(String src) {
        return src.replaceFirst("(?sm)^package.*", "")
                .replaceFirst("(?sm)/.*\\*\\*(.*)\\*/", "$1")
                .replaceAll("(?m)^\\s*\\*", "");
    }

    private static String scrubOffExcessiveTags(String src) {
        String description = pruneTagFromFront(src, "html");
        description = pruneTagFromFront(description, "/head");
        description = pruneTagFromFront(description, "body");
        description = pruneTagFromEnd(description, "/html");
        return pruneTagFromEnd(description, "/body");
    }

    private static String pruneTagFromFront(String description, String tag) {
        int index = Math.max(indexOfTag(description, tag.toLowerCase(Locale.ENGLISH)), indexOfTag(description, tag.toUpperCase(Locale.ENGLISH)));
        if (index < 0) return description;
        return description.substring(index);
    }

    private static String pruneTagFromEnd(String description, String tag) {
        int index = Math.max(description.lastIndexOf("<" + tag.toLowerCase(Locale.ENGLISH) + ">"),
                description.lastIndexOf("<" + tag.toUpperCase(Locale.ENGLISH) + ">"));
        if (index < 0) return description;
        return description.substring(0, index);
    }

    private static int indexOfTag(String text, String tag) {
        int pos = text.indexOf("<" + tag + ">");
        if (pos > 0) pos += tag.length() + 2;
        return pos;
    }

    public GroovyRootDoc getRootDoc() {
        rootDoc.resolve();
        return rootDoc;
    }
}

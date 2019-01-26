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

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.UnicodeEscapingReader;
import org.codehaus.groovy.antlr.java.Groovifier;
import org.codehaus.groovy.antlr.java.Java2GroovyConverter;
import org.codehaus.groovy.antlr.java.JavaLexer;
import org.codehaus.groovy.antlr.java.JavaRecognizer;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.antlr.treewalker.PreOrderTraversal;
import org.codehaus.groovy.antlr.treewalker.SourceCodeTraversal;
import org.codehaus.groovy.antlr.treewalker.Visitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.ResourceGroovyMethods;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import static org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc.CODE_REGEX;
import static org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc.LINK_REGEX;
import static org.codehaus.groovy.tools.groovydoc.SimpleGroovyClassDoc.TAG_REGEX;

/*
 *  todo: order methods alphabetically (implement compareTo enough?)
 */
public class GroovyRootDocBuilder {
    private final Logger log = Logger.create(GroovyRootDocBuilder.class);
    private static final char FS = '/';
    private final List<LinkArgument> links;
    private final GroovyDocTool tool;
    private final String[] sourcepaths;
    private final SimpleGroovyRootDoc rootDoc;
    private final Properties properties;

    public GroovyRootDocBuilder(GroovyDocTool tool, String[] sourcepaths, List<LinkArgument> links, Properties properties) {
        this.tool = tool;
        this.sourcepaths = sourcepaths;
        this.links = links;
        this.rootDoc = new SimpleGroovyRootDoc("root");
        this.properties = properties;
    }

    // parsing
    public Map<String, GroovyClassDoc> getClassDocsFromSingleSource(String packagePath, String file, String src)
            throws RecognitionException, TokenStreamException {
        if (file.indexOf(".java") > 0) { // simple (for now) decision on java or groovy
            // java
            return parseJava(packagePath, file, src);
        }
        if (file.indexOf(".sourcefile") > 0) {
            // java (special name used for testing)
            return parseJava(packagePath, file, src);
        }
        // not java, try groovy instead :-)
        return parseGroovy(packagePath, file, src);
    }

    private Map<String, GroovyClassDoc> parseJava(String packagePath, String file, String src)
            throws RecognitionException, TokenStreamException {
        SourceBuffer sourceBuffer = new SourceBuffer();
        JavaRecognizer parser = getJavaParser(src, sourceBuffer);
        String[] tokenNames = parser.getTokenNames();
        try {
            parser.compilationUnit();
        } catch (OutOfMemoryError e) {
            log.error("Out of memory while processing: " + packagePath + "/" + file);
            throw e;
        }
        AST ast = parser.getAST();

        // modify the Java AST into a Groovy AST (just token types)
        Visitor java2groovyConverter = new Java2GroovyConverter(tokenNames);
        AntlrASTProcessor java2groovyTraverser = new PreOrderTraversal(java2groovyConverter);
        java2groovyTraverser.process(ast);

        // now mutate (groovify) the ast into groovy
        Visitor groovifier = new Groovifier(tokenNames, false);
        AntlrASTProcessor groovifierTraverser = new PreOrderTraversal(groovifier);
        groovifierTraverser.process(ast);

        // now do the business     
        Visitor visitor = new SimpleGroovyClassDocAssembler(packagePath, file, sourceBuffer, links, properties, false);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);

        traverser.process(ast);

        return ((SimpleGroovyClassDocAssembler) visitor).getGroovyClassDocs();
    }

    private Map<String, GroovyClassDoc> parseGroovy(String packagePath, String file, String src)
            throws RecognitionException, TokenStreamException {
        SourceBuffer sourceBuffer = new SourceBuffer();
        GroovyRecognizer parser = getGroovyParser(src, sourceBuffer);
        try {
            parser.compilationUnit();
        } catch (OutOfMemoryError e) {
            log.error("Out of memory while processing: " + packagePath + "/" + file);
            throw e;
        }
        AST ast = parser.getAST();

        // now do the business
        Visitor visitor = new SimpleGroovyClassDocAssembler(packagePath, file, sourceBuffer, links, properties, true);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        return ((SimpleGroovyClassDocAssembler) visitor).getGroovyClassDocs();
    }

    private static JavaRecognizer getJavaParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        JavaLexer lexer = new JavaLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        JavaRecognizer parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

    private static GroovyRecognizer getGroovyParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
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
        if (path != null && path.length() > 0) {
            try {
                String content = ResourceGroovyMethods.getText(new File(path));
                calcThenSetOverviewDescription(content);
            } catch (IOException e) {
                System.err.println("Unable to load overview file: " + e.getMessage());
            }
        }
    }

    private void processFile(String filename, File srcFile, boolean isAbsolute) throws IOException {
        String src = ResourceGroovyMethods.getText(srcFile);
        String relPackage = tool.getPath(filename).replace('\\', FS);
        String packagePath = isAbsolute ? "DefaultPackage" : relPackage;
        String file = tool.getFile(filename);
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
            Map<String, GroovyClassDoc> classDocs = getClassDocsFromSingleSource(packagePath, file, src);
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
        } catch (RecognitionException e) {
            log.error("ignored due to RecognitionException: " + filename + " [" + e.getMessage() + "]");
            log.debug("ignored due to RecognitionException: " + filename + " [" + e.getMessage() + "]", e);
        } catch (TokenStreamException e) {
            log.error("ignored due to TokenStreamException: " + filename + " [" + e.getMessage() + "]");
            log.debug("ignored due to TokenStreamException: " + filename + " [" + e.getMessage() + "]", e);
        }
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

    // TODO remove dup with SimpleGroovyClassDoc
    private String replaceTags(String orig, String relPath) {
        String result = orig.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex

        // {@link processing hack}
        result = replaceAllTags(result, "", "", LINK_REGEX, relPath);

        // {@code processing hack}
        result = replaceAllTags(result, "<TT>", "</TT>", CODE_REGEX, relPath);

        // hack to reformat other groovydoc block tags (@see, @return, @param, @throws, @author, @since) into html
        result = replaceAllTags(result + "@endMarker", "<DL><DT><B>$1:</B></DT><DD>", "</DD></DL>", TAG_REGEX, relPath);
        // remove @endMarker
        result = result.substring(0, result.length() - 10);

        return SimpleGroovyClassDoc.decodeSpecialSymbols(result);
    }

    private String replaceAllTags(String self, String s1, String s2, Pattern regex, String relPath) {
        return SimpleGroovyClassDoc.replaceAllTags(self, s1, s2, regex, links, relPath, rootDoc, null);
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
        int index = Math.max(indexOfTag(description, tag.toLowerCase()), indexOfTag(description, tag.toUpperCase()));
        if (index < 0) return description;
        return description.substring(index);
    }

    private static String pruneTagFromEnd(String description, String tag) {
        int index = Math.max(description.lastIndexOf("<" + tag.toLowerCase() + ">"),
                description.lastIndexOf("<" + tag.toUpperCase() + ">"));
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

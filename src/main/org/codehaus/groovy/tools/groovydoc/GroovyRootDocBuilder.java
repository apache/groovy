/*
 * Copyright 2003-2009 the original author or authors.
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
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/*
 * todo
 *  comma at the end of method parameters
 *  add comments
 *  static modifier
 *  order methods alphabetically (implement compareTo enough?)
 *  provide links to other html files (e.g. return type of a method)
 */
public class GroovyRootDocBuilder {
    private static final char FS = '/';
    private List<LinkArgument> links;
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
        parser.compilationUnit();
        AST ast = parser.getAST();

        // modify the Java AST into a Groovy AST (just token types)
        Visitor java2groovyConverter = new Java2GroovyConverter(tokenNames);
        AntlrASTProcessor java2groovyTraverser = new PreOrderTraversal(java2groovyConverter);
        java2groovyTraverser.process(ast);

        // now mutate (groovify) the ast into groovy
        Visitor groovifier = new Groovifier(tokenNames);
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
//        String[] tokenNames = parser.getTokenNames();
        parser.compilationUnit();
        AST ast = parser.getAST();

        // now do the business
        Visitor visitor = new SimpleGroovyClassDocAssembler(packagePath, file, sourceBuffer, links, properties, true);
        AntlrASTProcessor traverser = new SourceCodeTraversal(visitor);
        traverser.process(ast);
        return ((SimpleGroovyClassDocAssembler) visitor).getGroovyClassDocs();
    }

    private JavaRecognizer getJavaParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        JavaLexer lexer = new JavaLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        JavaRecognizer parser = JavaRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

    private GroovyRecognizer getGroovyParser(String input, SourceBuffer sourceBuffer) {
        UnicodeEscapingReader unicodeReader = new UnicodeEscapingReader(new StringReader(input), sourceBuffer);
        GroovyLexer lexer = new GroovyLexer(unicodeReader);
        unicodeReader.setLexer(lexer);
        GroovyRecognizer parser = GroovyRecognizer.make(lexer);
        parser.setSourceBuffer(sourceBuffer);
        return parser;
    }

    public void buildTree(List<String> filenames) throws IOException, RecognitionException, TokenStreamException {
        setOverview();

        List<File> sourcepathFiles = new ArrayList<File>();
        for (String sourcepath : sourcepaths) {
            sourcepathFiles.add(new File(sourcepath).getAbsoluteFile());
        }

        for (String filename : filenames) {
            for (File spath : sourcepathFiles) {
                File srcFile = new File(spath, filename);
                if (srcFile.exists()) {
                    processFile(filename, srcFile);
                    break;
                }
            }
        }
    }

    private void setOverview() {
        String path = properties.getProperty("overviewFile");
        if (path != null && path.length() > 0) {
            try {
                String content = DefaultGroovyMethods.getText(new File(path));
                calcThenSetOverviewDescription(content);
            } catch (IOException e) {
                System.err.println("Unable to load overview file: " + e.getMessage());
            }
        }
    }

    private void processFile(String filename, File srcFile) throws IOException {
        String src = DefaultGroovyMethods.getText(srcFile);
        String packagePath = tool.getPath(filename).replace('\\', FS);
        String file = tool.getFile(filename);
        SimpleGroovyPackageDoc packageDoc = (SimpleGroovyPackageDoc) rootDoc.packageNamed(packagePath);
        if (packageDoc == null) {
            packageDoc = new SimpleGroovyPackageDoc(packagePath);
        }
        if (filename.endsWith("package.html")) {
            processHtmlPackage(src, packageDoc);
            return;
        }
        try {
            Map<String, GroovyClassDoc> classDocs = getClassDocsFromSingleSource(packagePath, file, src);
            rootDoc.putAllClasses(classDocs);
            packageDoc.putAll(classDocs);
            rootDoc.put(packagePath, packageDoc);
        } catch (RecognitionException e) {
            System.err.println("ignored due to RecognitionException: " + filename + " [" + e.getMessage() + "]");
        } catch (TokenStreamException e) {
            System.err.println("ignored due to TokenStreamException: " + filename + " [" + e.getMessage() + "]");
        }
    }

    private void processHtmlPackage(String src, SimpleGroovyPackageDoc packageDoc) {
        String description = calcThenSetPackageDescription(src, packageDoc);
        calcThenSetSummary(description, packageDoc);
    }

    private void calcThenSetSummary(String src, SimpleGroovyPackageDoc packageDoc) {
        packageDoc.setSummary(SimpleGroovyDoc.calculateFirstSentence(src));
    }

    private String calcThenSetPackageDescription(String src, SimpleGroovyPackageDoc packageDoc) {
        String description = scrubOffExcessiveTags(src);
        packageDoc.setDescription(description);
        return description;
    }

    private void calcThenSetOverviewDescription(String src) {
        String description = scrubOffExcessiveTags(src);
        rootDoc.setDescription(description);
    }

    private String scrubOffExcessiveTags(String src) {
        String description = pruneTagFromFront(src, "html");
        description = pruneTagFromFront(description, "/head");
        description = pruneTagFromFront(description, "body");
        description = pruneTagFromFront(description, "p");
        description = pruneTagFromEnd(description, "/p");
        description = pruneTagFromEnd(description, "/html");
        return pruneTagFromEnd(description, "/body");
    }

    private String pruneTagFromFront(String description, String tag) {
        int index = Math.max(indexOfTag(description, tag.toLowerCase()), indexOfTag(description, tag.toUpperCase()));
        if (index < 0) return description;
        return description.substring(index);
    }

    private String pruneTagFromEnd(String description, String tag) {
        int index = Math.max(description.indexOf("<" + tag.toLowerCase() + ">"),
                description.indexOf("<" + tag.toUpperCase() + ">"));
        if (index < 0) return description;
        return description.substring(0, index);
    }

    private int indexOfTag(String text, String tag) {
        int pos = text.indexOf("<" + tag + ">");
        if (pos > 0) pos += tag.length() + 2;
        return pos;
    }

    public GroovyRootDoc getRootDoc() {
        rootDoc.resolve();
        return rootDoc;
    }
}

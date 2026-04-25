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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import groovy.test.GroovyTestCase;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.codehaus.groovy.tools.groovydoc.antlr4.GroovyDocParser;
import org.codehaus.groovy.tools.groovydoc.gstringTemplates.GroovyDocTemplateInfo;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GroovyDocToolTest extends GroovyTestCase {
    private static final String MOCK_DIR = "mock/doc";
    private static final String TEMPLATES_DIR = "main/resources/org/codehaus/groovy/tools/groovydoc/gstringTemplates";

    GroovyDocTool xmlTool;
    GroovyDocTool xmlToolForTests;
    GroovyDocTool plainTool;
    GroovyDocTool htmlTool;

    public void setUp() {
        plainTool = new GroovyDocTool(new String[]{"src/test/groovy"});

        xmlTool = makeXmlTool(new ArrayList<>(), new Properties());
        xmlToolForTests = makeXmlTool(new ArrayList<>(), new Properties(), new String[] {"src/test/groovy", "src/test/resources", "../../src/test/groovy"});

        ArrayList<LinkArgument> links = new ArrayList<>();
        LinkArgument link = new LinkArgument();
        link.setHref("https://docs.oracle.com/javase/8/docs/api/");
        link.setPackages("java.,org.xml.,javax.,org.xml.");
        links.add(link);

        htmlTool = makeHtmltool(links, null, new Properties());
    }

    public void tearDown() {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(null);
    }

    private GroovyDocTool makeXmlTool(ArrayList<LinkArgument> links, Properties props) {
        return makeXmlTool(links, props, new String[] {"src/main/java", "../../src/main/java", "src/test/groovy"});
    }

    private GroovyDocTool makeXmlTool(ArrayList<LinkArgument> links, Properties props, String[] sources) {
        return new GroovyDocTool(
                new FileSystemResourceManager("src"), // template storage
                sources, // source file dirs
                new String[]{TEMPLATES_DIR + "/topLevel/rootDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/packageLevel/packageDocStructuredData.xml"},
                new String[]{TEMPLATES_DIR + "/classLevel/classDocStructuredData.xml"},
                links,
     null,
                props
        );
    }

    private GroovyDocTool makeHtmltool(ArrayList<LinkArgument> links, String javaVersion, Properties props) {
        return makeHtmltool(links, javaVersion, props, new String[] {"src/test/groovy", "../../src/test/groovy"});
    }

    private GroovyDocTool makeHtmltool(ArrayList<LinkArgument> links, String javaVersion, Properties props, String[] sources) {
        return new GroovyDocTool(
                new FileSystemResourceManager("src/main/resources"), // template storage
                sources, // source file dirs
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                links,
                javaVersion,
                props
        );
    }

    // GROOVY-11682
    public void testLinkArgumentModuleComposesJpmsStyleUrl() {
        LinkArgument link = new LinkArgument();
        link.setHref("https://docs.oracle.com/en/java/javase/17/docs/api/");
        assertEquals("https://docs.oracle.com/en/java/javase/17/docs/api/", link.getHref());

        link.setModule("java.base");
        assertEquals("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/", link.getHref());

        LinkArgument noSlash = new LinkArgument();
        noSlash.setHref("https://example.org/api");
        noSlash.setModule("java.sql");
        assertEquals("https://example.org/api/java.sql/", noSlash.getHref());
    }

    // GROOVY-11682
    public void testLinkWithModuleProducesJpmsStyleUrlInRenderedDoc() throws Exception {
        ArrayList<LinkArgument> links = new ArrayList<>();
        LinkArgument link = new LinkArgument();
        link.setHref("https://docs.oracle.com/en/java/javase/17/docs/api/");
        link.setModule("java.base");
        link.setPackages("java.,javax.");
        links.add(link);

        GroovyDocTool jpmsTool = makeHtmltool(links, null, new Properties());
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        jpmsTool.add(List.of(base + "/DocumentedClass.groovy"));

        MockOutputTool output = new MockOutputTool();
        jpmsTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull("Expected DocumentedClass.html in output", doc);
        assertTrue("Expected JPMS-style module segment in URL, got:\n" + doc,
            doc.contains("https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/Object.html"));
    }

    // GROOVY-3782 (stray-brace rendering bug): the old regex chain broke
    // `{@inheritDoc}` into bogus `{<DL><DT><B>inheritDoc:</B>...` HTML. The
    // GROOVY-11939 tokenizer refactor fixed that as a side-effect. Verify.
    public void testInheritDocDoesNotLeaveStrayBrace() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithInheritDocStub.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/InheritDocChild.html");
        assertNotNull("Expected InheritDocChild page", doc);
        assertFalse("stray-{ or broken block tag around inheritDoc in:\n" + doc,
                doc.contains("{<DL><DT><B>inheritDoc") || doc.contains("inheritDoc:</B>"));
    }

    // GROOVY-11954: resolveClass must not let a shared root-level cache smear
    // a short-name resolution across classes with different imports. Here two
    // classes in the same run both use the short name "Date" — one imports
    // java.util.Date, the other java.sql.Date. Both must render links to their
    // own import, regardless of which class is resolved first.
    public void testShortNameResolutionHonoursPerClassImports() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        // Feed both files into the same doc run so they share a GroovyRootDoc.
        htmlTool.add(List.of(
                base + "/AmbiguousDateUtil.groovy",
                base + "/AmbiguousDateSql.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String utilDoc = output.getText(MOCK_DIR + "/" + base + "/AmbiguousDateUtil.html");
        assertNotNull("Expected AmbiguousDateUtil.html in output", utilDoc);
        assertTrue("AmbiguousDateUtil should link java.util.Date, got:\n" + utilDoc,
                utilDoc.contains("java/util/Date.html"));
        assertFalse("AmbiguousDateUtil must not link java.sql.Date:\n" + utilDoc,
                utilDoc.contains("java/sql/Date.html"));

        String sqlDoc = output.getText(MOCK_DIR + "/" + base + "/AmbiguousDateSql.html");
        assertNotNull("Expected AmbiguousDateSql.html in output", sqlDoc);
        assertTrue("AmbiguousDateSql should link java.sql.Date, got:\n" + sqlDoc,
                sqlDoc.contains("java/sql/Date.html"));
        assertFalse("AmbiguousDateSql must not link java.util.Date:\n" + sqlDoc,
                sqlDoc.contains("java/util/Date.html"));
    }

    // GROOVY-11941: additionalStylesheets property causes templates to emit
    // extra <link rel="stylesheet"> tags at the appropriate relative-root path.
    public void testAdditionalStylesheetsInTemplates() throws Exception {
        Properties props = new Properties();
        props.setProperty("additionalStylesheets", "custom1.css,custom2.css");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);

        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        tool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String classDoc = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classDoc);
        // Class-level template uses classDoc.relativeRootPath as prefix.
        String prefix = "../../../../../../";
        assertTrue("Expected additional stylesheet link with relative root prefix in:\n" + classDoc,
                classDoc.contains("href=\"" + prefix + "custom1.css\""));
        assertTrue("Expected second additional stylesheet link in:\n" + classDoc,
                classDoc.contains("href=\"" + prefix + "custom2.css\""));
    }

    // GROOVY-11938 stage 2: external {@snippet file="..."} reads from the
    // package's snippet-files/ directory.
    public void testSnippetTagExternalFormLoadsFromSnippetFiles() throws Exception {
        String fixtureSourcePath = "src/test/resources/docfiles-fixture";
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/docfiles";

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src/main/resources"),
                new String[]{fixtureSourcePath},
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                new ArrayList<>(), null, new Properties()
        );
        tool.add(List.of(pkg + "/HasDocFiles.groovy"));

        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + pkg + "/HasDocFiles.html");
        assertNotNull(doc);
        assertTrue("Expected <pre><code class='language-groovy'> wrapping snippet file in:\n" + doc,
                doc.contains("<pre><code class=\"language-groovy\">"));
        // Example.groovy's content should appear:
        assertTrue("Expected snippet file content in:\n" + doc,
                doc.contains("hello from Example"));
        // ASF license header on Example.groovy should be stripped by default.
        int snipStart = doc.indexOf("<pre><code class=\"language-groovy\">");
        int snipEnd = doc.indexOf("</code></pre>", snipStart);
        String snip = doc.substring(snipStart, snipEnd);
        assertFalse("ASF header should be stripped from external snippet by default:\n" + snip,
                snip.contains("Licensed to the Apache"));
    }

    // Auto-strip opt-out: {@snippet file="X" keepHeader=true} preserves the
    // file content verbatim, and lang is inferred from the file's extension
    // when no explicit lang= is given.
    public void testSnippetTagExternalFormKeepHeaderAndInfersLang() throws Exception {
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/docfiles";
        Path tmp = Files.createTempDirectory("snippet-keepheader-");
        Path pkgDir = tmp.resolve(pkg);
        Files.createDirectories(pkgDir);
        Files.writeString(pkgDir.resolve("KeepHeader.groovy"),
                "package " + pkg.replace('/', '.') + "\n" +
                "/**\n" +
                " * {@snippet file=\"Example.groovy\" keepHeader=true}\n" +
                " */\n" +
                "class KeepHeader {}\n");
        Path srcSnippetDir = Paths.get(
                "src/test/resources/docfiles-fixture", pkg, "snippet-files");
        Path dstSnippetDir = pkgDir.resolve("snippet-files");
        Files.createDirectories(dstSnippetDir);
        try (Stream<Path> s = Files.walk(srcSnippetDir)) {
            s.filter(Files::isRegularFile).forEach(f -> {
                try {
                    Files.copy(f, dstSnippetDir.resolve(f.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { throw new RuntimeException(e); }
            });
        }
        try {
            GroovyDocTool tool = new GroovyDocTool(
                    new FileSystemResourceManager("src/main/resources"),
                    new String[]{tmp.toString()},
                    GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                    new ArrayList<>(), null, new Properties()
            );
            tool.add(List.of(pkg + "/KeepHeader.groovy"));
            MockOutputTool output = new MockOutputTool();
            tool.renderToOutput(output, MOCK_DIR);

            String doc = output.getText(MOCK_DIR + "/" + pkg + "/KeepHeader.html");
            assertNotNull(doc);
            // Lang inferred from .groovy extension even without explicit lang=.
            assertTrue("Expected inferred language-groovy class in:\n" + doc,
                    doc.contains("<pre><code class=\"language-groovy\">"));
            // With keepHeader=true, ASF boilerplate is preserved.
            int snipStart = doc.indexOf("<pre><code class=\"language-groovy\">");
            int snipEnd = doc.indexOf("</code></pre>", snipStart);
            String snip = doc.substring(snipStart, snipEnd);
            assertTrue("keepHeader=true should preserve ASF header:\n" + snip,
                    snip.contains("Licensed to the Apache"));
        } finally {
            try (Stream<Path> s = Files.walk(tmp)) {
                s.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.delete(p); } catch (IOException ignore) {} });
            }
        }
    }

    // License-header auto-strip covers MIT / BSD / GPL style headers, not
    // just ASF: the heuristic matches a leading block comment containing
    // either "Licensed" or "Copyright", so standard MIT and BSD boilerplate
    // (which use "Copyright" but not "Licensed") is handled too.
    public void testSnippetTagExternalFormStripsMitStyleHeader() throws Exception {
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/docfiles";
        Path tmp = Files.createTempDirectory("snippet-mit-");
        Path pkgDir = tmp.resolve(pkg);
        Files.createDirectories(pkgDir);
        Files.writeString(pkgDir.resolve("MitHeader.groovy"),
                "package " + pkg.replace('/', '.') + "\n" +
                "/**\n" +
                " * {@snippet file=\"Mit.groovy\"}\n" +
                " */\n" +
                "class MitHeader {}\n");
        Path snippetDir = pkgDir.resolve("snippet-files");
        Files.createDirectories(snippetDir);
        Files.writeString(snippetDir.resolve("Mit.groovy"),
                "/*\n" +
                " * MIT License\n" +
                " *\n" +
                " * Copyright (c) 2026 The Example Authors\n" +
                " *\n" +
                " * Permission is hereby granted, free of charge, to any person\n" +
                " * obtaining a copy of this software...\n" +
                " */\n" +
                "println 'mit sample'\n");
        try {
            GroovyDocTool tool = new GroovyDocTool(
                    new FileSystemResourceManager("src/main/resources"),
                    new String[]{tmp.toString()},
                    GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                    new ArrayList<>(), null, new Properties()
            );
            tool.add(List.of(pkg + "/MitHeader.groovy"));
            MockOutputTool output = new MockOutputTool();
            tool.renderToOutput(output, MOCK_DIR);

            String doc = output.getText(MOCK_DIR + "/" + pkg + "/MitHeader.html");
            assertNotNull(doc);
            int snipStart = doc.indexOf("<pre><code class=\"language-groovy\">");
            int snipEnd = doc.indexOf("</code></pre>", snipStart);
            String snip = doc.substring(snipStart, snipEnd);
            assertFalse("MIT header ('Copyright' but not 'Licensed') should be stripped:\n" + snip,
                    snip.contains("MIT License"));
            assertFalse("Copyright line should be stripped too:\n" + snip,
                    snip.contains("Copyright (c) 2026"));
            assertTrue("Snippet body should survive:\n" + snip,
                    snip.contains("println 'mit sample'"));
        } finally {
            try (Stream<Path> s = Files.walk(tmp)) {
                s.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.delete(p); } catch (IOException ignore) {} });
            }
        }
    }

    // GROOVY-11938 stage 2: external snippet with region= selects a slice.
    public void testSnippetTagExternalFormWithRegion() throws Exception {
        String fixtureSourcePath = "src/test/resources/docfiles-fixture";
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/docfiles";

        // Synthesise a class on the fly that uses {@snippet file=... region=...}
        // Actually, just use a testfile in src/test/groovy that we add to sourcepath.
        // Here we reuse a class with an inline doc reference via the resource-dir.
        Path tmp = Files.createTempDirectory("snippet-region-");
        Path pkgDir = tmp.resolve(pkg);
        Files.createDirectories(pkgDir);
        Files.writeString(pkgDir.resolve("RegionClass.groovy"),
                "/*\n *  Licensed to the Apache Software Foundation (ASF) under one\n" +
                " *  or more contributor license agreements.  See the NOTICE file\n" +
                " *  distributed with this work for additional information\n" +
                " *  regarding copyright ownership.  The ASF licenses this file\n" +
                " *  to you under the Apache License, Version 2.0 (the\n" +
                " *  \"License\"); you may not use this file except in compliance\n" +
                " *  with the License.  You may obtain a copy of the License at\n" +
                " *\n" +
                " *    http://www.apache.org/licenses/LICENSE-2.0\n" +
                " */\n" +
                "package " + pkg.replace('/', '.') + "\n" +
                "/**\n" +
                " * {@snippet file=\"Regioned.groovy\" region=\"core\"}\n" +
                " */\n" +
                "class RegionClass {}\n");
        // Symlink/copy the snippet-files dir from fixture into the tmp tree.
        Path srcSnippetDir = Paths.get(
                "src/test/resources/docfiles-fixture", pkg, "snippet-files");
        Path dstSnippetDir = pkgDir.resolve("snippet-files");
        Files.createDirectories(dstSnippetDir);
        try (Stream<Path> s = Files.walk(srcSnippetDir)) {
            s.filter(Files::isRegularFile).forEach(f -> {
                try {
                    Files.copy(f, dstSnippetDir.resolve(f.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) { throw new RuntimeException(e); }
            });
        }

        try {
            GroovyDocTool tool = new GroovyDocTool(
                    new FileSystemResourceManager("src/main/resources"),
                    new String[]{tmp.toString()},
                    GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                    GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                    new ArrayList<>(), null, new Properties()
            );
            tool.add(List.of(pkg + "/RegionClass.groovy"));
            MockOutputTool output = new MockOutputTool();
            tool.renderToOutput(output, MOCK_DIR);

            String doc = output.getText(MOCK_DIR + "/" + pkg + "/RegionClass.html");
            assertNotNull(doc);
            assertTrue("Expected coreLogic (inside region) in:\n" + doc,
                    doc.contains("def coreLogic(int x)"));
            assertFalse("Should not include 'def setup' (outside region) in:\n" + doc,
                    doc.contains("def setup()"));
            assertFalse("Should not include 'def teardown' (outside region) in:\n" + doc,
                    doc.contains("def teardown()"));
        } finally {
            try (Stream<Path> s = Files.walk(tmp)) {
                s.sorted(Comparator.reverseOrder())
                        .forEach(p -> { try { Files.delete(p); } catch (IOException ignore) {} });
            }
        }
    }

    // GROOVY-11938 stage 1: the inline {@snippet} tag renders its body as a
    // verbatim <pre><code>...</code></pre> block with HTML-escaped content,
    // an optional language class, id, and extra class.
    public void testSnippetTagInlineFormRendersAsPreCode() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithSnippetTag.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/ClassWithSnippetTag.html");
        assertNotNull(doc);
        // Groovy snippet: check <pre><code class="language-groovy"> wrapping.
        assertTrue("Expected <pre><code class='language-groovy'> in:\n" + doc,
                doc.contains("<pre><code class=\"language-groovy\">"));
        // Body content preserved: the `def greet` line should appear escape-free
        // (Groovy has no angle brackets in this snippet).
        assertTrue("Expected snippet body content in:\n" + doc,
                doc.contains("def greet(String name)"));
        // HTML escaping: `<` in the Java snippet must appear as &lt;
        assertTrue("Expected HTML-escaped '<' in snippet body in:\n" + doc,
                doc.contains("if (x &lt; y)"));
        // Language + class merged.
        assertTrue("Expected language-java + example class merged in:\n" + doc,
                doc.contains("class=\"language-java example\""));
        // id attribute emitted.
        assertTrue("Expected id=\"hello\" in:\n" + doc,
                doc.contains("id=\"hello\""));
        // No literal `{@snippet lang=...` with attributes (i.e. an unprocessed
        // real snippet tag). Note that `{@code {@snippet}}` patterns in the
        // surrounding description legitimately render as literal text inside
        // a <CODE> block, so checking for bare `{@snippet` is too strict.
        assertFalse("{@snippet ... } with attributes should be substituted in:\n" + doc,
                doc.contains("{@snippet lang="));
    }

    // GROOVY-3782 (the actual {@inheritDoc} tag): the child method's doc
    // should be expanded with the parent's rendered description plus tags.
    public void testInheritDocSubstitutesParentMethodDoc() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithInheritDocStub.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/InheritDocChild.html");
        assertNotNull(doc);
        assertTrue("Parent description should be inherited into child in:\n" + doc,
                doc.contains("Parent-level description"));
        // The literal `{@inheritDoc}` must not appear anymore.
        assertFalse("{@inheritDoc} should be substituted, not left literal in:\n" + doc,
                doc.contains("{@inheritDoc}"));
    }

    // GROOVY-6016: {@value #FIELD} inline tag resolves same-class constants.
    public void testValueTagResolvesSameClassConstants() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        String klass = "ClassWithValueTag";
        htmlTool.add(List.of(base + "/" + klass + ".groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/" + klass + ".html");
        assertNotNull("Expected a page for the value-tag class", doc);
        assertTrue("Expected {@value #MAX} to render as 42 in:\n" + doc,
                doc.contains("Max allowed: 42"));
        assertTrue("Expected {@value #GREETING} to render with 'hello' in:\n" + doc,
                doc.contains("Greeting: \"hello\""));
        // Folded via ExpressionUtils.transformInlineConstants:
        assertTrue("Expected {@value #SUM} to fold 40+2 to 42 in:\n" + doc,
                doc.contains("Sum: 42"));
        assertTrue("Expected {@value #COMBINED} to fold string concat to \"hello\" in:\n" + doc,
                doc.contains("Combined: \"hello\""));
        // Bare {@value} on a field's own comment resolves to that field's value.
        assertTrue("Expected bare {@value} on MAX to render as 42 in:\n" + doc,
                doc.contains("Max allowed (bare: 42)"));
        assertTrue("Expected bare {@value} on GREETING to render as \"hello\" in:\n" + doc,
                doc.contains("Greeting (bare: \"hello\")"));
        // PROBE: BIG_TIMEOUT = SMALL_TIMEOUT + MEDIUM_TIMEOUT
        // Documented limits: {@value} only resolves when the initializer folds to
        // a ConstantExpression at the phase groovydoc runs (CONVERSION by default).
        // Same-class field references (VariableExpression) aren't resolved that
        // early, and runtime method calls never fold. Both render verbatim.
        assertTrue("BIG_TIMEOUT: cross-field arithmetic should stay verbatim in:\n" + doc,
                doc.contains("Default: {@value}."));
        assertTrue("FOUR: method-call initializer should stay verbatim in:\n" + doc,
                doc.contains("Four is: {@value}."));
    }

    // GROOVY-8025: annotations whose members are closure expressions must
    // not NPE during groovydoc processing. The original bug was in the old
    // SimpleGroovyClassDocAssembler — the refactor to GroovydocVisitor
    // inherently handles ClosureExpression via the ClassCodeVisitorSupport
    // walk, so these fixtures exercise the fixed path.
    public void testAnnotationWithClosureMemberDoesNotCrash() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        String klass = "ClassWithClosureInAnnotation";
        htmlTool.add(List.of(base + "/" + klass + ".groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/" + klass + ".html");
        assertNotNull("Expected a page for the closure-annotated class", doc);
        assertTrue("Expected the class title in:\n" + doc, doc.contains(klass));
    }

    // GROOVY-8025: broader coverage — single-expression closure, no-arg
    // closure with explicit `-> body`, closure with generic-typed params, and
    // multiline closure body, all as annotation members.
    public void testSpockStyleClosureAnnotationsDoNotCrash() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        String klass = "ClassWithSpockStyleAnnotations";
        htmlTool.add(List.of(base + "/" + klass + ".groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/" + klass + ".html");
        assertNotNull("Expected a page for the Spock-style-annotated class", doc);
        assertTrue("Expected method 'simpleClosure' in:\n" + doc, doc.contains("simpleClosure"));
        assertTrue("Expected method 'multilineClosureWithGenericParam' in:\n" + doc,
                doc.contains("multilineClosureWithGenericParam"));
        assertTrue("Expected method 'multilineBodyClosure' in:\n" + doc, doc.contains("multilineBodyClosure"));
    }

    // GROOVY-8877
    public void testScriptTopLevelDocCommentAppearsInOutput() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ScriptWithTopLevelDoc.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/ScriptWithTopLevelDoc.html");
        assertNotNull("Expected a script page", doc);
        assertTrue("Script-level doc should appear in:\n" + doc,
                doc.contains("GROOVY-8877 script-level documentation"));
    }

    // GROOVY-8877: a script whose only /** */ is attached by the parser to a
    // member should NOT also be lifted to script-level, or the same text
    // would appear in three places (script description + method summary
    // first-sentence + method detail) rather than two (summary + detail).
    public void testScriptMemberDocIsNotLiftedToScriptLevel() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        String klass = "ScriptWithOnlyMemberDoc";
        htmlTool.add(List.of(base + "/" + klass + ".groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String doc = output.getText(MOCK_DIR + "/" + base + "/" + klass + ".html");
        assertNotNull("Expected a script page", doc);
        int occurrences = doc.split("member-doc-not-lifted-to-script", -1).length - 1;
        // 2 = summary + detail. 3 would mean the comment was also lifted to
        // the script-level description.
        assertEquals("Member doc should appear exactly twice (summary + detail), got "
                + occurrences + " in:\n" + doc, 2, occurrences);
    }

    // GROOVY-11542 stage 1: verify that /// Markdown doc comments are
    // captured by GroovydocVisitor and surface as rawCommentText on the
    // matching SimpleGroovy*Doc, with the `markdown` flag set. Rendering
    // through CommonMark is stage 2.
    public void testMarkdownDocCaptureStage1() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithMarkdownDoc.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithMarkdownDoc.html");
        assertNotNull("Expected a page for the Markdown-doc class", page);
        // Stage 1: content makes it through (not yet Markdown-rendered, so the
        // raw * and ` characters may appear verbatim in the HTML output).
        assertTrue("Class-level /// body should flow through in:\n" + page,
                page.contains("stage 1 fixture"));
        assertTrue("Field-level /// body should flow through in:\n" + page,
                page.contains("Field-level Markdown doc"));
        assertTrue("Method-level /// body should flow through in:\n" + page,
                page.contains("Method Markdown body"));
        // Block tags inside the /// run still surface — until stage 3 they
        // travel as part of rawCommentText and get picked up by the existing
        // block-tag handling.
        assertTrue("@param x from /// run should be captured in:\n" + page,
                page.contains("an input"));
        assertTrue("@return from /// run should be captured in:\n" + page,
                page.contains("the doubled value"));
    }

    // GROOVY-11542 stage 2: /// Markdown doc comments render through CommonMark;
    // headings shift down (# -> <h3>), bullets, emphasis and fenced code blocks
    // all survive; block tags still flow through the existing TagRenderer.
    public void testMarkdownDocRendersAsHtml() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithMarkdownDoc.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithMarkdownDoc.html");
        assertNotNull(page);
        // Emphasis, code spans, and lists should become HTML.
        assertTrue("Expected **body** to render as <strong>body</strong> in:\n" + page,
                page.contains("<strong>body</strong>"));
        assertTrue("Expected `Markdown` to render as <code>Markdown</code> in:\n" + page,
                page.contains("<code>Markdown</code>"));
        assertTrue("Expected bullet list to render as <ul><li>first bullet</li> in:\n" + page,
                page.contains("<li>first bullet</li>"));
        // Field-level doc: *emphasis* -> <em>emphasis</em>.
        assertTrue("Expected *emphasis* to render as <em>emphasis</em> in:\n" + page,
                page.contains("<em>emphasis</em>"));
        // Block tags from the /// run still reach the rendered output.
        assertTrue("@param description should survive through TagRenderer in:\n" + page,
                page.contains("an input"));
        assertTrue("@return description should survive through TagRenderer in:\n" + page,
                page.contains("the doubled value"));
    }

    // GROOVY-11938 stage 3: {@snippet} markup comments. Recognises three
    // directives inside a snippet body — @highlight, @replace, @link — in
    // either EOL (after code) or standalone-line (applies to next line) form.
    // Markup-comment lines are stripped from the output.
    public void testSnippetMarkupDirectives() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithSnippetMarkup.groovy",
                             base + "/ScriptWithSiblingClassLinks.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithSnippetMarkup.html");
        assertNotNull(page);
        int snippetIdx = page.indexOf("<pre><code class=\"language-groovy\">");
        assertTrue("Snippet block expected in:\n" + page, snippetIdx >= 0);
        int snippetEnd = page.indexOf("</code></pre>", snippetIdx);
        String snippet = page.substring(snippetIdx, snippetEnd + "</code></pre>".length());

        // @highlight: "greet" wrapped in <b>.
        assertTrue("@highlight bold expected around `greet` in:\n" + snippet,
                snippet.contains("<b>greet</b>"));
        // @replace: "TODO: fill this in" string substituted with replacement text.
        assertFalse("Replaced text TODO should NOT appear in snippet:\n" + snippet,
                snippet.contains("TODO: fill this in"));
        assertTrue("Replacement text expected in snippet:\n" + snippet,
                snippet.contains("see SiblingHelper"));
        // @link: SiblingHelper wrapped in an anchor to its doc page.
        assertTrue("@link anchor wrapping SiblingHelper expected in:\n" + snippet,
                snippet.matches("(?s).*<a href=\"[^\"]*SiblingHelper\\.html\">SiblingHelper</a>.*"));
        // Markup-comment lines themselves must be stripped.
        assertFalse("// @highlight directive line should be stripped:\n" + snippet,
                snippet.contains("@highlight"));
        assertFalse("// @replace directive line should be stripped:\n" + snippet,
                snippet.contains("@replace"));
        assertFalse("// @link directive line should be stripped:\n" + snippet,
                snippet.contains("@link"));
    }

    // GROOVY-11945: @apiNote / @implSpec / @implNote render with their
    // Javadoc-standard display headings (not the raw lowercase tag names),
    // and their bodies get collated into the tags section alongside
    // @param / @return.
    public void testImplNoteTagsRenderWithProperHeadings() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithImplNoteTags.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithImplNoteTags.html");
        assertNotNull(page);
        assertTrue("API Note heading expected in:\n" + page,
                page.contains("<B>API Note:</B>"));
        assertTrue("Implementation Requirements heading expected in:\n" + page,
                page.contains("<B>Implementation Requirements:</B>"));
        assertTrue("Implementation Note heading expected in:\n" + page,
                page.contains("<B>Implementation Note:</B>"));
        // Bodies reach the rendered page.
        assertTrue("apiNote body expected:\n" + page,
                page.contains("null-check the result"));
        assertTrue("implSpec body expected:\n" + page,
                page.contains("non-negative contract"));
        assertTrue("implNote body expected:\n" + page,
                page.contains("uses a sieve for efficiency"));
        // Raw lowercase tag name should NOT appear as a heading.
        assertFalse("Raw 'implNote:' should not leak as a heading:\n" + page,
                page.contains("<B>implNote:</B>"));
        assertFalse("Raw 'implSpec:' should not leak as a heading:\n" + page,
                page.contains("<B>implSpec:</B>"));
        assertFalse("Raw 'apiNote:' should not leak as a heading:\n" + page,
                page.contains("<B>apiNote:</B>"));
    }

    // GROOVY-11938 stage 3 region support: a directive with region="name"
    // activates at its line and applies until the matching // @end marker.
    public void testSnippetMarkupRegionScoping() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithSnippetMarkup.groovy",
                             base + "/ScriptWithSiblingClassLinks.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithSnippetMarkup.html");
        assertNotNull(page);
        // Locate the SECOND snippet block (the region-scoped one).
        int first = page.indexOf("<pre><code class=\"language-groovy\">");
        int second = page.indexOf("<pre><code class=\"language-groovy\">", first + 1);
        assertTrue("Second snippet block expected", second > first);
        int end = page.indexOf("</code></pre>", second);
        String snippet = page.substring(second, end);

        // Inside region: both `call` and `call2` should have <mark> wrapping "call".
        assertTrue("Region-scoped highlight should mark `call` inside def call(x):\n" + snippet,
                snippet.contains("def <mark>call</mark>(x)"));
        assertTrue("Region-scoped highlight should mark `call` inside def call2(x):\n" + snippet,
                snippet.contains("def <mark>call</mark>2(x)"));
        // Outside region: `call` on the afterRegion line should NOT be highlighted.
        int afterIdx = snippet.indexOf("afterRegion");
        String afterPortion = snippet.substring(afterIdx);
        assertFalse("Highlight must not leak past @end into afterRegion line:\n" + afterPortion,
                afterPortion.contains("<mark>"));
        // @end / @start marker lines themselves stripped.
        assertFalse("@end region marker line should be stripped:\n" + snippet,
                snippet.contains("@end"));
    }

    // GROOVY-11938 stage 5: Markdown and {@snippet} interop. A /// doc comment
    // containing a Markdown heading, an inline {@snippet}, and a fenced
    // Markdown code block emits three distinct HTML chunks without the
    // rendering passes interfering.
    public void testMarkdownAndSnippetInterop() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithMarkdownAndSnippet.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithMarkdownAndSnippet.html");
        assertNotNull(page);
        // The Markdown heading (# Markdown + snippet interop) becomes <h3>
        // (heading-shift from class-level context — see MarkdownRenderer).
        assertTrue("Expected Markdown # heading shifted to <h3> in:\n" + page,
                page.contains("<h3>Markdown + snippet interop</h3>"));
        // Both code blocks should emit <pre><code class="language-groovy">.
        // The {@snippet lang="groovy" :...} one (TagRenderer output) and the
        // fenced ```groovy block (CommonMark output) share the same class.
        int occurrences = page.split("<code class=\"language-groovy\">", -1).length - 1;
        assertEquals("Expected exactly two <code class=\"language-groovy\"> blocks — "
                + "one from {@snippet}, one from the Markdown fence — in:\n" + page,
                2, occurrences);
        // {@snippet} body: `$name` dollar-sign must be preserved (no GString
        // interpretation, no mangling), inside-string quotes get HTML-escaped
        // to &quot; but the value text is intact.
        assertTrue("Expected snippet body to preserve $name literal in:\n" + page,
                page.contains("$name!"));
        assertTrue("Expected snippet greet() body in:\n" + page,
                page.contains("def greet(name)"));
        // Markdown fenced block body: CommonMark HTML-escapes quotes the same
        // way TagRenderer does, and our CodeBraceMasker converts `{` / `}`
        // inside code fences to numeric entities so inline-tag-looking text
        // stays literal.
        assertTrue("Expected fenced-block farewell body with masked braces in:\n" + page,
                page.contains("def farewell() &#123; &quot;Bye!&quot; &#125;"));
    }

    // GROOVY-11947: -theme=light locks the palette to light. The stylesheet
    // should emit only the light :root values and NO prefers-color-scheme
    // dark @media block. Prism templates link only the light theme.
    public void testThemeLightLocksLightPaletteAndOmitsDarkMedia() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("theme", "light");
        props.put("syntaxHighlighter", "prism");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String stylesheet = output.getText(MOCK_DIR + "/stylesheet.css");
        assertNotNull(stylesheet);
        // Light-palette values only; no dark media block.
        assertTrue("Light --fg expected in stylesheet",
                stylesheet.contains("--fg: #343437"));
        assertFalse("@media (prefers-color-scheme: dark) must NOT appear when theme=light",
                stylesheet.contains("@media (prefers-color-scheme: dark) {"));

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertTrue("Prism light theme expected (no media attr) when theme=light:\n" + classPage,
                classPage.matches("(?s).*href=\"[^\"]*prism\\.min\\.css\">.*"));
        assertFalse("Prism dark theme must NOT be linked when theme=light",
                classPage.contains("prism-dark.min.css"));
    }

    // GROOVY-11947: -theme=dark locks the palette to dark. The stylesheet
    // :root gets dark values; no @media block. Prism links only the dark theme.
    public void testThemeDarkLocksDarkPaletteAndOmitsMediaBlock() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("theme", "dark");
        props.put("syntaxHighlighter", "prism");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String stylesheet = output.getText(MOCK_DIR + "/stylesheet.css");
        assertNotNull(stylesheet);
        assertTrue("Dark --fg expected in stylesheet",
                stylesheet.contains("--fg: #e4e4e4"));
        assertFalse("Light --fg should NOT appear when theme=dark",
                stylesheet.contains("--fg: #343437"));
        assertFalse("@media (prefers-color-scheme: dark) must NOT appear when theme=dark",
                stylesheet.contains("@media (prefers-color-scheme: dark) {"));

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertTrue("Prism dark theme expected (no media attr) when theme=dark:\n" + classPage,
                classPage.matches("(?s).*href=\"[^\"]*prism-dark\\.min\\.css\">.*"));
        // Light theme link must NOT appear — careful not to confuse with prism-dark.min.css
        // which also contains 'prism.' — check for 'prism.min.css' with the expected href terminator.
        assertFalse("Prism light theme must NOT be linked when theme=dark:\n" + classPage,
                classPage.matches("(?s).*href=\"[^\"]*/prism\\.min\\.css[\"].*"));
    }

    // GROOVY-11947: -theme=auto (default) keeps the media-query-driven
    // behaviour — both palettes and both Prism themes present with media.
    public void testThemeAutoKeepsMediaQueryBehaviour() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("theme", "auto");
        props.put("syntaxHighlighter", "prism");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String stylesheet = output.getText(MOCK_DIR + "/stylesheet.css");
        assertNotNull(stylesheet);
        assertTrue(stylesheet.contains("--fg: #343437"));
        assertTrue(stylesheet.contains("@media (prefers-color-scheme: dark) {"));

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertTrue(classPage.contains("prism.min.css\" media=\"(prefers-color-scheme: light)\""));
        assertTrue(classPage.contains("prism-dark.min.css\" media=\"(prefers-color-scheme: dark)\""));
    }

    // GROOVY-11946: the stylesheet declares CSS custom properties on :root and
    // a matching @media (prefers-color-scheme: dark) override. OS-light readers
    // see no visual change; OS-dark readers get the dark palette automatically.
    public void testStylesheetContainsCssVariablesAndDarkMediaBlock() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String stylesheet = output.getText(MOCK_DIR + "/stylesheet.css");
        assertNotNull("stylesheet.css expected in output", stylesheet);
        // Semantic tokens live on :root and are referenced via var(--...).
        assertTrue(":root block expected:\n" + stylesheet.substring(0, Math.min(stylesheet.length(), 600)),
                stylesheet.contains(":root"));
        assertTrue("--fg token expected:\n" + stylesheet.substring(0, Math.min(stylesheet.length(), 600)),
                stylesheet.contains("--fg:"));
        assertTrue("var(--fg) reference expected somewhere in stylesheet",
                stylesheet.contains("var(--fg)"));
        // Dark-mode media query with token overrides.
        assertTrue("@media prefers-color-scheme: dark block expected in stylesheet",
                stylesheet.contains("@media (prefers-color-scheme: dark) {"));
        // Key tokens should be redefined inside the dark block — a reasonable
        // assertion that at minimum --fg and --bg have dark-mode values.
        int darkStart = stylesheet.indexOf("@media (prefers-color-scheme: dark)");
        String darkBlock = stylesheet.substring(darkStart);
        assertTrue("--fg override expected in dark block:\n" + darkBlock,
                darkBlock.contains("--fg:"));
        assertTrue("--bg override expected in dark block:\n" + darkBlock,
                darkBlock.contains("--bg:"));
    }

    // GROOVY-11946: when syntax highlighting is on, both Prism themes should
    // be linked with matching prefers-color-scheme media attributes so the
    // browser auto-selects the appropriate one.
    public void testPrismThemesHaveColorSchemeMediaAttributes() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("syntaxHighlighter", "prism");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertTrue("Light Prism theme should carry prefers-color-scheme:light media:\n" + classPage,
                classPage.contains("prism.min.css\" media=\"(prefers-color-scheme: light)\""));
        assertTrue("Dark Prism theme should be linked with prefers-color-scheme:dark media:\n" + classPage,
                classPage.contains("prism-dark.min.css\" media=\"(prefers-color-scheme: dark)\""));
    }

    // GROOVY-11938 stage 4: opt-in client-side syntax highlighting via Prism.
    // When -syntaxHighlighter=prism is passed, the head of each content page
    // should include prism.min.css and the bundled language scripts; when
    // it's unset (default), none of the prism refs should appear.
    public void testSyntaxHighlighterPrismInjectsHead() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("syntaxHighlighter", "prism");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));

        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertTrue("Expected prism.min.css <link> in class page head:\n" + classPage,
                classPage.contains("prism.min.css"));
        assertTrue("Expected prism.js <script> in class page head:\n" + classPage,
                classPage.contains("prism.js"));
        assertTrue("Expected prism-groovy language component in class page head:\n" + classPage,
                classPage.contains("prism-groovy.min.js"));

        String overview = output.getText(MOCK_DIR + "/overview-summary.html");
        assertNotNull(overview);
        assertTrue("Expected prism.min.css in overview:\n" + overview,
                overview.contains("prism.min.css"));
    }

    public void testSyntaxHighlighterOffByDefault() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/DocumentedClass.groovy"));
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertFalse("Prism should NOT be referenced when default (syntaxHighlighter=none):\n" + classPage,
                classPage.contains("prism.js"));
        assertFalse("Prism CSS should NOT be referenced when default:\n" + classPage,
                classPage.contains("prism.min.css"));
    }

    // GROOVY-11542 stage 3a: inline tags inside a Markdown code fence or
    // code span stay literal (TagRenderer must not expand them).
    public void testMarkdownCodeFencePreservesInlineTagSyntax() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ClassWithMarkdownCodeFence.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ClassWithMarkdownCodeFence.html");
        assertNotNull(page);
        // Outside the fence, {@code markdown-outside-fence} should expand —
        // TagRenderer renders it as <CODE>markdown-outside-fence</CODE> (or
        // similar) wrapped in code markup.
        assertTrue("Expected {@code ...} outside the fence to expand in:\n" + page,
                page.contains("markdown-outside-fence"));
        // Inside a fenced block, {@link Foo} must stay LITERAL. The masked
        // braces come back as &#123; / &#125; numeric entities — the browser
        // shows them as { / } but TagRenderer never saw them as a tag.
        assertFalse("{@link Foo} inside a ``` fence must NOT render as an anchor in:\n" + page,
                page.contains(">Foo</a>"));
        assertTrue("Inside fence, `{@link Foo}` should appear with masked braces as entities in:\n" + page,
                page.contains("&#123;@link Foo&#125;"));
        // Inside an inline `...` span, same rule applies to {@link Bar}.
        assertFalse("{@link Bar} inside a code span must NOT render as an anchor in:\n" + page,
                page.contains(">Bar</a>"));
        assertTrue("Inside a code span, `{@link Bar}` should appear with masked braces in:\n" + page,
                page.contains("&#123;@link Bar&#125;"));
    }

    // GROOVY-11542 stage 3b: /// leading a script file lifts to the
    // synthetic Script class, parallel to the GROOVY-8877 /** */ lift.
    public void testMarkdownScriptLevelDocLifts() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ScriptWithMarkdownTopLevelDoc.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String page = output.getText(MOCK_DIR + "/" + base + "/ScriptWithMarkdownTopLevelDoc.html");
        assertNotNull("Expected a page for the Markdown-script", page);
        assertTrue("Expected the script-level /// body to surface on the script page in:\n" + page,
                page.contains("Markdown"));
        // The **Markdown** emphasis in the body should render as <strong>.
        assertTrue("Expected **Markdown** to render as <strong>Markdown</strong> in:\n" + page,
                page.contains("<strong>Markdown</strong>"));
    }

    // Task #23: {@link} cross-references between a script and classes declared
    // in the same source file resolve to working hyperlinks. The script and
    // sibling classes compile to top-level classes in the same package, so the
    // existing same-package {@link} resolver handles them symmetrically — no
    // code change needed; this test locks the behaviour down.
    public void testScriptAndSiblingClassCrossLinks() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/ScriptWithSiblingClassLinks.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String scriptPage = output.getText(MOCK_DIR + "/" + base + "/ScriptWithSiblingClassLinks.html");
        String helperPage = output.getText(MOCK_DIR + "/" + base + "/SiblingHelper.html");
        assertNotNull("Expected a page for the script", scriptPage);
        assertNotNull("Expected a page for the sibling class", helperPage);

        // Script → sibling class: {@link SiblingHelper#otherMethod()}
        assertTrue("Script page should link to SiblingHelper.otherMethod in:\n" + scriptPage,
                scriptPage.contains("SiblingHelper.html#otherMethod()")
                        && scriptPage.contains(">SiblingHelper.otherMethod</a>"));

        // Sibling class → script: {@link ScriptWithSiblingClassLinks#method()}
        assertTrue("Sibling page should link to ScriptWithSiblingClassLinks.method in:\n" + helperPage,
                helperPage.contains("ScriptWithSiblingClassLinks.html#method()")
                        && helperPage.contains(">ScriptWithSiblingClassLinks.method</a>"));
    }

    // GROOVY-11943: -noindex / -nodeprecatedlist / -nohelp skip the matching
    // auxiliary top-level page AND suppress its nav-bar link on every page
    // that would otherwise reference it.
    public void testDisableFlagsSkipPageAndNavLink() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        Properties props = new Properties();
        props.put("noIndex", "true");
        props.put("noDeprecatedList", "true");
        props.put("noHelp", "true");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/DocumentedClass.groovy"));

        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        assertNull("index-all.html should be skipped when noIndex=true",
                output.getText(MOCK_DIR + "/index-all.html"));
        assertNull("deprecated-list.html should be skipped when noDeprecatedList=true",
                output.getText(MOCK_DIR + "/deprecated-list.html"));
        assertNull("help-doc.html should be skipped when noHelp=true",
                output.getText(MOCK_DIR + "/help-doc.html"));

        String overview = output.getText(MOCK_DIR + "/overview-summary.html");
        assertNotNull(overview);
        assertFalse("Overview nav should not link to suppressed index-all.html:\n" + overview,
                overview.contains("href=\"index-all.html\""));
        assertFalse("Overview nav should not link to suppressed deprecated-list.html:\n" + overview,
                overview.contains("href=\"deprecated-list.html\""));
        assertFalse("Overview nav should not link to suppressed help-doc.html:\n" + overview,
                overview.contains("href=\"help-doc.html\""));

        String classPage = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        assertNotNull(classPage);
        assertFalse("Class nav should not link to suppressed index-all.html:\n" + classPage,
                classPage.contains("index-all.html"));
        assertFalse("Class nav should not link to suppressed deprecated-list.html:\n" + classPage,
                classPage.contains("deprecated-list.html"));
        assertFalse("Class nav should not link to suppressed help-doc.html:\n" + classPage,
                classPage.contains("help-doc.html"));
    }

    // GROOVY-11942: overview-tree.html renders the class hierarchy rooted at
    // java.lang.Object and an Interface Hierarchy section for traits/interfaces.
    public void testTreePagesRenderExpectedHierarchy() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(
                base + "/GroovyClassWithMultipleInterfaces.groovy",
                base + "/GroovyInterface1.groovy"));

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        String overview = output.getText(MOCK_DIR + "/overview-tree.html");
        assertNotNull("Expected overview-tree.html in output", overview);
        assertTrue("Overview tree should contain Class Hierarchy section:\n" + overview,
                overview.contains("Class Hierarchy"));
        assertTrue("Overview tree should list GroovyClassWithMultipleInterfaces under Object:\n" + overview,
                overview.contains("GroovyClassWithMultipleInterfaces"));
        assertTrue("Overview tree should contain Interface Hierarchy section:\n" + overview,
                overview.contains("Interface Hierarchy"));
        assertTrue("Interface tree should list GroovyInterface1:\n" + overview,
                overview.contains("GroovyInterface1"));

        String pkgTree = output.getText(MOCK_DIR + "/" + base + "/package-tree.html");
        assertNotNull("Expected package-tree.html in output", pkgTree);
        assertTrue("Package tree should mention the package name:\n" + pkgTree,
                pkgTree.contains("org.codehaus.groovy.tools.groovydoc.testfiles"));
        assertTrue("Package tree should link back to All Packages overview:\n" + pkgTree,
                pkgTree.contains("overview-tree.html"));

        // Nav bar on the class page should now link to package-tree.html.
        String klassPage = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");
        assertNotNull(klassPage);
        assertTrue("Class page nav should link to package-tree.html:\n" + klassPage,
                klassPage.contains("href=\"package-tree.html\""));
    }

    // GROOVY-10162
    public void testAbstractMethodEnumWithPerConstantBodiesDoesNotProduceAnonymousInnerPages() throws Exception {
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        String klass = "EnumWithAbstractMethodAndConstantBodies";
        Properties props = new Properties();
        // phase 7 = CLASS_GENERATION, by which point the per-constant anonymous
        // inner classes have been synthesised. The fix must keep them out of
        // the doc output at any phase.
        props.put("phaseOverride", "7");
        GroovyDocTool tool = makeHtmltool(new ArrayList<>(), null, props);
        tool.add(List.of(base + "/" + klass + ".groovy"));

        MockOutputTool output = new MockOutputTool();
        tool.renderToOutput(output, MOCK_DIR);

        String enumDoc = output.getText(MOCK_DIR + "/" + base + "/" + klass + ".html");
        assertNotNull("Expected the enum's own HTML page", enumDoc);

        assertNull("Unexpected anonymous inner page " + klass + ".1.html",
                output.getText(MOCK_DIR + "/" + base + "/" + klass + ".1.html"));
        assertNull("Unexpected anonymous inner page " + klass + ".2.html",
                output.getText(MOCK_DIR + "/" + base + "/" + klass + ".2.html"));
    }

    // GROOVY-5986
    public void testDocFilesAndSnippetFilesAreCopiedIntoOutput() throws Exception {
        String fixtureSourcePath = "src/test/resources/docfiles-fixture";
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/docfiles";

        Path tmpOut = Files.createTempDirectory("groovydoc-docfiles-");
        try {
            GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src/main/resources"),
                new String[]{fixtureSourcePath},
                GroovyDocTemplateInfo.DEFAULT_DOC_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_PACKAGE_TEMPLATES,
                GroovyDocTemplateInfo.DEFAULT_CLASS_TEMPLATES,
                new ArrayList<>(), null, new Properties()
            );
            tool.add(List.of(pkg + "/HasDocFiles.groovy"));
            tool.renderToOutput(new FileOutputTool(), tmpOut.toString());

            Path docFilesSample = tmpOut.resolve(pkg + "/doc-files/sample.html");
            Path docFilesNested = tmpOut.resolve(pkg + "/doc-files/sub/nested.txt");
            Path snippetFile    = tmpOut.resolve(pkg + "/snippet-files/Example.groovy");

            assertTrue("Expected doc-files/sample.html to be copied, not found at " + docFilesSample,
                    Files.isRegularFile(docFilesSample));
            assertTrue("Expected doc-files/sub/nested.txt (nested subdir) to be copied, not found at " + docFilesNested,
                    Files.isRegularFile(docFilesNested));
            assertTrue("Expected snippet-files/Example.groovy to be copied, not found at " + snippetFile,
                    Files.isRegularFile(snippetFile));

            String sampleContent = Files.readString(docFilesSample);
            assertTrue("doc-files/sample.html content mismatch", sampleContent.contains("GROOVY-5986 sample doc-files content"));
            String nestedContent = Files.readString(docFilesNested);
            assertTrue("doc-files/sub/nested.txt content mismatch", nestedContent.contains("nested doc-files content"));
            String snippetContent = Files.readString(snippetFile);
            assertTrue("snippet-files/Example.groovy content mismatch", snippetContent.contains("hello from Example"));
        } finally {
            deleteRecursively(tmpOut);
        }
    }

    private static void deleteRecursively(Path root) throws IOException {
        if (!Files.exists(root)) return;
        try (Stream<Path> stream = Files.walk(root)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try { Files.delete(p); } catch (IOException ignored) {}
            });
        }
    }

    private static List<String> createSwitchExpressionJavaSources(Path root, int count) throws IOException {
        String pkg = "org/codehaus/groovy/tools/groovydoc/testfiles/switchexpr";
        String packageName = pkg.replace('/', '.');
        Path pkgDir = root.resolve(pkg);
        Files.createDirectories(pkgDir);

        List<String> sources = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            String className = "SwitchExpression" + i;
            Files.writeString(pkgDir.resolve(className + ".java"), switchExpressionJavaSource(packageName, className));
            sources.add(pkg + "/" + className + ".java");
        }
        return sources;
    }

    private static String switchExpressionJavaSource(String packageName, String className) {
        return "package " + packageName + ";\n"
                + "/**\n"
                + " * Exercise Java switch expressions with yield.\n"
                + " */\n"
                + "public class " + className + " {\n"
                + "    public int number(int value) {\n"
                + "        return switch (value) {\n"
                + "            case 0 -> 0;\n"
                + "            default -> {\n"
                + "                yield value + 1;\n"
                + "            }\n"
                + "        };\n"
                + "    }\n"
                + "}\n";
    }

    // GROOVY-9057
    public void testParseErrorIsTrackedInErrorCount() throws Exception {
        assertEquals("Initial error count should be zero", 0, xmlToolForTests.getErrorCount());
        List<String> srcList = new ArrayList<>();
        srcList.add("broken/BrokenSyntax.groovy");
        xmlToolForTests.add(srcList);
        assertTrue("Expected errorCount > 0 after parsing malformed source, was "
                + xmlToolForTests.getErrorCount(), xmlToolForTests.getErrorCount() > 0);
    }

    public void testPlainGroovyDocTool() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/GroovyDocToolTest.java");
        plainTool.add(srcList);
        GroovyRootDoc root = plainTool.getRootDoc();

        // loop through classes in tree
        GroovyClassDoc[] classDocs = root.classes();
        for (int i = 0; i < classDocs.length; i++) {
            GroovyClassDoc clazz = root.classes()[i];
            assertEquals("GroovyDocToolTest", clazz.name());

            // loop through methods in class
            boolean seenThisMethod = false;
            GroovyMethodDoc[] methodDocs = clazz.methods();
            for (int j = 0; j < methodDocs.length; j++) {
                GroovyMethodDoc method = clazz.methods()[j];
                if ("testPlainGroovyDocTool".equals(method.name())) {
                    seenThisMethod = true;
                    break;
                }
            }
            assertTrue(seenThisMethod);
        }
    }

    public void testGroovyDocTheCategoryMethodClass() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("groovy/cli/picocli/CliBuilder.groovy");
        srcList.add("groovy/test/GroovyLogTestCase.groovy");
        srcList.add("groovy/mock/interceptor/StrictExpectation.groovy");
        srcList.add("org/codehaus/groovy/runtime/GroovyCategorySupport.java");
        srcList.add("org/codehaus/groovy/runtime/ConvertedMap.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);

        String groovyCategorySupportDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.html");
        assertTrue("Expect hasCategoryInAnyThread in:\n" + groovyCategorySupportDocument, groovyCategorySupportDocument != null &&
                groovyCategorySupportDocument.indexOf("<method modifiers=\"public static \" returns=\"boolean\" name=\"hasCategoryInAnyThread\">") > 0);

        String categoryMethodDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethodList.html");
        assertNotNull("Expected to find GroovyCategorySupport.CategoryMethodList in: " + output, categoryMethodDocument);
        assertTrue("Expected add in:\n" + categoryMethodDocument, categoryMethodDocument != null &&
                categoryMethodDocument.indexOf("<method modifiers=\"public \" returns=\"boolean\" name=\"add\">") > 0);

        String packageDocument = output.getText(MOCK_DIR + "/org/codehaus/groovy/runtime/packageDocStructuredData.xml");
        assertNotNull("Failed to find 'packageDocStructuredData.xml' in generated output", packageDocument);
        assertTrue(packageDocument.indexOf("<class name=\"GroovyCategorySupport\" />") > 0);
        assertTrue("Expected GroovyCategorySupport.CategoryMethod in:\n" + packageDocument, packageDocument.indexOf("<class name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);

        String rootDocument = output.getText(MOCK_DIR + "/rootDocStructuredData.xml");
        assertNotNull("Failed to find 'rootDocStructuredData.xml' in generated output", rootDocument);
        assertTrue(rootDocument.indexOf("<package name=\"org/codehaus/groovy/runtime\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport\" name=\"GroovyCategorySupport\" />") > 0);
        assertTrue(rootDocument.indexOf("<class path=\"org/codehaus/groovy/runtime/GroovyCategorySupport.CategoryMethod\" name=\"GroovyCategorySupport.CategoryMethod\" />") > 0);
    }

    public void testConstructors() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/TestConstructors";
        srcList.add(base + ".groovy");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String constructorDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, constructorDoc);
        assertTrue(constructorDoc.indexOf("<constructor modifiers=\"public \" name=\"TestConstructors\">") > 0);
        assertTrue(constructorDoc.indexOf("<parameter type=\"java.lang.ClassLoader\" name=\"parent\" />") > 0);
    }

    public void testInterfaceConstructor() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        final String groovyInterface = "GroovyInterface1";
        htmlTool.add(List.of(base + "/" + groovyInterface + ".groovy"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/"+ groovyInterface +".html");

        final Matcher ctor = Pattern.compile(Pattern.quote("GroovyInterface1()")).matcher(groovydoc);

        assertFalse("The Groovy interface should not have default constructor", ctor.find());
    }

    public void testClassComment() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc,builderDoc.contains("A class comment"));
    }

    public void testMethodComment() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithMethodComment.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String defTabColDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithMethodComment.html");
        assertTrue(defTabColDoc.contains("This is a method comment"));
    }

    public void testPackageName() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<containingPackage name=\"org/codehaus/groovy/tools/groovydoc/testfiles\">org.codehaus.groovy.tools.groovydoc.testfiles</containingPackage>"));
    }

    public void testExtendsClauseWithoutSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<extends>BuilderSupport</extends>"));
    }

    public void testExtendsClauseWithSuperClassInTree() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/Builder";
        srcList.add(base + ".java");
        srcList.add("groovy/util/BuilderSupport.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String builderDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, builderDoc);
        assertTrue(builderDoc.contains("<extends>BuilderSupport</extends>"));
    }

    public void testInterfaceExtendsClauseWithMultipleInterfaces() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterfaceWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterfaceWithMultipleInterfaces.html");
        assertNotNull("GroovyInterfaceWithMultipleInterfaces not found in: " + output, groovyClassDoc);
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterfaceWithMultipleInterfaces.html");
        assertNotNull("JavaInterfaceWithMultipleInterfaces not found in: " + output, javaClassDoc);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(javaClassDoc, javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testImplementsClauseWithMultipleInterfaces() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.html");
        assertNotNull("GroovyClassWithMultipleInterfaces not found in: " + output, groovyClassDoc);
        assertTrue(groovyClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
        assertNotNull("JavaClassWithMultipleInterfaces not found in: " + output, javaClassDoc);
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testFullyQualifiedNamesInImplementsClause() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.java");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/JavaInterface1.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);

        String groovyClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/GroovyClassWithMultipleInterfaces.html");
        assertTrue(groovyClassDoc, groovyClassDoc.indexOf("<interface>GroovyInterface1</interface>") > 0);
        assertTrue(groovyClassDoc, groovyClassDoc.indexOf("<interface>Runnable</interface>") > 0);

        String javaClassDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithMultipleInterfaces.html");
        assertTrue(javaClassDoc.indexOf("<interface>JavaInterface1</interface>") > 0);
        assertTrue(javaClassDoc.indexOf("<interface>Runnable</interface>") > 0);
    }

    public void testDefaultPackage() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("DefaultPackageClassSupport.java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/DefaultPackage/DefaultPackageClassSupport.html");
        assertTrue(doc.indexOf("<extends>GroovyTestCase</extends>") > 0);
    }

    public void testJavaClassMultiCatch() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/MultiCatchExample";
        srcList.add(base + ".java");
        xmlToolForTests.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlToolForTests.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, doc);
        assertTrue(doc, doc.contains("foo has a multi-catch exception inside"));
    }

    public void testStaticModifier() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/StaticModifier";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String staticModifierDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, staticModifierDoc);
        assertTrue("static not found in: \"" + staticModifierDoc + "\"", staticModifierDoc.contains("static"));
    }

    public void testAnonymousInnerClassMethodsNotIncluded() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/ClassWithAnonymousInnerClass";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String classWithAnonymousInnerClassDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, classWithAnonymousInnerClassDoc);
        assertTrue("visibleMethod missing from: \"" + classWithAnonymousInnerClassDoc + "\"",
                classWithAnonymousInnerClassDoc.contains("name=\"visibleMethod\""));
        assertEquals("Anonymous class getType() leaked into outer doc:\n" + classWithAnonymousInnerClassDoc,
                0, StringGroovyMethods.count(classWithAnonymousInnerClassDoc, "name=\"getType\""));
        assertEquals("Anonymous class getValue() leaked into outer doc:\n" + classWithAnonymousInnerClassDoc,
                0, StringGroovyMethods.count(classWithAnonymousInnerClassDoc, "name=\"getValue\""));
        assertFalse("Anonymous field leaked into outer doc:\n" + classWithAnonymousInnerClassDoc,
                classWithAnonymousInnerClassDoc.contains("hiddenField"));
        assertFalse("Anonymous-body local type leaked into outer doc:\n" + classWithAnonymousInnerClassDoc,
                classWithAnonymousInnerClassDoc.contains("HiddenType"));

        String namedNestedDoc = output.getText(MOCK_DIR + "/" + base + ".NamedNested.html");
        assertNotNull("Expected GroovyDoc for named nested class " + base + ".NamedNested", namedNestedDoc);
        assertTrue("visibleNestedMethod missing from named nested doc:\n" + namedNestedDoc,
                namedNestedDoc.contains("name=\"visibleNestedMethod\""));
    }

    public void testJavaEnumConstantBodyMethodsNotIncluded() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/JavaEnumWithConstantBodies";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String javaEnumDoc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, javaEnumDoc);
        assertEquals("Per-constant enum class bodies leaked draw() implementations into enum doc:\n" + javaEnumDoc,
                1, StringGroovyMethods.count(javaEnumDoc, "name=\"draw\""));
    }

    public void testSqlAnonymousParameterImplementationsDoNotLeakIntoClassDoc() throws Exception {
        GroovyDocTool sqlXmlTool = makeXmlTool(new ArrayList<>(), new Properties(),
                new String[]{"subprojects/groovy-sql/src/main/java", "../groovy-sql/src/main/java"});
        sqlXmlTool.add(List.of("groovy/sql/Sql.java"));

        MockOutputTool output = new MockOutputTool();
        sqlXmlTool.renderToOutput(output, MOCK_DIR);

        String sqlDoc = output.getText(MOCK_DIR + "/groovy/sql/Sql.html");
        assertNotNull("Expected GroovyDoc for groovy/sql/Sql", sqlDoc);
        assertEquals("Sql doc should not expose getType() from anonymous parameter implementations:\n" + sqlDoc,
                0, StringGroovyMethods.count(sqlDoc, "name=\"getType\""));
        assertEquals("Sql doc should not expose getValue() from anonymous parameter implementations:\n" + sqlDoc,
                0, StringGroovyMethods.count(sqlDoc, "name=\"getValue\""));
    }

    public void testJavaClassWithDiamondOperator() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for " + base, doc);
        assertTrue("stringList not found in: \"" + doc + "\"", doc.contains("stringList"));
    }

    public void testJavaStaticNestedClassWithDiamondOperator() throws Exception {
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/JavaStaticNestedClassWithDiamond";
        srcList.add(base + ".java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/" + base + ".html");
        assertNotNull("No GroovyDoc found for outer class " + base, doc);
        assertTrue("Outer class expectedObject not found in: \"" + doc + "\"", doc.contains("expectedObject"));
        String docNested = output.getText(MOCK_DIR + "/" + base + ".Nested.html");
        assertNotNull("No GroovyDoc found for nested class " + base, docNested);
        assertTrue("Nested class comment not found in: \"" + docNested + "\"", docNested.contains("static nested class comment"));
    }

    public void testVisibilityPublic() throws Exception {
        Properties props = new Properties();
        props.put("publicScope", "true");
        testVisibility(props, true, false, false, false);
    }

    public void testVisibilityProtected() throws Exception {
        Properties props = new Properties();
        props.put("protectedScope", "true");
        testVisibility(props, true, true, false, false);
    }

    public void testVisibilityPackage() throws Exception {
        Properties props = new Properties();
        props.put("packageScope", "true");
        props.put("phaseOverride", "7");
        testVisibility(props, true, true, true, false);
    }

    public void testVisibilityPrivate() throws Exception {
        Properties props = new Properties();
        props.put("privateScope", "true");
        props.put("phaseOverride", "7");
        testVisibility(props, true, true, true, true);
    }

    public void testSinglePropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "str properties should be there", "<a href=\"#str\">str</a>", true);
    }

    public void testReOrderPropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "str1 properties should be there", "<a href=\"#str1\">str1</a>", true);
    }

    public void testCheckOtherTypesPropertiesFromGetterSetter() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "int properties should be there", "<a href=\"#int\">int</a>", true);
    }

    public void testPropertiesShouldNotBePresentForGetterAlone() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "shouldNotBePresent properties shouldn't be there", "<a href=\"#shouldNotBePresent\">shouldNotBePresent</a>", false);
    }

    public void testPropertiesPublicGetPrivateSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_public_get_private_set shouldn't be present"
                    , "<a href=\"#_public_get_private_set\">_public_get_private_set</a>", false);
    }

    public void testPropertiesPrivateGetPublicSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_private_get_public_set shouldn't be present",
                "<a href=\"#_private_get_public_set\">_private_get_public_set</a>", false);
    }

    public void testPropertiesPrivateGetPrivateSet() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "_private_get_private_set shouldn't be present",
                "<a href=\"#_private_get_private_set\">_private_get_private_set</a>", false);
    }

    public void testPropertiesShouldBePresentForSetIsBooleanType() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "testBoolean properties should be there", "<a href=\"#testBoolean\">testBoolean</a>", true);
    }

    public void testPropertiesShouldBePresentForIsSetBooleanType() throws Exception {
        testPropertiesFromGetterSetter("GeneratePropertyFromGetSet", "testBoolean2 properties should be there","<a href=\"#testBoolean2\">testBoolean2</a>", true);
    }

    private void testPropertiesFromGetterSetter(String fileName, String assertMessage,String expected,boolean isTrue) throws Exception {
        htmlTool = makeHtmltool(new ArrayList<>(), null, new Properties());
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/";
        srcList.add(base + fileName + ".groovy");
        htmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String exampleClass = output.getText(MOCK_DIR + "/" + base + fileName + ".html");
        if (isTrue)
            assertTrue(assertMessage, exampleClass.contains(expected));
        else
            assertFalse(assertMessage,exampleClass.contains(expected));
    }

    private void testVisibility(Properties props, boolean a, boolean b, boolean c, boolean d) throws Exception {
        htmlTool = makeHtmltool(new ArrayList<>(), null, props);
        List<String> srcList = new ArrayList<>();
        String base = "org/codehaus/groovy/tools/groovydoc/testfiles/ExampleVisibility";
        srcList.add(base + "G.groovy");
        srcList.add(base + "J.java");
        htmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String javaExampleClass = output.getText(MOCK_DIR + "/" + base + "J.html");
        assertMethodVisibility(base + "J", output, javaExampleClass, a, b, c, d);
        String groovyExampleClass = output.getText(MOCK_DIR + "/" + base + "G.html");
        assertMethodVisibility(base + "G", output, groovyExampleClass, a, b, c, d);
    }

    private void assertMethodVisibility(String base, MockOutputTool output, String text, boolean a, boolean b, boolean c, boolean d) {
        assertNotNull("No GroovyDoc found for " + base + "\nFound: " + output, text);
        assertTrue("method a1" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#a1()\">a1</a>"));
        assertTrue("method a2" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#a2()\">a2</a>"));
        assertTrue("method b" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("<a href=\"#b()\">b</a>"));
        assertTrue("method c1" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#c1()\">c1</a>"));
        assertTrue("method c2" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#c2()\">c2</a>"));
        assertTrue("method d" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("<a href=\"#d()\">d</a>"));

        assertTrue("field _a" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("<a href=\"#_a\">_a</a>"));
        assertTrue("field _b" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("<a href=\"#_b\">_b</a>"));
        assertTrue("field _c" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("<a href=\"#_c\">_c</a>"));
        assertTrue("field _d" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("<a href=\"#_d\">_d</a>"));

        assertTrue("class A1" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("A1</a></code>"));
        assertTrue("class A2" + (a ? " not" : "") + " found in: \"" + text + "\"", a ^ !text.contains("A2</a></code>"));
        assertTrue("class B" + (b ? " not" : "") + " found in: \"" + text + "\"", b ^ !text.contains("B</a></code>"));
        assertTrue("class C" + (c ? " not" : "") + " found in: \"" + text + "\"", c ^ !text.contains("C</a></code>"));
        assertTrue("class D" + (d ? " not" : "") + " found in: \"" + text + "\"", d ^ !text.contains("D</a></code>"));
    }

    public void testMultipleConstructorErrorBug() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/MultipleConstructorErrorBug.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/MultipleConstructorErrorBug.html");
        // VARBINARY() and other methods were assumed to be Constructors, make sure they aren't anymore...
        assertTrue(text,text.indexOf("<method modifiers=\"public static \" returns=\"java.lang.String\" name=\"VARBINARY\">") > 0);
    }

    public void testReturnTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyClassDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue("GroovyClassDoc should appear in:\n" + text, text.indexOf("org.codehaus.groovy.groovydoc.GroovyClassDoc") > 0);
    }

    public void testParameterTypeResolution() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.java");
        srcList.add("org/codehaus/groovy/groovydoc/GroovyPackageDoc.java");
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String text = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/SimpleGroovyRootDoc.html");
        assertTrue("GroovyPackageDoc should appear in:\n" + text, text.indexOf("<parameter type=\"org.codehaus.groovy.groovydoc.GroovyPackageDoc\"") > 0);
    }

    public void testFileEncodingFallbackToCharset() throws Exception {
        String expectedCharset = "ISO-88591";

        Properties props = new Properties();
        props.setProperty("charset", expectedCharset);

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<>(),
     null,
                props);

        assertEquals("'fileEncoding' falls back to 'charset' if not provided", expectedCharset, tool.properties.getProperty("fileEncoding"));
    }

    public void testCharsetFallbackToFileEncoding() throws Exception {
        String expectedCharset = "ISO-88591";

        Properties props = new Properties();
        props.setProperty("fileEncoding", expectedCharset);

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<>(),
                null,
                props);

        assertEquals("'charset' falls back to 'fileEncoding' if not provided", expectedCharset, tool.properties.getProperty("charset"));

    }

    public void testFileEncodingCharsetFallbackToDefaultCharset() throws Exception {
        String expectedCharset = Charset.defaultCharset().name();

        GroovyDocTool tool = new GroovyDocTool(
                new FileSystemResourceManager("src"),
                new String[0],
                new String[0],
                new String[0],
                new String[0],
                new ArrayList<>(),
                null,
                new Properties());

        assertEquals("'charset' falls back to the default charset", expectedCharset, tool.properties.getProperty("charset"));
        assertEquals("'fileEncoding' falls back to the default charset", expectedCharset, tool.properties.getProperty("fileEncoding"));
    }

    // GROOVY-5940
    public void testWrongPackageNameInClassHierarchyWithPlainTool() throws Exception {
        List<String> srcList = new ArrayList<>();

        String fullPathBaseA = "org/codehaus/groovy/tools/groovydoc/testfiles/a/Base";
        srcList.add(fullPathBaseA + ".groovy");

        String fullPathBaseB = "org/codehaus/groovy/tools/groovydoc/testfiles/b/Base";
        srcList.add(fullPathBaseB + ".groovy");

        String fullPathBaseC = "org/codehaus/groovy/tools/groovydoc/testfiles/c/Base";

        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantA.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/b/DescendantB.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantC.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/a/DescendantD.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantE.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/c/DescendantF.groovy");

        plainTool.add(srcList);

        GroovyRootDoc root = plainTool.getRootDoc();

        // loop through classes in tree
        GroovyClassDoc classDocDescendantA = getGroovyClassDocByName(root, "DescendantA");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantA, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantB = getGroovyClassDocByName(root, "DescendantB");
        assertEquals(fullPathBaseB, root.classNamed(classDocDescendantB, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantC = getGroovyClassDocByName(root, "DescendantC");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantC, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantD = getGroovyClassDocByName(root, "DescendantD");
        assertEquals(fullPathBaseA, root.classNamed(classDocDescendantD, "Base").getFullPathName());

        GroovyClassDoc classDocDescendantE = getGroovyClassDocByName(root, "DescendantE");
        assertNotNull("Expecting to find DescendantE", classDocDescendantE);
        GroovyClassDoc base = root.classNamed(classDocDescendantE, "Base");
        assertNotNull("Expecting to find Base in: " + Arrays.stream(root.classes()).map(GroovyClassDoc::getFullPathName).collect(Collectors.joining(", ")), base);
        assertEquals(fullPathBaseC, base.getFullPathName());

        GroovyClassDoc classDocDescendantF = getGroovyClassDocByName(root, "DescendantF");
        assertNotNull("Expecting to find DescendantF in: " + Arrays.stream(root.classes()).map(GroovyClassDoc::getFullPathName).collect(Collectors.joining(", ")), classDocDescendantF);
        assertEquals(fullPathBaseC, root.classNamed(classDocDescendantF, "Base").getFullPathName());
    }

    // GROOVY-5939
    public void testArrayPropertyLinkWithSelfReference() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String arrayPropertyLinkDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.html");

        Pattern p = Pattern.compile("<a(.+?)ArrayPropertyLink.html'>(.+?)</a>\\[]");
        Matcher m = p.matcher(arrayPropertyLinkDoc);

        assertTrue(m.find());
        assertEquals("There should be at least a single reference to the ArrayPropertyLink[] in:\n" + arrayPropertyLinkDoc, "ArrayPropertyLink", m.group(2));
    }

    public void testClassesAreNotInitialized() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String doc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/staticInit/UsesClassesWithFailingStaticInit.html");

        assertTrue("Expected JavaWithFailingStaticInit and GroovyWithFailingStaticInit in:\n" + doc, doc.contains("JavaWithFailingStaticInit") && doc.contains("GroovyWithFailingStaticInit"));
    }

    public void testArrayPropertyLinkWithExternalReference() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/ArrayPropertyLink.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String propertyLinkDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/PropertyLink.html");

        Pattern p = Pattern.compile("<a(.+?)ArrayPropertyLink.html'>(.+?)</a>\\[]");
        Matcher m = p.matcher(propertyLinkDoc);

        assertTrue(m.find());
        assertEquals("There has to be at least a single reference to the ArrayPropertyLink[]", "ArrayPropertyLink", m.group(2));
    }

    public void testInnerEnumReference() throws Exception {
        List<String> srcList = new ArrayList<>();

        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/InnerEnum.groovy");
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String derivDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/InnerClassProperty.html");

        // TODO FIXME? - old behavior: Enum was not qualified by outer class InnerEnum
        Pattern p = Pattern.compile("<a(.+?)testfiles/InnerEnum.Enum.html'>(InnerEnum\\.)?(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue("Expecting to find InnerEnum.Enum anchor in:\n" + derivDoc, m.find());
        assertEquals("There has to be a reference to class Enum", "Enum", m.group(3));
    }

    public void testEnumConstantsDocumentedAndInitNotDocumented() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        final String klass = "EnumWithDeprecatedConstants";
        htmlTool.add(Arrays.asList(
            base + "/"+ klass +".groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/"+ klass +".html");
        assertTrue(groovydoc.matches("(?s).*<table .*summary=\"Enum constants summary table\".*>bar<.*</table>.*"));

        final Matcher ctor = Pattern.compile(Pattern.quote("$INIT")).matcher(groovydoc);

        assertFalse("enum $INIT static method should not be documented", ctor.find());
    }

    public void testClassAliasing() throws Exception {

        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Alias.groovy");
        htmlTool.add(srcList);

        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String derivDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Alias.html");

        Pattern p = Pattern.compile("<a href='(.+?)java/util/ArrayList.html' title='ArrayList'>(.+?)</a>");
        Matcher m = p.matcher(derivDoc);

        assertTrue("expect ArrayList anchor in:\n" + derivDoc, m.find());
        assertEquals("Expect link text to contain ArrayList", "ArrayList", m.group(2));
    }

    public void testImplementedInterfaceWithAlias() throws Exception {
        // FooAdapter imports both api.Foo and lib.Foo, using "lib.Foo as FooImpl" to disambiguate.
        // lib.Foo is imported later than api.Foo, so groovydoc tries to resolve to lib.Foo first.
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/api/Foo.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/lib/Foo.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/alias/FooAdapter.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String fooAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/alias/FooAdapter.html");

        // "Interfaces and Traits" section should show "Foo" as one of the implemented interfaces,
        // and that should link to api/Foo.html, not to lib/Foo.html.
        final Matcher interfacesAndTraits = Pattern.compile(
                "<dt>All Implemented Interfaces and Traits:</dt>\\s*" +
                "<dd><a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/alias/(api|lib)/Foo\\.html'>(Foo|FooImpl)</a>"
        ).matcher(fooAdapterDoc);

        // Constructor is actually "FooAdapter(FooImpl foo)",
        // but it should show "Foo" as the link text, not "FooImpl".
        // The Foo parameter type should link to lib/Foo.html, not api/Foo.html.
        final Matcher constructor = Pattern.compile(
                "FooAdapter(</[a-z]+>)*\\(<a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/alias/(api|lib)/Foo.html'>(Foo|FooImpl)</a> foo\\)"
        ).matcher(fooAdapterDoc);

        assertTrue("Interfaces and Traits pattern should match for this test to make sense in: " + fooAdapterDoc, interfacesAndTraits.find());
        assertTrue("Constructor pattern should match for this test to make sense", constructor.find());

        assertEquals("The implemented interface should link to api.Foo", "api", interfacesAndTraits.group(1));
        assertEquals("The implemented interface link text should be Foo", "Foo", interfacesAndTraits.group(2));
        assertEquals("The constructor parameter should link to lib.Foo", "lib", constructor.group(2));
        assertEquals("The constructor parameter link text should be Foo", "Foo", constructor.group(3));
    }

    public void testGroovyExtendsImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Groovy interface b.Test imports a.List and extends List.
        // List should be recognized as a.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/List.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/Test.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/Test.html");

        // Test should etends a.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/))List)\\.html'[^>]*>List</a>").matcher(testAdapterDoc);

        assertTrue("Test interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/List", extendedClass.group(1));
    }

    public void testInheritedProperties() throws Exception {
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/props/Child.groovy",
                "org/codehaus/groovy/tools/groovydoc/testfiles/props/Parent.groovy",
                "org/codehaus/groovy/tools/groovydoc/testfiles/props/GrandParent.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String childDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/props/Child.html");

        final Matcher inheritedProperties = Pattern.compile("(?s)<span>Inherited properties</span>" +
            ".*<a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/props/Parent.html'>Parent</a>.*<code>(\\w*)</code>" +
            ".*<a href='[./]*/org/codehaus/groovy/tools/groovydoc/testfiles/props/GrandParent.html'>GrandParent</a>.*<code>(\\w*)</code>").matcher(childDoc);

        assertTrue("Should find inherited properties", inheritedProperties.find());
        assertEquals("Should find Parent property", "fooP", inheritedProperties.group(1));
        assertEquals("Should find GrandParent property", "fooGP", inheritedProperties.group(2));
    }

    public void testJavaExtendsImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Java interface b.Test imports a.List and extends List.
        // List should be recognized as a.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/List.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/Test.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/Test.html");

        // Test should etends a.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/))List)\\.html'[^>]*>List</a>").matcher(testAdapterDoc);

        assertTrue("Test interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/List", extendedClass.group(1));
    }

    public void testGroovyExtendsStarImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Groovy interface b.TestStar imports a.* and extends List.
        // List should be recognized as a.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/List.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStar.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStar.html");

        // TestStar should etends a.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/))List)\\.html'[^>]*>List</a>").matcher(testAdapterDoc);

        assertTrue("TestStar interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/List", extendedClass.group(1));
    }

    public void testJavaExtendsStarImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Java interface b.TestStar imports a.* and extends List.
        // List should be recognized as a.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/List.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStar.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStar.html");

        // TestStar should etends a.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/))List)\\.html'[^>]*>List</a>").matcher(testAdapterDoc);

        assertTrue("TestStar interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/List", extendedClass.group(1));
    }

    public void testGroovyExtendsStaticImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Groovy interface b.TestStatic imports a.StaticList.List and extends List.
        // List should be recognized as a.StaticList.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/StaticList.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStatic.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStatic.html");

        // TestStatic should etends a.StaticList.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/StaticList\\.))List)\\.html'[^>]*>((StaticList\\.)?List)</a>").matcher(testAdapterDoc);

        assertTrue("TestStatic interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/StaticList.List", extendedClass.group(1));
        assertEquals("Classes from imported packages should shadow classes from default packages", "StaticList.List", extendedClass.group(5));
    }

    public void testJavaExtendsStaticImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Java interface b.TestStatic imports a.StaticList.List and extends List.
        // List should be recognized as a.StaticList.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/StaticList.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStatic.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStatic.html");

        // TestStatic should etends a.StaticList.List".
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/StaticList\\.))List)\\.html'[^>]*>((StaticList\\.)?List)</a>").matcher(testAdapterDoc);

        assertTrue("TestStatic interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/StaticList.List", extendedClass.group(1));
        assertEquals("Classes from imported packages should shadow classes from default packages", "StaticList.List", extendedClass.group(5));
    }

    public void testGroovyExtendsStaticImportedAliasesClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Groovy interface b.TestStatic imports a.StaticList.ListAlias as List and extends List.
        // List should be recognized as a.StaticList.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/StaticList.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticAlias.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticAlias.html");

        // TestStatic should etends a.StaticList.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/StaticList\\.))List(Alias)?)\\.html'[^>]*>((StaticList\\.)?List(Alias)?)</a>").matcher(testAdapterDoc);

        assertTrue("TestStatic interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/StaticList.ListAlias", extendedClass.group(1));
        assertEquals("Classes from imported packages should shadow classes from default packages", "StaticList.ListAlias", extendedClass.group(6));
    }

    public void testGroovyExtendsStaticStarImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Groovy interface b.TestStaticStar imports a.StaticList.* and extends List.
        // List should be recognized as a.StaticList.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/StaticList.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticStar.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticStar.html");

        // TestStatic should etends a.StaticList.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/StaticList\\.))List)\\.html'[^>]*>((StaticList\\.)?List)</a>").matcher(testAdapterDoc);

        assertTrue("TestStaticStar interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/StaticList.List", extendedClass.group(1));
        assertEquals("Classes from imported packages should shadow classes from default packages", "StaticList.List", extendedClass.group(5));
    }

    public void testJavaExtendsStaticStarImportedClassWithNameWhichExistInDefaultPackages() throws Exception {
        // Java interface b.TestStaticStar imports a.StaticList.* and extends List.
        // List should be recognized as a.StaticList.List and not java.util.List
        htmlTool.add(Arrays.asList(
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/a/StaticList.java",
                "org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticStar.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        final String testAdapterDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/groovy_10593/b/TestStaticStar.html");

        // TestStatic should etends a.StaticList.List
        final Matcher extendedClass = Pattern.compile("extends\\s+<a[^>]*href='[^']*(((java/util/)|(a/StaticList\\.))List)\\.html'[^>]*>((StaticList\\.)?List)</a>").matcher(testAdapterDoc);

        assertTrue("TestStaticStar interface should extends List", extendedClass.find());

        assertEquals("Classes from imported packages should shadow classes from default packages", "a/StaticList.List", extendedClass.group(1));
        assertEquals("Classes from imported packages should shadow classes from default packages", "StaticList.List", extendedClass.group(5));
    }

    public void testClassDeclarationHeader() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
                base + "/JavaInterfaceWithTypeParam.java",
                base + "/GroovyInterfaceWithTypeParam.groovy",
                base + "/JavaInterfaceWithMultipleInterfaces.java",
                base + "/GroovyInterfaceWithMultipleInterfaces.groovy",
                base + "/ClassWithMethodComment.java",
                base + "/DocumentedClass.groovy",
                base + "/JavaClassWithMultipleInterfaces.java",
                base + "/GroovyClassWithMultipleInterfaces.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javaBaseInterface = output.getText(MOCK_DIR + "/" + base + "/JavaInterfaceWithTypeParam.html");
        final String groovyBaseInterface = output.getText(MOCK_DIR + "/" + base + "/GroovyInterfaceWithTypeParam.html");
        final String javaDerivedInterface = output.getText(MOCK_DIR + "/" + base + "/JavaInterfaceWithMultipleInterfaces.html");
        final String groovyDerivedInterface = output.getText(MOCK_DIR + "/" + base + "/GroovyInterfaceWithMultipleInterfaces.html");
        final String javaBaseClass = output.getText(MOCK_DIR + "/" + base + "/ClassWithMethodComment.html");
        final String groovyBaseClass = output.getText(MOCK_DIR + "/" + base + "/DocumentedClass.html");
        final String javaDerivedClass = output.getText(MOCK_DIR + "/" + base + "/JavaClassWithMultipleInterfaces.html");
        final String groovyDerivedClass = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");

        final String object = Pattern.quote(
            "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Object.html' title='Object'>Object</a>");
        final String interfaces = Pattern.quote(
            "org.codehaus.groovy.tools.groovydoc.testfiles.GroovyInterface1, " +
            "org.codehaus.groovy.tools.groovydoc.testfiles.JavaInterface1, " +
            "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Runnable.html' title='Runnable'>Runnable</a>");

        final Pattern baseInterface = Pattern.compile(
            "<pre>" +
            "(public )?interface (Java|Groovy)InterfaceWithTypeParam&lt;T&gt;" +
            "</pre>");
        final Pattern derivedInterface = Pattern.compile(
            "<pre>" +
            "(public )?interface (Java|Groovy)InterfaceWithMultipleInterfaces\n" +
            "extends " + interfaces +
            "</pre>");
        final Pattern baseClass = Pattern.compile(
            "<pre>" +
            "(public )?class (ClassWithMethodComment|DocumentedClass)\n" +
            "extends " + object +
            "(\nimplements groovy.lang.GroovyObject)?" +
            "</pre>");
        final Pattern derivedClass = Pattern.compile(
            "<pre>" +
            "(public )?abstract class (Java|Groovy)ClassWithMultipleInterfaces\n" +
            "extends " + object + "\n" +
            "implements " + interfaces +
            "(, groovy.lang.GroovyObject)?" +
            "</pre>");

        assertTrue("The Java base interface declaration header should match", baseInterface.matcher(javaBaseInterface).find());
        assertTrue("The Groovy base interface declaration header should match", baseInterface.matcher(groovyBaseInterface).find());
        assertTrue("The Java derived interface declaration header should match", derivedInterface.matcher(javaDerivedInterface).find());
        assertTrue("The Groovy derived interface declaration header should match", derivedInterface.matcher(groovyDerivedInterface).find());
        assertTrue("The Java base class declaration header should match in:\n" + javaBaseClass, baseClass.matcher(javaBaseClass).find());
        assertTrue("The Groovy base class declaration header should match in:\n" + groovyBaseClass, baseClass.matcher(groovyBaseClass).find());
        assertTrue("The Java derived class declaration header should match in:\n" + javaDerivedClass, derivedClass.matcher(javaDerivedClass).find());
        assertTrue("The Groovy derived class declaration header should match in:\n" + groovyDerivedClass, derivedClass.matcher(groovyDerivedClass).find());
    }

    public void testLanguageLevelNotSupported() throws Exception {
        htmlTool = makeHtmltool(new ArrayList<LinkArgument>(), ParserConfiguration.LanguageLevel.JAVA_1_4.name(), new Properties());

        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";

        htmlTool.add(Arrays.asList(base + "/Java.java"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        assertNull("Javadoc should be null since language level is not supported", javadoc);
    }

    public void testLanguageLevelSupported() throws Exception {
        htmlTool = makeHtmltool(new ArrayList<LinkArgument>(), ParserConfiguration.LanguageLevel.JAVA_5.name(), new Properties());

        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";

        htmlTool.add(Arrays.asList(base + "/Java.java"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        assertNotNull("Javadoc should not be null since language level is supported", javadoc);
    }

    public void testLanguageLevelSupportedForSwitchExpressions() throws Exception {
        Path tmp = Files.createTempDirectory("groovydoc-switch-expression-");
        try {
            List<String> sources = createSwitchExpressionJavaSources(tmp, 1);
            GroovyDocTool tool = makeHtmltool(new ArrayList<>(), ParserConfiguration.LanguageLevel.JAVA_17.name(), new Properties(), new String[] {tmp.toString()});
            tool.add(sources);

            MockOutputTool output = new MockOutputTool();
            tool.renderToOutput(output, MOCK_DIR);

            String base = "org/codehaus/groovy/tools/groovydoc/testfiles/switchexpr";
            String javadoc = output.getText(MOCK_DIR + "/" + base + "/SwitchExpression0.html");
            assertNotNull("Javadoc should not be null since Java 17 switch expressions are supported", javadoc);
            assertEquals("Expected no parse errors for switch expression source", 0, tool.getErrorCount());
        } finally {
            deleteRecursively(tmp);
        }
    }

    public void testGroovydocParsingUsesExplicitJavaParserLanguageLevel() throws Exception {
        StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_1_4);
        JavaParser javaParser = new JavaParser(
                new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17)
        );
        GroovyDocParser parser = new GroovyDocParser(javaParser, new ArrayList<>(), new Properties());

        Map<String, GroovyClassDoc> classDocs = parser.getClassDocsFromSingleSource(
                "org/codehaus/groovy/tools/groovydoc/testfiles/switchexpr",
                "SwitchExpression.java",
                switchExpressionJavaSource(
                        "org.codehaus.groovy.tools.groovydoc.testfiles.switchexpr",
                        "SwitchExpression"
                )
        );

        assertFalse("Explicit JavaParser language level should parse switch expressions with yield", classDocs.isEmpty());
        assertEquals("StaticJavaParser language level should remain unchanged", ParserConfiguration.LanguageLevel.JAVA_1_4,
                StaticJavaParser.getParserConfiguration().getLanguageLevel());
    }

    public void testJavaGenericsTitle() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(List.of(base + "/Java.java"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");

        final Matcher title = Pattern.compile(Pattern.quote(
                "<h2 title=\"[Java] Class Java&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;\" class=\"title\">"+
                "[Java] Class Java&lt;N extends Number & Comparable&lt;? extends Number&gt;&gt;</h2>"
        )).matcher(javadoc);

        assertTrue("The title should have the generics information", title.find());
    }

    public void testGroovyGenericsTitle() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(List.of(base + "/Groovy.groovy"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Matcher title = Pattern.compile(
                "<h2 title=\"\\[Groovy] Trait Groovy&lt;N extends (java.lang.)?Number & (java.lang.)?Comparable&lt;\\? extends (java.lang.)?Number&gt;&gt;\" class=\"title\">"+
                        "\\[Groovy] Trait Groovy&lt;N extends (java.lang.)?Number & (java.lang.)?Comparable&lt;\\? extends (java.lang.)?Number&gt;&gt;</h2>"
        ).matcher(groovydoc);

        assertTrue("The title should have the generics information in:\n" + groovydoc, title.find());
    }

    public void testParamTagForTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern classTypeParams = Pattern.compile(
                "<DL><DT><B>Type Parameters:</B></DT><DD><code>N</code> -  Doc.</DD></DL>"
        );
        final Pattern methodTypeParams = Pattern.compile(
                "<DL><DT><B>Type Parameters:</B></DT><DD><code>C</code> -  Doc.</DD><DD><code>D</code> -  Doc.</DD></DL>"
        );

        assertTrue("The Java class doc should have type parameters definitions in:\n" + javadoc, classTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy class doc should have type parameters definitions in:\n" + groovydoc, classTypeParams.matcher(groovydoc).find());
        assertTrue("The Java method doc should have type parameters definitions in:\n" + javadoc, methodTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method doc should have type parameters definitions in:\n" + groovydoc, methodTypeParams.matcher(groovydoc).find());
    }

    public void testMethodTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern methodSummaryTypeParams = Pattern.compile(
                "<td class=\"colFirst\"><code>&lt;C, D&gt;</code></td>"
        );
        final Pattern methodDetailsTypeParams = Pattern.compile(
                "<h4>&lt;C, D&gt; .*int <strong>compare</strong>\\("
        );

        assertTrue("The Java method summary should have type parameters", methodSummaryTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method summary should have type parameters", methodSummaryTypeParams.matcher(groovydoc).find());
        assertTrue("The Java method details should have type parameters", methodDetailsTypeParams.matcher(javadoc).find());
        assertTrue("The Groovy method details should have type parameters in:\n" + groovydoc, methodDetailsTypeParams.matcher(groovydoc).find());
    }

    public void testMethodParamTypeParams() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/generics";
        htmlTool.add(Arrays.asList(
                base + "/Java.java",
                base + "/Groovy.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");
        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");

        final Pattern methodSummary = Pattern.compile(
                "<code><strong><a href=\"#compare\\((java.lang.)?Class, (java.lang.)?Class\\)\">compare</a></strong>"
                        + "\\("
                        + "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;C&gt; c, "
                        + "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;D&gt; d"
                        + "\\)"
                        + "</code>"
        );
        final Pattern methodDetailAnchor = Pattern.compile(
                "<a name=\"compare\\((java.lang.)?Class, (java.lang.)?Class\\)\"><!-- --></a>"
        );
        final Pattern methodDetailTitle = Pattern.compile(Pattern.quote(
                "<strong>compare</strong>" +
                        "(" +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;C&gt; c, " +
                        "<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>&lt;D&gt; d" +
                        ")"
        ));

        assertTrue("The Java method summary should include type parameters in:\n" + javadoc, methodSummary.matcher(javadoc).find());
        assertTrue("The Java method detail anchor should NOT include type parameters in:\n" + javadoc, methodDetailAnchor.matcher(javadoc).find());
        assertTrue("The Java method detail title should include type parameters in:\n" + javadoc, methodDetailTitle.matcher(javadoc).find());
        assertTrue("The Groovy method summary should include type parameters in:\n" + groovydoc, methodSummary.matcher(groovydoc).find());
        assertTrue("The Groovy method detail anchor should NOT include type parameters in:\n" + groovydoc, methodDetailAnchor.matcher(groovydoc).find());
        assertTrue("The Groovy method detail title should include type parameters in:\n" + groovydoc, methodDetailTitle.matcher(groovydoc).find());
    }

    public void testAnnotations() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/anno";
        Properties props = new Properties();
        props.put("phaseOverride", "7");
        htmlTool = makeHtmltool(new ArrayList<>(), null, props);
        htmlTool.add(Arrays.asList(
                base + "/Groovy.groovy",
                base + "/Java.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Groovy.html");
        final String javadoc = output.getText(MOCK_DIR + "/" + base + "/Java.html");

        assertTrue("The Groovy class declaration header should have the annotation in:\n" + groovydoc, Pattern.compile(Pattern.quote(
                "<pre>@groovy.transform.EqualsAndHashCode(cache=true)\n" +
                        "class Groovy"
        )).matcher(groovydoc).find());

        // GROOVY-4634: @SuppressWarnings is not @Documented so is filtered out (matches Javadoc behavior)
        assertTrue("The Java class declaration header should have only the @Documented annotation in:\n" + javadoc, Pattern.compile(
                "<pre>@(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>|java.lang.Deprecated)\n" +
                        "public class Java"
        ).matcher(javadoc).find());
        assertFalse("@SuppressWarnings (not @Documented) should not appear in:\n" + javadoc,
                javadoc.contains("SuppressWarnings"));

        // GROOVY-9572: @Internal-annotated members (per GEP-17) are hidden from groovydoc output
        assertFalse("Field annotated with @Internal should be hidden in:\n" + groovydoc,
                groovydoc.contains("<strong>annotatedField</strong>"));

        assertTrue("The Java field details should have the annotation", Pattern.compile(
                "<h4>@(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>|java.lang.Deprecated)<br>" +
                        "public&nbsp;(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a>|java.lang.String) " +
                        "<strong>annotatedField</strong></h4>"
        ).matcher(javadoc).find());

        assertTrue("The Groovy property details should have the annotation", Pattern.compile(
            "<h4>@(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>|java.lang.Deprecated)<br>" +
                "(<a href='https://docs.oracle.com/javase/8/docs/api/java/util/List.html' title='List'>List</a>|java.util.List) <strong>annotatedProperty</strong></h4>"
        ).matcher(groovydoc).find());

        // Java doesn't have properties section

        assertTrue("The Groovy ctor details should have the annotation in:\n" + groovydoc, Pattern.compile(
                "<h4>@groovy.transform.NamedVariant<br>" +
                        "<strong>Groovy</strong>\\(" +
                        "(<a href='https://docs.oracle.com/javase/8/docs/api/java/util/List.html' title='List'>List</a>|java.util.List) ctorParam" +
                        "\\)</h4>"
        ).matcher(groovydoc).find());
        assertTrue("The Groovy ctor details should have the annotation in:\n" + groovydoc, Pattern.compile(
                "<h4>@groovy.transform.Generated<br>" +
                        "<strong>Groovy</strong>\\(" +
                        "@groovy.transform.NamedParam\\(value=\"ctorParam\", type=java.util.List\\)<br>" +
                        "(<a href='https://docs.oracle.com/javase/8/docs/api/java/util/Map.html' title='Map'>Map</a>|java.util.Map) namedArgs" +
                        "\\)</h4>"
        ).matcher(groovydoc).find());

        assertTrue("The Java ctor details should have the annotation in:\n" + javadoc, Pattern.compile(
                "<h4>@(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>|java.lang.Deprecated)<br>" +
                        "public&nbsp;<strong>Java</strong>\\(\\)</h4>"
        ).matcher(javadoc).find());

        // Note also the param annotation
        assertTrue("The Groovy method details should have the annotations in:\n" + groovydoc, Pattern.compile(
                "<h4>@groovy.transform.NamedVariant<br>" +
                        "void <strong>annotatedMethod</strong>\\(" +
                        "(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a>|java.lang.String) methodParam" +
                        "\\)</h4>"
        ).matcher(groovydoc).find());
        // Note also the param annotation
        assertTrue("The Groovy method details should have the annotations in:\n" + groovydoc, Pattern.compile(
                "<h4>@groovy.transform.Generated<br>" +
                        "void <strong>annotatedMethod</strong>\\(" +
                        "@groovy.transform.NamedParam\\(required=true, value=\"methodParam\", type=java.lang.String\\)<br>" +
                        "(<a href='https://docs.oracle.com/javase/8/docs/api/java/util/Map.html' title='Map'>Map</a>|java.util.Map) namedArgs" +
                        "\\)</h4>"
        ).matcher(groovydoc).find());

        // GROOVY-4634: @CommandLine.Parameters (picocli) is not @Documented so is filtered out
        assertTrue("The Java method details should have the @Documented annotation in:\n" + javadoc, Pattern.compile(
                "<h4>@(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Deprecated.html' title='Deprecated'>Deprecated</a>|java.lang.Deprecated)<br>" +
                        "public&nbsp;void <strong>annotatedMethod</strong>\\(" +
                        "(<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/String.html' title='String'>String</a>|java.lang.String) annotatedParam" +
                        "\\)</h4>"
        ).matcher(javadoc).find());
        assertFalse("@CommandLine.Parameters (not @Documented) should not appear on annotatedParam in:\n" + javadoc,
                javadoc.contains("@CommandLine.Parameters"));
    }

    // GROOVY-9572A: members annotated with groovy.transform.@Internal are hidden,
    // unless -showInternal / showInternal=true is set.
    public void testInternalAnnotationHidesMembers() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles/anno";
        Properties props = new Properties();
        props.put("phaseOverride", "7");

        // default: hidden
        GroovyDocTool defaultTool = makeHtmltool(new ArrayList<>(), null, props);
        defaultTool.add(Arrays.asList(base + "/Groovy.groovy"));
        MockOutputTool defaultOut = new MockOutputTool();
        defaultTool.renderToOutput(defaultOut, MOCK_DIR);
        String defaultDoc = defaultOut.getText(MOCK_DIR + "/" + base + "/Groovy.html");
        assertNotNull(defaultDoc);
        assertFalse("annotatedField is @Internal and should be hidden by default in:\n" + defaultDoc,
                defaultDoc.contains("<strong>annotatedField</strong>"));

        // opt-in: shown
        Properties opted = new Properties();
        opted.put("phaseOverride", "7");
        opted.put("showInternal", "true");
        GroovyDocTool optedTool = makeHtmltool(new ArrayList<>(), null, opted);
        optedTool.add(Arrays.asList(base + "/Groovy.groovy"));
        MockOutputTool optedOut = new MockOutputTool();
        optedTool.renderToOutput(optedOut, MOCK_DIR);
        String optedDoc = optedOut.getText(MOCK_DIR + "/" + base + "/Groovy.html");
        assertNotNull(optedDoc);
        assertTrue("annotatedField should reappear with showInternal=true in:\n" + optedDoc,
                optedDoc.contains("<strong>annotatedField</strong>"));
    }

    public void testAbstractMethods() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/GroovyClassWithMultipleInterfaces.groovy",
            base + "/JavaClassWithDiamond.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");
        final String javadoc = StringGroovyMethods.normalize(output.getText(MOCK_DIR + "/" + base + "/JavaClassWithDiamond.html"));

        final Pattern methodSummary = Pattern.compile("<code>(public&nbsp;)?abstract&nbsp;void</code>");
        final Pattern methodDetails = Pattern.compile("<h4>(public&nbsp;)?abstract&nbsp;void <strong>link</strong>");

        assertTrue("The Groovy method summary should contain 'abstract'", methodSummary.matcher(groovydoc).find());
        assertTrue("The Java method summary should contain 'abstract'", methodSummary.matcher(javadoc).find());
        assertTrue("The Groovy method details should contain 'abstract'", methodDetails.matcher(groovydoc).find());
        assertTrue("The Java method details should contain 'abstract'", methodDetails.matcher(javadoc).find());
    }

    public void testLinksToSamePackage() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
                base + "/GroovyInterface1.groovy",
                base + "/JavaClassWithDiamond.java"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyInterface1.html");
        final String javadoc = StringGroovyMethods.normalize(output.getText(MOCK_DIR + "/" + base + "/JavaClassWithDiamond.html"));

        final Matcher groovyClassComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='Java'>Java</a> " +
                        "<DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html' title='JavaClassWithDiamond'>JavaClassWithDiamond</a></DD>" +
                        "</DL></p>"
        )).matcher(groovydoc);
        final Matcher groovyMethodComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='Java link'>Java link</a> " +
                        "<DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/JavaClassWithDiamond.html#link()' title='JavaClassWithDiamond.link'>JavaClassWithDiamond.link</a></DD>" +
                        "</DL></p>"
        )).matcher(groovydoc);
        final Matcher javaClassComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='Groovy link'>Groovy link</a>\n" +
                        "  <DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html' title='GroovyInterface1'>GroovyInterface1</a></DD>" +
                        "</DL></p>"
        )).matcher(javadoc);
        final Matcher javaMethodComment = Pattern.compile(Pattern.quote(
                "<p> <a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='Groovy link'>Groovy link</a>\n" +
                        "      <DL><DT><B>See Also:</B></DT>" +
                        "<DD><a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html#link()' title='GroovyInterface1.link'>GroovyInterface1.link</a></DD>" +
                        "</DL></p>"
        )).matcher(javadoc);

        assertTrue("The Groovy class comment should contain links", groovyClassComment.find());
        assertTrue("The Groovy method comment should contain links", groovyMethodComment.find());
        assertTrue("The Java class comment should contain links", javaClassComment.find());
        assertTrue("The Java method comment should contain links", javaMethodComment.find());
    }

    public void testPrivateDefaultConstructor() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/GroovyClassWithMultipleInterfaces.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyClassWithMultipleInterfaces.html");

        final Matcher matcher = Pattern.compile(Pattern.quote("GroovyClassWithMultipleInterfaces()")).matcher(groovydoc);

        assertFalse("Private ctor should not be listed", matcher.find());
    }

    public void testDeprecated() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/DeprecatedClass.groovy",
            base + "/DeprecatedField.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/deprecated-list.html");
        assertTrue(groovydoc, groovydoc.contains("summary=\"Deprecated Classes table, listing deprecated classes, and an explanation\""));
        assertTrue(groovydoc, groovydoc.contains("<a href=\"org/codehaus/groovy/tools/groovydoc/testfiles/DeprecatedClass.html\">DeprecatedClass</a>"));
    }

    public void testProperty() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(Arrays.asList(
            base + "/Alias.groovy"
        ));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/Alias.html");

        final Matcher summary = Pattern.compile(Pattern.quote(
            "<code><strong><a href='https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html' " +
                "title='ArrayList'>ArrayList</a></strong></code>"
        )).matcher(groovydoc);
        final Matcher detail = Pattern.compile(Pattern.quote(
            "<h4><a href='https://docs.oracle.com/javase/8/docs/api/java/util/ArrayList.html' " +
                "title='ArrayList'>ArrayList</a> <strong>arrayList</strong></h4>"
        )).matcher(groovydoc);

        assertTrue("Property summary should be found", summary.find());
        assertTrue("Property detail should be found", detail.find());
    }

    public void testArray() throws Exception {
        final String base = "org/codehaus/groovy/tools/groovydoc/testfiles";
        htmlTool.add(List.of(base + "/GroovyInterface1.groovy"));

        final MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);

        final String groovydoc = output.getText(MOCK_DIR + "/" + base + "/GroovyInterface1.html");

        final String klass = Pattern.quote("<a href='https://docs.oracle.com/javase/8/docs/api/java/lang/Class.html' title='Class'>Class</a>");
        final String groovyInterface1 = Pattern.quote("<a href='../../../../../../org/codehaus/groovy/tools/groovydoc/testfiles/GroovyInterface1.html' title='GroovyInterface1'>GroovyInterface1</a>");

        final Matcher klassArray = Pattern.compile(klass + "&lt;\\? extends " + groovyInterface1 + "&gt;\\[]").matcher(groovydoc);
        final Matcher primArray = Pattern.compile(Pattern.quote("byte[]")).matcher(groovydoc);

        assertTrue("Class<? extends GroovyInterface1>[] is found instead of Class[]", klassArray.find());
        assertTrue("byte[] is found instead of [B", primArray.find());
    }

    public void testScript() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Script.groovy");

        Properties props = new Properties();
        props.put("packageScope", "true");
        props.put("phaseOverride", "7");
        xmlTool = makeXmlTool(new ArrayList<>(), props);
        xmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        xmlTool.renderToOutput(output, MOCK_DIR);
        String scriptDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Script.html");
        assertNotNull("Expected to find Script.html in: " + output, scriptDoc);
        assertTrue("There should be a reference to method sayHello in: " + scriptDoc, containsTagWithName(scriptDoc, "method", "sayHello"));
        assertTrue("Expecting say Hello in:\n" + scriptDoc, scriptDoc.contains("Use this to say Hello"));

        assertTrue("There should be a reference to method sayGoodbye", containsTagWithName(scriptDoc, "method", "sayGoodbye"));
        assertTrue("Expecting bid farewell in:\n" + scriptDoc, scriptDoc.contains("Use this to bid farewell"));

        assertTrue("There should be a reference to property instanceProp in:\n" + scriptDoc, containsTagWithName(scriptDoc, "field", "instanceProp"));

        assertTrue("There should be a reference to field staticField", containsTagWithName(scriptDoc, "field", "staticField"));

        assertFalse("Script local variables should not appear in groovydoc output", scriptDoc.contains("localVar"));
    }

    public void testScriptCommandLineOptions() throws Exception {
        List<String> srcList = new ArrayList<>();
        srcList.add("org/codehaus/groovy/tools/groovydoc/testfiles/Script.groovy");

        // default params
        htmlTool.add(srcList);
        MockOutputTool output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        String scriptDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Script.html");
        assertNotNull("Expected to find Script.html in: " + output, scriptDoc);
        assertTrue("There should be a reference to method run in: " + scriptDoc, scriptDoc.contains("#run()"));
        assertTrue("There should be a reference to method main in: " + scriptDoc, scriptDoc.contains("#main("));

        // -noscript case
        Properties props = new Properties();
        props.put("processScripts", "false");
        htmlTool = makeHtmltool(new ArrayList<>(), null, props);
        htmlTool.add(srcList);
        output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        scriptDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Script.html");
        assertNull("Expected to not find Script.html in: " + output, scriptDoc);

        // -nomainforscript case
        props = new Properties();
        props.put("includeMainForScripts", "false");
        htmlTool = makeHtmltool(new ArrayList<>(), null, props);
        htmlTool.add(srcList);
        output = new MockOutputTool();
        htmlTool.renderToOutput(output, MOCK_DIR);
        scriptDoc = output.getText(MOCK_DIR + "/org/codehaus/groovy/tools/groovydoc/testfiles/Script.html");
        assertNotNull("Expected to find Script.html in: " + output, scriptDoc);
        assertTrue("There should be a reference to method run in: " + scriptDoc, scriptDoc.contains("#run()"));
        assertFalse("There should not be a reference to method main in: " + scriptDoc, scriptDoc.contains("#main("));
    }

    private boolean containsTagWithName(String text, String tagname, String name) {
        return text.matches("(?s).*<"+ tagname + "[^>]* name=\""+ name + "\".*");
    }

    private GroovyClassDoc getGroovyClassDocByName(GroovyRootDoc root, String name) {
        GroovyClassDoc[] classes = root.classes();

        for (GroovyClassDoc clazz : classes) {
            if (clazz.getFullPathName().endsWith(name)) {
                return clazz;
            }
        }

        return null;
    }
}

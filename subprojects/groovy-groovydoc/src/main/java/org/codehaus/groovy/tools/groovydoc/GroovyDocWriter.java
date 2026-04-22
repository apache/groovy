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
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Stream;

/**
 * Write GroovyDoc resources to destination.
 */
public class GroovyDocWriter {
    private final Logger log = Logger.create(GroovyDocWriter.class);
    private final OutputTool output;
    private final GroovyDocTemplateEngine templateEngine;
    private static final String FS = "/";
    /**
     * Per-package subdirectories in source whose contents are mirrored verbatim
     * into the output. GROOVY-5986 ({@code doc-files}) is the Javadoc-inherited
     * case; GROOVY-11938 ({@code snippet-files}, for {@code {@snippet file=...}})
     * piggybacks on the same scanner.
     */
    private static final List<String> RESOURCE_DIRS = Arrays.asList("doc-files", "snippet-files");
    private final Properties properties;
    private final String[] sourcepaths;

    @Deprecated
    public GroovyDocWriter(GroovyDocTool tool, OutputTool output, GroovyDocTemplateEngine templateEngine, Properties properties) {
        this(output, templateEngine, properties);
    }

    public GroovyDocWriter(OutputTool output, GroovyDocTemplateEngine templateEngine, Properties properties) {
        this(output, templateEngine, properties, null);
    }

    public GroovyDocWriter(OutputTool output, GroovyDocTemplateEngine templateEngine, Properties properties, String[] sourcepaths) {
        this.output = output;
        this.templateEngine = templateEngine;
        this.properties = properties;
        this.sourcepaths = sourcepaths == null ? new String[0] : Arrays.copyOf(sourcepaths, sourcepaths.length);
    }

    public void writeClasses(GroovyRootDoc rootDoc, String destdir) throws Exception {
        for (GroovyClassDoc classDoc : rootDoc.classes()) {
            writeClassToOutput(classDoc, destdir);
        }
    }

    public void writeClassToOutput(GroovyClassDoc classDoc, String destdir) throws Exception {
        if (classDoc.isPublic() || classDoc.isProtected() && "true".equals(properties.getProperty("protectedScope")) ||
                classDoc.isPackagePrivate() && "true".equals(properties.getProperty("packageScope")) || "true".equals(properties.getProperty("privateScope"))) {
            String destFileName = destdir + FS + classDoc.getFullPathName() + ".html";
            log.debug("Generating " + destFileName);
            String renderedSrc = templateEngine.applyClassTemplates(classDoc);
            output.writeToOutput(destFileName, renderedSrc, properties.getProperty("fileEncoding"));
        }
    }

    public void writePackages(GroovyRootDoc rootDoc, String destdir) throws Exception {
        for (GroovyPackageDoc packageDoc : rootDoc.specifiedPackages()) {
            if (new File(packageDoc.name()).isAbsolute()) continue;
            output.makeOutputArea(destdir + FS + packageDoc.name());
            writePackageToOutput(packageDoc, destdir);
            copyResourceFiles(packageDoc, destdir);
        }
        StringBuilder sb = new StringBuilder();
        for (GroovyPackageDoc packageDoc : rootDoc.specifiedPackages()) {
            sb.append(packageDoc.nameWithDots());
            sb.append("\n");
        }
        String destFileName = destdir + FS + "package-list";
        log.debug("Generating " + destFileName);
        output.writeToOutput(destFileName, sb.toString(), properties.getProperty("fileEncoding"));
    }

    /**
     * GROOVY-5986 / GROOVY-11938: mirror any {@code doc-files/} or
     * {@code snippet-files/} subdirectory found under the package in any
     * sourcepath entry into the corresponding output package. Matches
     * Javadoc's "Miscellaneous Unprocessed Files" behaviour.
     */
    private void copyResourceFiles(GroovyPackageDoc packageDoc, String destdir) {
        for (String sourcepath : sourcepaths) {
            for (String resourceDir : RESOURCE_DIRS) {
                Path srcDir = Paths.get(sourcepath, packageDoc.name(), resourceDir);
                if (!Files.isDirectory(srcDir)) continue;
                Path dstDir = Paths.get(destdir, packageDoc.name(), resourceDir);
                copyTree(srcDir, dstDir);
            }
        }
    }

    private void copyTree(Path srcDir, Path dstDir) {
        try (Stream<Path> stream = Files.walk(srcDir)) {
            stream.forEach(srcFile -> {
                Path rel = srcDir.relativize(srcFile);
                Path dstFile = dstDir.resolve(rel);
                try {
                    if (Files.isDirectory(srcFile)) {
                        output.makeOutputArea(dstFile.toString());
                    } else {
                        output.copyResource(srcFile.toString(), dstFile.toString());
                    }
                } catch (Exception e) {
                    log.warn("Failed to copy " + srcFile + " to " + dstFile + ": " + e.getMessage());
                }
            });
        } catch (IOException e) {
            log.warn("Failed to walk " + srcDir + ": " + e.getMessage());
        }
    }

    public void writePackageToOutput(GroovyPackageDoc packageDoc, String destdir) throws Exception {
        Iterator<String> templates = templateEngine.packageTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            String renderedSrc = templateEngine.applyPackageTemplate(template, packageDoc);
            String destFileName = destdir + FS + packageDoc.name() + FS + GroovyDocUtil.getFile(template);
            log.debug("Generating " + destFileName);
            output.writeToOutput(destFileName, renderedSrc, properties.getProperty("fileEncoding"));
        }
    }

    public void writeRoot(GroovyRootDoc rootDoc, String destdir) throws Exception {
        output.makeOutputArea(destdir);
        writeRootDocToOutput(rootDoc, destdir);
    }

    public void writeRootDocToOutput(GroovyRootDoc rootDoc, String destdir) throws Exception {
        Iterator<String> templates = templateEngine.docTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            if (isDisabledRootTemplate(template)) continue;
            String destFileName = destdir + FS + GroovyDocUtil.getFile(template);
            log.debug("Generating " + destFileName);
            if (hasBinaryExtension(template)) {
                templateEngine.copyBinaryResource(template, destFileName);
            } else {
                String renderedSrc = templateEngine.applyRootDocTemplate(template, rootDoc);
                output.writeToOutput(destFileName, renderedSrc, properties.getProperty("fileEncoding"));
            }
        }
    }

    // GROOVY-11943: javadoc-parity disable flags skip the matching top-level page.
    private boolean isDisabledRootTemplate(String template) {
        String name = GroovyDocUtil.getFile(template);
        if ("true".equals(properties.getProperty("noIndex")) && "index-all.html".equals(name)) return true;
        if ("true".equals(properties.getProperty("noDeprecatedList")) && "deprecated-list.html".equals(name)) return true;
        if ("true".equals(properties.getProperty("noHelp")) && "help-doc.html".equals(name)) return true;
        return false;
    }

    private static boolean hasBinaryExtension(String template) {
        if (template.endsWith(".gif") || template.endsWith(".ico")) return true;
        // GROOVY-11938 stage 4: Prism.js bundle. The minified JS/CSS files
        // contain `$` characters which the GString template engine would
        // otherwise try to interpret as expressions, so copy verbatim.
        int lastSlash = template.lastIndexOf('/');
        String name = lastSlash >= 0 ? template.substring(lastSlash + 1) : template;
        if (name.startsWith("prism") && (name.endsWith(".js") || name.endsWith(".css"))) return true;
        return false;
    }

}

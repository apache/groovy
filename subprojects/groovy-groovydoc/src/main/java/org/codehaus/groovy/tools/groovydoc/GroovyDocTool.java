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

import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.tools.shell.util.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class GroovyDocTool {
    private final Logger log = Logger.create(GroovyDocTool.class);
    private final GroovyRootDocBuilder rootDocBuilder;
    private final GroovyDocTemplateEngine templateEngine;
    private final ParserConfiguration.LanguageLevel javaLanguageLevel;

    protected Properties properties;

    /**
     * Constructor for use by people who only want to interact with the Groovy Doclet Tree (rootDoc)
     *
     * @param sourcepaths where the sources to be added can be found
     */
    public GroovyDocTool(String[] sourcepaths) {
        this(null, sourcepaths, null);
    }

    public GroovyDocTool(ResourceManager resourceManager, String[] sourcepaths, String classTemplate) {
        this(resourceManager, sourcepaths, new String[]{}, new String[]{}, new String[]{classTemplate}, new ArrayList<LinkArgument>(), null, new Properties());
    }

    /**
     * Constructs a GroovyDocTool instance with the specified parameters.
     *
     * @param resourceManager  the resource manager for handling resources, or null if not required
     * @param sourcepaths      the paths to the source files to be processed
     * @param docTemplates     the templates for generating documentation
     * @param packageTemplates the templates for generating package-level documentation
     * @param classTemplates   the templates for generating class-level documentation
     * @param links            a list of link arguments for external references
     * @param javaVersion      the Java version to be used for parsing and processing Java source files
     * @param properties       additional properties to be used when generating the groovydoc
     */
    public GroovyDocTool(ResourceManager resourceManager, String[] sourcepaths, String[] docTemplates, String[] packageTemplates, String[] classTemplates, List<LinkArgument> links, String javaVersion, Properties properties) {
        rootDocBuilder = new GroovyRootDocBuilder(sourcepaths, links, properties);
        javaLanguageLevel = calculateLanguageLevel(javaVersion);

        String defaultCharset = Charset.defaultCharset().name();

        String fileEncoding = properties.getProperty("fileEncoding");
        String charset = properties.getProperty("charset");

        if (fileEncoding == null || fileEncoding.isEmpty()) fileEncoding = charset;
        if (charset == null || charset.isEmpty()) charset = fileEncoding;

        properties.setProperty("fileEncoding", fileEncoding != null && !fileEncoding.isEmpty() ? fileEncoding : defaultCharset);
        properties.setProperty("charset", charset != null && !charset.isEmpty() ? charset : defaultCharset);

        this.properties = properties;

        if (resourceManager == null) {
            templateEngine = null;
        } else {
            templateEngine = new GroovyDocTemplateEngine(this, resourceManager, docTemplates, packageTemplates, classTemplates, properties);
        }
    }

    private ParserConfiguration.LanguageLevel calculateLanguageLevel(String javaVersion) {
        String version = Optional.ofNullable(javaVersion)
            .map(String::trim)
            .map(s -> s.toUpperCase())
            .filter(s -> !s.isEmpty())
            .orElse(null);

        if (version == null) {
            return null;
        }

        try {
            return ParserConfiguration.LanguageLevel.valueOf(version);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported Java Version: " + javaVersion);
        }
    }

    public void add(List<String> filenames) throws IOException {
        if (templateEngine != null) {
            // only print out if we are being used for template generation
            log.debug("Loading source files for " + filenames);
        }

        ParserConfiguration.LanguageLevel previousLanguageLevel = StaticJavaParser.getParserConfiguration().getLanguageLevel();
        try {
            if(javaLanguageLevel != null) {
                StaticJavaParser.getParserConfiguration().setLanguageLevel(javaLanguageLevel);
            }
            rootDocBuilder.buildTree(filenames);
        }
        finally {
            if(javaLanguageLevel != null) {
                StaticJavaParser.getParserConfiguration().setLanguageLevel(previousLanguageLevel);
            }
        }
    }

    public GroovyRootDoc getRootDoc() {
        return rootDocBuilder.getRootDoc();
    }

    public void renderToOutput(OutputTool output, String destdir) throws Exception {
        // expect just one scope to be set on the way in but now also set higher levels of visibility
        if ("true".equals(properties.getProperty("privateScope"))) properties.setProperty("packageScope", "true");
        if ("true".equals(properties.getProperty("packageScope"))) properties.setProperty("protectedScope", "true");
        if ("true".equals(properties.getProperty("protectedScope"))) properties.setProperty("publicScope", "true");
        if (templateEngine != null) {
            GroovyDocWriter writer = new GroovyDocWriter(output, templateEngine, properties);
            GroovyRootDoc rootDoc = rootDocBuilder.getRootDoc();
            writer.writeRoot(rootDoc, destdir);
            writer.writePackages(rootDoc, destdir);
            writer.writeClasses(rootDoc, destdir);
        } else {
            throw new UnsupportedOperationException("No template engine was found");
        }
    }

    @Deprecated
    static String getPath(String filename) {
        String path = new File(filename).getParent();
        // path length of 1 indicates that probably is 'default package' i.e. "/"
        if (path == null || (path.length() == 1 && !Character.isJavaIdentifierStart(path.charAt(0)))) {
            path = "DefaultPackage"; // "DefaultPackage" for 'default package' path, rather than null...
        }
        return path;
    }

    @Deprecated
    static String getFile(String filename) {
        return new File(filename).getName();
    }

}

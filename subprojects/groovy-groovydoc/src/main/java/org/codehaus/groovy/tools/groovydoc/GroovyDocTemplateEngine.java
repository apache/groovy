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

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.DefaultGroovyMethodsSupport;
import org.codehaus.groovy.runtime.IOGroovyMethods;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.WARNING;

/**
 * Process Groovydoc templates.
 */
public class GroovyDocTemplateEngine {

    private static final System.Logger LOGGER = System.getLogger(GroovyDocTemplateEngine.class.getName());
    private final TemplateEngine engine;
    private final ResourceManager resourceManager;
    private final Properties properties;
    private final Map<String, Template> docTemplates; // cache
    private final List<String> docTemplatePaths; // once per documentation set
    private final Map<String, Template> packageTemplates; // cache
    private final List<String> packageTemplatePaths; // once per package
    private final Map<String, Template> classTemplates; // cache
    private final List<String> classTemplatePaths; // once per class

    /**
     * Creates a template engine with a resource manager, a single class-level template, and default properties.
     */
    public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager, String classTemplate) {
        this(tool, resourceManager, new String[]{}, new String[]{}, new String[]{classTemplate}, new Properties());
    }

    /**
     * Creates a template engine with separate sets of documentation, package, and class templates.
     */
    public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager,
                                   String[] docTemplates,
                                   String[] packageTemplates,
                                   String[] classTemplates,
                                   Properties properties) {
        this.resourceManager = resourceManager;
        this.properties = properties;
        this.docTemplatePaths = Arrays.asList(docTemplates);
        this.packageTemplatePaths = Arrays.asList(packageTemplates);
        this.classTemplatePaths = Arrays.asList(classTemplates);
        this.docTemplates = new LinkedHashMap<String, Template>();
        this.packageTemplates = new LinkedHashMap<String, Template>();
        this.classTemplates = new LinkedHashMap<String, Template>();
        engine = new GStringTemplateEngine();

    }

    /**
     * Applies the configured class template to the supplied class documentation model.
     *
     * @param classDoc class documentation to render
     * @return rendered class documentation
     */
    String applyClassTemplates(GroovyClassDoc classDoc) {
        String templatePath = classTemplatePaths.get(0); // todo (iterate)
        String templateWithBindingApplied = "";
        try {
            Template t = classTemplates.get(templatePath);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(templatePath));
                classTemplates.put(templatePath, t);
            }
            Map<String, Object> binding = new LinkedHashMap<String, Object>();
            binding.put("classDoc", classDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).writeTo(reasonableSizeWriter()).toString();
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error processing class template for: {0}", classDoc.getFullPathName());
            LOGGER.log(ERROR, "Template error", e);
        }
        return templateWithBindingApplied;
    }

    private static StringWriter reasonableSizeWriter() {
        return new StringWriter(65536);
    }

    /**
     * Applies a package template to the supplied package documentation model.
     *
     * @param template template path to render
     * @param packageDoc package documentation to render
     * @return rendered package documentation
     */
    String applyPackageTemplate(String template, GroovyPackageDoc packageDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = packageTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                packageTemplates.put(template, t);
            }
            Map<String, Object> binding = new LinkedHashMap<String, Object>();
            binding.put("packageDoc", packageDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error processing package template for: {0}", packageDoc.name());
            LOGGER.log(ERROR, "Template error", e);
        }
        return templateWithBindingApplied;
    }

    /**
     * Applies a root-document template to the supplied root documentation model.
     *
     * @param template template path to render
     * @param rootDoc root documentation to render
     * @return rendered root documentation
     */
    String applyRootDocTemplate(String template, GroovyRootDoc rootDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = docTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                docTemplates.put(template, t);
            }
            Map<String, Object> binding = new LinkedHashMap<String, Object>();
            binding.put("rootDoc", rootDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            LOGGER.log(ERROR, "Error processing root doc template");
            LOGGER.log(ERROR, "Template error", e);
        }
        return templateWithBindingApplied;
    }

    /**
     * Returns the configured class template paths.
     *
     * @return iterator over class template paths
     */
    Iterator<String> classTemplatesIterator() {
        return classTemplatePaths.iterator();
    }

    /**
     * Returns the configured package template paths.
     *
     * @return iterator over package template paths
     */
    Iterator<String> packageTemplatesIterator() {
        return packageTemplatePaths.iterator();
    }

    /**
     * Returns the configured top-level documentation template paths.
     *
     * @return iterator over documentation template paths
     */
    Iterator<String> docTemplatesIterator() {
        return docTemplatePaths.iterator();
    }

    /**
     * Copies a binary resource (image or Prism.js bundle) from the classpath to the given destination file path.
     */
    public void copyBinaryResource(String template, String destFileName) {
        if (resourceManager instanceof ClasspathResourceManager) {
            OutputStream outputStream = null;
            try {
                InputStream inputStream = ((ClasspathResourceManager) resourceManager).getInputStream(template);
                outputStream = Files.newOutputStream(Paths.get(destFileName));
                IOGroovyMethods.leftShift(outputStream, inputStream);
            } catch (IOException e) {
                LOGGER.log(WARNING, "Resource {0} skipped due to: {1}", template, e.getMessage());
            } catch (NullPointerException e) {
                LOGGER.log(WARNING, "Resource {0} not found so skipped", template);
            } finally {
                DefaultGroovyMethodsSupport.closeQuietly(outputStream);
            }
        }
    }
}

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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Process Groovydoc templates.
 */
public class GroovyDocTemplateEngine {
    private final TemplateEngine engine;
    private final ResourceManager resourceManager;
    private final Properties properties;
    private final Map<String, Template> docTemplates; // cache
    private final List<String> docTemplatePaths; // once per documentation set
    private final Map<String, Template> packageTemplates; // cache
    private final List<String> packageTemplatePaths; // once per package
    private final Map<String, Template> classTemplates; // cache
    private final List<String> classTemplatePaths; // once per class

    public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager, String classTemplate) {
        this(tool, resourceManager, new String[]{}, new String[]{}, new String[]{classTemplate}, new Properties());
    }

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
        this.docTemplates = new LinkedHashMap<>();
        this.packageTemplates = new LinkedHashMap<>();
        this.classTemplates = new LinkedHashMap<>();
        engine = new GStringTemplateEngine();

    }

    String applyClassTemplates(GroovyClassDoc classDoc) {
        String templatePath = classTemplatePaths.get(0); // todo (iterate)
        String templateWithBindingApplied = "";
        try {
            Template t = classTemplates.get(templatePath);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(templatePath));
                classTemplates.put(templatePath, t);
            }
            Map<String, Object> binding = new LinkedHashMap<>();
            binding.put("classDoc", classDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).writeTo(reasonableSizeWriter()).toString();
        } catch (Exception e) {
            System.out.println("Error processing class template for: " + classDoc.getFullPathName());
            e.printStackTrace();
        }
        return templateWithBindingApplied;
    }

    private static StringWriter reasonableSizeWriter() {
        return new StringWriter(65536);
    }

    String applyPackageTemplate(String template, GroovyPackageDoc packageDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = packageTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                packageTemplates.put(template, t);
            }
            Map<String, Object> binding = new LinkedHashMap<>();
            binding.put("packageDoc", packageDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            System.out.println("Error processing package template for: " + packageDoc.name());
            e.printStackTrace();
        }
        return templateWithBindingApplied;
    }

    String applyRootDocTemplate(String template, GroovyRootDoc rootDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = docTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                docTemplates.put(template, t);
            }
            Map<String, Object> binding = new LinkedHashMap<>();
            binding.put("rootDoc", rootDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            System.out.println("Error processing root doc template");
            e.printStackTrace();
        }
        return templateWithBindingApplied;
    }

    Iterator<String> classTemplatesIterator() {
        return classTemplatePaths.iterator();
    }

    Iterator<String> packageTemplatesIterator() {
        return packageTemplatePaths.iterator();
    }

    Iterator<String> docTemplatesIterator() {
        return docTemplatePaths.iterator();
    }

    public void copyBinaryResource(String template, String destFileName) {
        if (resourceManager instanceof ClasspathResourceManager) {
            OutputStream outputStream = null;
            try {
                InputStream inputStream = ((ClasspathResourceManager) resourceManager).getInputStream(template);
                outputStream = new FileOutputStream(destFileName);
                IOGroovyMethods.leftShift(outputStream, inputStream);
            } catch (IOException e) {
                System.err.println("Resource " + template + " skipped due to: " + e.getMessage());
            } catch (NullPointerException e) {
                System.err.println("Resource " + template + " not found so skipped");
            } finally {
                DefaultGroovyMethodsSupport.closeQuietly(outputStream);
            }
        }
    }
}

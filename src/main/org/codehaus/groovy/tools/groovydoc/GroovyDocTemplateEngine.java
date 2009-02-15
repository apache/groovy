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

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Process Groovydoc templates.
 */
public class GroovyDocTemplateEngine {
    private TemplateEngine engine;
    private GroovyDocTool tool; // TODO use it or lose it
    private ResourceManager resourceManager;
    private Properties properties;
    private Map<String, Template> docTemplates; // cache
    private List<String> docTemplatePaths; // once per documentation set
    private Map<String, Template> packageTemplates; // cache
    private List<String> packageTemplatePaths; // once per package
    private Map<String, Template> classTemplates; // cache
    private List<String> classTemplatePaths; // once per class

    public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager, String classTemplate) {
        this(tool, resourceManager, new String[]{}, new String[]{}, new String[]{classTemplate}, new Properties());
    }

    public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager,
                                   String[] docTemplates,
                                   String[] packageTemplates,
                                   String[] classTemplates,
                                   Properties properties) {
        this.tool = tool;
        this.resourceManager = resourceManager;
        this.properties = properties;
        this.docTemplatePaths = Arrays.asList(docTemplates);
        this.packageTemplatePaths = Arrays.asList(packageTemplates);
        this.classTemplatePaths = Arrays.asList(classTemplates);
        this.docTemplates = new HashMap<String, Template>();
        this.packageTemplates = new HashMap<String, Template>();
        this.classTemplates = new HashMap<String, Template>();
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
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("classDoc", classDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return templateWithBindingApplied;
    }

    String applyPackageTemplate(String template, GroovyPackageDoc packageDoc) {
        String templateWithBindingApplied = "";
        try {
            Template t = packageTemplates.get(template);
            if (t == null) {
                t = engine.createTemplate(resourceManager.getReader(template));
                packageTemplates.put(template, t);
            }
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("packageDoc", packageDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
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
            Map<String, Object> binding = new HashMap<String, Object>();
            binding.put("rootDoc", rootDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
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

/*
	String applyClassTemplatesWithVelocity(GroovyClassDoc classDoc) {
//		Iterator templates = classTemplates.iterator();
//		while (templates.hasNext)
		String templatePath = (String) classTemplates.get(0); // todo (iterate)
			
		String templateWithBindingApplied = "";
		try {
//			Template t = new GStringTemplateEngine().createTemplate(template);
			VelocityTemplateEngine t = new VelocityTemplateEngine(new File(".").getAbsolutePath());

			Map binding = new HashMap();
	        binding.put("classDoc", classDoc);
	        
//	        templateWithBindingApplied = t.make(binding).toString();
	        templateWithBindingApplied = t.apply(templatePath,binding);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return templateWithBindingApplied;
	}
*/

    public void copyBinaryResource(String template, String destFileName) {
        if (resourceManager instanceof ClasspathResourceManager) {
            OutputStream outputStream = null;
            try {
                InputStream inputStream = ((ClasspathResourceManager) resourceManager).getInputStream(template);
                outputStream = new FileOutputStream(destFileName);
                DefaultGroovyMethods.leftShift(outputStream, inputStream);
            } catch (IOException e) {
                System.err.println("Resource " + template + " skipped due to: " + e.getMessage());
            } catch (NullPointerException e) {
                System.err.println("Resource " + template + " not found so skipped");
            } finally {
                DefaultGroovyMethods.closeQuietly(outputStream);
            }
        }
    }
}

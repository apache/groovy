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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import groovy.text.GStringTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

/*
 * todo
 *  comma at the end of method parameters
 *  add comments
 *  static modifier
 *  order methods alphabetically (implement compareTo enough?)
 *  provide links to other html files (e.g. return type of a method)
 */
public class GroovyDocTemplateEngine {
    private TemplateEngine engine;
    private GroovyDocTool tool;
    private ResourceManager resourceManager;
    private Properties properties;
    //	private String relativeTemplatePath;
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
            Map binding = new HashMap();
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
            Map binding = new HashMap();
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
            Map binding = new HashMap();
            binding.put("rootDoc", rootDoc);
            binding.put("props", properties);
            templateWithBindingApplied = t.make(binding).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return templateWithBindingApplied;
    }

    Iterator classTemplatesIterator() {
        return classTemplatePaths.iterator();
    }

    Iterator packageTemplatesIterator() {
        return packageTemplatePaths.iterator();
    }

    Iterator docTemplatesIterator() {
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

}

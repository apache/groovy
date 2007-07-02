/*
 * Copyright 2003-2007 the original author or authors.
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
//	private String relativeTemplatePath;
	private Map docTemplates; // cache
	private List docTemplatePaths; // once per documentation set
	private Map packageTemplates; // cache
	private List packageTemplatePaths; // once per package
	private Map classTemplates; // cache
	private List classTemplatePaths; // once per class
	
	
	public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager, String classTemplate) {
		this(tool, resourceManager, new String[]{}, new String[]{}, new String[] {classTemplate});
	}

	public GroovyDocTemplateEngine(GroovyDocTool tool, ResourceManager resourceManager,
			String[] docTemplates, 
			String[] packageTemplates, 
			String[] classTemplates) {
		this.tool = tool;
		this.resourceManager = resourceManager;
		this.docTemplatePaths = Arrays.asList(docTemplates);
		this.packageTemplatePaths = Arrays.asList(packageTemplates);
		this.classTemplatePaths = Arrays.asList(classTemplates);
		this.docTemplates = new HashMap();
		this.packageTemplates = new HashMap();
		this.classTemplates = new HashMap();
		engine = new GStringTemplateEngine();
		
	}
	
	String applyClassTemplates(GroovyClassDoc classDoc) {
		String templatePath = (String) classTemplatePaths.get(0); // todo (iterate)
			
		String templateWithBindingApplied = "";
		try {
			Template t = (Template) classTemplates.get(templatePath);
			if (t == null) {
				t = engine.createTemplate(resourceManager.getReader(templatePath));
				classTemplates.put(templatePath, t);
			}
			Map binding = new HashMap();
	        binding.put("classDoc", classDoc);
	        
	        templateWithBindingApplied = t.make(binding).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return templateWithBindingApplied;
	}

	String applyPackageTemplate(String template, GroovyPackageDoc packageDoc) {
		String templatePath = template;
			
		String templateWithBindingApplied = "";
		try {
			Template t = (Template) packageTemplates.get(templatePath);
			if (t == null) {
				t = engine.createTemplate(resourceManager.getReader(templatePath));
				packageTemplates.put(templatePath, t);
			}

			Map binding = new HashMap();
	        binding.put("packageDoc", packageDoc);
	        
	        templateWithBindingApplied = t.make(binding).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return templateWithBindingApplied;
	}

	String applyRootDocTemplate(String template, GroovyRootDoc rootDoc) {
		String templatePath = template;
			
		String templateWithBindingApplied = "";
		try {
			Template t = (Template) docTemplates.get(templatePath);
			if (t == null) {
				t = engine.createTemplate(resourceManager.getReader(templatePath));
				docTemplates.put(templatePath, t);
			}

			Map binding = new HashMap();
	        binding.put("rootDoc", rootDoc);
	        
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

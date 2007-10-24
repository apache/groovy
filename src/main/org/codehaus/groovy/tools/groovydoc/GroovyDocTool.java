/*
 * Copyright 2007 Jeremy Rayner
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

import java.io.IOException;
import java.io.File;

import org.codehaus.groovy.groovydoc.GroovyRootDoc;

import antlr.RecognitionException;
import antlr.TokenStreamException;

public class GroovyDocTool {
	
	
	public GroovyDocTool(ResourceManager resourceManager, String sourcepath, String classTemplate) {
		this(resourceManager, sourcepath, new String[]{}, new String[]{}, new String[] {classTemplate});
	}

	public GroovyDocTool(ResourceManager resourceManager, String sourcepath, String[] docTemplates, String[] packageTemplates, String[] classTemplates) {
		rootDocBuilder = new GroovyRootDocBuilder(this, sourcepath);
		templateEngine = new GroovyDocTemplateEngine(this, resourceManager, docTemplates, packageTemplates, classTemplates);
	}
	
	public void add(String filename) throws RecognitionException, TokenStreamException, IOException {
		System.out.println("Loading source files for " + filename);
		rootDocBuilder.buildTree(filename);
	}
	
	public GroovyRootDoc getRootDoc() {
		return rootDocBuilder.getRootDoc();
	}

	public void renderToOutput(OutputTool output, String destdir) throws Exception {
		GroovyDocWriter writer = new GroovyDocWriter(this, output, templateEngine);
		GroovyRootDoc rootDoc = rootDocBuilder.getRootDoc();
		writer.writeRoot(rootDoc, destdir);
		writer.writePackages(rootDoc, destdir);
		writer.writeClasses(rootDoc, destdir);
	}
	
	private final GroovyRootDocBuilder rootDocBuilder;
	private final GroovyDocTemplateEngine templateEngine;

	String getPath(String filename) {
		String path = new File(filename).getParent();
		// path length of 1 indicates that probably is 'default package' i.e. "/"
		if (path == null || path.length() == 1) {
			path = "DefaultPackage"; // "DefaultPackage" for 'default package' path, rather than null...
		}
		return path;
	}
	String getFile(String filename) {
        return new File(filename).getName();        
	}
		

}

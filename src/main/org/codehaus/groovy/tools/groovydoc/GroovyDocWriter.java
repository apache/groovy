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
import java.util.Iterator;

import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;

/**
 * Write GroovyDoc resources to destination.
 */
public class GroovyDocWriter {
    private GroovyDocTool tool;
    private OutputTool output;
    private GroovyDocTemplateEngine templateEngine;
    private static final String FS = "/";

    public GroovyDocWriter(GroovyDocTool tool, OutputTool output, GroovyDocTemplateEngine templateEngine) {
        this.tool = tool;
        this.output = output;
        this.templateEngine = templateEngine;
    }

    public void writeClasses(GroovyRootDoc rootDoc, String destdir) throws Exception {
        for (GroovyClassDoc classDoc : Arrays.asList(rootDoc.classes())) {
            writeClassToOutput(classDoc, destdir);
        }
    }

    public void writeClassToOutput(GroovyClassDoc classDoc, String destdir) throws Exception {
        String destFileName = destdir + FS + classDoc.getFullPathName() + ".html";
        System.out.println("Generating " + destFileName);
        String renderedSrc = templateEngine.applyClassTemplates(classDoc);
        output.writeToOutput(destFileName, renderedSrc);
    }

    public void writePackages(GroovyRootDoc rootDoc, String destdir) throws Exception {
        for (GroovyPackageDoc packageDoc : Arrays.asList(rootDoc.specifiedPackages())) {
            output.makeOutputArea(destdir + FS + packageDoc.name());
            writePackageToOutput(packageDoc, destdir);
        }
    }

    public void writePackageToOutput(GroovyPackageDoc packageDoc, String destdir) throws Exception {
        Iterator<String> templates = templateEngine.packageTemplatesIterator();
        while (templates.hasNext()) {
            String template = templates.next();
            String renderedSrc = templateEngine.applyPackageTemplate(template, packageDoc);
            String destFileName = destdir + FS + packageDoc.name() + FS + tool.getFile(template);
            System.out.println("Generating " + destFileName);
            output.writeToOutput(destFileName, renderedSrc);
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
            String destFileName = destdir + FS + tool.getFile(template);
            System.out.println("Generating " + destFileName);
            if (hasBinaryExtension(template)) {
                templateEngine.copyBinaryResource(template, destFileName);
            } else {
                String renderedSrc = templateEngine.applyRootDocTemplate(template, rootDoc);
                output.writeToOutput(destFileName, renderedSrc);
            }
        }
    }

    private boolean hasBinaryExtension(String template) {
        return template.endsWith(".gif");
    }

}

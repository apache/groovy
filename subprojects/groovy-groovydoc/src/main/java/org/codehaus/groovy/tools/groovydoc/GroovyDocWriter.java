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
import java.util.Iterator;
import java.util.Properties;

/**
 * Write GroovyDoc resources to destination.
 */
public class GroovyDocWriter {
    private final Logger log = Logger.create(GroovyDocWriter.class);
    private final OutputTool output;
    private final GroovyDocTemplateEngine templateEngine;
    private static final String FS = "/";
    private final Properties properties;

    @Deprecated
    public GroovyDocWriter(GroovyDocTool tool, OutputTool output, GroovyDocTemplateEngine templateEngine, Properties properties) {
        this(output, templateEngine, properties);
    }

    public GroovyDocWriter(OutputTool output, GroovyDocTemplateEngine templateEngine, Properties properties) {
        this.output = output;
        this.templateEngine = templateEngine;
        this.properties = properties;
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

    private static boolean hasBinaryExtension(String template) {
        return template.endsWith(".gif") || template.endsWith(".ico");
    }

}

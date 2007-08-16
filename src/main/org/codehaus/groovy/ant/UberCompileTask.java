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

package org.codehaus.groovy.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Javac;

import java.io.File;
import java.io.IOException;

/**
 * Compiles Java and Groovy source files.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class UberCompileTask
    extends CompileTaskSupport
{
    private final LoggingHelper log = new LoggingHelper(this);

    private GenerateStubsTask genStubsTask;

    private GroovycTask groovycTask;

    private Javac javacTask;

    public GenerateStubsTask createGeneratestubs() {
        if (genStubsTask == null) {
            genStubsTask = new GenerateStubsTask();
            genStubsTask.setProject(getProject());
        }
        return genStubsTask;
    }

    public GroovycTask createGroovyc() {
        if (groovycTask == null) {
            groovycTask = new GroovycTask();
            groovycTask.setProject(getProject());
        }
        return groovycTask;
    }

    public Javac createJavac() {
        if (javacTask == null) {
            javacTask = new Javac();
            javacTask.setProject(getProject());
        }
        return javacTask;
    }

    public void execute() throws BuildException {
        validate();

        GenerateStubsTask genstubs = createGeneratestubs();
        genstubs.classpath = classpath;
        genstubs.config = config;
        genstubs.src = src;
        if (genstubs.destdir == null) {
            genstubs.destdir = createTempDir();
        }

        // Append the stubs dir to the classpath for other tasks
        classpath.createPathElement().setLocation(genstubs.destdir);

        Javac javac = createJavac();
        javac.setSrcdir(src);
        javac.setDestdir(destdir);
        javac.setClasspath(classpath);

        GroovycTask groovyc = createGroovyc();
        groovyc.classpath = classpath;
        groovyc.config = config;
        groovyc.src = src;
        groovyc.destdir = destdir;
        
        log.info("Compiling...");

        // Invoke each task in the right order
        genstubs.execute();
        javac.execute();
        groovyc.execute();
    }

    private File createTempDir()  {
        try {
            File dir = File.createTempFile("groovy-", "stubs");
            dir.delete();
            dir.mkdirs();
            return dir;
        }
        catch (IOException e) {
            throw new BuildException(e, getLocation());
        }
    }
}
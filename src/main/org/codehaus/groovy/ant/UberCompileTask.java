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
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;

import java.io.File;
import java.io.IOException;

/**
 * Compiles Java and Groovy source files.
 *
 * This works by invoking the {@link GenerateStubsTask} task, then the
 * {@link Javac} task and then the {@link GroovycTask}.  Each task can
 * be configured by creating a nested element.  Common configuration
 * such as the source dir and classpath is picked up from this tasks
 * configuration.
 *
 * @version $Id$
 * @author <a href="mailto:jason@planet57.com">Jason Dillon</a>
 */
public class UberCompileTask
    extends Task
{
    private final LoggingHelper log = new LoggingHelper(this);

    protected Path src;

    protected File destdir;

    protected Path classpath;
    
    private GenerateStubsTask genStubsTask;

    private GroovycTask groovycTask;

    private Javac javacTask;

    public Path createSrc() {
        if (src == null) {
            src = new Path(getProject());
        }
        return src.createPath();
    }

    public void setSrcdir(final Path srcDir) {
        if (src == null) {
            src = srcDir;
        }
        else {
            src.append(srcDir);
        }
    }

    public Path getSrcdir() {
        return src;
    }

    public void setDestdir(final File dir) {
        assert dir != null;

        this.destdir = dir;
    }

    public void setClasspath(final Path path) {
        if (classpath == null) {
            classpath = path;
        }
        else {
            classpath.append(path);
        }
    }

    public Path getClasspath() {
        return classpath;
    }

    public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(getProject());
        }

        return classpath.createPath();
    }

    public void setClasspathRef(final Reference r) {
        createClasspath().setRefid(r);
    }

    protected void validate() throws BuildException {
        if (src == null) {
            throw new BuildException("Missing attribute: srcdir (or one or more nested <src> elements).", getLocation());
        }

        if (destdir == null) {
            throw new BuildException("Missing attribute: destdir", getLocation());
        }

        if (!destdir.exists()) {
            throw new BuildException("Destination directory does not exist: " + destdir, getLocation());
        }
    }

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
        genstubs.src = src;
        if (genstubs.destdir == null) {
            genstubs.destdir = createTempDir();
        }

        //
        // TODO: Make genstubs includes **/*.java,**/*.groovy by default
        //
        
        // Append the stubs dir to the classpath for other tasks
        classpath.createPathElement().setLocation(genstubs.destdir);

        Javac javac = createJavac();
        javac.setSrcdir(src);
        javac.setDestdir(destdir);
        javac.setClasspath(classpath);

        //
        // TODO: Make javac includes **/*.java by default
        //

        GroovycTask groovyc = createGroovyc();
        groovyc.classpath = classpath;
        groovyc.src = src;
        groovyc.destdir = destdir;

        //
        // TODO: Make groovyc includes **/*.groovy by default
        //

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
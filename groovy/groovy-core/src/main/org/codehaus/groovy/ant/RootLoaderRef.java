/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
 
package org.codehaus.groovy.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.groovy.tools.LoaderConfiguration;
import org.codehaus.groovy.tools.RootLoader;

 
/**
 * Sets the RootLoader as reference.
 * Reexecution of this task will set a new instance of RootLoader for
 * the reference. 
 *
 * arguments:
 * <ul>
 * <li>ref</li>
 * <li>classpath</li>
 * </ul>
 * 
 * all arguments are requiered. 
 *
 * As ant requieres an AntClassLoader as reference, this will create a RootLoader
 * and set an AntClassLoader as child and stored in the reference. The AntClassLoader
 * instance will not have a classpath nor will it have access to the classpath somehow,
 * all loading is done by the RootLoader parent. To avoid problems with loading classes 
 * multiple times and using them at the same time, this task will filter out the ant jars
 * and the commons-logging jars. This only works if the ant jars are starting with "ant-" and
 * the logging jar starts with "commons-logging-".
 * 
 * This was needed because if ant wants to access a task argument that uses for example a Path
 * it look for a matching method which includes a matching class. But two classes of the same name
 * with different classloaders are different, so ant would not be able to find the method.
 *
 * @see org.codehaus.groovy.tools.RootLoader
 * @author Jochen Theodorou
 * @version $Revision$ 
 */
public class RootLoaderRef extends MatchingTask {
    private String name;
    private Path taskClasspath;
    
    /**
     * sets the name of the reference which should store the Loader
     */
    public void setRef(String n){
        name = n;
    }
    
    public void execute() throws BuildException {
        if (taskClasspath==null || taskClasspath.size()==0) {
            throw new BuildException("no classpath given");
        }
        Project project = getProject();
        AntClassLoader loader = new AntClassLoader(makeRoot(),true);
        project.addReference(name,loader);
    }
    
    private RootLoader makeRoot() {
        String[] list = taskClasspath.list();
        LoaderConfiguration lc = new LoaderConfiguration();
        for (int i=0; i<list.length; i++) {
            if (list[i].matches(".*ant-[^/]*jar$")) {
                continue;
            }
            if (list[i].matches(".*commons-logging-[^/]*jar$")) {
                continue;
            }
            lc.addFile(list[i]);
        }
        return new RootLoader(lc);
    }
    
    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (taskClasspath == null) {
            taskClasspath = classpath;
        }
        else {
            taskClasspath.append(classpath);
        }
    }
    
    /**
     * Adds a reference to a classpath defined elsewhere.
     * @param r a reference to a classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }
    
    /**
     * Adds a path to the classpath.
     * @return a class path to be configured
     */
    public Path createClasspath() {
        if (taskClasspath == null) {
            taskClasspath = new Path(getProject());
        }
        return taskClasspath.createPath();
    }
}

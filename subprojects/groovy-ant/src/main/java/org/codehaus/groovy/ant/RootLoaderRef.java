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
package org.codehaus.groovy.ant;

import org.apache.tools.ant.AntClassLoader;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.codehaus.groovy.tools.LoaderConfiguration;
import org.codehaus.groovy.tools.RootLoader;

import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Sets the RootLoader as reference.
 * Re-execution of this task will set a new instance of RootLoader for
 * the reference. 
 *
 * arguments:
 * <ul>
 * <li>ref</li>
 * <li>classpath</li>
 * </ul>
 *
 * all arguments are required.
 *
 * As ant requires an AntClassLoader as reference, this will create a RootLoader
 * and set an AntClassLoader as child and stored in the reference. The AntClassLoader
 * instance will not have a classpath nor will it have access to the classpath somehow,
 * all loading is done by the RootLoader parent. To avoid problems with loading classes
 * multiple times and using them at the same time, this task will filter out the ant jars
 * and the commons-logging jars. This only works if the ant jars are starting with "ant-" and
 * the logging jar starts with "commons-logging-".
 *
 * This was needed because if ant wants to access a task argument that uses for example a Path
 * it look for a matching method which includes a matching class. But two classes of the same name
 * with different class loaders are different, so ant would not be able to find the method.
 *
 * @see org.codehaus.groovy.tools.RootLoader
 */
public class RootLoaderRef extends MatchingTask {
    private String name;
    private Path taskClasspath;

    /**
     * sets the name of the reference which should store the Loader
     */
    public void setRef(String n) {
        name = n;
    }

    @Override
    public void execute() throws BuildException {
        if (taskClasspath == null || taskClasspath.size() == 0) {
            throw new BuildException("no classpath given");
        }
        Project project = getProject();
        String[] list = taskClasspath.list();
        LoaderConfiguration lc = new LoaderConfiguration();
        for (String s : list) {
            if (s.matches(".*ant-[^/]*jar$")) {
                continue;
            }
            if (s.matches(".*commons-logging-[^/]*jar$")) {
                continue;
            }
            if (s.matches(".*xerces-[^/]*jar$")) {
                continue;
            }
            lc.addFile(s);
        }
        AntClassLoader loader = AccessController.doPrivileged((PrivilegedAction<AntClassLoader>) () -> new AntClassLoader(new RootLoader(lc), true));
        project.addReference(name, loader);
    }

    /**
     * Set the classpath to be used for this compilation.
     *
     * @param classpath an Ant Path object containing the compilation classpath.
     */
    public void setClasspath(Path classpath) {
        if (taskClasspath == null) {
            taskClasspath = classpath;
        } else {
            taskClasspath.append(classpath);
        }
    }

    /**
     * Adds a reference to a classpath defined elsewhere.
     *
     * @param r a reference to a classpath
     */
    public void setClasspathRef(Reference r) {
        createClasspath().setRefid(r);
    }

    /**
     * Adds a path to the classpath.
     *
     * @return a class path to be configured
     */
    public Path createClasspath() {
        if (taskClasspath == null) {
            taskClasspath = new Path(getProject());
        }
        return taskClasspath.createPath();
    }
}

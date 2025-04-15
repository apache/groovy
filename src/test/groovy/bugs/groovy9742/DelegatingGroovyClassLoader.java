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
package bugs.groovy9742;

import groovy.lang.GroovyClassLoader;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class DelegatingGroovyClassLoader extends ClassLoader {
    public int loadedCount = 0;
    public DelegatingGroovyClassLoader(ClassLoader parent) {
        super(parent);
        groovyClassLoader = new GroovyClassLoader(this);
    }
    private static final File srcDir;

    static {
        try {
            srcDir = new File(DelegatingGroovyClassLoader.class.getResource("/").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private final GroovyClassLoader groovyClassLoader;
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        synchronized (getClassLoadingLock(name)) {
            Class<?> c = doFindClass(name);
            if (c != null) {
                if (resolve) {
                    resolveClass(c);
                }
                return c;
            }
        }
        return super.loadClass(name, resolve);
    }
    private Class<?> doFindClass(String name) {
        File classFile = new File(srcDir, name.replace('.', '/') + ".groovy");
        if (classFile.exists()) {
            try {
//                System.out.println("PARSE\t: " + name);
                Class<?> clz = groovyClassLoader.parseClass(classFile);
                loadedCount++;
//                System.out.println("PARSED\t: " + clz);
                return clz;
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}


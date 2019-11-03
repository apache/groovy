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
package org.codehaus.groovy.tools;

import java.io.FileInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Helper class to initialize the Groovy runtime.
 */
public class GroovyStarter {

    static void printUsage() {
        System.out.println("possible programs are 'groovyc','groovy','console', and 'groovysh'");
        System.exit(1);
    }
    
    
    public static void rootLoader(String args[]) {
        String conf = System.getProperty("groovy.starter.conf",null);
        final LoaderConfiguration lc = new LoaderConfiguration();
        
        // evaluate parameters
        boolean hadMain=false, hadConf=false, hadCP=false;
        int argsOffset = 0;
        while (args.length-argsOffset>0 && !(hadMain && hadConf && hadCP)) {
            if (args[argsOffset].equals("--classpath")) {
                if (hadCP) break;
                if (args.length==argsOffset+1) {
                    exit("classpath parameter needs argument");
                }
                lc.addClassPath(args[argsOffset+1]);
                argsOffset+=2;
                hadCP=true;
            } else if (args[argsOffset].equals("--main")) {
                if (hadMain) break;
                if (args.length==argsOffset+1) {
                    exit("main parameter needs argument");
                }
                lc.setMainClass(args[argsOffset+1]);
                argsOffset+=2;
                hadMain=true;
            } else if (args[argsOffset].equals("--conf")) {
                if (hadConf) break;
                if (args.length==argsOffset+1) {
                    exit("conf parameter needs argument");
                }
                conf=args[argsOffset+1];
                argsOffset+=2;
                hadConf=true;
            } else {
                break;
            }            
        }

        // this allows to override the commandline conf
        String confOverride = System.getProperty("groovy.starter.conf.override",null);
        if (confOverride!=null) conf = confOverride;

        // we need to know the class we want to start
        if (lc.getMainClass()==null && conf==null) {
            exit("no configuration file or main class specified");
        }
        
        // copy arguments for main class 
        String[] newArgs = new String[args.length-argsOffset];
        System.arraycopy(args, 0 + argsOffset, newArgs, 0, newArgs.length);
        // load configuration file
        if (conf!=null) {
            try {
                lc.configure(new FileInputStream(conf));
            } catch (Exception e) {
                System.err.println("exception while configuring main class loader:");
                exit(e);
            }
        }
        // create loader and execute main class
        ClassLoader loader = AccessController.doPrivileged((PrivilegedAction<RootLoader>) () -> new RootLoader(lc));
        Method m=null;
        try {
            Class c = loader.loadClass(lc.getMainClass());
            m = c.getMethod("main", String[].class);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException e1) {
            exit(e1);
        }
        try {
            m.invoke(null, new Object[]{newArgs});
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e3) {
            exit(e3);
        }
    }
    
    private static void exit(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }
    
    private static void exit(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
    
    public static void main(String args[]) {
        try {
            rootLoader(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}

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
package org.codehaus.groovy.tools;

import java.lang.reflect .*;
import java.io.FileInputStream;



/**
 * Helper class to help classworlds to load classes. 
 */
public class GroovyStarter {

    static void printUsage() {
        System.out.println("possible programs are 'groovyc','groovy','console','grok' and 'groovysh'");
        System.exit(1);
    }
    
    
    public static void rootLoader(String args[]) {
        String conf = System.getProperty("groovy.starter.conf",null);
        LoaderConfiguration lc = new LoaderConfiguration();
        
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
            } else if (args[argsOffset].equals("--main")) {
                if (hadMain) break;
                if (args.length==argsOffset+1) {
                    exit("main parameter needs argument");
                }
                lc.setMainClass(args[argsOffset+1]);
                argsOffset+=2;
            } else if (args[argsOffset].equals("--conf")) {
                if (hadConf) break;
                if (args.length==argsOffset+1) {
                    exit("conf parameter needs argument");
                }
                conf=args[argsOffset+1];
                argsOffset+=2;
            } else {
                break;
            }            
        }
        
        // we need to know the class we want to start
        if (lc.getMainClass()==null && conf==null) {
            exit("no configuration file or main class specified");
        }
        
        // copy arguments for main class 
        String[] newArgs = new String[args.length-argsOffset];
        for (int i=0; i<newArgs.length; i++) {
            newArgs[i] = args[i+argsOffset];
        }        
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
        ClassLoader loader = new RootLoader(lc);
        Method m=null;
        try {
            Class c = loader.loadClass(lc.getMainClass());
            m = c.getMethod("main", new Class[]{String[].class});
        } catch (ClassNotFoundException e1) {
            exit(e1);
        } catch (SecurityException e2) {
            exit(e2);
        } catch (NoSuchMethodException e2) {
            exit(e2);
        }
        try {
            m.invoke(null, new Object[]{newArgs});
        } catch (IllegalArgumentException e3) {
            exit(e3);
        } catch (IllegalAccessException e3) {
            exit(e3);
        } catch (InvocationTargetException e3) {
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
 
    // after migration from classworlds to the rootloader rename
    // the rootLoader method to main and remove this method as 
    // well as the classworlds method
   /* public static void main(String args[],ClassWorld classWorld ) {
        classworlds(args,classWorld);
    }*/
    
    public static void main(String args[]) {
        try {
            rootLoader(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /*public static void classworlds(String oldArgs[],ClassWorld classWorld ) {
        try {
            // Creates a realm with *just* the system classloader
            ClassRealm system = classWorld.newRealm("system");
     
            // Get the groovy realm
            ClassRealm groovy = classWorld.getRealm("groovy");
           
            // import everything from the system realm, because imports
            // are searched *first* in Classworlds
            groovy.importFrom("system", "");
            
            //add tools.jar to classpath
            String tools = System.getProperty("tools.jar");
            if (tools!=null) {
            	URL ref = (new File(tools)).toURI().toURL();
            	groovy.addConstituent(ref);
            }
        
            if (oldArgs.length==0) {
                printUsage();
                System.exit(1);
            }
            
            String program = oldArgs[0].toLowerCase();
            String[] args = new String[oldArgs.length-1];
            for (int i=0; i<args.length; i++) {
                args[i] = oldArgs[i+1];
            }
            
            if (program.equals("groovyc")) {
                org.codehaus.groovy.tools.FileSystemCompiler.main(args);
            } else if (program.equals("groovy")) {
                GroovyMain.main(args);
            } else if (program.equals("console")) {
                // work around needed, because the console is compiled after this files
                Class c = Class.forName("groovy.ui.Console");
                Method m= c.getMethod("main", new Class[]{String[].class});
                m.invoke(null, new Object[]{args});
            } else if (program.equals("groovysh")) {
                InteractiveShell.main(args);
             } else if (program.equals("grok")) {
                org.codehaus.groovy.tools.Grok.main(args);
            } else {
                System.out.println("unknown program "+program);
                printUsage();
                System.exit(1);
            }
        
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
        
    }*/
    
}

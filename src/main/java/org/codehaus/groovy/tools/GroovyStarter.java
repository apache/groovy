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

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.LogManager;

import static java.lang.System.Logger.Level.ERROR;

/**
 * Helper class to initialize the Groovy runtime.
 */
public class GroovyStarter {

    // Logger is in a holder class so it is not initialized until after
    // configuration (including logging properties) has been loaded.
    private static class LogHolder {
        static final System.Logger LOGGER = System.getLogger(GroovyStarter.class.getName());
    }

    static void printUsage() {
        System.out.println("possible programs are 'groovyc','groovy','console', and 'groovysh'");
        System.exit(1);
    }

    public static void main(String[] args) {
        try {
            rootLoader(args);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void rootLoader(String[] args) {
        String conf = System.getProperty("groovy.starter.conf",null);
        final LoaderConfiguration lc = new LoaderConfiguration();

        // evaluate parameters
        boolean hadMain=false, hadConf=false, hadCP=false;
        int argsOffset = 0;
        label:
        while (args.length-argsOffset>0 && !(hadMain && hadConf && hadCP)) {
            switch (args[argsOffset]) {
                case "--classpath":
                    if (hadCP) break label;
                    if (args.length == argsOffset + 1) {
                        exit("classpath parameter needs argument");
                    }
                    lc.addClassPath(args[argsOffset + 1]);
                    argsOffset += 2;
                    hadCP = true;
                    break;
                case "--main":
                    if (hadMain) break label;
                    if (args.length == argsOffset + 1) {
                        exit("main parameter needs argument");
                    }
                    lc.setMainClass(args[argsOffset + 1]);
                    argsOffset += 2;
                    hadMain = true;
                    break;
                case "--conf":
                    if (hadConf) break label;
                    if (args.length == argsOffset + 1) {
                        exit("conf parameter needs argument");
                    }
                    conf = args[argsOffset + 1];
                    argsOffset += 2;
                    hadConf = true;
                    break;
                default:
                    break label;
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
        String[] mainArgs = Arrays.copyOfRange(args, argsOffset, args.length);
        // load configuration file
        if (conf!=null) {
            try {
                lc.configure(new FileInputStream(conf));
            } catch (Exception e) {
                LogHolder.LOGGER.log(ERROR, "Exception while configuring main class loader");
                exit(e);
            }
        }

        // Auto-discover user logging configuration from ~/.groovy/logging.properties
        // if no explicit logging config was set via -D or groovy-starter.conf.
        // This must happen before any logger in LogHolder is accessed.
        if (System.getProperty("java.util.logging.config.file") == null) {
            File loggingConfig = new File(System.getProperty("user.home"), ".groovy/logging.properties");
            if (loggingConfig.isFile()) {
                System.setProperty("java.util.logging.config.file", loggingConfig.getAbsolutePath());
                try {
                    LogManager.getLogManager().readConfiguration();
                } catch (Exception ignore) {
                }
            }
        }

        // create loader and execute main class
        ClassLoader loader = getLoader(lc);
        Method m = null;
        try {
            Class<?> c = loader.loadClass(lc.getMainClass());
            m = c.getMethod("main", String[].class);
        } catch (ReflectiveOperationException | SecurityException e2) {
            exit(e2);
        }
        try {
            m.invoke(null, new Object[]{mainArgs});
        } catch (ReflectiveOperationException | IllegalArgumentException e3) {
            exit(e3);
        }
    }

    private static ClassLoader getLoader(LoaderConfiguration lc) {
        return new RootLoader(lc);
    }

    private static void exit(Exception e) {
        e.printStackTrace();
        System.exit(1);
    }

    private static void exit(String text) {
        LogHolder.LOGGER.log(ERROR, text);
        System.exit(1);
    }
}

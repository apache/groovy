/* Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT c;pWARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.runtime;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * Originally was grails.utils.GrailsUtils
 * Grails utility methods for command line and GUI applications
 *
 * @author Graeme Rocher
 * @since 0.2
 *
 * @version $Revision: 5544 $
 * First Created: 02-Jun-2006
 * Last Updated: $Date: 2007-09-21 13:53:07 -0500 (Fri, 21 Sep 2007) $
 *
 */
public class StackTraceUtils {

	//private static final Logger LOG  = Logger.getLogger(StackUtils.class.getName()); //TODO use shell logger?  commons logger?
    private static final Logger STACK_LOG  = Logger.getLogger("StackTrace"); //TODO use shell logger?  commons logger?
    private static final String[] GROOVY_PACKAGES =
            System.getProperty("groovy.sanitized.stacktraces",
                "org.codehaus.groovy.runtime.," +
                "org.codehaus.groovy.reflection.," +
                "groovy.lang.," +
                "gjdk.groovy.lang.," +
                "sun." +
                "java.lang.reflect."
            ).split("(\\s|,)+");

//    static {
//        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
//        String version = null;
//        try {
//            Resource[] manifests = resolver.getResources("classpath*:META-INF/GRAILS-MANIFEST.MF");
//            Manifest grailsManifest = null;
//            for (int i = 0; i < manifests.length; i++) {
//                Resource r = manifests[i];
//                Manifest mf = new Manifest(r.getInputStream());
//                String implTitle = mf.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_TITLE);
//                if(!StringUtils.isBlank(implTitle) && implTitle.equals(GRAILS_IMPLEMENTATION_TITLE))   {
//                    grailsManifest = mf;
//                    break;
//                }
//            }
//
//            if(grailsManifest != null) {
//                version = grailsManifest.getMainAttributes().getValue(Attributes.Name.IMPLEMENTATION_VERSION);
//            }
//
//            if(StringUtils.isBlank(version)) {
//                LOG.error("Unable to read Grails version from MANIFEST.MF. Are you sure it the grails-core jar is on the classpath? " );
//                version = "Unknown";
//            }
//        } catch (IOException e) {
//            version = "Unknown";
//            LOG.error("Unable to read Grails version from MANIFEST.MF. Are you sure it the grails-core jar is on the classpath? " + e.getMessage(), e);
//        }
//
//        GRAILS_VERSION = version;
//    }

    /**
     * <p>Remove all apparently Grails-internal trace entries from the exception instance<p>
     * <p>This modifies the original instance and returns it, it does not clone</p>
     * @param t
     * @return The exception passed in, after cleaning the stack trace
     */
    public static Throwable sanitize(Throwable t) {
        // Note that this getProperty access may well be synced...
        if (!Boolean.parseBoolean(System.getProperty("groovy.full.stacktrace", "false"))) {
            StackTraceElement[] trace = t.getStackTrace();
            List newTrace = new ArrayList();
            for (int i = 0; i < trace.length; i++) {
                StackTraceElement stackTraceElement = trace[i];
                if (isApplicationClass(stackTraceElement.getClassName())) {
                    newTrace.add( stackTraceElement);
                }
            }
            // We don't want to lose anything, so log it
            STACK_LOG.log(Level.WARNING, "Sanitizing stacktrace:", t);
            StackTraceElement[] clean = new StackTraceElement[newTrace.size()];
            newTrace.toArray(clean);
            t.setStackTrace(clean);
        }
        return t;
    }

    public static void printSanitizedStackTrace(Throwable t, PrintWriter p) {
        t = StackTraceUtils.sanitize(t);

        StackTraceElement[] trace = t.getStackTrace();
        for (int i = 0; i < trace.length; i++) {
            StackTraceElement stackTraceElement = trace[i];
            p.println(  "at "+stackTraceElement.getClassName()
                        +"("+stackTraceElement.getMethodName()
                        +":"+stackTraceElement.getLineNumber()+")");
        }
    }

    public static void printSanitizedStackTrace(Throwable t) {
        printSanitizedStackTrace(t, new PrintWriter(System.err));
    }

    public static boolean isApplicationClass(String className) {
        for (int i = 0; i < GROOVY_PACKAGES.length; i++) {
            String grailsPackage = GROOVY_PACKAGES[i];
            if (className.startsWith(grailsPackage)) {
                return false;
            }
        }
        return true;
    }

    /**
     * <p>Extracts the root cause of the exception, no matter how nested it is</p>
     * @param t
     * @return The deepest cause of the exception that can be found
     */
    public static Throwable extractRootCause(Throwable t) {
        Throwable result = t;
        while (result.getCause() != null) {
            result = result.getCause();
        }
        return result;
    }

    /**
     * <p>Get the root cause of an exception and sanitize it for display to the user</p>
     * <p>This will MODIFY the stacktrace of the root cause exception object and return it</p>
     * @param t
     * @return The root cause exception instance, with its stace trace modified to filter out grails runtime classes
     */
    public static Throwable sanitizeRootCause(Throwable t) {
        return StackTraceUtils.sanitize(StackTraceUtils.extractRootCause(t));
    }

    /**
     * <p>Sanitize the exception and ALL nested causes</p>
     * <p>This will MODIFY the stacktrace of the exception instance and all its causes irreversibly</p>
     * @param t
     * @return The root cause exception instances, with stack trace modified to filter out grails runtime classes
     */
    public static Throwable deepSanitize(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null) {
            current = StackTraceUtils.sanitize(current.getCause());
        }
        return StackTraceUtils.sanitize(t);
    }
}

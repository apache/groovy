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
package groovy.lang;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to add a repository for resolving Grape dependencies.
 * <p>
 * For example:
 * <pre>
 * {@code @GrabResolver}(name='restlet.org', root='http://maven.restlet.org')
 * {@code @Grab}(group='org.restlet', module='org.restlet', version='1.1.6')
 * class MyRestlet extends org.restlet.Restlet {
 *   // ...
 * }
 * </pre>
 * By default, the Grapes subsystem uses an Ivy chained resolver. Each resolver
 * added using {@code @GrabResolver} is appended to the chain. By default, the grape
 * subsystem is shared globally, so added resolvers will become available for any subsequent
 * grab calls. Dependency resolution follows Ivy's artifact resolution which tries
 * to resolve artifacts in the order specified in the chain of resolvers.
 * <p>
 * Further information about customising grape behavior can be found on the Grape documentation page:
 * <a href="http://groovy-lang.org/grape.html">http://groovy-lang.org/grape.html</a>.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.TYPE})
public @interface GrabResolver {
    /**
     * Allows a shorthand form which sets the name and root to this value.
     * Must not be used if name() or root() is non-empty.
     */
    String value() default "";

    /**
     * A meaningful name for a repo containing the grape/artifact.
     * A non-empty value is required unless value() is used.
     */
    String name() default "";

    /**
     * The URL for a repo containing the grape/artifact.
     * A non-empty value is required unless value() is used.
     */
    String root() default "";

    /**
     * Defaults to Maven2 compatibility. Set false for Ivy only compatibility.
     */
    boolean m2Compatible() default true;

    /**
     * By default, when a {@code @GrabResolver} annotation is used, a {@code Grape.addResolver()} call is added
     * to the static initializers of the class the annotatable node appears in.
     * If you wish to disable this, add {@code initClass=false} to the annotation.
     */
    boolean initClass() default true;
}

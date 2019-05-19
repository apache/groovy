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
 * Used to grab the referenced artifact and its dependencies and make it available on the Classpath.
 * <p>
 * Some examples:
 * <pre>
 * {@code @Grab}(group='commons-lang', module='commons-lang', version='2.4')
 * import org.apache.commons.lang.WordUtils
 * println "Hello ${WordUtils.capitalize('world')}"
 * </pre>
 * Or using the compact Gradle-inspired syntax:
 * <pre>
 * {@code @Grab}('commons-lang:commons-lang:2.4')
 * import org.apache.commons.lang.WordUtils
 * println "Hello ${WordUtils.capitalize('world')}"
 * </pre>
 * or the same thing again using the Ivy-inspired syntax variant:
 * <pre>
 * {@code @Grab}('commons-lang#commons-lang;2.4')
 * import org.apache.commons.lang.WordUtils
 * println "Hello ${WordUtils.capitalize('world')}"
 * </pre>
 * Further information such as where artifacts are downloaded to, how to add additional resolvers,
 * how to customise artifact resolution etc., can be found on the Grape documentation page:
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
public @interface Grab {
    /**
     * The organisation or group, e.g.: "org.apache.ant". A non-empty value is required unless value() is used.
     */
    String group() default "";

    /**
     * The module or artifact, e.g.: "ant-junit". A non-empty value is required unless value() is used.
     */
    String module() default "";

    /**
     * The revision or version, e.g.: "1.7.1". A non-empty value is required unless value() is used.
     */
    String version() default "";

    /**
     * The classifier if in use, e.g.: "jdk14"
     */
    String classifier() default "";

    /**
     * Defaults to {@code true} but set to {@code false} if you don't want transitive dependencies also to be downloaded.
     * You may then need additional {@code @Grab} statements for any required dependencies.
     */
    boolean transitive() default true;

    /**
     * Defaults to {@code false} but set to {@code true} to indicate to the underlying Ivy conflict manager that this
     * dependency should be forced to the given revision. Otherwise, depending on the conflict manager in play, a later
     * compatible version might be used instead.
     */
    boolean force() default false;

    /**
     * Defaults to {@code false} but set to {@code true} if the dependency artifacts may change without a corresponding
     * revision change. Not normally recommended but may be useful for certain kinds of snapshot artifacts.
     * May reduce the amount of underlying Ivy caching. Proper behavior may be dependent on the resolver in use.
     */
    boolean changing() default false;

    /**
     * The configuration if in use (normally only used by internal ivy repositories).
     * One or more comma separated values with or without square brackets,
     * e.g.&#160;for hibernate you might have "default,proxool,oscache" or "[default,dbcp,swarmcache]".
     * This last hibernate example assumes you have set up such configurations in your local Ivy repo
     * and have changed your grape config (using grapeConfig.xml) or the {@code @GrabConfig} annotation
     * to point to that repo.
     */
    String conf() default "";

    /**
     * The extension of the artifact (normally safe to leave at default value of "jar" but other values like "zip"
     * are sometimes useful).
     */
    String ext() default "";

    /**
     * The type of the artifact (normally safe to leave at default value of "jar" but other values like "sources" and "javadoc" are sometimes useful).
     * But see also the "classifier" attribute which is also sometimes used for "sources" and "javadoc".
     */
    String type() default "";

    /**
     * Allows a more compact convenience form in one of two formats with optional appended attributes.
     * Must not be used if group(), module() or version() are used.
     * <p>
     * You can choose either format but not mix-n-match:<br>
     * {@code group:module:version:classifier@ext} (where only group and module are required)<br>
     * {@code group#module;version[confs]} (where only group and module are required and confs,
     * if used, is one or more comma separated configuration names)<br>
     * In addition, you can add any valid Ivy attributes at the end of your string value using
     * semi-colon separated name = value pairs, e.g.:<br>
     * {@code @Grab('junit:junit:*;transitive=false')}<br>
     * {@code @Grab('group=junit;module=junit;version=4.8.2;classifier=javadoc')}<br>
     */
    String value() default "";

    /**
     * By default, when a {@code @Grab} annotation is used, a {@code Grape.grab()} call is added
     * to the static initializers of the class the annotatable node appears in.
     * If you wish to disable this, add {@code initClass=false} to the annotation.
     */
    boolean initClass() default true;
}

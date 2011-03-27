/*
 * Copyright 2003-2011 the original author or authors.
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
package groovy.lang;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

/**
 * Used to grab the referenced artifact and its dependencies and make it available on the Classpath.
 * <p/>
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
     * The organisation or group, e.g.: "org.apache.ant"
     */
    String group() default "";

    /**
     * The module or artifact, e.g.: "ant-junit"
     */
    String module();

    /**
     * The revision or version, e.g.: "1.7.1"
     */
    String version();

    /**
     * The classifier if in use, e.g.: "jdk14"
     */
    String classifier() default "";

    /**
     * Set to false if you don't want transitive dependencies also to be downloaded.
     * You may then need additional {@code @Grab} statements for any required dependencies.
     */
    boolean transitive() default true;

    /**
     * The configuration if in use (normally only used by internal ivy repositories).
     * One or more coma separated values with or without square brackets,
     * e.g. for hibernate you might have "default,proxool,oscache" or "[default,dbcp,swarmcache]".
     * This last hibernate example assumes you have set up such configurations in your
     * local Ivy repo and changed your grape config to point to that repo.
     */
    String conf() default "";

    /**
     * The extension of the artifact (normally safe to leave at default value of "jar" but other values like "zip" are sometimes useful)
     */
    String ext() default "";

    /**
     * The type of the artifact (normally safe to leave at default value of "jar" but other values like "sources" and "javadoc" are sometimes useful)
     */
    String type() default "";

    /**
     * Allows a more compact convenience format in one of two formats.
     * <p/>
     * You can choose either format but not mix-n-match:<br/>
     * {@code group:module:version:classifier@ext} (where only group and module are required)<br/>
     * {@code group#module;version[confs]} (where only group and module are required and confs, if used, is one or more comma separated configuration names)<br/>
     */
    String value() default "";

    /**
     * By default, when a {@code @Grab} annotation is used, the {@code grab()} call is added
     * to the static initializers of the class the annotatable node appears in.
     * If you wish to disable this, add {@code initClass=false} to the annotation.
     */
    boolean initClass() default true;
}

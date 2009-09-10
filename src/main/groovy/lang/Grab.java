/*
 * Copyright 2003-2009 the original author or authors.
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
 *
 * Some examples:
 * <pre>
 * {@code @Grab}(group='commons-lang', module='commons-lang', version='2.4')
 * import org.apache.commons.lang.WordUtils
 * println "Hello ${WordUtils.capitalize('world')}"
 * </pre>
 * Or using the compact syntax:
 * <pre>
 * {@code @Grab}('commons-lang:commons-lang:2.4')
 * import org.apache.commons.lang.WordUtils
 * println "Hello ${WordUtils.capitalize('world')}"
 * </pre>
 * or this variant:
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
     * @return the organisation or group, e.g. "org.apache.ant"
     */
    String group() default "";

    /**
     * @return the module or artifact, e.g. "ant-junit"
     */
    String module();

    /**
     * @return the revision or version, e.g. "1.7.1"
     */
    String version();

    /**
     * @return the classifier if in use, e.g. "jdk14"
     */
    String classifier() default "";

    /**
     * @return set to false if you don't want transitive dependencies also to be downloaded
     */
    boolean transitive() default true;

    /**
     * @return the configuration if in use (normally only used by internal ivy repositories)
     */
    String conf() default "";

    /**
     * @return the extension of the artifact (normally safe to leave at default value of "jar")
     */
    String ext() default "";

    /**
     * allows a more compact convenience format in one of two formats:<br/>
     * group:module:version:classifier@ext (where only group and module are required)<br/>
     * group#module;version[confs] (where version is optional and confs is one or more comma separated configurations)<br/>
     */
    String value() default "";

    /**
     * By default, when a @Grab annotation is used, the grab() call is added
     * to the static initializers of the class the annotatable node appears in.
     * If you wish to disable this add initClass=false to the annotation.
     */
    boolean initClass() default true;
}

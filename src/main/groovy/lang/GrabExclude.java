/*
 * Copyright 2003-2013 the original author or authors.
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Used to exclude an indirectly referenced artifact from the classpath.
 * <p>
 * Further information about customising grape behavior can be found on the Grape documentation page:
 * <a href="http://groovy.codehaus.org/Grape">http://groovy.codehaus.org/Grape</a>.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({
        ElementType.CONSTRUCTOR,
        ElementType.FIELD,
        ElementType.LOCAL_VARIABLE,
        ElementType.METHOD,
        ElementType.PARAMETER,
        ElementType.TYPE})
public @interface GrabExclude {
    /**
     * The organisation or group, e.g.: "org.apache.ant"
     */
    String group() default "";

    /**
     * The module or artifact, e.g.: "ant-junit"
     */
    String module();

    /**
     * Allows a more compact convenience format in one of two formats,
     * e.g.: "org.apache.ant:ant-junit" or "org.apache.ant#ant-junit".
     */
    String value() default "";
}
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
 * Used to exclude an indirectly referenced artifact (a transitive dependency) from the classpath.
 * <p>
 * Examples:<br>
 * {@code @GrabExclude(group='mysql', module='mysql-connector-java') // group/module form}<br>
 * {@code @GrabExclude('mysql:mysql-connector-java') // compact form}<br>
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
public @interface GrabExclude {

    /**
     * The organisation or group, e.g.: "org.apache.ant"; required unless the compact form is used.
     */
    String group() default "";

    /**
     * The module or artifact, e.g.: "ant-junit"; required unless the compact form is used.
     */
    String module() default "";

    /**
     * Allows you to specify the group (organisation) and the module (artifact) in one of two compact convenience formats,
     * e.g.: <code>@GrabExclude('org.apache.ant:ant-junit')</code> or <code>@GrabExclude('org.apache.ant#ant-junit')</code>
     */
    String value() default "";
}
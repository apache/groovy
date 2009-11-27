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
 * Used to add a repository for resolving Grape dependencies
 * <p/>
 * For example:
 * <pre>
 * {@code @GrabResolver}(name='restlet.org', root='http://maven.restlet.org')
 * {@code @Grab}(group='org.restlet', module='org.restlet', version='1.1.6')
 * class MyRestlet extends org.restlet.Restlet {
 *   // ...
 * }
 * </pre>
 * @author Merlyn Albery-Speyer
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
    String name();
    String root();
    boolean m2Compatible() default true;
}

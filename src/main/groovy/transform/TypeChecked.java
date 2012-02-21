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
package groovy.transform;

import java.lang.annotation.*;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;
import org.codehaus.groovy.transform.stc.TypeCheckerPluginFactory;

/**
 * This will let the Groovy compiler use compile time checks in the style of Java.
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({   ElementType.METHOD,         ElementType.TYPE,
            ElementType.CONSTRUCTOR
})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.StaticTypesTransformation")
public @interface TypeChecked {
    Class<? extends TypeCheckerPluginFactory> pluginFactory() default TypeCheckerPluginFactory.class;
}
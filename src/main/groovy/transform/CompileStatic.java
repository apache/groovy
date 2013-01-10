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

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.*;

/**
 * This will let the Groovy compiler use compile time checks in the style of Java
 * then perform static compilation, thus bypassing the Groovy meta object protocol.
 *
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 * @author Cedric Champeau
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({   ElementType.METHOD,         ElementType.TYPE,
            ElementType.CONSTRUCTOR,    ElementType.FIELD,
            ElementType.LOCAL_VARIABLE, ElementType.PACKAGE
})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.sc.StaticCompileTransformation")
public @interface CompileStatic {
    TypeCheckingMode value() default TypeCheckingMode.PASS;

    /**
     * The list of (classpath resources) paths to type checking DSL scripts, also known
     * as type checking extensions.
     * @return an array of paths to groovy scripts that must be on compile classpath
     */
    String[] extensions() default {};
}

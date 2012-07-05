/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package groovy.runtime.metaclass.org.codehaus.groovy.control

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder
import groovy.transform.CompileStatic

/**
 * This metaclass adds a "customizers" method onto the {@link CompilerConfiguration} class, allowing
 * to use the builder syntax to define compilation customizers when configuration is created in Groovy.
 *
 * @author Cedric Champeau
 * @since 2.1.0
 */
@CompileStatic
class CompilerConfigurationMetaClass extends DelegatingMetaClass {
    CompilerConfigurationMetaClass(MetaClass delegate) {
        super(delegate)
    }

    Object invokeMethod(Object object, String method, Object[] arguments) {
        if (method == 'customizers' && arguments && arguments[0] instanceof Closure) {
            customizers((CompilerConfiguration)object, (Closure)arguments[0])
        } else {
            super.invokeMethod object, method, arguments
        }
    }

    /**
     * Add several compilation customizers using the builder syntax.
     * @see org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder
     * @param builderBody the builder body
     * @return this configuration instance
     * @since 2.1.0
     */
    public static CompilerConfiguration customizers(CompilerConfiguration config, Closure builderBody) {
        CompilerCustomizationBuilder builder = new CompilerCustomizationBuilder()
        Object result = builder.invokeMethod("customizers", builderBody)
        if (result instanceof CompilationCustomizer) {
            config.addCompilationCustomizers((CompilationCustomizer)result)
        } else if (result instanceof List) {
            List items = (List) result
            for (Object item : items) {
                if (item instanceof CompilationCustomizer) {
                    config.addCompilationCustomizers((CompilationCustomizer)item)
                }
            }
        }
        if (result instanceof CompilationCustomizer[]) {
            config.addCompilationCustomizers((CompilationCustomizer[])result)
        }
        return config
    }
}

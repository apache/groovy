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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.expr.AttributeExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

/**
 * This interface defines a high-level API for handling type checking errors. As a dynamic language and a platform
 * for developing DSLs, the Groovy language provides a lot of means to supply custom bindings or methods that are
 * not possible to find at compile time. However, it is still possible to help the compiler, for example by
 * telling it what is the type of an unresolved property.
 *
 * For basic DSL type checking, implementing those methods would help the type checker and make it silent where it
 * normally throws errors.
 *
 * @author Cedric Champeau
 * @since 2.1.0
 */
public interface TypeCheckingErrorHandler {
    boolean handleUnresolvedVariableExpression(VariableExpression vexp);

    boolean handleUnresolvedProperty(PropertyExpression pexp);

    boolean handleUnresolvedAttribute(AttributeExpression aexp);
}

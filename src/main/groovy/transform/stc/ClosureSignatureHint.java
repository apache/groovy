/*
 * Copyright 2003-2013 the original author or authors.
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
package groovy.transform.stc;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;

import java.util.List;

/**
 * <p>A closure signature hint class is always used in conjunction with the {@link ClosureParams} annotation. It is
 * called at compile time (or may be used by IDEs) to infer the types of the parameters of a {@link groovy.lang.Closure}.</p>

 * <p>A closure hint class is responsible for generating the list of arguments that a closure accepts. Since closures
 * may accept several signatures, {@link #getClosureSignatures(org.codehaus.groovy.ast.MethodNode, String[])} should
 * return a list.</p>
 *
 * <p>Whenever the type checker encounters a method call that targets a method accepting a closure, it will search
 * for the {@link ClosureParams} annotation on the {@link groovy.lang.Closure} argument. If it is found, then it
 * creates an instance of the hint class and calls the {@link #getClosureSignatures(org.codehaus.groovy.ast.MethodNode, String[])}
 * method, which will in turn return the list of signatures.</p>
 *
 * <p><i>Note that the signature concept here is used only to describe the parameter types, not the result type, which
 * is found in the generic type argument of the {@link groovy.lang.Closure} class.</i></p>
 *
 * <p>Several predefined hints can be found, which should cover most of the use cases.</p>
 *
 * @author Cédric Champeau
 * @since 2.3.0
 *
 */
public abstract class ClosureSignatureHint {

    /**
     * A helper method which will extract the n-th generic type from a class node.
     * @param type the class node from which to pick a generic type
     * @param gtIndex the index of the generic type to extract
     * @return the n-th generic type, or {@link org.codehaus.groovy.ast.ClassHelper#OBJECT_TYPE} if it doesn't exist.
     */
    public static ClassNode pickGenericType(ClassNode type, int gtIndex) {
        final GenericsType[] genericsTypes = type.getGenericsTypes();
        if (genericsTypes==null || genericsTypes.length<gtIndex) {
            return ClassHelper.OBJECT_TYPE;
        }
        return genericsTypes[gtIndex].getType();
    }

    /**
     * A helper method which will extract the n-th generic type from the n-th parameter of a method node.
     * @param node the method node from which the generic type should be picked
     * @param parameterIndex the index of the parameter in the method parameter list
     * @param gtIndex the index of the generic type to extract
     * @return the generic type, or {@link org.codehaus.groovy.ast.ClassHelper#OBJECT_TYPE} if it doesn't exist.
     */
    public static ClassNode pickGenericType(MethodNode node, int parameterIndex, int gtIndex) {
        final Parameter[] parameters = node.getParameters();
        final ClassNode type = parameters[parameterIndex].getOriginType();
        return pickGenericType(type, gtIndex);
    }

    /**
     * <p>Subclasses should implement this method, which returns the list of accepted closure signatures.</p>
     *
     * <p>The compiler will call this method each time, in a source file, a method call using a closure
     * literal is encountered and that the target method has the corresponding {@link groovy.lang.Closure} parameter
     * annotated with {@link groovy.transform.stc.ClosureParams}. So imagine the following code needs to be compiled:</p>
     *
     * <code>@groovy.transform.TypeChecked
     * void doSomething() {
     *     println ['a','b'].collect { it.toUpperCase() }
     * }</code>
     *
     * <p>The <i>collect</i> method accepts a closure, but normally, the type checker doesn't have enough type information
     * in the sole {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#collect(java.util.Collection, groovy.lang.Closure)} method
     * signature to infer the type of <i>it</i>. With the annotation, it will now try to find an annotation on the closure parameter.
     * If it finds it, then an instance of the hint class is created and the type checker calls it with the following arguments:</p>
     * <ul>
     *     <li>the method node corresponding to the target method (here, the {@link org.codehaus.groovy.runtime.DefaultGroovyMethods#collect(java.util.Collection, groovy.lang.Closure)} method</li>
     *     <li>the (optional) list of options found in the annotation</li>
     * </ul>
     *
     * <p>Now, the hint instance can return the list of expected parameters. Here, it would have to say that the collect method accepts
     * a closure for which the only argument is of the type of the first generic type of the first argument.</p>
     * <p>With that type information, the type checker can now infer that the type of <i>it</i> is <i>String</i>, because the first argument (here the receiver of the collect method)
     * is a <i>List&lt;String&gt;</i></p>
     *
     * <p></p>
     *
     * <p>Subclasses are therefore expected to return the signatures according to the available context, which is only the target method and the potential options.</p>
     *
     * @param node the method node for which a {@link groovy.lang.Closure} parameter was annotated with
     *             {@link groovy.transform.stc.ClosureParams}
     * @param options the options, corresponding to the {@link groovy.transform.stc.ClosureParams#options()} found on the annotation
     * @return a non-null list of signature, where a signature corresponds to an array of class nodes, each of them matching a parameter.
     */
    public abstract List<ClassNode[]> getClosureSignatures(MethodNode node, final String[] options);
}

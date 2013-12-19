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
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p>A special hint which handles a common use case in the Groovy methods that work on maps. In case of an
 * iteration on a list of map entries, you often want the user to be able to work either on a {@link Map.Entry map entry}
 * or on a key,value pair.</p>
 * <p>The result is a closure which can have the following forms:</p>
 * <ul>
 *     <li><code>{ key, value -> ...}</code> where key is the key of a map entry, and value the corresponding value</li>
 *     <li><code>{ entry -> ... }</code> where entry is a {@link java.util.Map.Entry map entry}</li>
 *     <li><code>{ ...}</code> where <i>it</i> is an implicit {@link java.util.Map.Entry map entry}</li>
 * </ul>
 * <p>This hint handles all those cases by picking the generics from the first argument of the method (by default).</p>
 * <p>If options is not empty, then you can specify another index for the parameter:</p>
 * <code>void doSomething(String str, Map&lt;K,&gt;V map, @ClosureParams(value=MapEntryOrKeyValue.class,options="1") Closure c) { ... }</code>
 */
public class MapEntryOrKeyValue extends ClosureSignatureHint {
    private final static ClassNode MAPENTRY_TYPE = ClassHelper.make(Map.Entry.class);

    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final String[] options) {
        int index = 0;
        if (options!=null && options.length>0) {
            index = Integer.valueOf(options[0]);
        }
        GenericsType[] genericsTypes = node.getParameters()[index].getOriginType().getGenericsTypes();
        if (genericsTypes==null) {
            // would happen if you have a raw Map type for example
            genericsTypes = new GenericsType[] {
                new GenericsType(ClassHelper.OBJECT_TYPE),
                new GenericsType(ClassHelper.OBJECT_TYPE)
            };
        }
        ClassNode[] firstSig = {genericsTypes[0].getType(), genericsTypes[1].getType()};
        ClassNode mapEntry = MAPENTRY_TYPE.getPlainNodeReference();
        mapEntry.setGenericsTypes(genericsTypes);
        ClassNode[] secondSig = {mapEntry};
        return Arrays.asList(firstSig, secondSig);
    }
}

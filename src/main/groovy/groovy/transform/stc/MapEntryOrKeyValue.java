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
package groovy.transform.stc;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>A special hint which handles a common use case in the Groovy methods that work on maps. In case of an
 * iteration on a list of map entries, you often want the user to be able to work either on a {@link java.util.Map.Entry} map entry
 * or on a key,value pair.</p>
 * <p>The result is a closure which can have the following forms:</p>
 * <ul>
 *     <li><code>{ key, value {@code ->} ... }</code> where key is the key of a map entry, and value the corresponding value</li>
 *     <li><code>{ entry {@code ->} ... }</code> where entry is a {@link java.util.Map.Entry} map entry</li>
 *     <li><code>{ ... }</code> where <i>it</i> is an implicit {@link java.util.Map.Entry} map entry</li>
 * </ul>
 * <p>This hint handles all those cases by picking the generics from the first argument of the method (by default).</p>
 * <p>The options array is used to modify the behavior of this hint. Each string in the option array consists of
 * a key=value pair.</p>
 * <ul>
 *     <li><i>argNum=index</i> of the parameter representing the map (by default, 0)</li>
 *     <li><i>index=true or false</i>, by default false. If true, then an additional "int" parameter is added,
 *     for "withIndex" variants</li>
 * </ul>
 * <code>void doSomething(String str, Map&lt;K,&gt;V map, @ClosureParams(value=MapEntryOrKeyValue.class,options="argNum=1") Closure c) { ... }</code>
 */
public class MapEntryOrKeyValue extends ClosureSignatureHint {
    private static final ClassNode MAPENTRY_TYPE = ClassHelper.make(Map.Entry.class);

    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final String[] options, final ASTNode usage) {
        Options opt;
        try {
            opt = Options.parse(node, usage, options);
        } catch (IncorrectTypeHintException e) {
            sourceUnit.addError(e);
            return Collections.emptyList();
        }
        GenericsType[] genericsTypes = node.getParameters()[opt.parameterIndex].getOriginType().getGenericsTypes();
        if (genericsTypes==null) {
            // would happen if you have a raw Map type for example
            genericsTypes = new GenericsType[] {
                new GenericsType(ClassHelper.OBJECT_TYPE),
                new GenericsType(ClassHelper.OBJECT_TYPE)
            };
        }
        ClassNode[] firstSig;
        ClassNode[] secondSig;
        ClassNode mapEntry = MAPENTRY_TYPE.getPlainNodeReference();
        mapEntry.setGenericsTypes(genericsTypes);
        if (opt.generateIndex) {
            firstSig = new ClassNode[] {genericsTypes[0].getType(), genericsTypes[1].getType(), ClassHelper.int_TYPE};
            secondSig = new ClassNode[] {mapEntry, ClassHelper.int_TYPE};

        } else {
            firstSig = new ClassNode[] {genericsTypes[0].getType(), genericsTypes[1].getType()};
            secondSig = new ClassNode[] {mapEntry};
        }
        return Arrays.asList(firstSig, secondSig);
    }
    
    private static final class Options {
        final int parameterIndex;
        final boolean generateIndex;

        private Options(final int parameterIndex, final boolean generateIndex) {
            this.parameterIndex = parameterIndex;
            this.generateIndex = generateIndex;
        }
        
        static Options parse(MethodNode mn, ASTNode source, String[] options) throws IncorrectTypeHintException {
            int pIndex = 0;
            boolean generateIndex = false;
            for (String option : options) {
                String[] keyValue = option.split("=");
                if (keyValue.length==2) {
                    String key = keyValue[0];
                    String value = keyValue[1];
                    if ("argNum".equals(key)) {
                        pIndex = Integer.parseInt(value);
                    } else if ("index".equals(key)) {
                        generateIndex = Boolean.valueOf(value);
                    } else {
                        throw new IncorrectTypeHintException(mn, "Unrecognized option: "+key, source.getLineNumber(), source.getColumnNumber());
                    }
                } else {
                    throw new IncorrectTypeHintException(mn, "Incorrect option format. Should be argNum=<num> or index=<boolean> ", source.getLineNumber(), source.getColumnNumber());
                }
            }
            return new Options(pIndex, generateIndex);
        }
    } 
}

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

import antlr.RecognitionException;
import antlr.TokenStreamException;
import org.codehaus.groovy.antlr.AntlrParserPlugin;
import org.codehaus.groovy.antlr.parser.GroovyLexer;
import org.codehaus.groovy.antlr.parser.GroovyRecognizer;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.syntax.Reduction;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;


/**
 * <p>A closure parameter hint class that is convenient if you want to use a String representation
 * of the signature. It makes use of the {@link ClosureParams#options() option strings}, where
 * each string corresponds to a single signature.</p>
 *
 * <p>The following example describes a closure as accepting a single signature (List&lt;T&gt; list -&gt;):</p>
 *
 * <code>public &lt;T&gt; T apply(T src, @ClosureParams(value=FromString.class, options="List&lt;T&gt;" Closure&lt;T&gt; cl)</code>
 *
 * <p>The next example describes a closure as accepting two signatures (List&lt;T&gt; list -&gt;) and (T t -&gt;):</p>
 *
 * <code>public &lt;T&gt; T apply(T src, @ClosureParams(value=FromString.class, options={"List&lt;T&gt;","T"} Closure&lt;T&gt; cl)</code>
 *
 * <p>It is advisable not to use this hint as a replacement for the various {@link FirstParam}, {@link SimpleType},
 * ... hints because it is actually much slower. Using this hint should therefore be limited
 * to cases where it's not possible to express the signature using the existing hints.</p>
 *
 * @author Cédric Champeau
 * @since 2.3.0
 */
public class FromString extends ClosureSignatureHint {

    @Override
    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final String[] options, final ASTNode usage) {
        List<ClassNode[]> list = new ArrayList<ClassNode[]>(options.length);
        for (String option : options) {
            list.add(parseOption(option, sourceUnit, compilationUnit, node, usage));
        }
        return list;
    }

    /**
     * Parses a string representing a type, that must be aligned with the current context.
     * For example, <i>"List&lt;T&gt;"</i> must be converted into the appropriate ClassNode
     * for which <i>T</i> matches the appropriate placeholder.
     *
     *
     * @param option a string representing a type
     * @param sourceUnit the source unit (of the file being compiled)
     * @param compilationUnit the compilation unit (of the file being compiled)
     * @param mn the method node
     * @param usage
     * @return a class node if it could be parsed and resolved, null otherwise
     */
    private ClassNode[] parseOption(final String option, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final MethodNode mn, final ASTNode usage) {
        GroovyLexer lexer = new GroovyLexer(new StringReader("DummyNode<"+option+">"));
        final GroovyRecognizer rn = GroovyRecognizer.make(lexer);
        try {
            rn.classOrInterfaceType(true);
            final AtomicReference<ClassNode> ref = new AtomicReference<ClassNode>();
            AntlrParserPlugin plugin = new AntlrParserPlugin() {
                @Override
                public ModuleNode buildAST(final SourceUnit sourceUnit, final ClassLoader classLoader, final Reduction cst) throws ParserException {
                    ref.set(makeTypeWithArguments(rn.getAST()));
                    return null;
                }
            };
            plugin.buildAST(null, null, null);
            ClassNode parsedNode = ref.get();
            // the returned node is DummyNode<Param1, Param2, Param3, ...)
            GenericsType[] parsedNodeGenericsTypes = parsedNode.getGenericsTypes();
            if (parsedNodeGenericsTypes==null) {
                return null;
            }
            ClassNode[] signature = new ClassNode[parsedNodeGenericsTypes.length];
            for (int i = 0; i < parsedNodeGenericsTypes.length; i++) {
                final GenericsType genericsType = parsedNodeGenericsTypes[i];
                signature[i] = resolveClassNode(sourceUnit, compilationUnit, mn, usage, genericsType.getType());
            }
            return signature;
        } catch (RecognitionException e) {
            sourceUnit.addError(new IncorrectTypeHintException(mn, e, usage.getLineNumber(), usage.getColumnNumber()));
        } catch (TokenStreamException e) {
            sourceUnit.addError(new IncorrectTypeHintException(mn, e, usage.getLineNumber(), usage.getColumnNumber()));
        } catch (ParserException e) {
            sourceUnit.addError(new IncorrectTypeHintException(mn, e, usage.getLineNumber(), usage.getColumnNumber()));
        }
        return null;
    }

    private ClassNode resolveClassNode(final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final MethodNode mn, final ASTNode usage, final ClassNode parsedNode) {
        ClassNode dummyClass = new ClassNode("dummy",0, ClassHelper.OBJECT_TYPE);
        dummyClass.setModule(new ModuleNode(sourceUnit));
        dummyClass.setGenericsTypes(mn.getDeclaringClass().getGenericsTypes());
        MethodNode dummyMN = new MethodNode(
                "dummy",
                0,
                parsedNode,
                Parameter.EMPTY_ARRAY,
                ClassNode.EMPTY_ARRAY,
                EmptyStatement.INSTANCE
        );
        dummyMN.setGenericsTypes(mn.getGenericsTypes());
        dummyClass.addMethod(dummyMN);
        ResolveVisitor visitor = new ResolveVisitor(compilationUnit) {
            @Override
            protected void addError(final String msg, final ASTNode expr) {
                sourceUnit.addError(new IncorrectTypeHintException(mn, msg, usage.getLineNumber(), usage.getColumnNumber()));
            }
        };
        visitor.startResolving(dummyClass, sourceUnit);
        return dummyMN.getReturnType();
    }

}

/**
 *
 * Copyright 2004 James Strachan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.control;

import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.TokenStream;
import org.codehaus.groovy.syntax.lexer.GroovyLexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.lexer.ReaderCharStream;
import org.codehaus.groovy.syntax.parser.Parser;
import org.codehaus.groovy.syntax.parser.ASTBuilder;
import org.codehaus.groovy.syntax.parser.ParserException;
import org.codehaus.groovy.ast.ModuleNode;

import java.io.Reader;

/**
 * @version $Revision$
 */
public class ClassicParserPlugin implements ParserPlugin {


    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        //
        // Create a lexer and token stream

        GroovyLexer lexer = new GroovyLexer(new ReaderCharStream(reader));
        TokenStream stream = new LexerTokenStream(lexer);

        //
        // Do the parsing
        Parser parser = new Parser(sourceUnit, stream);
        return parser.parse();
    }

    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {
        ASTBuilder builder = new ASTBuilder( sourceUnit, classLoader );
        return builder.build( cst );
    }
}

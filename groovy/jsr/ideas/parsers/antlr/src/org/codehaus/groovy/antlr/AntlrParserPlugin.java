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
package org.codehaus.groovy.antlr;

import antlr.RecognitionException;
import antlr.TokenStreamException;
import antlr.collections.AST;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.ParserPlugin;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.parser.ParserException;

import java.io.Reader;

/**
 * @version $Revision$
 */
public class AntlrParserPlugin extends ParserPlugin {
    private GroovyRecognizer parser;

    public Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException {
        parser = GroovyRecognizer.make(reader);
        parser.setFilename(sourceUnit.getName());

        return new Reduction(null);
    }

    public ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException {

        // start parsing at the compilationUnit rule
        try {
            parser.compilationUnit();
        }
        catch (RecognitionException e) {
            // TODO
            throw new ParserException(e.getMessage(), new Token(-1, e.getFilename(), e.getLine(), e.getColumn()));
        }
        catch (TokenStreamException e) {
            throw new ParserException(e.getMessage(), Token.EOF);
        }

        ModuleNode module = new ModuleNode(sourceUnit);
        AST ast = parser.getAST();
        convertGroovy(ast, module);
        return module;
    }

    /**
     * Converts the Antlr AST to the Groovy AST
     */
    protected void convertGroovy(AST ast, ModuleNode module) {
        /** TODO */
        
    }
}

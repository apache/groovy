/*
 * Copyright 2003-2007 the original author or authors.
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
package org.codehaus.groovy.control;

import org.codehaus.groovy.syntax.Reduction;
import org.codehaus.groovy.syntax.ParserException;
import org.codehaus.groovy.ast.ModuleNode;

import java.io.Reader;

/**
 * A simple extension point to allow us to switch between the classic Groovy parser and the new Antlr based parser
 * 
 * @version $Revision$
 */
public interface ParserPlugin {

    Reduction parseCST(SourceUnit sourceUnit, Reader reader) throws CompilationFailedException;

    ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) throws ParserException;
}

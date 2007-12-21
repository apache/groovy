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
package groovy.lang;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;

/**
 * An exception thrown by the interpreter
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GroovyRuntimeException extends RuntimeException {

    private ModuleNode module;
    private ASTNode node;

    public GroovyRuntimeException() {
    }

    public GroovyRuntimeException(String message) {
        super(message);
    }

    public GroovyRuntimeException(String message, ASTNode node) {
        super(message);
        this.node = node;
    }

    public GroovyRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public GroovyRuntimeException(Throwable t) {
        super();
        initCause(t);
    }

    public void setModule(ModuleNode module) {
        this.module = module;
    }

    public ModuleNode getModule() {
        return module;
    }

    public String getMessage() {
        return getMessageWithoutLocationText() + getLocationText();
    }

    public ASTNode getNode() {
        return node;
    }

    public String getMessageWithoutLocationText() {
        return super.getMessage();
    }

    protected String getLocationText() {
        String answer = ". ";
        if (node != null) {
            answer += "At [" + node.getLineNumber() + ":" + node.getColumnNumber() + "] ";
        }
        if (module != null) {
            answer += module.getDescription();
        }
        if (answer.equals(". ")) {
            return "";
        }
        return answer;
    }
}

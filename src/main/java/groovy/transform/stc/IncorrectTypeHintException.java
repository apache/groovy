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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.syntax.SyntaxException;

public class IncorrectTypeHintException extends SyntaxException {
    private static final long serialVersionUID = 4481159236968540419L;

    public IncorrectTypeHintException(final MethodNode mn, final Throwable e, int line, int column) {
        super("Incorrect type hint in @ClosureParams in class "+mn.getDeclaringClass().getName()+" method "+mn.getTypeDescriptor()+" : "+e.getMessage(), e, line, column);
    }

    public IncorrectTypeHintException(final MethodNode mn, final String msg, final int line, final int column) {
        super("Incorrect type hint in @ClosureParams in class "+mn.getDeclaringClass().getName()+" method "+mn.getTypeDescriptor()+" : "+msg, line, column);
    }
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 * IBM - Initial API and implementation
 ******************************************************************************/


package org.codehaus.groovy.classgen;

import java.util.List;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.syntax.parser.RuntimeParserException;


/**
 * RuntimeIncompleteClassException
 * 
 */
public class RuntimeIncompleteClassException extends RuntimeParserException {

    /**
     * @param a_message
     * @param a_node
     */
    public RuntimeIncompleteClassException(List a_classnames, ASTNode a_node) {
        super("Incomplete class: does not implement abstract methods: " + a_classnames, a_node);
    }

}

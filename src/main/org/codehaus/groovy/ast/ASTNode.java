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
package org.codehaus.groovy.ast;

import java.util.Map;


/**
 * Base class for any AST node. This class supports basic information used in all
 * nodes of the AST<ul>
 * <li> line and column number information. Usually a node represents a certain
 * area in a text file determined by a starting position and an ending position.
 * For nodes that do not represent this, this information will be -1. A node can
 * also be configured in its line/col information using another node through 
 * setSourcePosition(otherNode).</li>
 * <li> every node can store meta data. A phase operation or transform can use 
 * this to transport arbitrary information to another phase operation or 
 * transform. The only requirement is that the other phase operation or transform
 * runs after the part storing the information. To save memory this map is null
 * by default. If the information transport is done and the map does not store
 * any information anymore, it is strongly recommended to set a null map again.</li> 
 * </ul>
 * <li> a text representation of this node trough getText(). This was in the 
 * past used for assertion messages. Since the usage of power asserts this 
 * method will not be called for this purpose anymore and might be removed in
 * future versions of Groovy</li>
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @author <a href="maito:blackdrag@gmx.org>Jochen "blackdrag" Theodorou</a>
 * @version $Revision$
 */
public class ASTNode {

    private int lineNumber = -1;
    private int columnNumber = -1;
    private int lastLineNumber = -1;
    private int lastColumnNumber = -1;
    private Map nodeMetaData = null; 

    public void visit(GroovyCodeVisitor visitor) {
        throw new RuntimeException("No visit() method implemented for class: " + getClass().getName());
    }

    public String getText() {
        return "<not implemented yet for class: " + getClass().getName() + ">";
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public int getLastLineNumber() {
        return lastLineNumber;
    }

    public void setLastLineNumber(int lastLineNumber) {
        this.lastLineNumber = lastLineNumber;
    }

    public int getLastColumnNumber() {
        return lastColumnNumber;
    }

    public void setLastColumnNumber(int lastColumnNumber) {
        this.lastColumnNumber = lastColumnNumber;
    }
    
    /**
     * Sets the source position using another ASTNode.
     * The sourcePosition consists of a line/column pair for
     * the start and a line/column pair for the end of the
     * expression or statement 
     * 
     */
    public void setSourcePosition(ASTNode node) {
        this.columnNumber = node.getColumnNumber();
        this.lastLineNumber = node.getLastLineNumber();
        this.lastColumnNumber = node.getLastColumnNumber();
        this.lineNumber = node.getLineNumber();
    }
    
    /**
     * Gets the node meta data. By default this map will be null.
     * We do this to save memory for the AST.
     * @return the node meta data map
     */
    public Map getNodeMetaData() {
        return nodeMetaData;
    }
    
    /**
     * Sets the node meta data. Note: if all the meta is deleted from
     * the map, this method should be called with null.
     * @param m - the node meta data map
     */
    public void setNodeMeataData(Map m) {
        this.nodeMetaData = m;
    }
}

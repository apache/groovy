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
package org.codehaus.groovy.antlr;

import antlr.Token;

/**
 * This is a Token sub class to track line information 
 */
public class GroovySourceToken extends Token implements SourceInfo{
    protected int line;
    protected String text = "";
    protected int col;
    protected int lineLast;
    protected int colLast;

    
    /**
     * Constructor using a token type
     * 
     * @param t the type
     */
    public GroovySourceToken(int t) {
        super(t);
    }
    
    public int getLine() {
        return line;
    }

    /**
     * get the source token text
     * @return the source token text
     */
    public String getText() {
        return text;
    }

    public void setLine(int l) {
        line = l;
    }

    /**
     * set the source token text
     * @param s the text
     */
    public void setText(String s) {
        text = s;
    }

    public String toString() {
        return 
            "[\"" + getText() + "\",<" + type + ">,"+
            "line=" + line + ",col=" + col + 
            ",lineLast=" + lineLast + ",colLast=" + colLast +
            "]";
    }

    public int getColumn() {
        return col;
    }

    public void setColumn(int c) {
        col = c;
    }
    
    public int getLineLast() {
        return lineLast;
    }

    public void setLineLast(int lineLast) {
        this.lineLast = lineLast;
    }

    public int getColumnLast() {
        return colLast;
    }

    public void setColumnLast(int colLast) {
        this.colLast = colLast;
    }
}

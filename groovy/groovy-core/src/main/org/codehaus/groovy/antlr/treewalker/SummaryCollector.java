/*
 $Id$

 Copyright 2005 (C) Jeremy Rayner. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.antlr.treewalker;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.AntlrSourceSummary;
import org.codehaus.groovy.antlr.syntax.AntlrClassSource;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.syntax.ClassSource;
import org.codehaus.groovy.syntax.SourceSummary;

import java.util.Stack;

/**
 * A visitor for the antlr ast that creates a summary of the parsed source unit
 *
 * @author Jeremy Rayner
 */
public class SummaryCollector extends VisitorAdapter {
    private SourceSummary sourceSummary;
    private Stack stack;

    public SourceSummary getSourceSummary() {
        return sourceSummary;
    }

    public SummaryCollector() {
        sourceSummary = new AntlrSourceSummary();
        stack = new Stack();
    }

    public void setUp() {
    }

    public void visitClassDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            ClassSource classSource = new AntlrClassSource(
                    t.childOfType(GroovyTokenTypes.IDENT).getText(),t
            );
            // Here we assume that the optional restrictions (JLS$7.6)
            // on compilation units in file-based implementations apply
            // http://java.sun.com/docs/books/jls/second_edition/html/packages.doc.html#26783
            GroovySourceAST modifiers = t.childOfType(GroovyTokenTypes.MODIFIERS);

            if (isPublic(modifiers)) {
                sourceSummary.addPublic(classSource);
            }
        }
    }

    /* figure out if this modifier node represents a public entity */
    private boolean isPublic(GroovySourceAST modifiers) {
        boolean isPublic = true;
        if (modifiers != null) {
            GroovySourceAST modprot = modifiers.childOfType(GroovyTokenTypes.LITERAL_protected);
            GroovySourceAST modpriv = modifiers.childOfType(GroovyTokenTypes.LITERAL_private);
            if (modpriv != null || modprot != null) {
                isPublic = false;
            }
        }
        return isPublic;
    }

    public void tearDown() {
    }

    public void push(GroovySourceAST t) {
        stack.push(t);
    }
    public GroovySourceAST pop() {
        if (!stack.empty()) {
            return (GroovySourceAST) stack.pop();
        }
        return null;
    }

}

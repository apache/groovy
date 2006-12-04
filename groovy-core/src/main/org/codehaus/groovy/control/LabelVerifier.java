/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

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
package org.codehaus.groovy.control;

import java.util.Iterator;
import java.util.LinkedList;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;

/**
 * This class checks the handling of labels in the AST
 * 
 * @author Jochen Theodorou
 */
public class LabelVerifier extends ClassCodeVisitorSupport {

    private SourceUnit source;
    private LinkedList visitedLabels;
    private LinkedList continueLabels;
    private LinkedList breakLabels;
    boolean inLoop=false;
    boolean inSwitch=false;
    
    public LabelVerifier(SourceUnit src) {
        source = src;
    }
    
    protected SourceUnit getSourceUnit() {
        return source;
    }
    
    private void init(){
        visitedLabels = new LinkedList();
        continueLabels = new LinkedList();
        breakLabels = new LinkedList();
        inLoop=false;
        inSwitch=false;
    }
    
    protected void visitClassCodeContainer(Statement code) {
        init();
        super.visitClassCodeContainer(code);
        assertNoLabelsMissed();
    }
    
   public void visitStatement(Statement statement) {
       String label = statement.getStatementLabel();
       
       if (label!=null) {
           for (Iterator iter = breakLabels.iterator(); iter.hasNext();) {
               BreakStatement element = (BreakStatement) iter.next();
               if (element.getLabel().equals(label)) iter.remove();
           }
           
           for (Iterator iter = continueLabels.iterator(); iter.hasNext();) {
               ContinueStatement element = (ContinueStatement) iter.next();
               if (element.getLabel().equals(label)) iter.remove();
           }
           
           visitedLabels.add(label);
       }
       
       super.visitStatement(statement);
}
    
    public void visitForLoop(ForStatement forLoop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitForLoop(forLoop);
        inLoop = oldInLoop;
    }
    
    public void visitDoWhileLoop(DoWhileStatement loop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitDoWhileLoop(loop);
        inLoop = oldInLoop;
    }     
    
    public void visitWhileLoop(WhileStatement loop) {
        boolean oldInLoop = inLoop;
        inLoop = true;
        super.visitWhileLoop(loop);
        inLoop = oldInLoop;
    }
    
    public void visitBreakStatement(BreakStatement statement) {
        String label = statement.getLabel();
        boolean hasNamedLabel = label!=null;
        if (!hasNamedLabel && !inLoop && !inSwitch) {
            addError("the break statement is only allowed inside loops or switches",statement);
        } else if (hasNamedLabel && !inLoop) {
            addError("the break statement with named label is only allowed inside loops",statement);
        }
        if (label!=null) {
            boolean found=false;
            for (Iterator iter = visitedLabels.iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                if (element.equals(label)) {
                    found = true;
                    break;
                }
            }
            if (!found) breakLabels.add(statement);
        }
        
        super.visitBreakStatement(statement);
    }
    
    public void visitContinueStatement(ContinueStatement statement) {
        String label = statement.getLabel();
        boolean hasNamedLabel = label!=null;
        if (!hasNamedLabel && !inLoop) {
            addError("the continue statement is only allowed inside loops",statement);
        } 
        if (label!=null) {
            boolean found=false;
            for (Iterator iter = visitedLabels.iterator(); iter.hasNext();) {
                String element = (String) iter.next();
                if (element.equals(label)) {
                    found = true;
                    break;
                }
            }
            if (!found) continueLabels.add(statement);
        }
        
        super.visitContinueStatement(statement);
    }
    
    protected void assertNoLabelsMissed() {
        //TODO: report multiple missing labels of the same name only once
        for (Iterator iter = continueLabels.iterator(); iter.hasNext();) {
            ContinueStatement element = (ContinueStatement) iter.next();
            addError("continue to missing label",element);
        }
        for (Iterator iter = breakLabels.iterator(); iter.hasNext();) {
            BreakStatement element = (BreakStatement) iter.next();
            addError("break to missing label",element);
        }
    }
    
    public void visitSwitch(SwitchStatement statement) {
        inSwitch=true;
        super.visitSwitch(statement);
    }

}

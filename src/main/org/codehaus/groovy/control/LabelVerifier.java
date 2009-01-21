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
        boolean oldInSwitch = inSwitch;
        inSwitch = true;
        super.visitSwitch(statement);
        inSwitch = oldInSwitch;
    }

}

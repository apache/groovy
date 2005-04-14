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
package org.codehaus.groovy.ant;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceCodeVisitor;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.tree.analysis.*;


/**
 * Compiles Groovy source files. This task can take the following
 * arguments:
 * <ul>
 * <li>sourcedir
 * <li>destdir
 * <li>classpath
 * </ul>
 * Of these arguments, the <b>sourcedir</b> and <b>destdir</b> are required.
 * <p>
 * When this task executes, it will recursively scan the sourcedir and
 * destdir looking for Groovy source files to compile. This task makes its
 * compile decision based on timestamp.
 * 
 * Based heavily on the Javac implementation in Ant
 *
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$ 
 */
public class VerifyClass extends MatchingTask {
    private String topDir=null;
    private boolean verbose = false;
    
    public VerifyClass() {}

    public void execute() throws BuildException {
        if (topDir==null) throw new BuildException("no dir attribute is set");
        File top = new File(topDir);
        if (!top.exists()) throw new BuildException("the directory "+top+" does not exist");
        log ("top dir is "+top);
        int fails = execute(top);
        if (fails==0) {
            log ("no bytecode problems found");
        } else {
            log ("found "+fails+" failing classes");
        }
    }
    
    public void setDir(String dir) throws BuildException {
        topDir = dir;
    }
    
    public void setVerbose(boolean v) {
        verbose = v;
    }
    
    private int execute(File dir) {
        int fails = 0;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f =files[i];
            if (f.isDirectory()) {
                fails += execute(f);
            } else if (f.getName().endsWith(".class")) {
                try {
                    boolean ok = readClass(f.getCanonicalPath());
                    if (!ok) fails++;
                } catch (IOException ioe) {
                    log(ioe.getMessage());
                    throw new BuildException(ioe);
                } 
            }
        }
        return fails;
    }
    
    private boolean readClass(String clazz) throws IOException {
        ClassReader cr = new ClassReader(new FileInputStream(clazz));
        TreeClassAdapter ca = new TreeClassAdapter(null);
        cr.accept(new CheckClassAdapter(ca), true);
        boolean failed=false;
        
        List methods = ca.classNode.methods;
        for (int i = 0; i < methods.size(); ++i) {
          MethodNode method = (MethodNode)methods.get(i);
          if (method.instructions.size() > 0) {
            Analyzer a = new Analyzer(new SimpleVerifier());
            try {
              a.analyze(ca.classNode, method);
              continue;
            } catch (Exception e) {
              e.printStackTrace();
            }
            final Frame[] frames = a.getFrames();

            if (!failed) {
                failed=true;
                log("verifying of class "+clazz+" failed");
            }
            if (verbose) log(method.name + method.desc);
            TraceCodeVisitor cv = new TraceCodeVisitor(null) {
              public void visitMaxs (int maxStack, int maxLocals) {
                StringBuffer buffer = new StringBuffer();
                for (int i = 0; i < text.size(); ++i) {
                  String s = frames[i] == null ? "null" : frames[i].toString();
                  while (s.length() < maxStack+maxLocals+1) {
                    s += " ";
                  }
                  buffer.append(Integer.toString(i + 100000).substring(1));
                  buffer.append(" ");
                  buffer.append(s);
                  buffer.append(" : ");
                  buffer.append(text.get(i));
                }
                if (verbose) log(buffer.toString());
              }
            };
            for (int j = 0; j < method.instructions.size(); ++j) {
              Object insn = method.instructions.get(j);
              if (insn instanceof AbstractInsnNode) {
                ((AbstractInsnNode)insn).accept(cv);
              } else {
                cv.visitLabel((Label)insn);
              }
            }
            cv.visitMaxs(method.maxStack, method.maxLocals);
          }
        }
        return !failed;
    }
    
}

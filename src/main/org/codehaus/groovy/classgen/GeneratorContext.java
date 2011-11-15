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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CompileUnit;
import org.codehaus.groovy.ast.MethodNode;


/**
 * A context shared across generations of a class and its inner classes
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class GeneratorContext {

    private int innerClassIdx = 1;
    private int closureClassIdx = 1;
    private CompileUnit compileUnit;
    
    public GeneratorContext(CompileUnit compileUnit) {
        this.compileUnit = compileUnit;
    }

    public GeneratorContext(CompileUnit compileUnit, int innerClassOffset) {
        this.compileUnit = compileUnit;
        this.innerClassIdx = innerClassOffset;
    }

    public int getNextInnerClassIdx() {
        return innerClassIdx++;
    }

    public CompileUnit getCompileUnit() {
        return compileUnit;
    }

    public String getNextClosureInnerName(ClassNode owner, ClassNode enclosingClass, MethodNode enclosingMethod) {
        String ownerShortName = owner.getNameWithoutPackage();
        String classShortName = enclosingClass.getNameWithoutPackage();
        if (classShortName.equals(ownerShortName)) {
            classShortName = "";
        }
        else {
            classShortName += "_";
        }
        // remove $
        int dp = classShortName.lastIndexOf("$");
        if (dp >= 0) {
            classShortName = classShortName.substring(++dp);
        }
        // remove leading _
        if (classShortName.startsWith("_")) {
            classShortName = classShortName.substring(1);
        }
        String methodName = "";
        if (enclosingMethod != null) {
            methodName = enclosingMethod.getName() + "_";

            if (enclosingClass.isDerivedFrom(ClassHelper.CLOSURE_TYPE)) {
                methodName = "";
            }
            methodName = methodName.replace('<', '_');
            methodName = methodName.replace('>', '_');
            methodName = methodName.replaceAll(" ", "_");
        }
        return "_" + classShortName + methodName + "closure" + closureClassIdx++;
    }
}

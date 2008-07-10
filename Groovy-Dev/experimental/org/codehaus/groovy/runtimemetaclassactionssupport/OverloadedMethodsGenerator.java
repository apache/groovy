/*
 * Copyright 2005 John G. Wilson
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
 *
 */

package org.codehaus.groovy.runtimemetaclassactionssupport;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;


/**
 * @author John Wilson
 *
 */

public class OverloadedMethodsGenerator extends MethodGenerator {
  private final MethodGenerator firstMethod;
  private final List generatorList = new LinkedList();
  private int methodIndex = 0;
  
  public OverloadedMethodsGenerator(final MethodGenerator firstMethod, final MethodGenerator secondMethod) {
    super(null);
    
    this.firstMethod = firstMethod;
    this.generatorList.add(firstMethod);
    this.generatorList.add(secondMethod);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#generateCall(java.lang.Class)
   */
  public String generateCall(final Class claz) {
  final StringBuffer code = new StringBuffer();
  final Iterator iter = this.generatorList.iterator();
  int i = 0;
  
    code.append("switch(chooseMethod(possible").append(getName()).append("Parameters").append(this.methodIndex).append(", args)) {\n");
    while (iter.hasNext()) {
    final MethodGenerator generator = (MethodGenerator)iter.next();
      
      code.append("case ").append(i++).append(": \n");
      code.append(generator.generateCall(claz));
    }
    code.append("default:\n");
    code.append("return groovy.lang.MetaClass.NO_METHOD_FOUND;\n");
    code.append("}\n");

    return code.toString();
  }
  
  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#startCall(java.lang.StringBuffer)
   */
  protected void startCall(final StringBuffer code) {

  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#generateDeclaration(int)
   */
  public String generateDeclaration(final int methodIndex) {
  final StringBuffer code = new StringBuffer();
  final Iterator iter = this.generatorList.iterator();
  int i = 1;
  
    this.methodIndex = methodIndex;
  
    while (iter.hasNext()) {
    final MethodGenerator generator = (MethodGenerator)iter.next();
    
      code.append(generator.generateDeclaration((methodIndex << 16) | i )); // TODO: find a better way of getting unique indexes
    }
    
    code.append("private final Class[][] possible").append(getName()).append("Parameters").append(methodIndex).append(" = new Class[][]{\n");
    final Iterator iter1 = this.generatorList.iterator();

    while (iter1.hasNext()) {
    final MethodGenerator generator = (MethodGenerator)iter1.next();
    final Class[] parameterTypes = generator.getParameterTypes();
    
      code.append("new Class[] {");
      
      for (int j = 0; j != parameterTypes.length; j++) {
      final Class parameterType = parameterTypes[j];
      
        if (parameterType.isPrimitive()) {
          if (parameterType == int.class) {
            code.append("int");
          } else if (parameterType == long.class) {
            code.append("long");
          } else if (parameterType == short.class) {
            code.append("short");
          } else if (parameterType == char.class) {
            code.append("char");
          } else if (parameterType == byte.class) {
            code.append("byte");
          } else if (parameterType == boolean.class) {
            code.append("boolean");
          } else if (parameterType == float.class) {
            code.append("float");
          } else if (parameterType == double.class) {
            code.append("double");
          } else {
            code.append("**** ERROR ***** found primative parameter type: " + parameterType.getName());
          }
        } else {
          getFullName(parameterType, code);
        }
        code.append(".class, ");
      }
      
      code.setLength(code.length() - 2);  // Trim the trailing ", "
      code.append("},\n ");
    }
    
    code.setLength(code.length() - 3);  // Trim the trailing ",\n "
    code.append("\n};\n");
    return code.toString();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#getName()
   */
  public String getName() {
    return firstMethod.getName();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#getNumberOfParameters()
   */
  public int getNumberOfParameters() {
    return firstMethod.getNumberOfParameters();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#getParameterTypes()
   */
  public Class[] getParameterTypes() {
    return firstMethod.getParameterTypes();
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#isOverloaded(org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator)
   */
  public boolean isOverloaded(final MethodGenerator other) {
     return firstMethod.isOverloaded(other);
  }

  /* (non-Javadoc)
   * @see org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator#processOverloaded(java.lang.Class, org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator)
   */
  public MethodGenerator processOverloaded(final Class claz, final MethodGenerator other) {
    if (other instanceof OverloadedMethodsGenerator) {
    final Iterator iter = ((OverloadedMethodsGenerator)other).generatorList.iterator();
    
      while (iter.hasNext()) {
        processOverloaded(claz, (MethodGenerator)iter.next());
      }
    } else {
    final Class[] p1 = other.getParameterTypes();
  
      for (int i = 0; i != this.generatorList.size(); i++) {
      final MethodGenerator generator = (MethodGenerator)this.generatorList.get(i);
      final Class[] p2 = generator.getParameterTypes();
      int j = 0;
      
        while (p1[j] == p2[j] && ++j != p1.length);
        
        if (j == p1.length) {
          // other is a duplicate of this method
          if (other.method.getDeclaringClass() == claz) {
            this.generatorList.add(i, other);
          }
          
          return this;
        }
      }
      
      this.generatorList.add(other);
    }
    
    return this;
  }
}

package org.codehaus.groovy.runtimemetaclassactionssupport;
import java.lang.reflect.Method;

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

/**
 * @author John Wilson
 *
 */

public class ReflectionMethodGenerator extends MethodGenerator {
  private int methodIndex;
  
  /**
   * @param method
   */
  public ReflectionMethodGenerator(Method method) {
    super(method);
  }

  /* (non-Javadoc)
   * @see MethodGenerator#generateCall()
   */
  public String generateCall(final Class claz) {
  final StringBuffer code = new StringBuffer();
    
    code.append("return ").append(this.method.getName() + this.methodIndex).append("Method.invoke(target, args);\n");

    return code.toString();
  }

  public String generateDeclaration(final int methodIndex) {
  final StringBuffer code = new StringBuffer();
    
    this.methodIndex = methodIndex;
    
    code.append("private Method ").append(this.method.getName() + this.methodIndex).append("Method;\n");
    code.append("{\ntry {\n").append(this.method.getName() + this.methodIndex).append("Method = ");
    code.append(this.method.getDeclaringClass().getName()).append(".class.getDeclaredMethod(\"").append(this.method.getName()).append("\", new Class[] {");
    
    final Class[] parameterTypes = this.method.getParameterTypes();
    if (parameterTypes.length != 0){
      for (int i = 0; i != parameterTypes.length; i++) {
      final Class parameterType = parameterTypes[i];
      
        if (parameterType.isPrimitive()) {
          if (parameterType == void.class) {
            code.append("void.class, ");
          } else if (parameterType == int.class) {
            code.append("void.class, ");
          } else if (parameterType == long.class) {
            code.append("void.class, ");
          } else if (parameterType == short.class) {
            code.append("void.class, ");
          } else if (parameterType == char.class) {
            code.append("void.class, ");
          } else if (parameterType == byte.class) {
            code.append("void.class, ");
          } else if (parameterType == boolean.class) {
            code.append("void.class, ");
          } else if (parameterType == float.class) {
            code.append("void.class, ");
          } else if (parameterType == double.class) {
            code.append("void.class, ");
          } else {
            code.append("**** ERROR ***** found primative return type: " + parameterType.getName());
          }
        } else {
          getFullName(parameterType, code);
          code.append(".class, ");
        }
      }
      
      code.setLength(code.length() - 2);   // trim the last ", " off
    }
    
    code.append("});\n");
    code.append("AccessController.doPrivileged(new PrivilegedAction() {\npublic Object run() {\n");
    code.append(this.method.getName() + this.methodIndex).append("Method.setAccessible(true);\nreturn null;\n}\n});\n");
    code.append("} catch (final NoSuchMethodException e) {}\n}\n");
    
    return code.toString();
  }
  
  /* (non-Javadoc)
   * @see MethodGenerator#startCall(java.lang.StringBuffer)
   */
  protected void startCall(StringBuffer code) {
  }

}

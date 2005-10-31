package org.codehaus.groovy.runtimemetaclassactionssupport;
import groovy.lang.GString;

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

public abstract class MethodGenerator {
  protected final static String DEFAULT_GROOVY_METHODS = "org.codehaus.groovy.runtime.DefaultGroovyMethods";
  protected final static String DEFAULT_STATIC_GROOVY_METHODS = "org.codehaus.groovy.runtime.DefaultGroovyStaticMethods";
  
  protected final Method method;
    
  /**
   * @param method
   */
  public MethodGenerator(Method method) {
    this.method = method;
  }
  
  public String getName() {
    return this.method.getName();
  }
  
  public int getNumberOfParameters() {
    return this.method.getParameterTypes().length;
  }
  
  public Class[] getParameterTypes() {
    return this.method.getParameterTypes();
  }
  
  public boolean isOverloaded(final MethodGenerator other) {
    return getName().equals(other.getName()) && getNumberOfParameters() == other.getNumberOfParameters();
  }
  
  public MethodGenerator processOverloaded(final Class claz, final MethodGenerator other) {
  final Class[] p1 = getParameterTypes();
  final Class[] p2 = other.getParameterTypes();
  
    for (int i = 0; i != p1.length; i++) {
      if (p1[i] != p2[i]) {
        return new OverloadedMethodsGenerator(this, other); // We have an overloaded method 
      }
    }
    
    // We have a duplicate - return the one which has its declaring class as the current class
    
    if (this.method.getDeclaringClass() == claz) {
      return this;
    } else {
      return other;
    }
  }
   
  public String generateDeclaration(int methodIndex) {
    return "";
  }
  
  public String generateCall(final Class claz) {
  final StringBuffer code = new StringBuffer();
  final Class returnType = method.getReturnType();
    
    if (returnType.isPrimitive()) {
      if (returnType == void.class) {
        makeCall(code).append(";\nreturn null;\n");
      } else if (returnType == int.class) {
        code.append("return new Integer(");
        makeCall(code).append(");\n");  
      } else if (returnType == long.class) {
        code.append("return new Long(");
        makeCall(code).append(");\n");  
      } else if (returnType == short.class) {
        code.append("return new Short(");
        makeCall(code).append(");\n");  
      } else if (returnType == char.class) {
        code.append("return new Char(");
        makeCall(code).append(");\n");  
      } else if (returnType == byte.class) {
        code.append("return new Byte(");
        makeCall(code).append(");\n");  
      } else if (returnType == boolean.class) {
        code.append("return new Boolean(");
        makeCall(code).append(");\n");  
      } else if (returnType == float.class) {
        code.append("return new Float(");
        makeCall(code).append(");\n");  
      } else if (returnType == double.class) {
        code.append("return new Double(");
        makeCall(code).append(");\n");  
      } else {
        code.append("**** ERROR ***** found primitive return type: " + returnType.getName());
      }
    } else {
      code.append("return ");
      makeCall(code).append(";\n");  
    }
    
    return code.toString();
  }
  
  private StringBuffer makeCall(final StringBuffer code) {
    startCall(code);
    addParameters(code, this.method.getParameterTypes()).append(')');
    
    return code;
  }
  
  protected abstract void startCall(StringBuffer code);
  
  protected StringBuffer addParameters(final StringBuffer code, final Class[] parameterTypes) {
    if (parameterTypes.length != 0) {
      for (int i = 0; i != parameterTypes.length; i++) {
      final Class parameterType = parameterTypes[i]; 
        
        if (parameterType.isPrimitive()) {
          if (parameterType == int.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).intValue(), ");
          } else if (parameterType == long.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).longValue(), ");
          } else if (parameterType == short.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).shortValue(), ");
          } else if (parameterType == char.class) {
            code.append("((Character)").append("args[");
            code.append(i).append("]).charValue(), ");
          } else if (parameterType == byte.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).byteValue(), ");
          } else if (parameterType == boolean.class) {
            code.append("((Boolean)").append("args[");
            code.append(i).append("]).booleanValue(), ");
          } else if (parameterType == float.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).floatValue(), ");
          } else if (parameterType == double.class) {
            code.append("((Number)").append("args[");
            code.append(i).append("]).doubleValue(), ");
          } else {
            code.append("**** ERROR ***** found primative parameter type: " + parameterType.getName());
          }
        } else if (parameterType == String.class) {
          code.append("args[").append(i).append("].toString(), ");
        } else if (parameterType == GString.class) {
          code.append("(groovy.lang.GString)((args[").append(i).append("] instanceof groovy.lang.GString) ? ");
          code.append("args[").append(i).append("] : ");
          code.append("new groovy.lang.GString(args[").append(i).append("].toString()){public String[] getStrings() {return (String [])this.getValues();}}), ");
        } else {
          code.append('(');
          getFullName(parameterType, code);
          code.append(")args[").append(i).append("], ");
        }
      }
      
      code.setLength(code.length() - 2);  // trim the last "' " off
    }
    
    return code;
  }
  
  protected StringBuffer getFullName(final Class claz, final StringBuffer code) {
  final Package classPackage = (claz.isArray()) ? claz.getComponentType().getPackage() : claz.getPackage(); 
  
    if (classPackage != null) {
      code.append(classPackage.getName()).append('.') ;
    }

    return code.append(claz.getSimpleName());
  }
}


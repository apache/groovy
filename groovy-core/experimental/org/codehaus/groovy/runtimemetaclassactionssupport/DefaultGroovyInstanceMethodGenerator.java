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

public class DefaultGroovyInstanceMethodGenerator extends MethodGenerator {

  /**
   * @param method
   */
  public DefaultGroovyInstanceMethodGenerator(Method method) {
    super(method);
  }
  
  public int getNumberOfParameters() {
    return this.method.getParameterTypes().length - 1;
  }
  
  /* (non-Javadoc)
   * @see MethodGenerator#getParameterTypes()
   */
  public Class[] getParameterTypes() {
  final Class[] parameterTypes = this.method.getParameterTypes();
  final Class[] result = new Class[parameterTypes.length - 1];
  
    System.arraycopy(parameterTypes, 1, result, 0, result.length);
    return result;
  }

  protected void startCall(final StringBuffer code) {
    code.append(DEFAULT_GROOVY_METHODS).append('.').append(this.method.getName()).append('(');
  }
  
  protected StringBuffer addParameters(final StringBuffer code, final Class[] parameterTypes) {   
  final Class[] newParameterTypes = new Class[parameterTypes.length - 1];
    
    System.arraycopy(parameterTypes, 1, newParameterTypes, 0, newParameterTypes.length);
    
    code.append('(');
    getFullName(parameterTypes[0], code);
    
    if (newParameterTypes.length == 0) {
      code.append(")target");
    } else {
      code.append(")target, ");
    }
    
    return super.addParameters(code, newParameterTypes);
  }
}

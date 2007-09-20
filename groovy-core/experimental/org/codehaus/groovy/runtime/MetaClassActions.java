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

package org.codehaus.groovy.runtime;


/**
 * @author John Wilson
 *
 */

public abstract class MetaClassActions {
  protected int chooseMethod(final Class[][] formalParameterLists, final Object[] actualParameters) {
    return 0; // TODO: implement parameter choosing code
  }
  
  public abstract Object doInvokeMethod(Object target, String name, Object[] args) throws Exception;
}

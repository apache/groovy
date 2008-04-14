import groovy.lang.MetaClass;

import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.runtime.MetaClassActions;
import org.codehaus.groovy.runtime.MetaClassActionsGenerator;

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

public class TestMeta {
  
  /**
   * @param args
   */
  public static void main(String[] args) throws Exception {   
    final MetaClassActions a1 = MetaClassActionsGenerator.getActions(ArrayList.class);
    final MetaClassActions a2 = MetaClassActionsGenerator.getActions(HashMap.class);
    final MetaClass m1 = InvokerHelper.getInstance().getMetaRegistry().getMetaClass(ArrayList.class);
    final MetaClass m2 = InvokerHelper.getInstance().getMetaRegistry().getMetaClass(HashMap.class);
    final ArrayList i1 = new ArrayList();
    final HashMap i2 = new HashMap();
    final Integer zero = new Integer(0);
    
    i1.add(new Integer(0));
    i1.add(new Integer(1));
    i1.add(new Integer(0));
    i1.add(new Integer(3));
    
    System.out.println(m1.invokeMethod(i1, "count", new Object[]{zero}));
    System.out.println(a1.doInvokeMethod(i1, "count", new Object[]{zero}));
    
    final String method = "toArray";
    
    long start = System.currentTimeMillis();
    
    for (int i = 0; i != 10000000; i++) {
      m1.invokeMethod(i1, method, new Object[]{});
    }
    
    System.out.println("Time taken via MetaClass: " + (System.currentTimeMillis() - start));
    
    start = System.currentTimeMillis();
    
    for (int i = 0; i != 10000000; i++) {
      a1.doInvokeMethod(i1, method, new Object[]{});
    }
    
    System.out.println("Time taken via MetaClassActions: " + (System.currentTimeMillis() - start));
    
    start = System.currentTimeMillis();
    
    for (int i = 0; i != 10000000; i++) {
      i1.toArray();
    }
    
    System.out.println("Time taken via direct call: " + (System.currentTimeMillis() - start));
  }
}
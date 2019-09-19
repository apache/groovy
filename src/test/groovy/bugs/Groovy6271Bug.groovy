/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.bugs

import gls.CompilableTestSupport
import groovy.test.NotYetImplemented

class Groovy6271Bug extends CompilableTestSupport {

    /*
org.codehaus.groovy.control.MultipleCompilationErrorsException: startup failed:
TestScripttestTraitWithCompileStaticAndCoercedClosure0.groovy: 17: [Static type checking] - Incompatible generic argument types. Cannot assign fj.data.hlist.HList$HCons <java.math.BigDecimal, fj.data.hlist.HList$HCons> to: fj.data.hlist.HList$HCons <Double, HCons>
 @ line 17, column 19.
                     nil().extend([1, 2] as Integer[]).extend("Bar").extend(4.0);
                     ^

TestScripttestTraitWithCompileStaticAndCoercedClosure0.groovy: 33: [Static type checking] - Cannot call fj.data.hlist.HList$HAppend <fj.data.hlist.HList$HCons, fj.data.hlist.HList$HCons, fj.data.hlist.HList$HCons>#append(fj.data.hlist.HList$HCons <String, HCons>, fj.data.hlist.HList$HCons <Double, HCons>) with arguments [fj.data.hlist.HList$HCons <java.lang.String, fj.data.hlist.HList$HCons>, fj.data.hlist.HList$HCons <java.math.BigDecimal, fj.data.hlist.HList$HCons>]
 @ line 33, column 127.
   ons<Integer[], HNil>>>>>> x = three.appe
                                 ^

2 errors
     */
    @NotYetImplemented
    void testGroovy6271Bug() {
        shouldCompile '''
            @Grab('org.functionaljava:functionaljava:3.1')
            import static fj.data.hlist.HList.HCons;
            import static fj.data.hlist.HList.HNil;
            import static fj.data.hlist.HList.HAppend.append;
            import static fj.data.hlist.HList.HAppend;
            import static fj.data.hlist.HList.nil;
            import groovy.transform.CompileStatic
            
            @CompileStatic
            public class HListExample {
              public static void main(String[] args) {
                final HCons<String, HCons<Integer, HCons<Boolean, HNil>>> a =
                  nil().extend(true).extend(3).extend("Foo");
            
                final HCons<Double, HCons<String, HCons<Integer[], HNil>>> b =
                  nil().extend([1, 2] as Integer[]).extend("Bar").extend(4.0);
            //      nil().extend(new Integer[]{1, 2}).extend("Bar").extend(4.0);
            
                final HAppend<HNil, HCons<Double, HCons<String, HCons<Integer[], HNil>>>,
                  HCons<Double, HCons<String, HCons<Integer[], HNil>>>> zero = append();
            
                final HAppend<HCons<Boolean, HNil>, HCons<Double, HCons<String, HCons<Integer[], HNil>>>,
                  HCons<Boolean, HCons<Double, HCons<String, HCons<Integer[], HNil>>>>> one = append(zero);
            
                final HAppend<HCons<Integer, HCons<Boolean, HNil>>, HCons<Double, HCons<String, HCons<Integer[], HNil>>>,
                  HCons<Integer, HCons<Boolean, HCons<Double, HCons<String, HCons<Integer[], HNil>>>>>> two = append(one);
            
                final HAppend<HCons<String, HCons<Integer, HCons<Boolean, HNil>>>,
                  HCons<Double, HCons<String, HCons<Integer[], HNil>>>,
                    HCons<String, HCons<Integer, HCons<Boolean, HCons<Double, HCons<String, HCons<Integer[], HNil>>>>>>> three = append(two);
            
                final HCons<String, HCons<Integer, HCons<Boolean, HCons<Double, HCons<String, HCons<Integer[], HNil>>>>>> x = three.append(a, b);
            
                System.out.println(x.head()); // Foo
                System.out.println(x.tail().tail().tail().tail().head()); // Bar
              }
            }
        '''
    }
}

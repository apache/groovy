/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.script

class MapFromList {
    void talk(a) {
        println("hello "+a)
    }

    void doit(args) {
        def i = 1
        def l = [:]
        args.each { 
        talk(it)
        l.put(it,i++)
        }
        l.each {
           println(it)
        }
    }

    static void main(args) {
        def a = ['tom','dick','harry']
        def t = new MapFromList()
        t.doit(a)
    }
}

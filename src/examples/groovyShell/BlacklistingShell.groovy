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
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.customizers.SecureASTCustomizer
import org.codehaus.groovy.control.messages.ExceptionMessage
import org.codehaus.groovy.ast.expr.MethodPointerExpression


/**
 * The blacklisting shell is similar to a GroovyShell in that it can evaluate text as
 * code and return a result. It is intended as an example of using blacklisting to prevent
 * running methods on a class - in this case, java.lang.System.  Please note that in creating
 * any secure environment, there is no substitution for using a SecurityManager.
 *
 * Amoung the many different calls this class prevents are:
 *   System.exit(0)
 *   Eval.me("System.exit(0)")
 *   evaluate("System.exit(0)")
 *   (new GroovyShell()).evaluate("System.exit(0)")
 *   Class.forName("java.lang.System").exit(0)
 *   System.&exit.call(0)
 *   System.getMetaClass().invokeMethod("exit",0)
 *   def s = System; s.exit(0)
 *   Script t = this; t.evaluate("System.exit(0)")
 *
 * The restrictions required, however, also prevent the following code from working:
 *   println "test"
 *   def s = "test" ; s.count("t") 
 *
 * @author Jim Driscoll (jamesgdriscoll@gmail.com)
 */
class BlacklistingShell {
    
    /**
     * Compiles the text into a Groovy object and then executes it, returning the result.
     * Prevents calling any method on java.lang.System within the VM
     * @param text
     *       the script to evaluate typed as a string
     * @throws SecurityException
     *       most likely the script is doing something other than arithmetic
     * @throws IllegalStateException
     *       if the script returns something other than a number
     */
    def evaluate(String text) {
        try {
            final SecureASTCustomizer secure = new SecureASTCustomizer()
            secure.with {

                receiversClassesBlackList = [
                    Object,
                    Script,
                    GroovyShell,
                    Eval,
                    System,
                ].asImmutable()
                
                expressionsBlacklist = [MethodPointerExpression].asImmutable()
                
            }
            CompilerConfiguration config = new CompilerConfiguration()
            config.addCompilationCustomizers(secure)
            GroovyClassLoader loader = new GroovyClassLoader(this.class.classLoader, config)
            Class clazz = loader.parseClass(text)
            Script script = (Script) clazz.newInstance();
            Object result = script.run()
            return result
        } catch (SecurityException ex) {
            throw new SecurityException("Could not evaluate script: $text", ex)
        } catch (MultipleCompilationErrorsException mce) {
            //this allows compilation errors to be seen by the user       
            mce.errorCollector.errors.each {
                if (it instanceof ExceptionMessage && it.cause instanceof SecurityException) {
                    throw it.cause
                }
            }
            throw mce
        }
    }            
}
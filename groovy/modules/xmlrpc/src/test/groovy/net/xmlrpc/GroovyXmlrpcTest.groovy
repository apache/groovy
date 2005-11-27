/*
 * Copyright 2004 (C) John Wilson. All Rights Reserved.
 * 
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met: 1. Redistributions of source code must retain
 * copyright statements and notices. Redistributions must also contain a copy
 * of this document. 2. Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the distribution. 3.
 * The name "groovy" must not be used to endorse or promote products derived
 * from this Software without prior written permission of The Codehaus. For
 * written permission, please contact info@codehaus.org. 4. Products derived
 * from this Software may not be called "groovy" nor may "groovy" appear in
 * their names without prior written permission of The Codehaus. "groovy" is a
 * registered trademark of The Codehaus. 5. Due credit should be given to The
 * Codehaus - http://groovy.codehaus.org/
 * 
 * THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 *  
 */

package groovy.net.xmlrpc

import java.net.ServerSocket
import java.math.BigInteger
import java.math.BigDecimal

import groovy.util.GroovyTestCase

/**
 * Tests the use of the structured Attribute type
 * 
 * @author <a href="mailto:tug@wilson.co.uk">John Wilson/a>

 */
public class GroovyXmlrpcTest extends GroovyTestCase {

    public void testXmlrpcCalls() {
    //
    // create a new XML-RPC server
    //
    def server = new XMLRPCServer(2, 10, 8, 1000, 1000)
    
    //
    // add methods that can be called remotely
    //
    
    server.validator1.arrayOfStructsTest = {structs ->      
                                              def count = 0
                                            
                                              for (struct in structs) {
                                                count += struct['curly']
                                              }
                                              
                                              return count
                                           }
    server.validator1.countTheEntities = {text -> foo(text) }
    
    server.validator1.easyStructTest = {struct -> return struct['larry'] + struct['moe'] + struct['curly'] }
    
    server.validator1.echoStructTest = {struct -> return struct }  
    
    server.validator1.manyTypesTest = {p1, p2, p3, p4, p5, p6 ->  
                                          return [p1, p2, p3, p4, p5, p6]
                                       }  
    
    server.validator1.moderateSizeArrayCheck = {array -> return array[0] + array[array.size() - 1] } 
    
    server.validator1.nestedStructTest = {struct ->  
                                            def day = struct['2000']['04']['01']
                                            return day['larry'] + day['moe'] + day['curly']
                                         }
    
    server.validator1.simpleStructReturnTest = {number ->  
                                                  return ['times10' : number * 10, 'times100' : number * 100, 'times1000' : number * 1000]
                                               }
                                   
    server.echo = {return it}
    
    //
    // switch the server on
    //
    def serverSocket = new ServerSocket(0)
    server.startServer(serverSocket)

    try {
    
      //
      // create a proxy of the server to handle calls
      //
      def serverProxy = new XMLRPCServerProxy("http://127.0.0.1:${serverSocket.getLocalPort()}")
      
      serverProxy.validator1.arrayOfStructsTest([['curly': 9], ['curly' : 3]]) {result ->
        assertEquals("validator1.arrayOfStructsTest", result, 12)
      }
      
      serverProxy.validator1.countTheEntities('<.\'"  l&oi ><><><>"""') {result ->
        assertEquals("serverProxy.validator1.countTheEntities", result['ctLeftAngleBrackets'], 4)
        assertEquals("serverProxy.validator1.countTheEntities", result['ctRightAngleBrackets'], 4)
        assertEquals("serverProxy.validator1.countTheEntities", result['ctApostrophes'], 1)
        assertEquals("serverProxy.validator1.countTheEntities", result['ctAmpersands'], 1)
        assertEquals("serverProxy.validator1.countTheEntities", result['ctQuotes'], 4)
      }
      
      
      serverProxy.validator1.manyTypesTest('a', 1.25, 'c', true, 2, 3) {result ->
        assertEquals("serverProxy.validator1.manyTypesTest", result[0], 'a')
        assertEquals("serverProxy.validator1.manyTypesTest", result[1], 1.25)
        assertEquals("serverProxy.validator1.manyTypesTest", result[2], 'c')
        assertEquals("serverProxy.validator1.manyTypesTest", result[3], true)
        assertEquals("serverProxy.validator1.manyTypesTest", result[4], 2)
        assertEquals("serverProxy.validator1.manyTypesTest", result[5], 3)
      }
      
      serverProxy.validator1.moderateSizeArrayCheck(['a', 'b', 'c']) {result ->
        assertEquals("serverProxy.validator1.moderateSizeArrayCheck", result, 'ac')
      }
      
      serverProxy.echo(["hello", "world"]) {result ->
        assertEquals("serverProxy.echo", result[0], "hello")
        assertEquals("serverProxy.echo", result[1], "world")
      }
      
      serverProxy.echo(["hello", "world"] as String[]) {result ->
        assertEquals("serverProxy.echo", result[0], "hello")
        assertEquals("serverProxy.echo", result[1], "world")
      }

      serverProxy.echo(['a', 'b'] as char[]) {result ->
        assertEquals("serverProxy.echo", result[0], 97)
        assertEquals("serverProxy.echo", result[1], 98)
      }

      serverProxy.echo(['a', 'b'] as Character[]) {result ->
        assertEquals("serverProxy.echo", result[0], 97)
        assertEquals("serverProxy.echo", result[1], 98)
      }
      
      serverProxy.echo([1, 2] as Integer[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as int[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as Long[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as long[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as Short[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as short[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as Byte[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1, 2] as byte[]) {result ->
        //
        // Note that this is a special case and gets transmitted as Base64 encoded
        //
        assertEquals("serverProxy.echo", result[0], 1 as byte)
        assertEquals("serverProxy.echo", result[1], 2 as byte)
      }
      
      serverProxy.echo([1G, 2G] as BigInteger[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1)
        assertEquals("serverProxy.echo", result[1], 2)
      }
      
      serverProxy.echo([1.0, 2.0] as Float[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1.0)
        assertEquals("serverProxy.echo", result[1], 2.0)
      }
      
      serverProxy.echo([1.0, 2.0] as float[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1.0)
        assertEquals("serverProxy.echo", result[1], 2.0)
      }
      
      serverProxy.echo([1.0, 2.0] as double[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1.0)
        assertEquals("serverProxy.echo", result[1], 2.0)
      }
      
      serverProxy.echo([1.0, 2.0] as Double[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1.0)
        assertEquals("serverProxy.echo", result[1], 2.0)
      }
      
      serverProxy.echo([1.0, 2.0] as BigDecimal[]) {result ->
        assertEquals("serverProxy.echo", result[0], 1.0)
        assertEquals("serverProxy.echo", result[1], 2.0)
      }
      
      serverProxy.echo([true, false] as Boolean[]) {result ->
        assertEquals("serverProxy.echo", result[0], true)
        assertEquals("serverProxy.echo", result[1], false)
      }
      
      serverProxy.echo([true, false] as boolean[]) {result ->
        assertEquals("serverProxy.echo", result[0], true)
        assertEquals("serverProxy.echo", result[1], false)
      }
    }
    finally {
      //
      // switch the server off
      //
      server.stopServer()
    }
  }
  
  def foo(text) {
    def ctLeftAngleBrackets = 0
    def ctRightAngleBrackets = 0
    def ctAmpersands = 0
    def ctApostrophes = 0
    def ctQuotes = 0
     
    for (c in text) {
      switch (c) {
        case '<' :
          ctLeftAngleBrackets++
          break;
          
        case '>' :
          ctRightAngleBrackets++
          break;
          
        case '&' :
          ctAmpersands++
          break;
          
        case '\'' :
          ctApostrophes++
          break;
          
        case '"' :
          ctQuotes++
          break;
      }
    }
    
    return ['ctLeftAngleBrackets' : ctLeftAngleBrackets,
            'ctRightAngleBrackets' : ctRightAngleBrackets,
            'ctAmpersands' : ctAmpersands,
            'ctApostrophes' : ctApostrophes,
            'ctQuotes' : ctQuotes]

  }                         
}

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
		server = new XMLRPCServer(2, 10, 8, 1000, 1000)
		
		//
		// add methods that can be called remotely
		//
		server.validator1.arrayOfStructsTest = {structs |      
							                   		count = 0
						                   		
							                   		for (struct in structs) {
							                   			count += struct['curly']
							                   		}
							                   		
							                   		return count
							                   }
		
		server.validator1.countTheEntities = {string |      
						                   		ctLeftAngleBrackets = 0
						                   		ctRightAngleBrackets = 0
						                   		ctAmpersands = 0
						                   		ctApostrophes = 0
						                   		ctQuotes = 0
						                   		 
						                   		for (c in string) {
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
						                   					
						                   				default:
						                   			}
						                   		}
						                   		
						                   		return ['ctLeftAngleBrackets' : ctLeftAngleBrackets,
								                   		'ctRightAngleBrackets' : ctRightAngleBrackets,
								                   		'ctAmpersands' : ctAmpersands,
								                   		'ctApostrophes' : ctApostrophes,
								                   		'ctQuotes' : ctQuotes]
						                   }
		
		server.validator1.easyStructTest = {struct | return struct['larry'] + struct['moe'] + struct['curly'] }
		
		server.validator1.echoStructTest = {struct | return struct }  
		
		server.validator1.manyTypesTest = {p1, p2, p3, p4, p5, p6 |  
						                   		return [p1, p2, p3, p4, p5, p6]
						                   }  
		
		server.validator1.moderateSizeArrayCheck = {array | return array[0] + array[array.size() - 1] } 
		
		server.validator1.nestedStructTest = {struct |  
							                   	day = struct['2000']['04']['01']
							                   		return day['larry'] + day['moe'] + day['curly']
							                   }
		
		server.validator1.simpleStructReturnTest = {number |  
								                   		return ['times10' : number * 10, 'times100' : number * 100, 'times1000' : number * 1000]
								                   }
								                   
		server.echo = {return it}
		
		//
		// switch the server on
		//
		serverSocket = new ServerSocket(0)
		server.startServer(serverSocket)

		try {
		
			//
			// create a proxy of the server to handle calls
			//
			serverProxy = new XMLRPCServerProxy("http://127.0.0.1:${serverSocket.getLocalPort()}")
			
			result = serverProxy.validator1.arrayOfStructsTest([['curly': 9], ['curly' : 3]])
			
			assertEquals("validator1.arrayOfStructsTest", result, 12)
			
			serverProxy.validator1.countTheEntities('<.\'"  l&oi ><><><>"""') { result |
				assertEquals("serverProxy.validator1.countTheEntities", result['ctLeftAngleBrackets'], 4)
				assertEquals("serverProxy.validator1.countTheEntities", result['ctRightAngleBrackets'], 4)
				assertEquals("serverProxy.validator1.countTheEntities", result['ctApostrophes'], 1)
				assertEquals("serverProxy.validator1.countTheEntities", result['ctAmpersands'], 1)
				assertEquals("serverProxy.validator1.countTheEntities", result['ctQuotes'], 4)
			}
			
			
			serverProxy.validator1.manyTypesTest('a', 1.125, 'c', 1, 2, 3) { result |
				assertEquals("serverProxy.validator1.manyTypesTest", result[0], 'a')
				assertEquals("serverProxy.validator1.manyTypesTest", result[1], 1.125)
				assertEquals("serverProxy.validator1.manyTypesTest", result[2], 'c')
				assertEquals("serverProxy.validator1.manyTypesTest", result[3], 1)
				assertEquals("serverProxy.validator1.manyTypesTest", result[4], 2)
				assertEquals("serverProxy.validator1.manyTypesTest", result[5], 3)
			}
			
			result = serverProxy.validator1.moderateSizeArrayCheck(['a', 'b', 'c'])
			
			assertEquals("serverProxy.validator1.moderateSizeArrayCheck", result, 'ac')
		}
		finally {
			//
			// switch the server off
			//
			server.stopServer()
		}
    }
}

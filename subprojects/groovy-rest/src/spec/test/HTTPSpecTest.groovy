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

/**
* Tests for groovy.io.HTTP
*/
package groovy.io;

import javax.net.ssl.SSLContext;

class HTTPSpecTest extends GroovyTestCase {

    void test1Http() {
        // tag::http1[]
        def t = HTTP.get(url:"http://time.jsontest.com/")
        assert t.response.code==200
        assert t.response.contentType =~ "/json"
        assert t.response.body instanceof Map // expecting response like:  {"time": "01:07:59 PM", "date": "05-31-2017"}
        assert t.response.body.time
        assert t.response.body.date
        // end::http1[]
    }

    void test2Https() {
        // tag::http2[]
        def t = HTTP.get(
            url:"https://api.github.com/"
        )
        assert t.response.code==200
        assert t.response.contentType =~ "/json"
        assert t.response.body instanceof Map // expecting response like:  { "current_user_url": "https://api.github.com/user", ... }
        assert t.response.body.current_user_url == "https://api.github.com/user"
        // end::http2[]
    }

    void test3HttpXml() {
        // tag::http3[]
        //create xml with builder
        def xml = new groovy.util.NodeBuilder().
            aaa(a0:'a00'){
                bbb(b0:'b00', "some text")
            }
        assert xml instanceof groovy.util.Node
        //send post request
        def t = HTTP.get(
            url:   "https://httpbin.org/post",
            query: [p1: "11&22", p2:"33 44"],
            //define body as closure, so it will be called to serialize data to output stream 
            body: {outStream,ctx->
                groovy.xml.XmlUtil.serialize(xml,outStream)
            },
            //define content type as xml so server should response in xml
            headers:[
                "content-type":"application/xml"
            ]
        )
        assert t.response.code==200
        //the https://httpbin.org/post service returns json object 
        //with `args` attribute with parameters and `data` attribute with body as string 
        assert t.response.contentType =~ "/json"
        assert t.response.body.args.p1 =="11&22"
        assert t.response.body.args.p2 =="33 44"
        assert t.response.body.data == groovy.xml.XmlUtil.serialize(xml)
        // end::http3[]
    }

    void test4PostJson() {
        // tag::http4[]
        def t = HTTP.post(
            url:   "https://httpbin.org/post",
            //define payload as maps/arrays
            body: [
              arr_int: [1,2,3,4,5,9],
              str: "hello",
            ],
            //let's specify content-type = json, so JsonOutput.toJson() will be applied
            headers:[
                "content-type":"application/json"
            ],
            encoding: "UTF-8",  //force to use utf-8 encoding for sending/receiving data
        )
        assert t.response.code==200
        assert t.response.contentType =~ "/json"
        assert t.response.body instanceof Map
        //the https://httpbin.org/post service returns json object with json key that contains posted body
        //so let's take it and validate
        def data = t.response.body.json  
        assert data.arr_int==[1,2,3,4,5,9]
        assert data.str=="hello"
        // end::http4[]
    }

}

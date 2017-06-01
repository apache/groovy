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
        def t = HTTP.get(url:"https://api.github.com/")
        assert t.response.code==200
        assert t.response.contentType =~ "/json"
        assert t.response.body instanceof Map // expecting response like:  { "current_user_url": "https://api.github.com/user", ... }
        assert t.response.body.current_user_url == "https://api.github.com/user"
        // end::http2[]
    }

    void test3HttpsXml() {
        // tag::http3[]
        //System.setProperty("https.protocols", "TLSv1");
        def t = HTTP.get(
            url:   "https://esb.alfabank.kiev.ua/services/Currency/getCardCrossRate",
            query: [ccyFrom: 'USD', ccyTo:'UAH'],
            ssl:   HTTP.getSSLContext("TLSv1")
        )
        assert t.response.code==200
        assert t.response.contentType =~ "/xml"
        assert t.response.body instanceof groovy.util.Node

        def ns = new groovy.xml.Namespace("http://sab/")
        assert t.response.body[ns.currencyCrossRate][ns.ccyFrom].text()=="USD"
        assert t.response.body[ns.currencyCrossRate][ns.ccyTo].text()=="UAH"
        // end::http3[]
    }

}

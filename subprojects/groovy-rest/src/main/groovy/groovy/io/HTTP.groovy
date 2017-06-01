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
package groovy.io;

import groovy.json.JsonOutput;
import groovy.transform.Memoized;

import java.security.KeyStore;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import java.security.SecureRandom;
import javax.net.ssl.HttpsURLConnection;

/**
 * simple http rest client for groovy
 * by dlukyanov@ukr.net 
 */
@groovy.transform.CompileStatic
public class HTTP{
    //default response handler
    /** The default receiver that detects and applies *_RECEIVER by content-type response header. 
     * For content type '* /json' uses JSON_RECEIVER, for '* /xml' uses XML_RECEIVER, otherwise TEXT_RECEIVER
     */
    public static Closure DEFAULT_RECEIVER = {InputStream instr,Map ctx->
        Map response       = (Map)ctx.response;
        String contentType = response?.contentType;
        Closure receiver   = TEXT_RECEIVER; //default receiver
        if(contentType){
            if( contentType.indexOf('/json')>0 ){
                receiver = JSON_RECEIVER;
            }else if( contentType.indexOf('/xml')>0 ){
                receiver = XML_RECEIVER;
            }
        }
        ctx.receiver = receiver; //to be able to check what receiver was used
        return receiver(instr,ctx);
    }

    /** Receiver to get response as a text with encoding defined in ctx.
     * Stores parsed text (String) as `response.body`
     */
    public static Closure TEXT_RECEIVER = {InputStream instr,Map ctx->
        //todo: if encoding not defined evaluate it from response headers
        return instr.getText( (String)ctx.encoding );
    }
    
    /** Receiver to get response as json. groovy.json.JsonSlurper() used to parse incoming stream. 
     * Encoding could be defined through ctx.encoding.
     * Stores parsed json object as `response.body`
     */
    public static Closure JSON_RECEIVER = {InputStream instr, Map ctx-> 
        return new groovy.json.JsonSlurper().parse(instr,(String)ctx.encoding);
    }
    
    /** Receiver to get response as xml. groovy.util.XmlParser() used to parse incoming stream. 
     * Stores parsed xml (groovy.util.Node) object as `response.body`
     */
    public static Closure XML_RECEIVER = {InputStream instr, Map ctx-> 
        return new groovy.util.XmlParser().parse(instr);
    }
    
    /** Creates receiver that transfers incoming stream into the file. Stores created `java.io.File` object as `response.body` */
    public static Closure FILE_RECEIVER(File f){
        return { InputStream instr, Map ctx-> 
            f<<instr;
            return f;
        }
    }
    /** Sends request using http method 'GET'. See {@link #send(Map<String, Object>)} for parameter details. */
    public static Map<String,Object> get(Map<String,Object> ctx)throws IOException{
        ctx.put('method','GET');
        return send(ctx);
    }
    
    /** Sends request using http method 'HEAD'. See {@link #send(Map<String, Object>)} for parameter details. */
    public static Map<String,Object> head(Map<String,Object> ctx)throws IOException{
        ctx.put('method','HEAD');
        return send(ctx);
    }
    
    /** Sends request using http method 'POST'. See {@link #send(Map<String, Object>)} for parameter details. */
    public static Map<String,Object> post(Map<String,Object> ctx)throws IOException{
        ctx.put('method','POST');
        return send(ctx);
    }
    
    /** Sends request using http method 'PUT'. See {@link #send(Map<String, Object>)} for parameter details. */
    public static Map<String,Object> put(Map<String,Object> ctx)throws IOException{
        ctx.put('method','PUT');
        return send(ctx);
    }
    
    /** Sends request using http method 'DELETE'. See {@link #send(Map<String, Object>)} for parameter details. */
    public static Map<String,Object> delete(Map<String,Object> ctx)throws IOException{
        ctx.put('method','DELETE');
        return send(ctx);
    }
    
    /**
     * @param url string where to send request
     * @param query Map<String,String> parameters to append to url
     * @param method http method to be used in request. standard methods: GET, POST, PUT, DELETE, HEAD
     * @param headers key-value Map<String,String> with headers that should be sent with request
     * @param body request body/data to send to url (InputStream, CharSequence, groovy.lang.Writable, Closure{outStream,ctx->...}, or Map for json and x-www-form-urlencoded context types)
     * @param encoding encoding name to use to send/receive data - default UTF-8
     * @param connector Closure that will be called to init connection after header, method, ssl were set but before connection established.
     * @param receiver Closure that will be called to receive data from server. Default: {@link #DEFAULT_RECEIVER}. Available: {@link #JSON_RECEIVER}, {@link #XML_RECEIVER}, {@link #TEXT_RECEIVER}, {@link #FILE_RECEIVER(java.io.File)}.
     * @param ssl javax.net.ssl.SSLContext or String that evaluates the javax.net.ssl.SSLContext. example: send( url:..., ssl: "HTTP.getKeystoreSSLContext('./keystore.jks', 'testpass')" )
     * @return the modified ctx Map with new property `response`:
     * <table> 
     * <tr><td>response.code</td><td>http response code. for example '200' as Integer</td><tr> 
     * <tr><td>response.message</td><td>http response message. for example for code '404' it will be 'Not Found'</td><tr> 
     * <tr><td>response.contentType</td><td>http `content-type` header. returned by URLConnection.getContentType()</td><tr> 
     * <tr><td>response.headers</td><td>http response headers Map<String,List<String>> returned by URLConnection.getHeaderFields()</td><tr> 
     * <tr><td>response.body</td><td>response body returned by a *_RECEIVER. For example {@link #TEXT_RECEIVER} returns body as text, and {@link #FILE_RECEIVER} returns body as java.io.File object</td><tr> 
     * </table> 
     */
    public static Map<String,Object> send(Map<String,Object> ctx)throws IOException{
        String             url      = ctx.url;
        Map<String,String> headers  = (Map<String,String>)ctx.headers;
        String             method   = ctx.method;
        Object             body     = ctx.body;
        String             encoding = ctx.encoding?:"UTF-8";
        Closure            connector= (Closure)ctx.connector;
        Closure            receiver = (Closure)ctx.receiver?:DEFAULT_RECEIVER;
        Map<String,String> query    = (Map<String,String>)ctx.query;
        Object             sslCtxObj= ctx.ssl;
        
        //copy context and set default values
        ctx = [:] + ctx;
        ctx.encoding = encoding;
        String contentType="";
        
        if(query){
            url+="?"+query.collect{k,v-> k+"="+URLEncoder.encode(v,'UTF-8') }.join('&')
        }
        
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        if(sslCtxObj!=null && connection instanceof HttpsURLConnection){
            SSLContext         sslCtx   = null;
            if(sslCtxObj instanceof SSLContext){
                sslCtx = (SSLContext)sslCtxObj;
            }else if(sslCtxObj instanceof CharSequence){
                //assume this is a groovy code to get ssl context
                sslCtx = evaluateSSLContext((CharSequence)sslCtxObj);
            }else{
                throw new IllegalArgumentException("Unsupported ssl parameter ${sslCtxObj.getClass()}")
            }
            ((HttpsURLConnection)connection).setSSLSocketFactory(sslCtx.getSocketFactory());
        }
        
        connection.setDoOutput(true);
        connection.setRequestMethod(method);
        if ( headers!=null && !headers.isEmpty() ) {
            //add headers
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if(entry.getValue()){
                    connection.addRequestProperty(entry.getKey(), entry.getValue());
                    if("content-type".equals(entry.getKey().toLowerCase()))contentType=entry.getValue();
                }
            }
        }
        if( connector!=null )connection.with(connector);
        
        if(body!=null){
            //write body
            OutputStream out = connection.getOutputStream();
            if( body instanceof Closure ){
                ((Closure)body).call(out, ctx);
            }else if(body instanceof InputStream){
                out << (InputStream)body;
            }else if(body instanceof Writable){
                out.withWriter((String)ctx.encoding){
                    ((Writable)body).writeTo(it);
                }
            }else if(body instanceof Map){
                if( contentType =~ "(?i)[^/]+/json" ) {
                    out.withWriter((String)ctx.encoding){
                        it.append( JsonOutput.toJson((Map)body) );
                    }
                } else if( contentType =~ "(?i)[^/]+/x-www-form-urlencoded" ) {
                    out.withWriter((String)ctx.encoding) {
                        it.append( ((Map)body).collect{k,v-> ""+k+"="+URLEncoder.encode((String)v,'UTF-8') }.join('&') )
                    }
                } else {
                    throw new IOException("Map body type supported only for */json of */x-www-form-urlencoded content-type");
                }
            }else if(body instanceof CharSequence){
                out.withWriter((String)ctx.encoding){
                    it.append((CharSequence)body);
                }
            }else{
                throw new IOException("Unsupported body type: "+body.getClass());
            }
            out.flush();
            out.close();
            out=null;
        }
        
        Map response     = [:];
        ctx.response     = response;
        response.code    = connection.getResponseCode();
        response.message = connection.getResponseMessage();
        response.contentType = connection.getContentType();
        response.headers = connection.getHeaderFields();
        
        InputStream instr = null;
        
        if( ((int)response.code)>=400 ){
            try{
                instr = connection.getErrorStream();
            }catch(Exception ei){}
        }else{
            try{
                instr = connection.getInputStream();
            }catch(java.io.IOException ei){
                throw new IOException("fail to open InputStream for http code "+response.code+":"+ei);
            }
        }
        
        if(instr!=null) {
            instr = new BufferedInputStream(instr);
            response.body = receiver(instr,ctx);
            instr.close();
            instr=null;
        }
        return ctx;
    }
    /**
     * Creates keystore ssl context based on private key.
     * @param protocol used for SSLContext creation. Valid parameters: "TLS", "TLSv1", "TLSv1.1", "TLSv1.2", "SSL", "SSLv2", "SSLv3". 
     * @param keystorePath path to keystore ( usually keystore.jks file )
     * @param keystorePass password to keystore
     * @param keystoreType by default "JKS". Used for java.security.KeyStore.getInstance(java.lang.String)
     * @param keyPass password for the private key - by default and if null then equals to `keystorePass`
     */
    @Memoized
    public static SSLContext getKeystoreSSLContext(String protocol, String keystorePath, String keystorePass, String keystoreType="JKS", String keyPass = null){
        if(keyPass == null) keyPass=keystorePass;
        KeyStore clientStore = KeyStore.getInstance(keystoreType);
        clientStore.load(new File( keystorePath ).newInputStream(), keystorePass.toCharArray());
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(clientStore, keyPass.toCharArray());
        KeyManager[] kms = kmf.getKeyManagers();
        //init TrustCerts
        TrustManager[] trustCerts = new TrustManager[1];
        trustCerts[0] = new X509TrustManager() {
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) { }
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) { }
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(kms, trustCerts, new SecureRandom());
        return sslContext;
    }
    
    /**
     * Creates naive ssl context that trusts to all. Prints to System.err the warning if used...
     * @param protocol used for SSLContext creation. 
     *   Valid parameters: "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2". 
     *   For more information {@see javax.net.ssl.SSLContext#getInstance(java.lang.String)}
     */
    @Memoized
    public static SSLContext getNaiveSSLContext(String protocol="TLS"){
        System.err.println("HTTP.getNaiveSSLContext() used. Must be disabled on prod!");
        KeyManager[] kms = new KeyManager[0];
        TrustManager[] trustCerts = new TrustManager[1];                
        trustCerts[0] = new X509TrustManager() {
            public void checkClientTrusted( final X509Certificate[] chain, final String authType ) { }
            public void checkServerTrusted( final X509Certificate[] chain, final String authType ) { }
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }    
        }
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, trustCerts, new SecureRandom());
        return sslContext;
    }
    
    /**
     * Creates default ssl context but with forced protocol.
     * @param protocol used for SSLContext creation. 
     *   Valid parameters: "SSL", "SSLv2", "SSLv3", "TLS", "TLSv1", "TLSv1.1", "TLSv1.2". 
     *   For more information {@see javax.net.ssl.SSLContext#getInstance(java.lang.String)}
     */
    public static SSLContext getSSLContext(String protocol="TLS"){
        SSLContext sslContext = SSLContext.getInstance(protocol);
        sslContext.init(null, null, null);
        return sslContext;
    }
    
    /**
     * evaluates code that should return SSLContext.
     */
    @Memoized
    public static SSLContext evaluateSSLContext(CharSequence code) {
        Object ssl = new GroovyShell( HTTP.class.getClassLoader() ).evaluate( code as String );
        return (SSLContext) ssl;
    }
}

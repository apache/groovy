/*

 Copyright 2004 (C) John Wilson. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package groovy.net.xmlrpc;

import groovy.lang.Closure;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.co.wilson.net.http.MinMLHTTPServer;
import uk.co.wilson.net.xmlrpc.XMLRPCMessageProcessor;

/**
 * @author John Wilson (tug@wilson.co.uk)
 *
 */
public class XMLRPCServer extends GroovyObjectSupport {
private byte[] base64 = new byte[600];
{
	for (int i = 0; i != this.base64.length; i++) {
		this.base64[i] = (byte)i;
	}
}
public byte[] getBase64() { return this.base64;} // bodge to allow testing

	private static byte[] host;
	static {
		try {
			host  = ("Host: " + InetAddress.getLocalHost().getHostName() +"\r\n").getBytes();
		} catch (UnknownHostException e) {
			host = "Host: unknown\r\n ".getBytes();
		}
	}
	private static final byte[] userAgent = "User-Agent: Groovy XML-RPC\r\n".getBytes();
	private static final byte[] contentTypeXML = "Content-Type: text/xml\r\n".getBytes();
	private static final byte[] contentLength = "Content-Length: ".getBytes();
	private static final byte[] startResponse = ("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>\n" + 
												 "<methodResponse>\n" +
												 "\t<params>\n" +
												 "\t\t<param>\n").getBytes();
	private static final byte[] endResponse = ("\n" +
											   "\t\t</param>\n" +
											   "\t</params>\n" +
											   "</methodResponse>").getBytes();
	private static final byte[] startError = ("<?xml version=\"1.0\"?>\n" + 
											  "<methodResponse>\n" +
											  "\t<fault>\n" +
											  "\t\t<value>\n" +
											  "\t\t\t<struct>\n" +
											  "\t\t\t\t<member>\n" +
											  "\t\t\t\t\t<name>faultCode</name>\n" +
											  "\t\t\t\t\t<value><int>0</int></value>\n" +
											  "\t\t\t\t</member>\n" +
											  "\t\t\t\t<member>\n" +
											  "\t\t\t\t\t<name>faultString</name>\n" +
											  "\t\t\t\t\t<value><string>").getBytes();
	
	private static final byte[] endError = ("</string></value>\n" +
											"\t\t\t\t</member>\n" +
											"\t\t\t</struct>\n" +
											"\t\t</value>\n" +
											"\t</fault>\n" +
											"</methodResponse>\n").getBytes();
	
	private MinMLHTTPServer server = null;
	private final int minWorkers;
	private final int maxWorkers;
	private final int maxKeepAlives;
	private final int workerIdleLife;
	private final int socketReadTimeout;
	private final StringBuffer propertyPrefix = new StringBuffer();
	private final Map registeredMethods = new HashMap();

	/**
	 * @param minWorkers
	 * @param maxWorkers
	 * @param maxKeepAlives
	 * @param workerIdleLife
	 * @param socketReadTimeout
	 */
	public XMLRPCServer(final int minWorkers,
						final int maxWorkers,
						final int maxKeepAlives,
						final int workerIdleLife,
						final int socketReadTimeout)
	{
		this.minWorkers = minWorkers;
		this.maxWorkers = maxWorkers;
		this.maxKeepAlives = maxKeepAlives;
		this.workerIdleLife = workerIdleLife;
		this.socketReadTimeout = socketReadTimeout;
	}
	
	/**
	 * 
	 */
	public XMLRPCServer() {
		this(2, 10, 8, 60000, 60000);
	}
	
	/**
	 * @param serverSocket
	 */
	public void startServer(final ServerSocket serverSocket) throws IOException {
		if (this.server != null) stopServer();
		
		final MinMLHTTPServer server = new MinMLHTTPServer(serverSocket,
								                           this.minWorkers, 
														   this.maxWorkers, 
														   this.maxKeepAlives, 
														   this.workerIdleLife, 
														   this.socketReadTimeout) {

			/* (non-Javadoc)
			 * @see uk.co.wilson.net.MinMLSocketServer#makeNewWorker()
			 */
			protected Worker makeNewWorker() {
				return new HTTPWorker() {
					protected void processPost(final InputStream in,
											   final OutputStream out,
											   final String uri,
											   final String version)
						throws Exception
					{
						
						try {
						final StringBuffer buffer = new StringBuffer();
						final XMLRPCMessageProcessor requestParser = new XMLRPCMessageProcessor();
							
							out.write(version.getBytes());
							out.write(okMessage);
							out.write(userAgent);
							out.write(host);
							out.write(contentTypeXML);
							writeKeepAlive(out);
							out.write(contentLength);
							
							requestParser.parseMessage(in);
							
							final String methodName = requestParser.getMethodname();
							final List params = requestParser.getParams();
							final Object closure = XMLRPCServer.this.registeredMethods.get(methodName);
							
							if (closure == null) throw new GroovyRuntimeException("XML-RPC method " + methodName + " is not supported on this server");
							
							Object result = ((Closure)closure).call(params.toArray());
							
							if (result == null) result = new Integer(0);
							
							XMLRPCMessageProcessor.emit(buffer, result);
							
//							System.out.println(buffer.toString());
							
							final byte[] response = buffer.toString().getBytes("ISO-8859-1");
							
							out.write(String.valueOf(startResponse.length + response.length + endResponse.length).getBytes());
							out.write(endOfLine);
							out.write(endOfLine);
							out.write(startResponse);
							out.write(response);
							out.write(endResponse);
						}
						catch (final Throwable e) {
//						e.printStackTrace();
						final String message = e.getMessage();
						final byte[] error = ((message == null) ? endError.getClass().getName() : e.getMessage()).getBytes();
							
							out.write(String.valueOf(startError.length + error.length + endError.length).getBytes());
							out.write(endOfLine);
							out.write(endOfLine);
							out.write(startError);
							out.write(error);
							out.write(endError);
						}
					}
				};
			}
		};
		
		this.server = server;
		
		new Thread() {
			public void run() {
				server.start();
			}
		}.start();
	}
	
	public void stopServer() throws IOException {
		this.server.shutDown();
÷û½ÛñâGÔâÃ…A1±çüH(fğ^@òK
•-À$ˆ
dQ)IÖ€bş··¨µ™z×ç-C	äàXÄE—0ëéÌğ5'¨E]„vF”ÒV„3At	ÃM´×®û›3æ‚Ş#âv(Å®Óuç“z\›—ñ’ŞÏÖç4?Vm±Æá~"ªK†‚O¥“&Å"L}Öó§–WqÇ —,gvá\·Ò}vƒ4ÖÓÆ:C¯Ñó³ĞH 3Og‹#hÇe¯+WK|Û0«M[>C¾¬oo0#ÅN:„üXõ(ù£‰
nº «Iêg<Udnuìkñ˜ÎpåPÊ|_Ú3ê)˜»\Ìé£f@‹zûéÑHöÇu´6éöv •’V£²©®ØÈdÊeR0=|0ÛÛ7ğ·lMX<åÕá©²åèY“qp}oƒØÀVK¤/éz¿<éÑ‚>†¯UQnÑ¤YîY³8êw®£`;†h·tB²H±áÜ€²j°LVËÑ/8ÛETd¥SŠµ^TF
ÆÈŠ‘»£À²œÏÂ®bVª[gM›õ,D±§Q¬9Æ†îjbŞë_{p‡n7Æê9ğÆ%µJ?†nXå)!û÷r9a©HaQ
pùÂÜrÅ¯Êçnw¸â
F²U²h…?²uh´¼ĞZ…Û²,`9‡¼€Å×jn8ï‚2jÓ,bÕjö>,¹¬LÊ23¼6Ñ¤jÃaa»†Í%ÔòtÜB¹…hDê†¯Ë˜³éÔwíîÿrÓŒmı¹šöoA™wtj%Øİ~.p›`·c¶µ½
’µÌo=z€.•U!å‡•e¨~!¯Š§¥¶	Yˆ6×DÕJœøÄâÈ¶;H¯›‡ÄÓ>
<Ò%e<wr®`[”Ã-³Ã~fÆe>çÀÔ-ÖÃv†ÃxNÆé&ğr›†p¨¿ueâõƒØøCÍC{(W?ç9
8ÜË»ö€ë¬~6ÆÇŸÇpÿÜ9ğñ\>+ãÏxnúÙMâ”©wàâ+jJ{„	†?bâ«ü—±§ò‰!ß€º¶l®°vüææEÇfHğÌ/©/Ì±#{ îÁa½ŸèûÛFşğnü''ËËccşt†túWÇwpÿn¡å~©;êà!ÍPQ¯ô)	Ì/ÏOÿ^é[(¨¶`ë›ˆ—vŸõ|‰pRîİ"¨z› íI[&_áô¹ãÅÂÌäÂ´uj—‚Ú©¥¶QA>ÕBÙÄÆÈ|*´-0ÈşM)(ïÀÌå'c`îaö”ÏîN¤ƒ ãRí;p€1{sã«ÃÑá'F ş(Ù—ìÖ^yßwªo¨J–œÕ™×àşîì7Añ]îÛ,¸×MMƒ~füåÛT8ÿİ+ëİ„a’ê»’}
¬oÔoá£ì7æ×ö/øaí#ìWÊô·”ÌÙwhÿ÷¾©<iOâ‰ã÷j`¬¾šÄßë;UTõ½›;uÓy•…•6Ó8Úy†¬Ïhğ×*¶s -sÚ|K~Á…æøü#AÚê#ìÌ¼}Ïü|$³v^yíØ——Õ¦‚9±LšåX)“Êåš­‚,ˆpI=ZaÄqÁªó“j ˆ,ŒI™>ÀiOQ¯ÒT½ş™¥MiîÒêüf³ƒÈPMÎT'v¦««3+sîÎÆîüzòKá’˜ÀhªWIòÅkÎÚzsg“‡JV6? ¿…¾'­•ašğŒ¿§­¢ÉF`0%cödQávWÊË%IYÌOtFÓÖ6órÒ’ÆÖ„*óYÓ×í)T±¶qTAâPÇÍŒêV¡¥ªT§7 ¿½êÀù~>‡mÓ¥c×·D¤íÅ8ÚHIù“¦–5jêÀ#P«vçã5f·ó‰\~¾¼XîÎ*3±S»Ì•©±mBdfñ*Ã’ö´Ø!íkü‚/VÌÅ ¦±5NäØ@F[“÷ûZûµW?÷ÓÍ—Û{gõÕ%+k­å²ùÕõ=åhN”‹ËíªŞæ-³ÆÓ·5®3';%İcA3î ™ÂÍÅDz¤–ôÉ	67?÷«¾N;ı7‡_ÿvÂ­®J›¢×¬~\´
—¡nçEÍ=s[’šùNJêúåaÏßÁ"ŸÙ6<Ù£Ôİ^[—X¢vÇÚ
š5#í¬!Â(a³éƒŞ—ßgbH¡W©HCÒª2û–J’!·­|Ç“k:ÃØ›ÀÓÖ‚rn)æ…R‚Éµ*QmMmíİâÂQ‡š«ÌM.ËŒÊ=xCºm26;w«“ƒáC­¥P!Ó^thïEu´«İ“ó>ËçÖĞª[®‹•~AŠJäìq6,‘«ş‹Yë@˜'Å«[JŒv|ï%=û%dj')G•—šª³d¼+Tk0ºN^oÑmĞÀ£ÕóÊ†¬úNGûçñ¥0¡[ø“ui²íFô„ Ğ?”xÛ}I~QšúuÖS¥ÃÈ
nLœÊ'Óa"—µÏ^èe6?,„ˆ÷\ág2ÂTÏ'¢È’‚şIê£ÖWa¥J¯YáòóëÍÓëÌ+ºïÏ…“<µoÀqhÿ»È'ÒªŞè%ÒcBèŞæ8Jw©p¬½ˆ§Ô	Jg„`Ê¹3kì#ı˜Ân.Qr­cü —½XQ~¶(çè
†›<âMñCsTé|	sm‘Ç³@'hÙ
–3ÕNè¡ø³J'.=Vó+YJÿ¯°‘¹ŸN„‚G<Te%Ù¼ân˜ºúŸÎLÉ|îùNA½"®{£ÚğYKÒÉ…åkÛioKWx\KÄÆ·Mã; i~ŠüKTÉQœüCÍ«Dq¨nåsíbN‘’‰”˜¼c>8~@dFn‘~T@FŠ¼ÖEŒò6¨\˜ï%@ğlQOØàÙ{í«{mè\¾"÷Z—ÿŞy3OØìmDŠ7N+FÌ#mâ{2!	•Š>‘¤ºNº.Ï©mºÈ
£
ò"I„I„ªöíêRd…inÃÔÁÍ‘JVÖYE\h»zƒñŠMj„V#UkpÿÖ…oÚ6\„o„o
x™×!µAŞ(1!nÖ™~ÑC—g„ãWä÷àãœ³÷k«G®[{İ1Ÿü0nÔD&RPdbUL'Z0Ò#ú`«KSúK“<³Ëß-J<OÉ•q|ËE·møzÎ—nÀcdşioJÖsL,$ÇšU-yö=T<Y"xQâN2+ÎuIÈ=ˆÏK&ƒ¾b½p)¦8‘ü´¢ã6ş‹úl2(Ğ6˜IÇFgø»™oPĞ	•nÍè`®r­€VQ=©R)@ádÑ½ ²[£,]7â‰êû˜Y¨bzàLÕ’”!´p8Vù¶ş¼EôUN-Â¤y°ğÓS¸‘¯YQ¨À¢Úôºp}¤¬Ìx†0£Ó–gzWJàj^÷e›]VÎ-‚Xòˆp	3Æ”böƒ~”\›µ.W¢-\fÏP³ÿà'çì°{k¿kë%yğO1Ç	f	ÔÅ›ğUñÈ·X©é_	/oê\|Qì„™r/Æ0Ê9ê¾.Ğ1ßê!#ŸÙ‘
¤}0ˆàLãDOİÊ€r]ô!/8cP¹B®@Ó,&¨¼OàŒ×‚`«;Í#V§HcH_$åSvSê›!'³à»0Øz9ô¬[êÙ',Õc,İƒU˜Â.û¡MEäUŸ
%´rW4‚!•ª¿®¬ç˜9
Ä¨Yğ‰›‡’³àX2ª¢
// Copyright (c) 2001 The Wilson Partnership.
// All Rights Reserved.
// @(#)MinMLHTTPServer.java, 0.3, 14th October 2001
// Author: John Wilson - tug@wilson.co.uk

package uk.co.wilson.net.http;

/*
Copyright (c) 2001 John Wilson (tug@wilson.co.uk).
All rights reserved.
Redistribution and use in source and binary forms,
with or without modification, are permitted provided
that the following conditions are met:

Redistributions of source code must retain the above
copyright notice, this list of conditions and the
following disclaimer.

Redistributions in binary form must reproduce the
above copyright notice, this list of conditions and
the following disclaimer in the documentation and/or
other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY JOHN WILSON ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL JOHN WILSON
BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE
*/

import uk.co.wilson.net.MinMLSocketServer;

import java.net.ServerSocket;
import java.net.Socket;

import java.io.InputStream;
import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import java.util.StringTokenizer;

public abstract class MinMLHTTPServer extends MinMLSocketServer {
  public MinMLHTTPServer(final ServerSocket serverSocket,
                         final int minWorkers,
                         final int maxWorkers,
                         final int maxKeepAlives,
                         final int workerIdleLife,
                         final int socketReadTimeout)
  {
    super(serverSocket, minWorkers, maxWorkers, workerIdleLife);

    this.maxKeepAlives = maxKeepAlives;
    this.socketReadTimeout = socketReadTimeout;
  }

  private synchronized boolean startKeepAlive() {
   if (this.keepAliveCount < this.maxKeepAlives) {
      MinMLHTTPServer.this.keepAliveCount++;

      return true;
    }

    return false;
  }

  private synchronized void endKeepAlive() {
    this.keepAliveCount--;
  }

  private static class LimitedInputStream extends InputStream {
    public LimitedInputStream(final InputStream in, final int contentLength) {
      this.in = in;
      this.contentLength = contentLength;
    }

    public int available() throws IOException {
      return Math.min(this.in.available(), this.contentLength);
    }

    public void close() throws IOException {
      //
      // Don't close the input stream as there is more data
      // but skip past any unread data in this section
      //

      skip(this.contentLength);
    }

    public int read() throws IOException {
      if (this.contentLength == 0) return -1;

      this.contentLength--;

      return this.in.read();
    }

    public int read(final byte[] buffer) throws IOException {
      return read(buffer, 0, buffer.length);
     }

    public int read(final byte[] buffer, final int offset, int length) throws IOException {
      if (this.contentLength == 0) return -1;

      length = this.in.read(buffer, offset, Math.min(length, this.contentLength));

      if (length != -1) this.contentLength -= length;

      return length;
    }

    public long skip(long count) throws IOException {
      count = Math.min(count, this.contentLength);

      this.contentLength -= count;

      return this.in.skip(count);
     }

     private int contentLength;
     private final InputStream in;
  }

  protected abstract class HTTPWorker extends Worker {
    protected final void process(final Socket socket) throws Exception {
      try {
        socket.setSoTimeout(MinMLHTTPServer.this.socketReadTimeout);

        final InputStream in = new BufferedInputStream(socket.getInputStream());
        final OutputStream out = new BufferedOutputStream(socket.getOutputStream());
        int contentLength;

        do {
          contentLength = -1;

          while (readLine(in) != -1 && this.count == 0);  // skip any leading blank lines
          final StringTokenizer toks = new StringTokenizer(new String(this.buf, 0, this.count));
          final String method = toks.nextToken();
          final String uri = toks.nextToken();
          final String version = toks.hasMoreTokens() ? toks.nextToken() : "";

          while (readLine(in) != -1 && this.count != 0) {
          final String option = new String(this.buf, 0, this.count).trim().toLowerCase();

            if (option.startsWith("connection:")) {
              if (option.endsWith("keep-alive")) {
                if (!this.keepAlive)
                  this.keepAlive = MinMLHTTPServer.this.startKeepAlive();
              } else if (this.keepAlive) {
                  MinMLHTTPServer.this.endKeepAlive();
                  this.keepAlive = false;
              }
            } else if (option.startsWith("content-length:")) {
              contentLength = Integer.parseInt(option.substring(15).trim());
              //
              // This can throw NumberFormatException
              // In which case we will abort the transaction
              //
            }
          }

          if (contentLength == -1) {
            processMethod(in, out, method, uri, version);
          } else {
          final InputStream limitedIn = new LimitedInputStream(in, contentLength);

            processMethod(limitedIn, out, method, uri, version);

            limitedIn.close();  // skips unread bytes
          }

          out.flush();

        } while(contentLength != -1 && this.keepAlive);
      }
      finally {
        if (this.keepAlive == true) {
          MinMLHTTPServer.this.endKeepAlive();
          this.keepAlive = false;
        }
      }
    }

    protected void processMethod(final InputStream in,
                                 final OutputStream out,
                                 final String method,
                                 final String uri,
                                 final String version)
    throws Exception
    {
      if (method.equalsIgnoreCase("GET"))
        processGet(in, out, uri, version);
      else if (method.equalsIgnoreCase("HEAD"))
        processHead(in, out, uri, version);
      else if (method.equalsIgnoreCase("POST"))
        processPost(in, out, uri, version);
      else if (method.equalsIgnoreCase("PUT"))
        processPut(in, out, uri, version);
      else
        processOther(in, out, method, uri, version);
    }

    protected void processGet(final InputStream in,
                              final OutputStream out,
                              final String uri,
                              final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(get);
      out.write(errorMessage2);
    }

    protected void processHead(final InputStream in,
                               final OutputStream out,
                               final String uri,
                               final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(head);
      out.write(errorMessage2);
    }

    protected void processPost(final InputStream in,
                               final OutputStream out,
                               final String uri,
                               final String version)
      throws Exception
    {
      out.write(version.getBytes());
      out.write(errorMessage1);
      out.write(post);
      out.write(errorMessage2);
    }

    protected void processPut(final InputStream in,
                              final OutputStream out,
                              final String uri,
                              final Stri`MÿU,â {ñ$s≤WdCÏ)ëox‡"“X9F9ÊÁìC>ÿºPE'Ô¢X∞n&I{‡«0∂(ñ˙¯ bÑ¬Å›üe„W
n<∆±µÓöN™ºÌ¡Ïı≠›Næ‹¿*|´’IKÕSa∏gü~~<8ét®;÷d1C¬@ﬁ;+ôJ‹i Snn0Mf*Îà⁄D;∑ThA,´Ü™óŸ‹ı]OÊﬂùÍ]*
?£ ™e?0ÿÏc√Uü
å∫Ω)¬õúq\ar‚O‘>[˚N3 /øÍ<ÃºÕhë∑Ô¸∑úÈøï	˚îéˇ}ª‰/ïSü´}˜¸E+#∫?)@bµH„Z2fåŒúk¢°J–=ºÛ,:¯†ú◊l»wzzπùŸ:QyãÆê=í_ê:q…J?-	îNäYŸ™∑}ÛJ⁄∑◊sÍözÕZ˝c_Ÿ-w¸Uç5œ$é¿¡™Õ ï/Aë…)§øÔä^h<ßb∑∫A<gx¢SGù`ÒßÑ€%ﬁ∆A˚XS,8_Á–{ãY≈s◊èuÇÄrñ8e|u‚Ω“Ò¯¯èN
ƒÜÄöpz"‰£c°˚œu·B8HyØºÇçfÆáıGÃoâ’ÀS»5º!?…B¶ˆπëb€Äˆ˝æÂÃ°ù•>N,}‰¶C5ÊûV¬Aaú.u[0”ç	"uu÷˘›b.ÚRx˛–whM1»ïπu˛Ã.>¯ ; ÷œêAı«T¿|È,ºb4˛Ñæ≤ºX}-Q˝üÇ™qìÈ;˜òIK€Ë˝Ÿ6∞—5ÿå‘Ó-≥Çˇ∂Xˇüå¯óÍ‹RY’K]k±AXò©Oíf7v#øî"TCB>>uÓ G›>hæ+‡[Â≠£à3“^÷∫3tE„≠¥›©4Lg'È˚A˚û˜GG¥ü)2•D≈1∫ëâö∫É‰€#~(áWˆs3áK%wí|”qﬁNı?Zıfª≥õâ–D.@ƒ`£aBFNﬁ-Â@í@∫Ñ≠(´÷V |úÓs‡UY@gπZ§Ã†EeõeZ©+∞ıÄú«LJ˛¿“ñì®v…\l;†{úU^‘l°Œ‘`8¢Òíè¥õÛ0OA≈—-™©/‘†›ûdmKoîU)¥¢w7PC#
5√-º¶`ü£Ωå“(É´Qâ{¯£@D˛ï¬Å´Ú'+“ˆ≤ôÑfÅÙ˚NÑ[ß
=IÍmúè∆ZΩüÉ¡xÈÜõ"∏à∂iç¥∫ç¶SƒD§€xß#òΩà~F5 ”}à1âÔåçŒÉı)Íì3s:Â3;øu…‹_$0&VwOÆq ‡π*˜¢ ,›Üg‹˚È'óÁH¥`HÎG7xö|G
Ü+øÊv"Cõº”¥º#Iòg≠¶Pô5–å1Õ?ÇîÁõ‘A2?”{-‚yﬂ!1KÉ¥{È¬jÀ‡0#íLj§RÛReá>îî|Á`±‹¡$Û{ØâvåÔ—*íè úo£,
ﬂ+,ÓÅÂŒ∂ì˚kgM@N&€£H(°}çF-]}6 *£Úgıh#S˙>›Ò’°–ôvVÜûıió%π	iÛJ˘Ã7S~&¢µTO`«ƒw˚Ôû9ﬁA<ø≥mÇÉl7ÕÉWÌπ‰¶UÀÙ
î°µv∆ßF¯ñ(ÜjΩ∑¥Aﬁ‘ë[	/ã{Ü:&àÎ Wh…~	∞©a≠öP[µ¡A∂ﬂ∆`Ö„º#A]ÊBπå¨ã>à:n˜cõñ
yTqΩyπùë\Ùá¢<DœOæïVcGN.ÍÈÿﬁ®j{˝Nˆ≠'Áb¢è‚”J˙&Q‡˛@ou˜€˛7Q%dkm`n£Ôd˚_©QFvÖ—”íãøÄ_*ZÅW–‹ÄB≈V¸ã$ô]p|(•xª»˙Æ¡"”êa€ú†xØ<‡‚∂[1ÂÌUËèÊû…kû∂Îi)◊/wÓópùdÈ∞åCUî=é(ñÍòn1"aáï§¥Ÿ™ı∑HÖ,60xAJ¬·ü#±#æxP>8U¸(¢˚*Œl˘¡∏SègÙ˝ú˚™≥[Â¢ƒ÷tsn…◊x"Ÿs∑4·aé•xfÿfD?ù
9—|E¶√∏‰[
·;çfï˚JdC.‘‡ÖÅP≤•Åv◊’ÛGnúh√v≠‚,wê	®∂¢#ú∫âßHyóWÌ>†1sÉ8Ë3ßÓﬁ@,
Ãbò(k}ÿÄoJX≠µGßıÎ-p∑D*F.]›8{Äƒ◊•öxn„‰ßV™¿¸ ëqwÍØ.\ım^$Éa˙≈¿ôf`p+èL0:S¯„Cﬁ®Ã kw8df˘æuÙ"¶XRõ˜w(ú‹"òcBˆâÎCñ≥:◊Gí¯IÕ¶˝◊âäÛÿˆ*QUL\uÂy∞ı.∞ñœ±Œ—˚ß ãœBYvXHhE¶≈Á¸y∫ìÀ=¡k√PyIDa‹DÎ§G…◊ÚNËË;àxîûrjrÖ∞F‘$§•‰04ç"Øêj≥á ≥>º¸öbLåÍA>º%i±ˇ¸Ë˝øª”o˜>ò,~ÒJáŸ$|æLi¶pŒµT¯ı,ÆFAPs∂”_{'Õè/w†v°ƒ ‰ü”®Ñ?y•ˇ‡ïbçwíP¨∂¥Á∑ÚHŸ&ü¯ÿŒƒË4ñC<®Ÿ{–¥⁄ -–°êåü¥À–AnY\dÚ5·®sËZM«Är‘ﬂÕ~WMz¿]Ÿ71˛C&W».g‹é{°PtÑ·⁄‚ˆ$∏q›z/Î‘'ûîJNUÏIÛ_
Ëÿh“„˜ãbí]@?†·Âı2^ü”,ìyJ,a·÷u,ù«XI'Ó}ø∞ÈäZËq‚ŸÄ˙@	êÃô≈sJ2åìiòƒ|¿4=Æë P”ã;w[A]
Õ˚éÏ!%ãÿP⁄Ê˜ı3<]ê:√Õ;∆†˙È∑V&-Oeb0_êV)a*n¨Ü9¨˛∞‹Qm|ÏMË"¶ˇáS4K[Uﬁå…˘ø`Ãø•,™hÒ4"ƒB÷°
Û#…ôÖ5|¶˚úò£ @Ö*ﬂèê!g GñùÛ¨ ˚sìÛ˘{8ﬂ∞ΩmQwÍËÍ¿ÈR[´«ƒ:ü∑˜0b_§øóøÁÅêp7™çD]
%ôÅ
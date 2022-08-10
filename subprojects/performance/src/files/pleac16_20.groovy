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
 * Refer to pleac.sourceforge.net if wanting accurate comparisons with PERL.
 * Original author has included tweaked examples here solely for the purposes
 * of exercising the Groovy compiler.
 * In some instances, examples have been modified to avoid additional
 * dependencies or for dependencies not in common repos.
 */

// @@PLEAC@@_16.1
//----------------------------------------------------------------------------------
output = "program args".execute().text
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.2
//----------------------------------------------------------------------------------
proc = "vi myfile".execute()
proc.waitFor()
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.3
//----------------------------------------------------------------------------------
// Calling execute() on a String, String[] or List (of Strings or objects with
// a toString() method) will fork off another process.
// This doesn't replace the existing process but if you simply finish the original
// process (leaving the spawned process to finish asynchronously) you will achieve
// a similar thing.
"archive *.data".execute()
["archive", "accounting.data"].execute()
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.4
//----------------------------------------------------------------------------------
// sending text to the input of another process
proc = 'groovy -e "print System.in.text.toUpperCase()"'.execute()
Thread.start{
    def writer = new PrintWriter(new BufferedOutputStream(proc.out))
    writer.println('Hello')
    writer.close()
}
proc.waitFor()
// further process output from process
print proc.text.reverse()
// =>
// OLLEH
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.5
//----------------------------------------------------------------------------------
// filter your own output
keep = System.out
pipe = new PipedInputStream()
reader = new BufferedReader(new InputStreamReader(pipe))
System.setOut(new PrintStream(new BufferedOutputStream(new PipedOutputStream(pipe))))
int numlines = 2
Thread.start{
    while((next = reader.readLine()) != null) {
        if (numlines-- > 0) keep.println(next)
    }
}
(1..8).each{ println it }
System.out.close()
System.setOut(keep)
(9..10).each{ println it }
// =>
// 1
// 2
// 9
// 10


// filtering output by adding quotes and numbers
class FilterOutput extends Thread {
    Closure c
    Reader reader
    PrintStream orig
    FilterOutput(Closure c) {
        this.c = c
        orig = System.out
        def pipe = new PipedInputStream()
        reader = new BufferedReader(new InputStreamReader(pipe))
        System.setOut(new PrintStream(new BufferedOutputStream(new PipedOutputStream(pipe))))
    }
    void run() {
        def next
        while((next = reader.readLine()) != null) {
            c(orig, next)
        }
    }
    def close() {
        sleep 100
        System.out.close()
        System.setOut(orig)
    }
}
cnt = 0
number = { s, n -> cnt++; s.println(cnt + ':' + n) }
quote =  { s, n -> s.println('> ' + n) }
f1 = new FilterOutput(number); f1.start()
f2 = new FilterOutput(quote); f2.start()
('a'..'e').each{ println it }
f2.close()
f1.close()
//----------------------------------------------------------------------------------

import java.util.zip.GZIPInputStream
// @@PLEAC@@_16.6
//----------------------------------------------------------------------------------
// Groovy programs (like Java ones) would use streams here. Just process
// another stream instead of System.in or System.out:

// process url text
input = new URL(address).openStream()
// ... process 'input' stream

// process compressed file
input = new GZIPInputStream(new FileInputStream('source.gzip'))
// ... process 'input' stream
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.7
//----------------------------------------------------------------------------------
// To read STDERR of a process you execute
proc = 'groovy -e "println args[0]"'.execute()
proc.waitFor()
println proc.err.text
// => Caught: java.lang.ArrayIndexOutOfBoundsException: 0 ...

// To redirect your STDERR to a file
System.setErr(new PrintStream(new FileOutputStream("error.txt")))
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.8
//----------------------------------------------------------------------------------
// See 16.2, the technique allows both STDIN and STDOUT of another program to be
// changed at the same time, not just one or the other as per Perl 16.2 solution
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.9
//----------------------------------------------------------------------------------
// See 16.2 and 16.7, the techniques can be combined to allow all three streams
// (STDIN, STDOUT, STDERR) to be altered as required.
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.10
//----------------------------------------------------------------------------------
// Groovy can piggy-back on the many options available to Java here:
// JIPC provides a wide set of standard primitives: semaphore, event,
//   FIFO queue, barrier, shared memory, shared and exclusive locks:
//   http://www.garret.ru/~knizhnik/jipc/jipc.html
// sockets allow process to communicate via low-level packets
// CORBA, RMI, SOAP allow process to communicate via RPC calls
// shared files can also be used
// JMS allows process to communicate via a messaging service

// Simplist approach is to just link streams:
proc1 = 'groovy -e "println args[0]" Hello'.execute()
proc2 = 'groovy -e "print System.in.text.toUpperCase()"'.execute()
Thread.start{
    def reader = new BufferedReader(new InputStreamReader(proc1.in))
    def writer = new PrintWriter(new BufferedOutputStream(proc2.out))
    while ((next = reader.readLine()) != null) {
        writer.println(next)
    }
    writer.close()
}
proc2.waitFor()
print proc2.text
// => HELLO
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.11
//----------------------------------------------------------------------------------
// Java/Groovy would normally just use some socket-based technique for communicating
// between processes (see 16.10 for a list of options). If you really must use a named
// pipe, you have these options:
// (1) On *nix machines:
// * Create a named pipe by invoking the mkfifo utility using execute().
// * Open a named pipe by name - which is just like opening a file.
// * Run an external process setting its input and output streams (see 16.1, 16.4, 16.5)
// (2) On Windows machines, Using JCIFS to Connect to Win32 Named Pipes, see:
// http://jcifs.samba.org/src/docs/pipes.html
// Neither of these achieve exactly the same result as the Perl example but some
// scenarios will be almost identical.
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.12
//----------------------------------------------------------------------------------
// The comments made in 16.10 regarding other alternative IPC mechanisms also apply here.

// This example would normally be done with multiple threads in Java/Groovy as follows.
class Shared {
    String buffer = "not set yet"
    synchronized void leftShift(value){
        buffer = value
        notifyAll()
    }
    synchronized Object read() {
        return buffer
    }
}
def shared = new Shared()
rand = new Random()
threads = []
(1..5).each{
def    t = new Thread(){
        def me = t
//        for (j in 0..9) {
//            shared << "$me.name $j"
//            sleep 100 + rand.nextInt(200)
//        }
    }
    t.start()
}
while(1) {
    println shared.read()
    sleep 50
}
// =>
// not set yet
// Thread-2 0
// Thread-5 1
// Thread-1 1
// Thread-4 2
// Thread-3 1
// ...
// Thread-5 9

// Using JIPC between processes (as a less Groovy alternative that is closer
// to the original cookbook) is shown below.

// ipcWriterScript:
//import org.garret.jipc.client.JIPCClientFactory
port = 6000
factory = JIPCClientFactory.instance
session = factory.create('localhost', port)
mutex = session.createMutex("myMutex", false)
buffer = session.createSharedMemory("myBuffer", "not yet set")
name = args[0]
rand = new Random()
(0..99).each {
    mutex.lock()
    buffer.set("$name $it".toString())
    mutex.unlock()
    sleep 200 + rand.nextInt(500)
}
session.close()

// ipcReaderScript:
//import org.garret.jipc.client.JIPCClientFactory
class JIPCClientFactory{}
port = 6000
factory = JIPCClientFactory.instance
session = factory.create('localhost', port)
mutex = session.createMutex("myMutex", false)
buffer = session.createSharedMemory("myBuffer", "not yet set")
rand = new Random()
(0..299).each {
    mutex.lock()
    println buffer.get()
    mutex.unlock()
    sleep 150
}
session.close()

// kick off processes:
"java org.garret.jipc.server.JIPCServer 6000".execute()
"groovy ipcReaderScript".execute()
(0..3).each{ "groovy ipcWriterScript $it".execute() }

// =>
// ...
// 0 10
// 2 10
// 2 11
// 1 9
// 1 9
// 1 10
// 2 12
// 3 12
// 3 12
// 2 13
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.13
//----------------------------------------------------------------------------------
// Signal handling in Groovy (like Java) is operating system and JVM dependent.
// The ISO C standard only requires the signal names SIGABRT, SIGFPE, SIGILL,
// SIGINT, SIGSEGV, and SIGTERM to be defined but depending on your platform
// other signals may be present, e.g. Windows supports SIGBREAK. For more info
// see: http://www-128.ibm.com/developerworks/java/library/i-signalhandling/
// Note: if you start up the JVM with -Xrs the JVM will try to reduce its
// internal usage of signals. Also the JVM takes over meany hooks and provides
// platform independent alternatives, e.g. see java.lang.Runtime#addShutdownHook()

// To see what signals are available for your system (excludes ones taken over
// by the JVM):
sigs = '''HUP INT QUIT ILL TRAP ABRT EMT FPE KILL BUS SEGV SYS PIPE ALRM TERM
USR1 USR2 CHLD PWR WINCH URG POLL STOP TSTP CONT TTIN TTOU VTALRM PROF XCPU
XFSZ WAITING LWP AIO IO INFO THR BREAK FREEZE THAW CANCEL EMT
'''

sigs.tokenize(' \n').each{
    try {
        print ' ' + new sun.misc.Signal(it)
    } catch(IllegalArgumentException iae) {}
}
// =>  on Windows XP:
// SIGINT SIGILL SIGABRT SIGFPE SIGSEGV SIGTERM SIGBREAK
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.14
//----------------------------------------------------------------------------------
// To send a signal to your process:
Signal.raise(new Signal("INT"))
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.15
//----------------------------------------------------------------------------------
// install a signal handler
class DiagSignalHandler implements SignalHandler {
    @Override
    void handle(Signal signal) { }
}
diagHandler = new DiagSignalHandler()
Signal.handle(new Signal("INT"), diagHandler)
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.16
//----------------------------------------------------------------------------------
// temporarily install a signal handler
class DiagSignalHandler2 implements SignalHandler {
    @Override
    void handle(Signal signal) { }
}
diagHandler = new DiagSignalHandler2()
oldHandler = Signal.handle(new Signal("INT"), diagHandler)
Signal.handle(new Signal("INT"), oldHandler)
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.17
//----------------------------------------------------------------------------------
import sun.misc.Signal
import sun.misc.SignalHandler

class DiagSignalHandler3 implements SignalHandler {
    private oldHandler

    // Static method to install the signal handler
    static install(signal) {
        def diagHandler = new DiagSignalHandler3()
        diagHandler.oldHandler = Signal.handle(signal, diagHandler)
    }

    void handle(Signal sig) {
        println("Diagnostic Signal handler called for signal "+sig)
        // Output information for each thread
        def list = []
        Thread.activeCount().each{ list += null }
        Thread[] threadArray = list as Thread[]
        int numThreads = Thread.enumerate(threadArray)
        println("Current threads:")
        for (i in 0..<numThreads) {
            println("    "+threadArray[i])
        }

        // Chain back to previous handler, if one exists
        if ( oldHandler != SIG_DFL && oldHandler != SIG_IGN ) {
            oldHandler.handle(sig)
        }
    }
}
// install using:
DiagSignalHandler3.install(new Signal("INT"))
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.18
//----------------------------------------------------------------------------------
// See 16.17, just don't chain to the previous handler because the default handler
// will abort the process.
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.19
//----------------------------------------------------------------------------------
// Groovy relies on Java features here. Java doesn't keep the process around
// as it stores metadata in a Process object. You can call waitFor() or destroy()
// or exitValue() on the Process object. If the Process object is garbage collected,
// the process can still execute asynchronously with respect to the original process.

// For ensuring processes don't die, see:
// http://jakarta.apache.org/commons/daemon/
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.20
//----------------------------------------------------------------------------------
// There is no equivalent to a signal mask available directly in Groovy or Java.
// You can override and ignore individual signals using recipes 16.16 - 16.18.
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.21
//----------------------------------------------------------------------------------
t = new Timer()
t.runAfter(3500){
    println 'Took too long'
    System.exit(1)
}
def count = 0
6.times{
    count++
    sleep 1000
    println "Count = $count"
}
t.cancel()
// See also special JMX timer class: javax.management.timer.Timer
// For an external process you can also use: proc.waitForOrKill(3500)
//----------------------------------------------------------------------------------


// @@PLEAC@@_16.22
//----------------------------------------------------------------------------------
// One way to implement this functionality is to automatically replace the ~/.plan
// etc. files every fixed timed interval - though this wouldn't be efficient.
// Here is a simplified version which is a simplified version compared to the
// original cookbook. It only looks at the ~/.signature file and changes it
// freely. It also doesn't consider other news reader related files.

def sigs = '''
Make is like Pascal: everybody likes it, so they go in and change it.
--Dennis Ritchie
%%
I eschew embedded capital letters in names; to my prose-oriented eyes,
they are too awkward to read comfortably. They jangle like bad typography.
--Rob Pike
%%
God made the integers; all else is the work of Man.
--Kronecker
%%
I d rather have :rofix than const. --Dennis Ritchie
%%
If you want to program in C, program in C. It s a nice language.
I use it occasionally... :-) --Larry Wall
%%
Twisted cleverness is my only skill as a programmer.
--Elizabeth Zwicky
%%
Basically, avoid comments. If your code needs a comment to be understood,
it would be better to rewrite it so it s easier to understand.
--Rob Pike
%%
Comments on data are usually much more helpful than on algorithms.
--Rob Pike
%%
Programs that write programs are the happiest programs in the world.
--Andrew Hume
'''.trim().split(/\n%%\n/)
name = 'me@somewhere.org\n'
file = new File(System.getProperty('user.home') + File.separator + '.signature')
rand = new Random()
while(1) {
    file.delete()
    file << name + sigs[rand.nextInt(sigs.size())]
    sleep 10000
}

// Another way to implement this functionality (in a completely different way to the
// original cookbook) is to use a FileWatcher class, e.g.
// http://www.rgagnon.com/javadetails/java-0490.html (FileWatcher and DirWatcher)
// http://www.jconfig.org/javadoc/org/jconfig/FileWatcher.html

// These file watchers notify us whenever the file is modified, see Pleac chapter 7
// for workarounds to not being able to get last accessed time vs last modified time.
// (We would now need to touch the file whenever we accessed it to make it change).
// Our handler called from the watchdog class would update the file contents.
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.0
//----------------------------------------------------------------------------------
myClient = new Socket("Machine name", portNumber)
myAddress = myClient.inetAddress
myAddress.hostAddress  // string representation of host address
myAddress.hostName     // host name
myAddress.address      // IP address as array of bytes
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.1
//----------------------------------------------------------------------------------
s = new Socket("localhost", 5000);
s << "Why don't you call me anymore?\n"
s.close()
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.2
//----------------------------------------------------------------------------------
// commandline socket server echoing input back to originator
//groovy -l 5000 -e "println line"

// commandline socket server eching input to stderr
//groovy -l 5000 -e "System.err.println line"

// a web server as a script (extension to cookbook)
 server = new ServerSocket(5000)
 while(true) {
     server.accept() { socket ->
         socket.withStreams { input, output ->
             // ignore input and just serve dummy content
             output.withWriter { writer ->
                 writer << "HTTP/1.1 200 OK\n"
                 writer << "Content-Type: text/html\n\n"
                 writer << "<html><body>Hello World! It's ${new Date()}</body></html>\n"
             }
         }
     }
 }
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.3
//----------------------------------------------------------------------------------
server = new ServerSocket(5000)
while(true) {
    server.accept() { socket ->
        socket.withStreams { input, output ->
            w = new PrintWriter(output)
            w << "What is your name? "
            w.flush()
            r = input.readLine()
            System.err.println "User responded with $r"
            w.close()
        }
    }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.4
//----------------------------------------------------------------------------------
// UDP client
data = "Message".getBytes("ASCII")
addr = InetAddress.getByName("localhost")
port = 5000
packet = new DatagramPacket(data, data.length, addr, port)
socket = new DatagramSocket()
socket.send(packet)
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.5
//----------------------------------------------------------------------------------
// UDP server
socket = new DatagramSocket(5000)
buffer = (' ' * 4096) as byte[]
while(true) {
    incoming = new DatagramPacket(buffer, buffer.length)
    socket.receive(incoming)
    s = new String(incoming.data, 0, incoming.length)
    String reply = "Client said: '$s'"
    outgoing = new DatagramPacket(reply.bytes, reply.size(),
            incoming.address, incoming.port);
    socket.send(outgoing)
}

// UDP client
data = "Original Message".getBytes("ASCII")
addr = InetAddress.getByName("localhost")
port = 5000
packet = new DatagramPacket(data, data.length, addr, port)
socket = new DatagramSocket()
socket.send(packet)
socket.setSoTimeout(30000) // block for no more than 30 seconds
buffer = (' ' * 4096) as byte[]
response = new DatagramPacket(buffer, buffer.length)
socket.receive(response)
s = new String(response.data, 0, response.length)
println "Server said: '$s'"
// => Server said: 'Client said: 'Original Message''
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.6
//----------------------------------------------------------------------------------
// DOMAIN sockets not available in cross platform form.
// On Linux, use jbuds:
// http://www.graphixprose.com/jbuds/
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.7
//----------------------------------------------------------------------------------
// TCP socket
socketAddress = tcpSocket.remoteSocketAddress
println "$socketAddress.address, $socketAddress.hostName, $socketAddress.port"
// UDP packet
println "$udpPacket.address, $udpPacket.port"
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.8
//----------------------------------------------------------------------------------
// Print the fully qualified domain name for this IP address
println InetAddress.localHost.canonicalHostName
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.9
//----------------------------------------------------------------------------------
socket.shutdownInput()
socket.shutdownOutput()
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.10
//----------------------------------------------------------------------------------
// Spawn off a thread to handle each direction
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.11
//----------------------------------------------------------------------------------
// Spawn off a thread to handle each request.
// This is done automatically by the Groovy accept() method on ServerSocket.
// See 17.3 for an example.
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.12
//----------------------------------------------------------------------------------
// Use a thread pool
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.13
//----------------------------------------------------------------------------------
// Consider using Selector and/or SocketChannel, ServerSocketChannel and DatagramChannel
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.14
//----------------------------------------------------------------------------------
// When creating a socket on a multihomed machine, use the socket constructor with
// 4 params to select a specific address from those available:
socket = new Socket(remoteAddr, remotePort, localAddr, localPort)

// When creating a server on a multihomed machine supply the optional bindAddr param:
new ServerSocket(port, queueLength, bindAddr)
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.15
//----------------------------------------------------------------------------------
// Fork off a thread for your server and call setDaemon(true) on the thread.
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.16
//----------------------------------------------------------------------------------
// Consider using special packages designed to provide robust startup/shutdown
// capability, e.g.: http://jakarta.apache.org/commons/daemon/
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.17
//----------------------------------------------------------------------------------
// Alternative to cookbook as proposed inetd solution is not cross platform.
host = 'localhost'
for (port in 1..1024) {
    try {
        s = new Socket(host, port)
        println("There is a server on port $port of $host")
    }
    catch (Exception ex) {}
}
// You could open a ServerSocket() on each unused port and monitor those.
//----------------------------------------------------------------------------------


// @@PLEAC@@_17.18
//----------------------------------------------------------------------------------
// It's not too hard to write a TCP Proxy in Groovy but numerous Java packages
// already exist, so we might as well use one of those:
// http://ws.apache.org/axis/java/user-guide.html#AppendixUsingTheAxisTCPMonitorTcpmon
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.1
//----------------------------------------------------------------------------------
name = 'www.perl.com'
addresses = InetAddress.getAllByName(name)
println addresses // => {www.perl.com/208.201.239.36, www.perl.com/208.201.239.37}
// or to just resolve one:
println InetAddress.getByName(name) // => www.perl.com/208.201.239.36
// try a different address
name = 'groovy.codehaus.org'
addresses = InetAddress.getAllByName(name)
println addresses // => {groovy.codehaus.org/63.246.7.187}
// starting with IP address
address = InetAddress.getByAddress([208, 201, 239, 36] as byte[])
println address.hostName // => www.oreillynet.com

// For more complex operations use dnsjava: http://www.dnsjava.org/
import org.xbill.DNS.*
System.setProperty("sun.net.spi.nameservice.provider.1","dns,dnsjava")
Lookup lookup = new Lookup('cnn.com', Type.ANY)
records = lookup.run()
println "${records?.size()} record(s) found"
records.each{ println it }
// =>
// 17 record(s) found
// cnn.com.     55  IN  A   64.236.16.20
// cnn.com.     55  IN  A   64.236.16.52
// cnn.com.     55  IN  A   64.236.16.84
// cnn.com.     55  IN  A   64.236.16.116
// cnn.com.     55  IN  A   64.236.24.12
// cnn.com.     55  IN  A   64.236.24.20
// cnn.com.     55  IN  A   64.236.24.28
// cnn.com.     55  IN  A   64.236.29.120
// cnn.com.     324 IN  NS  twdns-02.ns.aol.com.
// cnn.com.     324 IN  NS  twdns-03.ns.aol.com.
// cnn.com.     324 IN  NS  twdns-04.ns.aol.com.
// cnn.com.     324 IN  NS  twdns-01.ns.aol.com.
// cnn.com.     3324    IN  SOA twdns-01.ns.aol.com. hostmaster.tbsnames.turner.com. 2007011203 900 300 604801 900
// cnn.com.     3324    IN  MX  10 atlmail3.turner.com.
// cnn.com.     3324    IN  MX  10 atlmail5.turner.com.
// cnn.com.     3324    IN  MX  20 nycmail2.turner.com.
// cnn.com.     3324    IN  MX  30 nycmail1.turner.com.

// faster reverse lookup using dnsjava
def reverseDns(hostIp) {
    name = ReverseMap.fromAddress(hostIp)
    rec = Record.newRecord(name, Type.PTR, DClass.IN)
    query = Message.newQuery(rec)
    response = new ExtendedResolver().send(query)
    answers = response.getSectionArray(Section.ANSWER)
    if (answers) return answers[0].rdataToString() else return hostIp
}
println '208.201.239.36 => ' + reverseDns('208.201.239.36')
// => 208.201.239.36 => www.oreillynet.com.

def hostAddrs(name) {
    addresses = Address.getAllByName(name)
    println addresses[0].canonicalHostName + ' => ' + addresses.collect{ it.hostAddress }.join(' ')
}
hostAddrs('www.ora.com')
// => www.oreillynet.com. => 208.201.239.36 208.201.239.37
hostAddrs('www.whitehouse.gov')
// => 61.9.209.153 => 61.9.209.153 61.9.209.151
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.2
//----------------------------------------------------------------------------------
// commons net examples (explicit error handling not shown)
import java.text.DateFormat
import org.apache.commons.net.ftp.FTPClient
// connect
server = "localhost"                   //server = "ftp.host.com"

ftp = new FTPClient()
ftp.connect( server )
ftp.login( 'anonymous', 'guest' )     //ftp.login( 'username', 'password' )

println "Connected to $server. $ftp.replyString"

// retrieve file
ftp.changeWorkingDirectory( '.' )  //ftp.changeWorkingDirectory( 'serverFolder' )
file = new File('README.txt') //new File('localFolder' + File.separator + 'localFilename')

file.withOutputStream{ os ->
    ftp.retrieveFile( 'README.txt', os )  //ftp.retrieveFile( 'serverFilename', os )
}

// upload file
file = new File('otherFile.txt') //new File('localFolder' + File.separator + 'localFilename')
file.withInputStream{ fis -> ftp.storeFile( 'otherFile.txt', fis ) }

// List the files in the directory
files = ftp.listFiles()
println "Number of files in dir: $files.length"
df = DateFormat.getDateInstance( DateFormat.SHORT )
files.each{ file ->
    println "${df.format(file.timestamp.time)}\t $file.name"
}

// Logout from the FTP Server and disconnect
ftp.logout()
ftp.disconnect()
// =>
// Connected to localhost. 230 User logged in, proceed.
// Number of files in dir: 2
// 18/01/07  otherFile.txt
// 25/04/06  README.txt


// Using AntBuilder; for more details, see:
// http://ant.apache.org/manual/OptionalTasks/ftp.html
ant = new AntBuilder()
ant.ftp(action:'send', server:'ftp.hypothetical.india.org', port:'2121',
        remotedir:'/pub/incoming', userid:'coder', password:'java1',
        depends:'yes', binary:'no', systemTypeKey:'Windows',
        serverTimeZoneConfig:'India/Calcutta'){
    fileset(dir:'htdocs/manual'){
        include(name:'**/*.html')
    }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.3
//----------------------------------------------------------------------------------
// using AntBuilder; for more info, see:
// http://ant.apache.org/manual/CoreTasks/mail.html
ant = new AntBuilder()
ant.mail(mailhost:'smtp.myisp.com', mailport:'1025', subject:'Test build'){
  from(address:'config@myisp.com')
  replyto(address:'me@myisp.com')
  to(address:'all@xyz.com')
  message("The ${buildname} nightly build has completed")
  attachments(){ // ant 1.7 uses files attribute in earlier versions
    fileset(dir:'dist'){
      include(name:'**/*.zip')
    }
  }
}

// using commons net
//import org.apache.commons.net.smtp.*
class SMTPClient{}
class SMTPReply{}
class SimpleSMTPHeader{}
client = new SMTPClient()
client.connect( "mail.myserver.com", 25 )
if( !SMTPReply.isPositiveCompletion(client.replyCode) ) {
    client.disconnect()
    System.err.println("SMTP server refused connection.")
    System.exit(1)
}

// Login
client.login( "myserver.com" )

// Set the sender and recipient(s)
client.setSender( "config@myisp.com" )
client.addRecipient( "all@xyz.com" )

// Use the SimpleSMTPHeader class to build the header
writer = new PrintWriter( client.sendMessageData() )
header = new SimpleSMTPHeader( "config@myisp.com", "all@xyz.com", "My Subject")
header.addCC( "me@myisp.com" )
header.addHeaderField( "Organization", "My Company" )

// Write the header to the SMTP Server
writer.write( header.toString() )

// Write the body of the message
writer.write( "This is a test..." )

// Close the writer 
writer.close()
if ( !client.completePendingCommand() ) // failure
    System.exit( 1 )

// Logout from the e-mail server (QUIT) and close connection
client.logout()
client.disconnect()

// You can also use JavaMail; for more details, see:
// http://java.sun.com/products/javamail/

// For testing programs which send emails, consider:
// Dumbster (http://quintanasoft.com/dumbster/)
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.4
//----------------------------------------------------------------------------------
// slight variation to original cookbook:
// prints 1st, 2nd and last articles from random newsgroup
//import org.apache.commons.net.nntp.NNTPClient
class NNTPClient{}
postingPerm = ['Unknown', 'Moderated', 'Permitted', 'Prohibited']
client = new NNTPClient()
client.connect("news.example.com")
list = client.listNewsgroups()
println "Found ${list.size()} newsgroups"
aList = list[new Random().nextInt(list.size())]
println "$aList.newsgroup has $aList.articleCount articles"
println "PostingPermission = ${postingPerm[aList.postingPermission]}"
first = aList.firstArticle
println "First=$first, Last=$aList.lastArticle"
client.retrieveArticle(first)?.eachLine{ println it }
client.selectNextArticle()
client.retrieveArticle()?.eachLine{ println it }
client.retrieveArticle(aList.lastArticle)?.eachLine{ println it }
writer = client.postArticle()
// ... use writer ...
writer.close()
client.logout()
if (client.isConnected()) client.disconnect()
// =>
// Found 37025 newsgroups
// alt.comp.sys.palmtops.pilot has 730 articles
// PostingPermission = Permitted
// First=21904, Last=22633
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.5
//----------------------------------------------------------------------------------
// slight variation to original cookbook to print summary of messages on server
// uses commons net
//import org.apache.commons.net.pop3.POP3Client
class POP3Client{}
server = 'pop.myisp.com'
username = 'gnat'
password = 'S33kr1T Pa55w0rD'
timeoutMillis = 30000

def printMessageInfo(reader, id) {
    def from, subject
    reader.eachLine{ line ->
        lower = line.toLowerCase()
        if (lower.startsWith("from: ")) from = line[6..-1].trim()
        else if (lower.startsWith("subject: ")) subject = line[9..-1].trim()
    }
    println "$id From: $from, Subject: $subject"
}

pop3 = new POP3Client()
pop3.setDefaultTimeout(timeoutMillis)
pop3.connect(server)

if (!pop3.login(username, password)) {
    System.err.println("Could not login to server.  Check password.")
    pop3.disconnect()
    System.exit(1)
}
messages = pop3.listMessages()
if (!messages) System.err.println("Could not retrieve message list.")
else if (messages.length == 0) println("No messages")
else {
    messages.each{ message ->
        reader = pop3.retrieveMessageTop(message.number, 0)
        if (!reader) {
            System.err.println("Could not retrieve message header. Skipping...")
        }
        printMessageInfo(new BufferedReader(reader), message.number)
    }
}

pop3.logout()
pop3.disconnect()

// You can also use JavaMail; for more details, see:
// http://java.sun.com/products/javamail/
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.6
//----------------------------------------------------------------------------------
// Variation to original cookbook: this more extensive example
// uses telnet to extract weather information about Sydney from
// a telnet-based weather server at the University of Michigan.
import org.apache.commons.net.telnet.TelnetClient

def readUntil( pattern ) {
    sb = new StringBuffer()
    while ((ch = reader.read()) != -1) {
        sb << (char) ch
        if (sb.toString().endsWith(pattern)) {
            def found = sb.toString()
            sb = new StringBuffer()
            return found
        }
    }
    return null
}

telnet = new TelnetClient()
telnet.connect( 'rainmaker.wunderground.com', 3000 )
reader = telnet.inputStream.newReader()
writer = new PrintWriter(new OutputStreamWriter(telnet.outputStream),true)
readUntil( "Welcome" )
println 'Welcome' + readUntil( "!" )
readUntil( "continue:" )
writer.println()
readUntil( "-- " )
writer.println()
readUntil( "Selection:" )
writer.println("10")
readUntil( "Selection:" )
writer.println("3")
x = readUntil( "Return" )
while (!x.contains('SYDNEY')) {
    writer.println()
    x = readUntil( "Return" )
}
m = (x =~ /(?sm).*(SYDNEY.*?)$/)
telnet.disconnect()
println m[0][1]
// =>
// Welcome to THE WEATHER UNDERGROUND telnet service!
// SYDNEY           FAIR      10AM   81  27
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.7
//----------------------------------------------------------------------------------
address = InetAddress.getByName("web.mit.edu")
timeoutMillis = 3000
println address.isReachable(timeoutMillis)
// => true (if firewalls don't get in the way, may require privileges on Linux,
//          may not use ICMP but rather Echo protocol on Windows machines)

// You can also use commons net EchoUDPClient and EchoTCPClient to interact
// with the Echo protocol - sometimes useful for ping-like functionality.
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.8
//----------------------------------------------------------------------------------
//import org.apache.commons.net.WhoisClient
class WhoisClient{}
whois = new WhoisClient()
whois.connect(WhoisClient.DEFAULT_HOST)
result = whois.query('cnn.com') // as text of complete query
println result // could extract info from result here (using e.g. regex)
whois.disconnect()
//----------------------------------------------------------------------------------


// @@PLEAC@@_18.9
//----------------------------------------------------------------------------------
// not exact equivalent to original cookbook: just shows raw functionality
client = new SMTPClient()
client.connect( "smtp.example.com", 25 )
println client.verify("george") // => true
println client.replyString // => 250 George Washington <george@wash.dc.gov>
println client.verify("jetson") // => false
println client.replyString // => 550 jetson... User unknown
client.expn("presidents")
println client.replyString
// =>
// 250-George Washington <george@wash.dc.gov>
// 250-Thomas Jefferson <tj@wash.dc.gov>
// 250-Ben Franklin <ben@here.us.edu>
// ...

// expect these commands to be disabled by most public servers due to spam
println client.replyString
// => 502 Command is locally disabled
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.0
//----------------------------------------------------------------------------------
// URLs have the same form as in Perl

// Invoking dynamic content is done through the same standard urls:
// http://mox.perl.com/cgi-bin/program?name=Johann&born=1685
// http://mox.perl.com/cgi-bin/program

// Groovy has Groovelets and GSP page support built-in. For a full
// web framework, see Grails: http://grails.codehaus.org/
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.1
//----------------------------------------------------------------------------------
// as a plain groovelet
param = request.getParameter('PARAM_NAME')
println """
<html><head>
<title>Howdy there!</title>
</head>
<body>
<p>
You typed: $param
</p>
</body>
</html>
"""

// as a groovelet using markup builder
import groovy.xml.MarkupBuilder
writer = new StringWriter()
builder = new MarkupBuilder(writer)
builder.html {
    head {
        title 'Howdy there!'
    }
    body {
        p('You typed: ' + request.getParameter('PARAM_NAME'))
    }
}
println writer.toString()

        /*
// as a GSP page:
<html><head>
<title>Howdy there!</title>
</head>
<body>
<p>
You typed: ${request.getParameter('PARAM_NAME')}
</p>
</body>
</html>
*/
// Request parameters are often encoded by the browser before
// sending to the server and usually can be printed out as is.
// If you need to convert, use commons lang StringEscapeUtils#escapeHtml()
// and StringEscapeUtils#unescapeHtml().

// Getting parameters:
who = request.getParameter('Name')
phone = request.getParameter('Number')
picks = request.getParameterValues('Choices') // String array or null

// Changing headers:
response.setContentType('text/html;charset=UTF-8')
response.setContentType('text/plain')
response.setContentType('text/plain')
response.setHeader('Cache-control', 'no-cache')
response.setDateHeader('Expires', System.currentTimeMillis() + 3*24*60*60*1000)
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.2
//----------------------------------------------------------------------------------
// The Java Servlet API has a special log() method for writing to the
// web server log.

// To send errors to custom HTML pages, update the web.xml deployment
// descriptor to include one or more <error-page> elements, e.g.:
        /*
<error-page>
    <error-code>404</error-code>
    <location>/404.html</location>
</error-page>
<error-page>
    <exception-type>java.lang.NullPointerException</exception-type>
    <location>/NpeError.gsp</location>
</error-page>
*/
// Another trick is to catch an exception within the servlet/gsp code
// and print it out into the HTML as a comment.
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.3
//----------------------------------------------------------------------------------
// 500 errors could occur if you have compile errors in your script.
// Pre-compile with your IDE or groovyc.

// You can use an expando, mock or map to run your scripts outside
// the web container environment. If you use Jetty as your container
// it has a special servlet tester, for more details:
// http://blogs.webtide.com/gregw/2006/12/16/1166307599250.html
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.4
//----------------------------------------------------------------------------------
// Web servers should be invoked with an appropriate Java security policy in place.
// This can be used to limit possible actions from hacking attempts.

// Normal practices limit hacking exposure. The JDBC API encourages the use
// of Prepared queries rather than encouraging practices which lead to SQL
// injection. Using system or exec is rarely used either as Java provides
// cross-platform mechanisms for most operating system level functionality.

// Other security measures should be complemented with SSL and authentication.
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.5
//----------------------------------------------------------------------------------
// Within the servlet element of your web.xml, there is a <load-on-startup> element.
// Use that on a per servlet basis to pre-load whichever servlets you like.
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.6
//----------------------------------------------------------------------------------
// As discussed in 19.3 and 19.4:
// Web servers should be invoked with an appropriate Java security policy in place.
// This can be used to limit possible actions from hacking attempts.

// Normal practices limit hacking exposure. The JDBC API encourages the use
// of Prepared queries rather than encouraging practices which lead to SQL
// injection. Using system or exec is rarely used either as Java provides
// cross-platform mechanisms for most operating system level functionality.

// In addition, if authentication is used, security can be locked down at a
// very fine-grained level on a per servlet action or per user (with JAAS) basis.
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.7
//----------------------------------------------------------------------------------
import groovy.xml.*
// using a builder:
Closure markup = {
    ol {
        ['red','blue','green'].each{ li(it) }
    }
}
println new StreamingMarkupBuilder().bind(markup).toString()
// => <ol><li>red</li><li>blue</li><li>green</li></ol>

names = 'Larry Moe Curly'.split(' ')
markup = {
    ul {
        names.each{ li(type:'disc', it) }
    }
}
println new StreamingMarkupBuilder().bind(markup).toString()
// <ul><li type="disc">Larry</li><li type="disc">Moe</li>
//     <li type="disc">Curly</li></ul>
//-----------------------------

m = { li("alpha") }
println new StreamingMarkupBuilder().bind(m).toString()
//     <li>alpha</li>

m = { ['alpha','omega'].each { li(it) } }
println new StreamingMarkupBuilder().bind(m).toString()
//     <li>alpha</li> <li>omega</li>
//-----------------------------

states = [
    "Wisconsin":  [ "Superior", "Lake Geneva", "Madison" ],
    "Colorado":   [ "Denver", "Fort Collins", "Boulder" ],
    "Texas":      [ "Plano", "Austin", "Fort Stockton" ],
    "California": [ "Sebastopol", "Santa Rosa", "Berkeley" ],
]

writer = new StringWriter()
builder = new MarkupBuilder(writer)
builder.table{
    caption('Cities I Have Known')
    tr{ th('State'); th(colspan:3, 'Cities') }
    states.keySet().sort().each{ state ->
        tr{
            th(state)
            states[state].sort().each{ td(it) }
        }
    }
}
println writer.toString()
// =>
// <table>
//   <caption>Cities I Have Known</caption>
//   <tr>
//     <th>State</th>
//     <th colspan='3'>Cities</th>
//   </tr>
//   <tr>
//     <th>California</th>
//     <td>Berkeley</td>
//     <td>Santa Rosa</td>
//     <td>Sebastopol</td>
//   </tr>
//   <tr>
//     <th>Colorado</th>
//     <td>Boulder</td>
//     <td>Denver</td>
//     <td>Fort Collins</td>
//   </tr>
//   <tr>
//     <th>Texas</th>
//     <td>Austin</td>
//     <td>Fort Stockton</td>
//     <td>Plano</td>
//   </tr>
//   <tr>
//     <th>Wisconsin</th>
//     <td>Lake Geneva</td>
//     <td>Madison</td>
//     <td>Superior</td>
//   </tr>
// </table>

import groovy.sql.Sql
import groovy.xml.MarkupBuilder

dbHandle = null
dbUrl = 'jdbc:hsqldb:...'
def getDb(){
    if (dbHandle) return dbHandle
//    def source = new org.hsqldb.jdbc.jdbcDataSource()
    def source = new jdbcDataSource()
    source.database = dbUrl
    source.user = 'sa'
    source.password = ''
    dbHandle = new Sql(source)
    return dbHandle
}

def findByLimit(limit) {
    db.rows "SELECT name,salary FROM employees where salary > $limit"
}

limit = request.getParameter('LIMIT')
writer = new StringWriter()
builder = new MarkupBuilder(writer)
builder.html {
    head { title('Salary Query') }
    h1('Search')
    form{
        p('Enter minimum salary')
        input(type:'text', name:'LIMIT')
        input(type:'submit')
    }
    if (limit) {
        h1('Results')
        table(border:1){
            findByLimit(limit).each{ row ->
                tr{ td(row.name); td(row.salary) }
            }
        }
    }
}
println writer.toString()
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.8
//----------------------------------------------------------------------------------
// The preferred way to redirect to resources within the web application:
dispatcher = request.getRequestDispatcher('hello.gsp')
dispatcher.forward(request, response)
// Old versions of web containers allowed this mechanism to also redirect
// to external resources but this was deemed a potential security risk.

// The suggested way to external sites (less efficient for internal resources):
response.sendRedirect("http://www.perl.com/CPAN/")

// set cookie and forward
oreo = new Cookie('filling', 'vanilla creme')
THREE_MONTHS = 3 * 30 * 24 * 60 * 60
oreo.maxAge = THREE_MONTHS
oreo.domain = '.pleac.sourceforge.net'
whither = 'http://pleac.sourceforge.net/pleac_ruby/cgiprogramming.html'
response.addCookie(oreo)
response.sendRedirect(whither)

// forward based on user agent
dir = 'http://www.science.uva.nl/%7Emes/jargon'
agent = request.getHeader('user-agent')
menu = [
    [/Mac/, 'm/macintrash.html'],
    [/Win(dows )?NT/, 'e/evilandrude.html'],
    [/Win|MSIE|WebTV/, 'm/microslothwindows.html'],
    [/Linux/, 'l/linux.html'],
    [/HP-UX/, 'h/hpsux.html'],
    [/SunOS/, 's/scumos.html'],
]
page = 'a/aportraitofj.randomhacker.html'
menu.each{
    if (agent =~ it[0]) page = it[1]
}
response.sendRedirect("$dir/$page")

// no response output
response.sendError(204, 'No Response')
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.9
//----------------------------------------------------------------------------------
// Consider TCPMON or similar: http://ws.apache.org/commons/tcpmon/
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.10
//----------------------------------------------------------------------------------
// helper method
//import javax.servlet.http.Cookie
class Cookie{}
import groovy.xml.MarkupBuilder

def getCookieValue(cookies, cookieName, defaultValue) {
    if (cookies) for (i in 0..<cookies.length) {
        if (cookieName == cookies[i].name) return cookies[i].value
    }
    return defaultValue
}

prefValue = getCookieValue(request.cookies, 'preference_name', 'default')
cookie = new Cookie('preference name',"whatever you'd like")
SECONDS_PER_YEAR = 60*60*24*365
cookie.maxAge = SECONDS_PER_YEAR * 2
response.addCookie(cookie)

cookname = 'fav_ice_cream'
favorite = request.getParameter('flavor')
tasty    = getCookieValue(request.cookies, cookname, 'mint')

writer = new StringWriter()
builder = new MarkupBuilder(writer)
builder.html {
    head { title('Ice Cookies') }
    body {
        h1('Hello Ice Cream')
        if (favorite) {
            p("You chose as your favorite flavor '$favorite'.")
            cookie = new Cookie(cookname, favorite)
            ONE_HOUR = 3600 // secs
            cookie.maxAge = ONE_HOUR
            response.addCookie(cookie)
        } else {
            hr()
            form {
                p('Please select a flavor: ')
                input(type:'text', name:'flavor', value:tasty)
            }
            hr()
        }
    }
}
println writer.toString()
//----------------------------------------------------------------------------------

// @@PLEAC@@_19.11
//----------------------------------------------------------------------------------
import groovy.xml.MarkupBuilder
// On Linux systems replace with: "who".execute().text
fakedWhoInput = '''
root tty1 Nov 2 17:57
hermie tty3 Nov 2 18:43
hermie tty4 Nov 1 20:01
sigmund tty2 Nov 2 18:08
'''.trim().split(/\n/)
name = request.getParameter('WHO')
if (!name) name = ''
writer = new StringWriter()
new MarkupBuilder(writer).html{
    head{ title('Query Users') }
    body{
        h1('Search')
        form{
            p('Which User?')
            input(type:'text', name:'WHO', value:name)
            input(type:'submit')
        }
        if (name) {
            h1('Results')
            lines = fakedWhoInput.grep(~/^$name\s.*/)
            if (lines) message = lines.join('\n')
            else message = "$name is not logged in"
            pre(message)
        }
    }
}
println writer.toString()
// if you need to escape special symbols, e.g. '<' or '>' use commons lang StringEscapeUtils
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.12
//----------------------------------------------------------------------------------
// frameworks typically do this for you, but shown here are the manual steps
// even when doing it manually, you would probably use session variables

// setting a hidden field
input(type:'hidden', value:'bacon')

// setting a value on the submit
input(type:'submit', name:".State", value:'Checkout')

// determining 'mode'
page = request.getParameter('.State')
if (!page) page = 'Default'

// forking with if chain
if (page == "Default") {
    frontPage()
} else if (page == "Checkout") {
    checkout()
} else {
    noSuchPage()
}

// forking with map
states = [
    Default:  this.&frontPage,
    Shirt:    this.&tShirt,
    Sweater:  this.&sweater,
    Checkout: this.&checkout,
    Card:     this.&creditCard,
    Order:    this.&order,
    Cancel:   this.&frontPage,
]

// calling each to allow hidden variable saving
states.each{ key, closure ->
    closure(page == key)
}

// exemplar method
def tShirt(active) {
    def sizes = ['XL', 'L', 'M', 'S']
    def colors = ['Black', 'White']
    if (!active) {
        hidden("size")
        hidden("color")
        return
    }
    p("You want to buy a t-shirt?");
    label("Size:  ");     dropDown("size", sizes)
    label("Color: ");     dropDown("color", colors)
    shopMenu()
}

// kicking off processing
html{
    head{ title('chemiserie store') }
    body {
        if (states[page]) process(page)
        else noSuchPage()
    }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.13
//----------------------------------------------------------------------------------
// get request parameters as map
map = request.parameterMap

// save to file
new File(filename).withOutputStream{ fos ->
    oos = new ObjectOutputStream(fos)
    oos.writeObject(map)
    oos.close()
}

// convert to text
sb = new StringBuffer()
map.each{ k,v -> sb << "$k=$v" }
text = sb.toString()
// to send text via email, see 18.3
//----------------------------------------------------------------------------------


// @@PLEAC@@_19.14
//----------------------------------------------------------------------------------
// you wouldn't normally do it this way, consider a framework like Grails
// even when doing it by hand, you would probably use session variables
import groovy.xml.MarkupBuilder

page = param('.State', 'Default')

states = [
    Default:  this.&frontPage,
    Shirt:    this.&shirt,
    Sweater:  this.&sweater,
    Checkout: this.&checkout,
    Card:     this.&creditCard,
    Order:    this.&order,
    Cancel:   this.&frontPage,
]

writer = new StringWriter()
b = new MarkupBuilder(writer)
b.html{
    head{ title('chemiserie store') }
    body {
        if (states[page]) process(page)
        else noSuchPage()
    }
}
println writer.toString()

def process(page) {
    b.form{
        states.each{ key, closure ->
            closure(page == key)
        }
    }
}

def noSuchPage() {
    b.p('Unknown request')
    reset('Click here to start over')
}

def shopMenu() {
    b.p()
    toPage("Shirt")
    toPage("Sweater")
    toPage("Checkout")
    reset('Empty My Shopping Cart')
}

def frontPage(active) {
    if (!active) return
    b.h1('Hi!')
    b.p('Welcome to our Shirt Shop! Please make your selection from the menu below.')
    shopMenu()
}

def shirt(active) {
    def sizes = ['XL', 'L', 'M', 'S']
    def colors = ['Black', 'White']
    def count = param('shirt_count',0)
    def color = param('shirt_color')
    def size = param('shirt_size')
    // sanity check
    if (count) {
        if (!(color in colors)) color = colors[0]
        if (!(size in sizes)) size = sizes[0]
    }
    if (!active) {
        if (size) hidden("shirt_size", size)
        if (color) hidden("shirt_color", color)
        if (count) hidden("shirt_count", count)
        return
    }
    b.h1 'T-Shirt'
    b.p '''What a shirt! This baby is decked out with all the options.
        It comes with full luxury interior, cotton trim, and a collar
        to make your eyes water! Unit price: $33.00'''
    b.h2 'Options'
    label("How Many?");  textfield("shirt_count")
    label("Size?");      dropDown("shirt_size", sizes)
    label("Color?");     dropDown("shirt_color", colors)
    shopMenu()
}

def sweater(active) {
    def sizes = ['XL', 'L', 'M']
    def colors = ['Chartreuse', 'Puce', 'Lavender']
    def count = param('sweater_count',0)
    def color = param('sweater_color')
    def size = param('sweater_size')
    // sanity check
    if (count) {
        if (!(color in colors)) color = colors[0]
        if (!(size in sizes)) size = sizes[0]
    }
    if (!active) {
        if (size) hidden("sweater_size", size)
        if (color) hidden("sweater_color", color)
        if (count) hidden("sweater_count", count)
        return
    }
    b.h1("Sweater")
    b.p("Nothing implies preppy elegance more than this fine " +
        "sweater. Made by peasant workers from black market silk, " +
        "it slides onto your lean form and cries out ``Take me, " +
        "for I am a god!''. Unit price: \$49.99.")
    b.h2("Options")
    label("How Many?"); textfield("sweater_count")
    label("Size?"); dropDown("sweater_size", sizes)
    label("Color?"); dropDown("sweater_color", colors)
    shopMenu()
}

def checkout(active) {
    if (!active) return
    b.h1("Order Confirmation")
    b.p("You ordered the following:")
    orderText()
    b.p("Is this right? Select 'Card' to pay for the items" +
        "or 'Shirt' or 'Sweater' to continue shopping.")
    toPage("Card")
    toPage("Shirt")
    toPage("Sweater")
}

def creditCard(active) {
    def widgets = 'Name Address1 Address2 City Zip State Phone Card Expiry'.split(' ')
    if (!active) {
        widgets.each{ hidden(it) }
        return
    }
    b.pre{
        label("Name: ");          textfield("Name")
        label("Address: ");       textfield("Address1")
        label(" ");               textfield("Address2")
        label("City: ");          textfield("City")
        label("Zip: ");           textfield("Zip")
        label("State: ");         textfield("State")
        label("Phone: ");         textfield("Phone")
        label("Credit Card #: "); textfield("Card")
        label("Expiry: ");        textfield("Expiry")
    }
    b.p("Click on 'Order' to order the items. Click on 'Cancel' to return shopping.")
    toPage("Order")
    toPage("Cancel")
}

def order(active) {
    if (!active) return
    b.h1("Ordered!")
    b.p("You have ordered the following items:")
    orderText()
    reset('Begin Again')
}

def orderText() {
    def shirts = param('shirt_count')
    def sweaters = param('sweater_count')
    if (shirts) {
        b.p("""You have ordered ${param('shirt_count')}
            shirts of size ${param('shirt_size')}
            and color ${param("shirt_color")}.""")
    }
    if (sweaters) {
        b.p("""You have ordered ${param('sweater_count')}
        sweaters of size ${param('sweater_size')}
        and color ${param('sweater_color')}.""")
    }
    if (!sweaters && !shirts) b.p("Nothing!")
    b.p("For a total cost of ${calcPrice()}")
}

def label(text) { b.span(text) }
def reset(text) { b.a(href:request.requestURI,text) }
def toPage(name) { b.input(type:'submit', name:'.State', value:name) }
def dropDown(name, values) {
    b.select(name:name){
        values.each{
            if (param(name)==it) option(value:it, selected:true, it)
            else option(value:it, it)
        }
    }
    b.br()
}
def hidden(name) {
    if (binding.variables.containsKey(name)) v = binding[name]
    else v = ''
    hidden(name, v)
}
def hidden(name, value) { b.input(type:'hidden', name:name, value:value) }
def textfield(name) { b.input(type:'text', name:name, value:param(name,'')); b.br() }
def param(name) { request.getParameter(name) }
def param(name, defValue) {
    def val = request.getParameter(name)
    if (val) return val else return defValue
}

def calcPrice() {
    def shirts = param('shirt_count', 0).toInteger()
    def sweaters = param('sweater_count', 0).toInteger()
    return (shirts * 33 + sweaters * 49.99).toString()
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.0
//----------------------------------------------------------------------------------
// Many packages are available for simulating a browser. A good starting point:
// http://groovy.codehaus.org/Testing+Web+Applications
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.1
//----------------------------------------------------------------------------------
// for non-binary content
urlStr = 'http://groovy.codehaus.org'
content = new URL(urlStr).text
println content.size() // => 34824

// for binary content
urlStr = 'http://groovy.codehaus.org/download/attachments/1871/gina_3d.gif'
bytes = new ByteArrayOutputStream()
bytes << new URL(urlStr).openStream()
println bytes.size() // => 6066

// various forms of potential error checking
try {
    new URL('x:y:z')
} catch (MalformedURLException ex) {
    println ex.message // => unknown protocol: x
}
try {
    new URL('cnn.com/not.there')
} catch (MalformedURLException ex) {
    println ex.message // => no protocol: cnn.com/not.there
}
try {
    content = new URL('http://cnn.com/not.there').text
} catch (FileNotFoundException ex) {
    println "Couldn't find: " + ex.message
    // => Couldn't find: http://www.cnn.com/not.there
}

// titleBytes example
def titleBytes(urlStr) {
    def lineCount = 0; def byteCount = 0
    new URL(urlStr).eachLine{ line ->
        lineCount++; byteCount += line.size()
    }
    println "$urlStr => ($lineCount lines, $byteCount bytes)"
}
titleBytes('http://www.tpj.com/')
// http://www.tpj.com/ => (677 lines, 25503 bytes)
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.2
//----------------------------------------------------------------------------------
// using HtmlUnit (htmlunit.sf.net)
import com.gargoylesoftware.htmlunit.WebClient

def webClient = new WebClient()
def page = webClient.getPage('http://search.cpan.org/')
// check page title
assert page.titleText.startsWith('The CPAN Search Site')
// fill in form and submit it
def form = page.getFormByName('f')
def field = form.getInputByName('query')
field.setValueAttribute('DB_File')
def button = form.getInputByValue('CPAN Search')
def result = button.click()
// check search result has at least one link ending in DB_File.pm
assert result.anchors.any{ a -> a.hrefAttribute.endsWith('DB_File.pm') }

// fields must be properly escaped
println URLEncoder.encode(/"this isn't <EASY>&<FUN>"/, 'utf-8')
// => %22this+isn%27t+%3CEASY%3E%26%3CFUN%3E%22

// proxies can be taken from environment, or specified
//System.properties.putAll( ["http.proxyHost":"proxy-host", "http.proxyPort":"proxy-port",
//    "http.proxyUserName":"user-name", "http.proxyPassword":"proxy-passwd"] )
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.3
//----------------------------------------------------------------------------------
// using HtmlUnit (htmlunit.sf.net)
import com.gargoylesoftware.htmlunit.WebClient

client = new WebClient()
html = client.getPage('http://www.perl.com/CPAN/')
println page.anchors.collect{ it.hrefAttribute }.sort().unique().join('\n')
// =>
// disclaimer.html
// http://bookmarks.cpan.org/
// http://faq.perl.org/
// mailto:cpan@perl.org
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.4
//----------------------------------------------------------------------------------
// split paragraphs
LS = System.properties.'line.separator'
new File(args[0]).text.split("$LS$LS").each{ para ->
    if (para.startsWith(" ")) println "<pre>\n$para\n</pre>"
    else {
        para = para.replaceAll(/(?m)^(>.*?)$/, /$1<br \/>/)            // quoted text
        para = para.replaceAll(/<URL:(.*)>/, /<a href="$1">$1<\/a>/)   // embedded URL
        para = para.replaceAll(/(http:\S+)/, /<a href="$1">$1<\/a>/)   // guessed URL
        para = para.replaceAll('\\*(\\S+)\\*', /<strong>$1<\/strong>/) // this is *bold* here
        para = para.replaceAll(/\b_(\S+)_\b/, /<em>$1<\/em>/)          // this is _italic_ here
        println "<p>\n$para\n</p>"                                     // add paragraph tags
    }
}

def encodeEmail(email) {
    println "<table>"
    email = URLEncoder.encode(email)
    email = text.replaceAll(/(\n[ \t]+)/, / . /)   // continuation lines
    email = text.replaceAll(/(?m)^(\S+?:)\s*(.*?)$/,
                  /<tr><th align="left">$1<\/th><td>$2<\/td><\/tr>/);
    println email
    println "</table>"
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.5
//----------------------------------------------------------------------------------
// using CyberNeko Parser (people.apache.org/~andyc/neko/doc)
class XmlParser{}
parser = new org.cyberneko.html.parsers.SAXParser()
parser.setFeature('http://xml.org/sax/features/namespaces', false)
page = new XmlParser(parser).parse('http://www.perl.com/CPAN/')
page.depthFirst().each{ println it.text() }
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.6
//----------------------------------------------------------------------------------
// removing tags, see 20.5

// extracting tags: htitle using cyberneko and XmlSlurper
parser = new org.cyberneko.html.parsers.SAXParser()
parser.setFeature('http://xml.org/sax/features/namespaces', false)
page = new XmlParser(parser).parse('http://www.perl.com/CPAN/')
println page.HEAD.TITLE[0].text()

// extracting tags: htitle using HtmlUnit
client = new WebClient()
html = client.getPage('http://www.perl.com/CPAN/')
println html.titleText
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.7
//----------------------------------------------------------------------------------
//import com.gargoylesoftware.htmlunit.WebClient
class WebClient{}

client = new WebClient()
page = client.getPage('http://www.perl.com/CPAN/')
page.anchors.each{
    checkUrl(page, it.hrefAttribute)
}

def checkUrl(page, url) {
    try {
        print "$url "
        qurl = page.getFullyQualifiedUrl(url)
        client.getPage(qurl)
        println 'OK'
    } catch (Exception ex) {
        println 'BAD'
    }
}
// =>
// modules/index.html OK
// RECENT.html OK
// http://search.cpan.org/recent OK
// http://mirrors.cpan.org/ OK
// http://perldoc.perl.org/ OK
// mailto:cpan@perl.org BAD
// http://www.csc.fi/suomi/funet/verkko.html.en/ BAD
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.8
//----------------------------------------------------------------------------------
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.HeadMethod
import java.text.DateFormat

urls = [
    "http://www.apache.org/",
    "http://www.perl.org/",
    "http://www.python.org/",
    "http://www.ora.com/",
    "http://jakarta.apache.org/",
    "http://www.w3.org/"
]

df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM)
client = new HttpClient()
urlInfo = [:]
urls.each{ url ->
    head = new HeadMethod(url)
    client.executeMethod(head)
    lastModified = head.getResponseHeader("last-modified")?.value
    urlInfo[df.parse(lastModified)]=url
}

urlInfo.keySet().sort().each{ key ->
    println "$key ${urlInfo[key]}"
}
// =>
// Sun Jan 07 21:48:15 EST 2007 http://www.apache.org/
// Sat Jan 13 12:44:32 EST 2007 http://jakarta.apache.org/
// Fri Jan 19 14:50:13 EST 2007 http://www.w3.org/
// Fri Jan 19 19:28:35 EST 2007 http://www.python.org/
// Sat Jan 20 09:36:08 EST 2007 http://www.ora.com/
// Sat Jan 20 13:25:53 EST 2007 http://www.perl.org/
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.9
//----------------------------------------------------------------------------------
// GString version (variables must be predefined):
username = 'Tom'
count = 99
total = 999
htmlStr = """
<!-- simple.template for internal template() function -->
<HTML><HEAD><TITLE>Report for $username</TITLE></HEAD>
<BODY><H1>Report for $username</H1>
$username logged in $count times, for a total of $total minutes.
"""
println htmlStr

// SimpleTemplateEngine version:
def html = '''
<!-- simple.template for internal template() function -->
<HTML><HEAD><TITLE>Report for $username</TITLE></HEAD>
<BODY><H1>Report for $username</H1>
$username logged in $count times, for a total of $total minutes.
'''

def engine = new groovy.text.SimpleTemplateEngine()
def reader = new StringReader(html)
def template = engine.createTemplate(reader)
println template.make(username:"Peter", count:"23", total: "1234")

// SQL version
import groovy.sql.Sql
user = 'Peter'
def sql = Sql.newInstance('jdbc:mysql://localhost:3306/mydb', 'dbuser',
                      'dbpass', 'com.mysql.jdbc.Driver')
sql.query("SELECT COUNT(duration),SUM(duration) FROM logins WHERE username='$user'") { answer ->
    println (template.make(username:user, count:answer[0], total:answer[1]))
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.10
//----------------------------------------------------------------------------------
// using built-in connection features
urlStr = 'http://jakarta.apache.org/'
url = new URL(urlStr)
connection = url.openConnection()
connection.ifModifiedSince = new Date(2007,1,18).time
connection.connect()
println connection.responseCode

// manually setting header field
connection = url.openConnection()
df = new java.text.SimpleDateFormat ("EEE, dd MMM yyyy HH:mm:ss 'GMT'")
df.setTimeZone(TimeZone.getTimeZone('GMT'))
connection.setRequestProperty("If-Modified-Since",df.format(new Date(2007,1,18)));
connection.connect()
println connection.responseCode
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.11
//----------------------------------------------------------------------------------
// The website http://www.robotstxt.org/wc/active/html/ lists many available robots
// including Java ones which can be used from Groovy. In particular, j-spider
// allows you to:
// + Check your site for errors (internal server errors, ...)
// + Outgoing and/or internal link checking
// + Analyze your site structure (creating a sitemap, ...)
// + Download complete web sites
// most of its functionality is available by tweaking appropriate configuration
// files and then running it as a standalone application but you can also write
// your own java classes.
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.12
//----------------------------------------------------------------------------------
// sample data, use 'LOGFILE = new File(args[0]).text' or similar
LOGFILE = '''
127.0.0.1 - - [04/Sep/2005:20:50:31 +0200] "GET /bus HTTP/1.1" 301 303
127.0.0.1 - - [04/Sep/2005:20:50:31 +0200] "GET /bus HTTP/1.1" 301 303 "-" "Opera/8.02 (X11; Linux i686; U; en)"
192.168.0.1 - - [04/Sep/2005:20:50:36 +0200] "GET /bus/libjs/layersmenu-library.js HTTP/1.1" 200 6228
192.168.0.1 - - [04/Sep/2005:20:50:36 +0200] "GET /bus/libjs/layersmenu-library.js HTTP/1.1" 200 6228 "http://localhost/bus/" "Opera/8.02 (X11; Linux i686; U; en)"
'''

// similar to perl version:
fields = ['client','identuser','authuser','date','time','tz','method','url','protocol','status','bytes']
regex = /^(\S+) (\S+) (\S+) \[([^:]+):(\d+:\d+:\d+) ([^\]]+)\] "(\S+) (.*?) (\S+)" (\S+) (\S+).*$/

LOGFILE.trim().split('\n').each{ line2 ->
    m = line2 =~ regex
    if (m.matches()) {
        for (idx in 0..<fields.size()) { println "${fields[idx]}=${m[0][idx+1]}" }
        println()
    }
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.13
//----------------------------------------------------------------------------------
// sample data, use 'LOGFILE = new File(args[0]).text' or similar
LOGFILE = '''
204.31.113.138 - - [03/Jul/1996:06:56:12 -0800] "POST /forms/login.jsp HTTP/1.0" 200 5593
fcrawler.looksmart.com - - [26/Apr/2000:00:00:12 -0400] "GET /contacts.html HTTP/1.0" 200 4595 "-" "FAST-WebCrawler/2.1-pre2 (ashen@looksmart.net)"
fcrawler.looksmart.com - - [26/Apr/2000:00:17:19 -0400] "GET /news/news.html HTTP/1.0" 200 16716 "-" "FAST-WebCrawler/2.1-pre2 (ashen@looksmart.net)"
ppp931.on.bellglobal.com - - [26/Apr/2000:00:16:12 -0400] "GET /download/windows/asctab31.zip HTTP/1.0" 200 1540096 "http://www.htmlgoodies.com/downloads/freeware/webdevelopment/15.html" "Mozilla/4.7 [en]C-SYMPA  (Win95; U)"
123.123.123.123 - - [26/Apr/2000:00:23:48 -0400] "GET /pics/wpaper.gif HTTP/1.0" 200 6248 "http://www.jafsoft.com/asctortf/" "Mozilla/4.05 (Macintosh; I; PPC)"
123.123.123.123 - - [26/Apr/2000:00:23:47 -0400] "GET /asctortf/ HTTP/1.0" 200 8130 "http://search.netscape.com/Computers/Data_Formats/Document/Text/RTF" "Mozilla/4.05 (Macintosh; I; PPC)"
123.123.123.123 - - [26/Apr/2000:00:23:48 -0400] "GET /pics/5star2000.gif HTTP/1.0" 200 4005 "http://www.jafsoft.com/asctortf/" "Mozilla/4.05 (Macintosh; I; PPC)"
123.123.123.123 - - [27/Apr/2000:00:23:50 -0400] "GET /pics/5star.gif HTTP/1.0" 200 1031 "http://www.jafsoft.com/asctortf/" "Mozilla/4.05 (Macintosh; I; PPC)"
123.123.123.123 - - [27/Apr/2000:00:23:51 -0400] "GET /pics/a2hlogo.jpg HTTP/1.0" 200 4282 "http://www.jafsoft.com/asctortf/" "Mozilla/4.05 (Macintosh; I; PPC)"
123.123.123.123 - - [27/Apr/2000:00:23:51 -0400] "GET /cgi-bin/newcount?jafsof3&width=4&font=digital&noshow HTTP/1.0" 200 36 "http://www.jafsoft.com/asctortf/" "Mozilla/4.05 (Macintosh; I; PPC)"
127.0.0.1 - frank [10/Oct/2000:13:55:36 -0700] "GET /apache_pb.gif HTTP/1.0" 200 2326
127.0.0.1 - - [04/Sep/2005:20:50:31 +0200] "GET / HTTP/1.1" 200 1927
127.0.0.1 - - [04/Sep/2005:20:50:31 +0200] "GET /bus HTTP/1.1" 301 303 "-" "Opera/8.02 (X11; Linux i686; U; en)"
192.168.0.1 - - [05/Sep/2005:20:50:36 +0200] "GET /bus/libjs/layersmenu-library.js HTTP/1.1" 200 6228
192.168.0.1 - - [05/Sep/2005:20:50:36 +0200] "GET /bus/libjs/layersmenu-library.js HTTP/1.1" 200 6228 "http://localhost/bus/" "Opera/8.02 (X11; Linux i686; U; en)"
'''

fields = ['client','identuser','authuser','date','time','tz','method','url','protocol','status','bytes']
regex = /^(\S+) (\S+) (\S+) \[([^:]+):(\d+:\d+:\d+) ([^\]]+)\] "(\S+) (.*?) (\S+)" (\S+) (\S+).*$/

class Summary {
    def hosts = [:]
    def what = [:]
    def accessCount = 0
    def postCount = 0
    def homeCount = 0
    def totalBytes = 0
}
totals = [:]
LOGFILE.trim().split('\n').each{ line3 ->
    m = line3 =~ regex
    if (m.matches()) {
        date = m[0][fields.indexOf('date')+1]
        s = totals.get(date, new Summary())
        s.accessCount++
        if (m[0][fields.indexOf('method')+1] == 'POST') s.postCount++
        s.totalBytes += (m[0][fields.indexOf('bytes')+1]).toInteger()
        def url = m[0][fields.indexOf('url')+1]
        if (url == '/') s.homeCount++
        s.what[url] = s.what.get(url, 0) + 1
        def host = m[0][fields.indexOf('client')+1]
        s.hosts[host] = s.hosts.get(host, 0) + 1
    }
}
report('Date','Hosts','Accesses','Unidocs','POST','Home','Bytes')
totals.each{ key, s ->
    report(key, s.hosts.size(), s.accessCount, s.what.size(), s.postCount, s.homeCount, s.totalBytes)
}
v = totals.values()
report('Grand Total', v.sum{it.hosts.size()}, v.sum{it.accessCount}, v.sum{it.what.size()},
        v.sum{it.postCount}, v.sum{it.homeCount}, v.sum{it.totalBytes} )

def report(a, b, c, d, e, f, g) {
    printf ("%12s %6s %8s %8s %8s %8s %10s\n", [a,b,c,d,e,f,g])
}
// =>
//         Date  Hosts Accesses  Unidocs     POST     Home      Bytes
//  03/Jul/1996      1        1        1        1        0       5593
//  10/Oct/2000      1        1        1        0        0       2326
//  04/Sep/2005      1        2        2        0        1       2230
//  05/Sep/2005      1        2        1        0        0      12456
//  26/Apr/2000      3        6        6        0        0    1579790
//  27/Apr/2000      1        3        3        0        0       5349
//  Grand Total      8       15       14        1        1    1607744


// Some open source log processing packages in Java:
// http://www.generationjava.com/projects/logview/index.shtml
// http://ostermiller.org/webalizer/
// http://jxla.nvdcms.org/en/index.xml
// http://polliwog.sourceforge.net/index.html
// as well as textual reports, most of these can produce graphical reports
// Most have their own configuration information and Java extension points.
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.14
//----------------------------------------------------------------------------------
 import org.cyberneko.html.filters.Writer
 import org.cyberneko.html.filters.DefaultFilter
 import org.apache.xerces.xni.parser.XMLDocumentFilter
 import org.apache.xerces.xni.*
 import org.cyberneko.html.parsers.DOMParser
 import org.xml.sax.InputSource

 input = '''
 <HTML><HEAD><TITLE>Hi!</TITLE></HEAD><BODY>
 <H1>Welcome to Scooby World!</H1>
 I have <A HREF="pictures.html">pictures</A> of the crazy dog
 himself. Here's one!<P>
 <IMG SRC="scooby.jpg" ALT="Good doggy!"><P>
 <BLINK>He's my hero!</BLINK> I would like to meet him some day,
 and get my picture taken with him.<P>
 P.S. I am deathly ill. <A HREF="shergold.html">Please send
 cards</A>.
 </BODY></HTML>
 '''

 class WordReplaceFilter extends DefaultFilter {
     private before, after
     WordReplaceFilter(b, a) { before = b; after = a }
     void characters(XMLString text, Augmentations augs) {
         char[] c = text.toString().replaceAll(before, after)
         super.characters(new XMLString(c, 0, c.size()), augs)
     }
     void setProperty(String s, Object o){}
 }
 XMLDocumentFilter[] filters = [
     new WordReplaceFilter(/(?sm)picture/, /photo/),
     new Writer()
 ]
 parser = new DOMParser()
 parser.setProperty("http://cyberneko.org/html/properties/filters", filters)
 parser.parse(new InputSource(new StringReader(input)))
//----------------------------------------------------------------------------------


// @@PLEAC@@_20.15
//----------------------------------------------------------------------------------
import org.cyberneko.html.filters.Writer
import org.cyberneko.html.filters.DefaultFilter
import org.apache.xerces.xni.parser.XMLDocumentFilter
import org.apache.xerces.xni.*
import org.cyberneko.html.parsers.DOMParser
import org.xml.sax.InputSource

input = '''
<HTML><HEAD><TITLE>Hi!</TITLE></HEAD><BODY>
<H1>Welcome to Scooby World!</H1>
I have <A HREF="pictures.html">pictures</A> of the crazy dog
himself. Here's one!<P>
<IMG SRC="scooby.jpg" ALT="Good doggy!"><P>
<BLINK>He's my hero!</BLINK> I would like to meet him some day,
and get my picture taken with him.<P>
P.S. I am deathly ill. <A HREF="shergold.html">Please send
cards</A>.
</BODY></HTML>
'''

class HrefReplaceFilter extends DefaultFilter {
    private before, after
    HrefReplaceFilter(b, a) { before = b; after = a }
    void startElement(QName element, XMLAttributes attributes, Augmentations augs) {
        def idx = attributes.getIndex('href')
        if (idx != -1) {
            def newtext = attributes.getValue(idx).replaceAll(before, after)
            attributes.setValue(idx, URLEncoder.encode(newtext))
        }
        super.startElement(element, attributes, augs)
    }
    void setProperty(String s, Object o){}
}
XMLDocumentFilter[] myfilters = [
    new HrefReplaceFilter(/shergold.html/, /cards.html/),
    new Writer()
]
parser = new DOMParser()
parser.setProperty("http://cyberneko.org/html/properties/filters", myfilters)
parser.parse(new InputSource(new StringReader(input)))
//----------------------------------------------------------------------------------

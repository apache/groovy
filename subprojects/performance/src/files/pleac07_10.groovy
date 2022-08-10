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

// @@PLEAC@@_7.0
//----------------------------------------------------------------------------------
//testfile = new File('/usr/local/widgets/data')  // unix
testfile = new File('Pleac/data/blue.txt')      // windows
testfile.eachLine{ if (it =~ /blue/) println it }

// Groovy (like Java) uses the File class as an abstraction for
// the path representing a potential file system resource.
// Channels and Streams (along with Reader adn Writer helper
// classes) are used to read and write to files (and other
// things). Files, channels, streams etc are all "normal"
// objects; they can be passed around in your programs just
// like other objects (though there are some restrictions
// covered elsewhere - e.g. you can't expect to pass a File
// object between JVMs on different machines running different
// operating systems and expect them to maintain a meaningful
// value across the different JVMs). In addition to Streams,
// there is also support for random access to files.

// Many operations are available on streams and channels. Some
// return values to indicate success or failure, some can throw
// exceptions, other times both styles of error reporting may be
// available.

// Streams at the lowest level are just a sequence of bytes though
// there are various abstractions at higher levels to allow
// interacting with streams at encoded character, data type or
// object levels if desired. Standard streams include System.in,
// System.out and System.err. Java and Groovy on top of that
// provide facilities for buffering, filtering and processing
// streams in various ways.

// File channels provide more powerful operations than streams
// for reading and writing files such as locks, buffering,
// positioning, concurrent reading and writing, mapping to memory
// etc. In the examples which follow, streams will be used for
// simple cases, channels when more advanced features are
// required. Groovy currently focusses on providing extra support
// at the file and stream level rather than channel level.
// This makes the simple things easy but lets you do more complex
// things by just using the appropriate Java classes. All Java
// classes are available within Groovy by default.

// Groovy provides syntactic sugar over the top of Java's file
// processing capabilities by providing meaning to shorthand
// operators and by automatically handling scaffolding type
// code such as opening, closing and handling exceptions behind
// the scenes. It also provides many powerful closure operators,
// e.g. file.eachLineMatch(pattern){ some_operation } will open
// the file, process it line-by-line, finding all lines which
// match the specified pattern and then invoke some operation
// for the matching line(s) if any, before closing the file.


// this example shows how to access the standard input stream
// numericCheckingScript:
prompt = '\n> '
print 'Enter text including a digit:' + prompt
new BufferedReader(new InputStreamReader(System.in)).eachLine{ line ->
                                               // line is read from System.in
    if (line =~ '\\d') println "Read: $line"   // normal output to System.out
    else System.err.println 'No digit found.'  // this message to System.err
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.1
//----------------------------------------------------------------------------------
// test values (change for your os and directories)
inputPath='Pleac/src/pleac7.groovy'; outPath='Pleac/temp/junk.txt'

// For input Java uses InputStreams (for byte-oriented processing) or Readers
// (for character-oriented processing). These can throw FileNotFoundException.
// There are also other stream variants: buffered, data, filters, objects, ...
inputFile = new File(inputPath)
inputStream = new FileInputStream(inputFile)
reader = new FileReader(inputFile)
inputChannel = inputStream.channel

// Examples for random access to a file
file = new RandomAccessFile(inputFile, "rw") // for read and write
channel = file.channel

// Groovy provides some sugar coating on top of Java
println inputFile.text.size()
// => 13496

// For output Java use OutputStreams or Writers. Can throw FileNotFound
// or IO exceptions. There are also other flavours of stream: buffered,
// data, filters, objects, ...
outFile = new File(outPath)
appendFlag = false
outStream = new FileOutputStream(outFile, appendFlag)
writer = new FileWriter(outFile, appendFlag)
outChannel = outStream.channel

// Also some Groovy sugar coating
outFile << 'A Chinese sailing vessel'
println outFile.text.size() // => 24

// @@PLEAC@@_7.2
//----------------------------------------------------------------------------------
// No problem with Groovy since the filename doesn't contain characters with
// special meaning; like Perl's sysopen. Options are either additional parameters
// or captured in different classes, e.g. Input vs Output, Buffered vs non etc.
new FileReader(inputPath)
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.3
//----------------------------------------------------------------------------------
// '~' is a shell expansion feature rather than file system feature per se.
// Because '~' is a valid filename character in some operating systems, and Java
// attempts to be cross-platform, it doesn't automatically expand Tilde's.
// Given that '~' expansion is commonly used however, Java puts the $HOME
// environment variable (used by shells to do typical expansion) into the
// "user.home" system property. This works across operating systems - though
// the value inside differs from system to system so you shouldn't rely on its
// content to be of a particular format. In most cases though you should be
// able to write a regex that will work as expected. Also, Apple's
// NSPathUtilities can expand and introduce Tildes on platforms it supports.
path = '~paulk/.cvspass'
name = System.getProperty('user.name')
home = System.getProperty('user.home')
println home + path.replaceAll("~$name(.*)", '$1')
// => C:\Documents and Settings\Paul/.cvspass
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.4
//----------------------------------------------------------------------------------
// The exception raised in Groovy reports the filename
try {
    new File('unknown_path/bad_file.ext').text
} catch (Exception ex) {
    System.err.println(ex.message)
}
// =>
// unknown_path\bad_file.ext (The system cannot find the path specified)
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.5
//----------------------------------------------------------------------------------
try {
    temp = File.createTempFile("prefix", ".suffix")
    temp.deleteOnExit()
} catch (IOException ex) {
    System.err.println("Temp file could not be created")
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.6
//----------------------------------------------------------------------------------
// no special features are provided, here is a way to do it manually
// DO NOT REMOVE THE FOLLOWING STRING DEFINITION.
pleac_7_6_embeddedFileInfo = '''
Script size is 13731
Last script update: Wed Jan 10 19:05:58 EST 2007
'''
ls = System.getProperty('line.separator')
file = new File('Pleac/src/pleac7.groovy')
regex = /(?ms)(?<=^pleac_7_6_embeddedFileInfo = ''')(.*)(?=^''')/
def readEmbeddedInfo() {
    m = file.text =~ regex
    println 'Found:\n' + m[0][1]
}
def writeEmbeddedInfo() {
    lastMod = new Date(file.lastModified())
    newInfo = "${ls}Script size is ${file.size()}${ls}Last script update: ${lastMod}${ls}"
    file.write(file.text.replaceAll(regex, newInfo))
}
readEmbeddedInfo()
// writeEmbeddedInfo()  // uncomment to make script update itself
// readEmbeddedInfo()   // uncomment to redisplay the embedded info after the update

// => (output when above two method call lines are uncommented)
// Found:
//
// Script size is 13550
// Last script update: Wed Jan 10 18:56:03 EST 2007
//
// Found:
//
// Script size is 13731
// Last script update: Wed Jan 10 19:05:58 EST 2007
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.7
//----------------------------------------------------------------------------------
// general pattern for reading from System.in is:
// System.in.readLines().each{ processLine(it) }

// general pattern for a filter which can either process file args or read from System.in is:
// if (args.size() != 0) args.each{
//     file -> new File(file).eachLine{ processLine(it) }
// } else System.in.readLines().each{ processLine(it) }

// note: the following examples are file-related per se. They show
// how to do option processing in scenarios which typically also
// involve file arguments. The reader should also consider using a
// pre-packaged options parser package (there are several popular
// ones) rather than the hard-coded processing examples shown here.

chopFirst = false
columns = 0
args = ['-c', '-30', 'somefile']

// demo1: optional c
if (args[0] == '-c') {
    chopFirst = true
    args = args[1..-1]
}

assert args == ["-30", "somefile"]
assert chopFirst

// demo2: processing numerical options
if (args[0] =~ /^-(\d+)$/) {
    columns = args[0][1..-1].toInteger()
    args = args[1..-1]
}

assert args == ["somefile"]
assert columns == 30

// demo3: multiple args (again consider option parsing package)
args = ['-n','-a','file1','file2']
nostdout = false
append = false
unbuffer = false
ignore_ints = false
files = []
args.each{ arg ->
    switch(arg) {
        case '-n': nostdout    = true; break
        case '-a': append      = true; break
        case '-u': unbuffer    = true; break
        case '-i': ignore_ints = true; break
        default: files += arg
    }
}
if (files.any{ it.startsWith('-')}) {
    System.err.println("usage: demo3 [-ainu] [filenames]")
}
// process files ...
assert nostdout && append && !unbuffer && !ignore_ints
assert files == ['file1','file2']

// find login: print all lines containing the string "login" (command-line version)
//% groovy -ne "if (line =~ 'login') println line" filename

// find login variation: lines containing "login" with line number (command-line version)
//% groovy -ne "if (line =~ 'login') println count + ':' + line" filename

// lowercase file (command-line version)
//% groovy -pe "line.toLowerCase()"


// count chunks but skip comments and stop when reaching "__DATA__" or "__END__"
chunks = 0; done = false
testfile = new File('Pleac/data/chunks.txt') // change on your system
lines = testfile.readLines()
for (line in lines) {
    if (!line.trim()) continue
    words = line.split(/[^\w#]+/).toList()
    for (word in words) {
        if (word =~ /^#/) break
        if (word in ["__DATA__", "__END__"]) { done = true; break }
        chunks += 1
    }
    if (done) break
}
println "Found $chunks chunks"


// groovy "one-liner" (cough cough) for turning .history file into pretty version:
//% groovy -e "m=new File(args[0]).text=~/(?ms)^#\+(\d+)\r?\n(.*?)$/;(0..<m.count).each{println ''+new Date(m[it][1].toInteger())+'  '+m[it][2]}" .history
// =>
// Sun Jan 11 18:26:22 EST 1970  less /etc/motd
// Sun Jan 11 18:26:22 EST 1970  vi ~/.exrc
// Sun Jan 11 18:26:22 EST 1970  date
// Sun Jan 11 18:26:22 EST 1970  who
// Sun Jan 11 18:26:22 EST 1970  telnet home
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.8
//----------------------------------------------------------------------------------
// test data for below
testPath = 'Pleac/data/process.txt'

// general pattern
def processWithBackup(inputPath, Closure processLine) {
    def input = new File(inputPath)
    def out = File.createTempFile("prefix", ".suffix")
    out.write('') // create empty file
    count = 0
    input.eachLine{ line ->
        count++
        processLine(out, line, count)
    }
    def dest = new File(inputPath + ".orig")
    dest.delete() // clobber previous backup
    input.renameTo(dest)
    out.renameTo(input)
}

// use withPrintWriter if you don't want the '\n''s appearing
processWithBackup(testPath) { out, line, count ->
    if (count == 20) {   // we are at the 20th line
        out << "Extra line 1\n"
        out << "Extra line 2\n"
    }
    out << line + '\n'
}

processWithBackup(testPath) { out, line, count ->
    if (!(count in 20..30)) // skip the 20th line to the 30th
        out << line + '\n'
}
// equivalent to "one-liner":
//% groovy -i.orig -pe "if (!(count in 20..30)) out << line" testPath
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.9
//----------------------------------------------------------------------------------
//% groovy -i.orig -pe 'FILTER COMMAND' file1 file2 file3 ...

// the following may also be possible on unix systems (unchecked)
//#!/usr/bin/groovy -i.orig -p
// filter commands go here

// "one-liner" templating scenario: change DATE -> current time
//% groovy -pi.orig -e 'line.replaceAll(/DATE/){new Date()}'

//% groovy -i.old -pe 'line.replaceAll(/\bhisvar\b/, 'hervar')' *.[Cchy] (globbing platform specific)

// one-liner for correcting spelling typos
//% groovy -i.orig -pe 'line.replaceAll(/\b(p)earl\b/i, '\1erl')' *.[Cchy] (globbing platform specific)
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.10
//----------------------------------------------------------------------------------
// general pattern
def processFileInplace(file, Closure processText) {
    def text = file.text
    file.write(processText(text))
}

// templating scenario: change DATE -> current time
testfile = new File('Pleac/data/pleac7_10.txt') // replace on your system
processFileInplace(testfile) { text ->
    text.replaceAll(/(?m)DATE/, new Date().toString())
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.11
//----------------------------------------------------------------------------------
// You need to use Java's Channel class to acquire locks. The exact
// nature of the lock is somewhat dependent on the operating system.
def processFileWithLock(file, processStream) {
    def random = new RandomAccessFile(file, "rw")
    def lock = random.channel.lock() // acquire exclusive lock
    processStream(random)
    lock.release()
    random.close()
}

// Instead of an exclusive lock you can acquire a shared lock.

// Also, you can acquire a lock for a region of a file by specifying
// start and end positions of the region when acquiring the lock.

// For non-blocking functionality, use tryLock() instead of lock().
def processFileWithTryLock(file, processStream) {
    random = new RandomAccessFile(file, "rw")
    channel = random.channel
    def MAX_ATTEMPTS = 30
    for (i in 0..<MAX_ATTEMPTS) {
        lock = channel.tryLock()
        if (lock != null) break
        println 'Could not get lock, pausing ...'
        Thread.sleep(500) // 500 millis = 0.5 secs
    }
    if (lock == null) {
        println 'Unable to acquire lock, aborting ...'
    } else {
        processStream(random)
        lock.release()
    }
    random.close()
}


// non-blocking multithreaded example: print first line while holding lock
Thread.start{
    processFileWithLock(testfile) { source ->
        println 'First reader: ' + source.readLine().toUpperCase()
        Thread.sleep(2000) // 2000 millis = 2 secs
    }
}
processFileWithTryLock(testfile) { source ->
    println 'Second reader: ' + source.readLine().toUpperCase()
}
// =>
// Could not get lock, pausing ...
// First reader: WAS LOWERCASE
// Could not get lock, pausing ...
// Could not get lock, pausing ...
// Could not get lock, pausing ...
// Could not get lock, pausing ...
// Second reader: WAS LOWERCASE
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.12
//----------------------------------------------------------------------------------
// In Java, input and output streams have a flush() method and file channels
// have a force() method (applicable also to memory-mapped files). When creating
// PrintWriters and // PrintStreams, an autoFlush option can be provided.
// From a FileInput or Output Stream you can ask for the FileDescriptor
// which has a sync() method - but you wouldn't you'd just use flush().

inputStream = testfile.newInputStream()    // returns a buffered input stream
autoFlush = true
printStream = new PrintStream(outStream, autoFlush)
printWriter = new PrintWriter(outStream, autoFlush)
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.13
//----------------------------------------------------------------------------------
// See the comments in 7.14 about scenarios where non-blocking can be
// avoided. Also see 7.14 regarding basic information about channels.
// An advanced feature of the java.nio.channels package is supported
// by the Selector and SelectableChannel classes. These allow efficient
// server multiplexing amongst responses from a number of potential sources.
// Under the covers, it allows mapping to native operating system features
// supporting such multiplexing or using a pool of worker processing threads
// much smaller in size than the total available connections.
//
// The general pattern for using selectors is:
//
//      while (true) {
//         selector.select()
//         def it = selector.selectedKeys().iterator()
//         while (it.hasNext()) {
//            handleKey(it++)
//            it.remove()
//         }
//      }
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.14
//----------------------------------------------------------------------------------
// Groovy has no special support for this apart from making it easier to
// create threads (see note at end); it relies on Java's features here.

// InputStreams in Java/Groovy block if input is not yet available.
// This is not normally an issue, because if you have a potential blocking
// operation, e.g. save a large file, you normally just create a thread
 // and save it in the background.

// Channels are one way to do non-blocking stream-based IO.
// Classes which implement the AbstractSelectableChannel interface provide
// a configureBlocking(boolean) method as well as an isBlocking() method.
// When processing a non-blocking stream, you need to process incoming
// information based on the number of bytes read returned by the various
// read methods. For non-blocking, this can be 0 bytes even if you pass
// a fixed size byte[] buffer to the read method. Non-blocking IO is typically
// not used with Files but more normally with network streams though they
// can when Pipes (couple sink and source channels) are involved where
// one side of the pipe is a file.
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.15
//----------------------------------------------------------------------------------
// Groovy uses Java's features here.
// For both blocking and non-blocking reads, the read operation returns the number
// of bytes read. In blocking operations, this normally corresponds to the number
// of bytes requested (typically the size of some buffer) but can have a smaller
// value at the end of a stream. Java also makes no guarantees about whether
// other streams in general will return bytes as they become available under
// certain circumstances (rather than blocking until the entire buffer is filled.
// In non-blocking operations, the number of bytes returned will typically be
// the number of bytes available (up to some maximum buffer or requested size).
//----------------------------------------------------------------------------------


// @@PLEAC@@_7.16
//----------------------------------------------------------------------------------
// This just works in Java and Groovy as per the previous examples.
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.17
//----------------------------------------------------------------------------------
// Groovy uses Java's features here.
// More work has been done in the Java on object caching than file caching
// with several open source and commercial offerings in that area. File caches
// are also available, for one, see:
// http://portals.apache.org/jetspeed-1/apidocs/org/apache/jetspeed/cache/FileCache.html
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.18
//----------------------------------------------------------------------------------
// The general pattern is: streams.each{ stream -> stream.println 'item to print' }
// See the MultiStream example in 13.5 for a coded example.
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.19
//----------------------------------------------------------------------------------
// You wouldn't normally be dealing with FileDescriptors. In case were you have
// one you would normally walk through all known FileStreams asking each for
// it's FileDescriptor until you found one that matched. You would then close
// that stream.
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.20
//----------------------------------------------------------------------------------
// There are several concepts here. At the object level, any two object references
// can point to the same object. Any changes made by one of these will be visible
// in the 'alias'. You can also have multiple stream, reader, writer or channel objects
// referencing the same resource. Depending on the kind of resource, any potential
// locks, the operations being requested and the behaviour of third-party programs,
// the result of trying to perform such concurrent operations may not always be
// deterministic. There are strategies for coping with such scenarious but the
// best bet is to avoid the issue.

// For the scenario given, copying file handles, that corresponds most closely
// with cloning streams. The best bet is to just use individual stream objects
// both created from the same file. If you are attempting to do write operations,
// then you should consider using locks.
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.21
//----------------------------------------------------------------------------------
// locking is built in to Java (since 1.4), so should not be missing
//----------------------------------------------------------------------------------

// @@PLEAC@@_7.22
//----------------------------------------------------------------------------------
// Java locking supports locking just regions of files.
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.0
//----------------------------------------------------------------------------------
datafile = new File('Pleac/data/pleac8_0.txt') // change on your system

datafile.eachLine{ line -> print line.size() }

lines = datafile.readLines()

wholeTextFile = datafile.text

// on command line Groovy use -a auto split pattern instead of record separator
// default pattern is /\s/
// groovy -a -e 'println "First word is ${split[0][1]}"'

// (additional examples to original cookbook to illustrate -a)
// Print processes owned by root:
// ps aux|groovy -ane "if(split[0][1] =~ 'root')println split[0][10..-1]"

// Print all logins from /etc/passwd that are not commented:
// groovy -a':' -ne "if(!(split[0][1] =~ /^#/))println split[0][1]" /etc/passwd

// Add the first and the penultimate column of a file:
// groovy -ape "split[0][1].toInteger()+split[0][-2].toInteger()" accounts.txt

// no BEGIN and END in Groovy (has been proposed, may be added soon)

datafile.withOutputStream{ stream ->
    stream.print "one" + "two" + "three"    // "onetwothree" -> file
    println "Baa baa black sheep."          // sent to $stdout
}

// use streams or channels for advanced file handling
int size = datafile.size()
buffer = ByteBuffer.allocate(size) // for large files, use some block size, e.g. 4096
channel = new FileInputStream(datafile).channel
println "Number of bytes read was: ${channel.read(buffer)}" // -1 = EOF

channel = new FileOutputStream(File.createTempFile("pleac8", ".junk")).channel
size = channel.size()
channel.truncate(size) // shrinks file (in our case to same size)

pos = channel.position()
println "I'm $pos bytes from the start of datafile"
channel.position(pos)  // move to pos (in our case unchanged)
channel.position(0)    // move to start of file
channel.position(size) // move to end of file

// no sysread and syswrite are available but dataInput/output streams
// can be used to achieve similar functionality, see 8.15.
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.1
//----------------------------------------------------------------------------------
testfile = new File('Pleac/data/pleac8_1.txt') // change on your system
// contents of testfile:
// DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) \
//         $(TEXINFOS) $(INFOS) $(MANS) $(DATA)
// DEP_DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS) \
//         $(TEXINFOS) $(INFO_DEPS) $(MANS) $(DATA) \
//         $(EXTRA_DIST)

lines = []
continuing = false
regex = /\\$/
testfile.eachLine{ line ->
    stripped = line.replaceAll(regex,'')
    if (continuing) lines[-1] += stripped
    else lines += stripped
    continuing = (line =~ regex)
}
println lines.join('\n')
// =>
// DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS)         $(TEXINFOS) $(INFOS) $(MANS) $(DATA)
// DEP_DISTFILES = $(DIST_COMMON) $(SOURCES) $(HEADERS)         $(TEXINFOS) $(INFO_DEPS) $(MANS) $(DATA)         $(EXTRA_DIST)

// to remove hidden spaces after the slash (but keep the slash):
def trimtail(line) {
    line = line.replaceAll(/(?<=\\)\s*$/, '')
}
b = /\\/  // backslash
assert "abc  $b"   == trimtail("abc  $b")
assert "abc  "     == trimtail("abc  ")
assert "abc  $b"   == trimtail("abc  $b  ")
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.2
//----------------------------------------------------------------------------------
// unixScript:
println ("wc -l < $filename".execute().text)

// for small files which fit in memory
println testfile.readLines().size()

// streaming approach (lines and paras)
lines = 0; paras = 1
testfile.eachLine{ lines++; if (it =~ /^$/) paras++ }
println "Found $lines lines and $paras paras."
// note: counts blank line at end as start of next empty para

// with a StreamTokenizer
st = new StreamTokenizer(testfile.newReader())
while (st.nextToken() != StreamTokenizer.TT_EOF) {}
println st.lineno()
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.3
//----------------------------------------------------------------------------------
// general pattern
def processWordsInFile(file, processWord) {
    testfile.splitEachLine(/\W+/) { matched ->
        matched.each{ w -> if (w) processWord(w) }
    }
}

testfile = new File('Pleac/src/pleac8.groovy')  // change path on your system

// count words
count = 0
processWordsInFile(testfile){ count++ }
println count

// (variation to Perl example)
// with a StreamTokenizer (counting words and numbers in Pleac chapter 8 source file)
words = 0; numbers = 0
st = new StreamTokenizer(testfile.newReader())
st.slashSlashComments(true) // ignore words and numbers in comments
while (st.nextToken() != StreamTokenizer.TT_EOF) {
    if (st.ttype == StreamTokenizer.TT_WORD) words++
    else if (st.ttype == StreamTokenizer.TT_NUMBER) numbers++
}
println "Found $words words and $numbers numbers."


// word frequency count
seen = [:]
processWordsInFile(testfile) {
    w = it.toLowerCase()
    if (seen.containsKey(w)) seen[w] += 1
    else seen[w] = 1
}
// output map in a descending numeric sort of its values
seen.entrySet().sort { a,b -> b.value <=> a.value }.each{ e ->
    printf("%5d %s\n", [e.value, e.key] )
}
// =>
//    25 pleac
//    22 line
//    20 file
//    19 println
//    19 lines
//    13 testfile
//    ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.4
//----------------------------------------------------------------------------------
testfile.readLines().reverseEach{
    println it
}

lines = testfile.readLines()
// normally one would use the reverseEach, but you can use
// a numerical index if you want
((lines.size() - 1)..0).each{
    println lines[it]
}

// Paragraph-based processing could be done as in 8.2.

// A streaming-based solution could use random file access
// and have a sliding buffer working from the back of the
// file to the front.
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.5
//----------------------------------------------------------------------------------
logfile = new File('Pleac/data/sampleLog.txt')
// logTailingScript:
sampleInterval = 2000 // 2000 millis = 2 secs
file = new RandomAccessFile( logfile, "r" )
filePointer = 0 // set to logfile.size() to begin tailing from the end of the file
while( true ) {
    // Compare the length of the file to the file pointer
    long fileLength = logfile.size()
    if( fileLength < filePointer ) {
        // Log file must have been rotated or deleted;
        System.err.println "${new Date()}: Reopening $logfile"
        file = new RandomAccessFile( logfile, "r" )
        filePointer = 0
    }
    if( fileLength > filePointer ) {
        // There is data to read
        file.seek( filePointer )
        while( (line = file.readLine()) != null ) {
            println '##' + line
        }
        filePointer = file.filePointer
    }
    // Sleep for the specified interval
    Thread.sleep( sampleInterval )
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.6
//----------------------------------------------------------------------------------
//testfile = newFile('/usr/share/fortune/humorists')

// small files:
random = new Random()
lines = testfile.readLines()
println lines[random.nextInt(lines.size())]

// streamed alternative
count = 0
def adage
testfile.eachLine{ line ->
    count++
    if (random.nextInt(count) < 1) adage = line
}
println adage
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.7
//----------------------------------------------------------------------------------
// non-streamed solution (like Perl and Ruby)
lines = testfile.readLines()
Collections.shuffle(lines)
println lines.join('\n')
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.8
//----------------------------------------------------------------------------------
desiredLine = 235
// for small files
lines = testfile.readLines()
println "Line $desiredLine: ${lines[desiredLine-1]}"

// streaming solution
reader = testfile.newReader()
count = 0
def line
while ((line = reader.readLine())!= null) {
    if (++count == desiredLine) break
}
println "Line $desiredLine: $line"
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.9
//----------------------------------------------------------------------------------
println testfile.text.split(/@@pleac@@_8./).size()
// => 23 (21 sections .0 .. .20 plus before .0 plus line above)
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.10
//----------------------------------------------------------------------------------
file = new RandomAccessFile( logfile, "rw" )
long previous, lastpos = 0
while( (line = file.readLine()) != null ) {
    previous = lastpos
    lastpos = file.filePointer
}
if (previous) file.setLength(previous)
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.11
//----------------------------------------------------------------------------------
// Java's streams are binary at the lowest level if not processed with
// higher level stream mechanisms or readers/writers. Some additions
// to the Perl cookbook which illustrate the basics.

// Print first ten bytes of a binary file:
def dumpStart(filename) {
    bytes = new File(filename).newInputStream()
    10.times{
        print bytes.read() + ' '
    }
    println()
}
dumpStart(System.getProperty('java.home')+'/lib/rt.jar')
// => 80 75 3 4 10 0 0 0 0 0 (note first two bytes = PK - you might recognize this
// as the starting sequence of a zip file)
dumpStart('Pleac/classes/pleac8.class') // after running groovyc compiler in src directory
// => 202 254 186 190 0 0 0 47 2 20 (starting bytes in HEX: CAFEBABE)

binfile = new File('Pleac/data/temp.bin')
binfile.withOutputStream{ stream -> (0..<20).each{ stream.write(it) }}
binfile.eachByte{ print it + ' ' }; println()
// => 0 1 2 3 4 5 6 7 8 9 10 11 12 13 14 15 16 17 18 19
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.12
//----------------------------------------------------------------------------------
// lets treat binfile as having 5 records of size 4, let's print out the 3rd record
recsize = 4
recno = 2 // index starts at 0
address = recsize * recno
randomaccess = new RandomAccessFile(binfile, 'r')
randomaccess.seek(address)
recsize.times{ print randomaccess.read() + ' ' }; println()  // => 8 9 10 11
randomaccess.close()
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.13
//----------------------------------------------------------------------------------
// let's take the example from 8.12 but replace the 3rd record with
// 90 - the original value in the file
// this is an alternative example to the Perl cookbook which is cross platform
// see chapter 1 regarding un/pack which could be combined with below
// to achieve the full functionality of the original 8.13
recsize = 4
recno = 2 // index starts at 0
address = recsize * recno
randomaccess = new RandomAccessFile(binfile, 'rw')
randomaccess.seek(address)
bytes = []
recsize.times{ bytes += randomaccess.read() }
randomaccess.seek(address)
bytes.each{ b -> randomaccess.write(90 - b) }
randomaccess.close()
binfile.eachByte{ print it + ' ' }; println()
// => 0 1 2 3 4 5 6 7 82 81 80 79 12 13 14 15 16 17 18 19
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.14
//----------------------------------------------------------------------------------
// reading a String would involve looping and collecting the read bytes

// simple bgets
// this is similar to the revised 8.13 but would look for the terminating 0

// simplistic strings functionality
binfile.eachByte{ b -> if ((int)b in 32..126) print ((char)b) }; println() // => RQPO
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.15
//----------------------------------------------------------------------------------
// You could combine the byte-level reading/writing mechanisms shown
// in 8.11 - 8.12 and combine that with the un/pack functionality from
// Chapter 1 to achieve the desired functionality. A more Java and Groovy
// friendly way to do this would be to use the Scattering and Gathering
// stream operations of channels for byte-oriented record fields or
// data-oriented records. Alternatively, the dataInput/output stream
// capabilities for data-oriented records. Finally, the
// objectInput/output stream capabilities could be used for object types.
// Note, these examples mix reading and writing even though the original
// Perl example was just about reading.


// fixed-length byte-oriented records using channels
// typical approach used with low-level protocols or file formats
import java.nio.*
binfile.delete(); binfile.createNewFile() // start from scratch
buf1 = ByteBuffer.wrap([10,11,12,13] as byte[]) // simulate 4 byte field
buf2 = ByteBuffer.wrap([44,45] as byte[])       // 2 byte field
buf3 = ByteBuffer.wrap('Hello'.bytes)           // String
records = [buf1, buf2, buf3] as ByteBuffer[]
channel = new FileOutputStream(binfile).channel
channel.write(records) // gathering byte records
channel.close()
binfile.eachByte{ print it + ' ' }; println()
// => 10 11 12 13 44 45 72 101 108 108 111
// ScatteringInputStream would convert this back into an array of byte[]


// data-oriented streams using channels
binfile.delete(); binfile.createNewFile() // start from scratch
buf = ByteBuffer.allocate(24)
now = System.currentTimeMillis()
buf.put('PI='.bytes).putDouble(Math.PI).put('Date='.bytes).putLong(now)
buf.flip() // readies for writing: set length and point back to start
channel = new FileOutputStream(binfile).channel
channel.write(buf)
channel.close()
// now read it back in
channel = new FileInputStream(binfile).channel
buf = ByteBuffer.allocate(24)
channel.read(buf)
buf.flip()
3.times{ print ((char)buf.get()) }
println (buf.getDouble())
5.times{ print ((char)buf.get()) }
println (new Date(buf.getLong()))
channel.close()
// =>
// PI=3.141592653589793
// Date=Sat Jan 13 00:14:50 EST 2007

// object-oriented streams
binfile.delete(); binfile.createNewFile() // start from scratch
class Person implements Serializable { def name, age }
binfile.withObjectOutputStream{ oos ->
    oos.writeObject(new Person(name:'Bernie',age:16))
    oos.writeObject([1:'a', 2:'b'])
    oos.writeObject(new Date())
}
// now read it back in
binfile.withObjectInputStream{ ois ->
    person = ois.readObject()
    println "$person.name is $person.age"
    println ois.readObject()
    println ois.readObject()
}
// =>
// Bernie is 16
// [1:"a", 2:"b"]
// Sat Jan 13 00:22:13 EST 2007
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.16
//----------------------------------------------------------------------------------
// use built-in Java property class
// suppose you have the following file:
// # set your database settings here
// server=localhost
// url=jdbc:derby:derbyDB;create=true
// user.name=me
// user.password=secret
props = new Properties()
propsfile=new File('Pleac/data/plain.properties')
props.load(propsfile.newInputStream())
props.list(System.out)
// =>
// -- listing properties --
// user.name=me
// user.password=secret
// url=jdbc:derby:derbyDB;create=true
// server=localhost

// There are also provisions for writing properties file.

// (additional example to Perl)
// You can also read and write xml properties files.
new File('Pleac/data/props.xml').withOutputStream{ os ->
    props.storeToXML(os, "Database Settings")
}
// =>
// <?xml version="1.0" encoding="UTF-8"?>
// <!DOCTYPE properties SYSTEM "http://java.sun.com/dtd/properties.dtd">
// <properties>
// <comment>Database Settings</comment>
// <entry key="user.password">secret</entry>
// <entry key="user.name">me</entry>
// <entry key="url">jdbc:derby:derbyDB;create=true</entry>
// <entry key="server">localhost</entry>
// </properties>
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.17
//----------------------------------------------------------------------------------
// The File class provides canRead(), canWrite() and canExecute() (JDK6) methods
// for finding out about security information specific to the user. JSR 203
// (expected in Java 7) provides access to additional security related attributes.

// Another useful package to use when wondering about the trustworthiness of a
// file is the java.security package. It contains many classes. Just one is
// MessageDigest. This would allow you to create a strong checksum of a file.
// Your program could refuse to operate if a file it was accessing didn't have the
// checksum it was expecting - an indication that it may have been tampered with.

// (additional info)
// While getting file-based security permissions correct is important, it isn't the
// only mechanism to use for security when using Java based systems. Java provides
// policy files and an authorization and authentication API which lets you secure
// any reources (not just files) at various levels of granularity with various
// security mechanisms.
// Security policies may be universal, apply to a particular codebase, or
// using JAAS apply to individuals. Some indicative policy statements:
// grant {
//     permission java.net.SocketPermission "*", "connect";
//     permission java.io.FilePermission "C:\\users\\cathy\\foo.bat", "read";
// };
// grant codebase "file:./*", Principal ExamplePrincipal "Secret" {
//     permission java.io.FilePermission "dummy.txt", "read";
// };
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.18
//----------------------------------------------------------------------------------
// general purpose utility methods
def getString(buf,size){
    // consider get(buf[]) instead of get(buf) for efficiency
    b=[]; size.times{b+=buf.get()}; new String(b as byte[]).trim()
}
def getInt(buf,size) {
    // normally in Java we would just use methods like getLong()
    // to read a long but wish to ignore platform issues here
    long val = 0
    for (n in 0..<size) { val += ((int)buf.get() & 0xFF) << (n * 8) }
    return val
}
def getDate(buf) {
    return new Date(getInt(buf,4) * 1000) // Java uses millis
}

// specific utility method (wtmp file from ubuntu 6.10)
def processWtmpRecords(file, origpos) {
    channel = new RandomAccessFile(file, 'r').channel
    recsize = 4 + 4 + 32 + 4 + 32 + 256 + 8 + 4 + 40
    channel.position(origpos)
    newpos = origpos
    buf = ByteBuffer.allocate(recsize)
    while ((count = channel.read(buf)) != -1) {
        if (count != recsize) break
        buf.flip()
        print getInt(buf,4) + ' '         // type
        print getInt(buf,4) + ' '         // pid
        print getString(buf,32) + ' '     // line
        print getString(buf,4) + ' '      // inittab
        print getString(buf,32) + ' '     // user
        print getString(buf,256) + ' '    // hostname
        buf.position(buf.position() + 8)  // skip
        println "${getDate(buf)} "        // time
        buf.clear()
        newpos = channel.position()
    }
    return newpos
}

wtmp = new File('Pleac/data/wtmp')
// wtmpTailingScript:
sampleInterval = 2000 // 2000 millis = 2 secs
filePointer = wtmp.size() // begin tailing from the end of the file
while(true) {
    // Compare the length of the file to the file pointer
    long fileLength = wtmp.size()
    if( fileLength > filePointer ) {
        // There is data to read
        filePointer = processWtmpRecords(wtmp, filePointer)
    }
    // Sleep for the specified interval
    Thread.sleep( sampleInterval )
}
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.19
//----------------------------------------------------------------------------------
// contains most of the functionality of the original (not guaranteed to be perfect)
// -i ignores errors, e.g. if one target is write protected, the others will work
// -u writes files in unbuffered mode (ignore for '|')
// -n not to stdout
// -a all files are in append mode
// '>>file1' turn on append for individual file
// '|wc' or '|grep x' etc sends output to forked process (only one at any time)
class MultiStream {
    private targets
    private ignoreErrors
    MultiStream(List targets, ignore) {
        this.targets = targets
        ignoreErrors = ignore
    }
    def println(String content) {
        targets.each{
            try {
                it?.write(content.bytes)
            } catch (Exception ex) {
                if (!ignoreErrors) throw ex
                targets -= it
                it?.close()
            }
        }
    }
    def close() { targets.each{ it?.close() } }
}

class TeeTarget {
    private filename
    private stream
    private p

    TeeTarget(String name, append, buffered, ignore) {
        if (name.startsWith('>>')) {
            createFileStream(name[2..-1],true,buffered,ignore)
        } else if (name.startsWith('|')) {
            createProcessReader(name[1..-1])
        } else {
            createFileStream(name,append,buffered,ignore)
        }
    }

    TeeTarget(OutputStream stream) { this.stream = stream }

    def write(bytes) { stream?.write(bytes) }
    def close() { stream?.close() }

    private createFileStream(name, append, buffered, ignore) {
        filename = name
        def fos
        try {
            fos = new FileOutputStream(name, append)
        } catch (Exception ex) {
            if (ignore) return
        }
        if (!buffered) stream = fos
        else stream = new BufferedOutputStream(fos)
    }
    private createWriter(os) {new PrintWriter(new BufferedOutputStream(os))}
    private createReader(is) {new BufferedReader(new InputStreamReader(is))}
    private createPiperThread(br, pw) {
        Thread.start{
            def next
            while((next = br.readLine())!=null) {
                pw.println(next)
            }
            pw.flush(); pw.close()
        }
    }
    private createProcessReader(name) {
        def readFromStream = new PipedInputStream()
        def r1 = createReader(readFromStream)
        stream = new BufferedOutputStream(new PipedOutputStream(readFromStream))
        p = Runtime.runtime.exec(name)
        def w1 = createWriter(p.outputStream)
        createPiperThread(r1, w1)
        def w2 = createWriter(System.out)
        def r2 = createReader(p.inputStream)
        createPiperThread(r2, w2)
    }
}

targets = []
append = false; ignore = false; includeStdout = true; buffer = true
(0..<args.size()).each{
    arg = args[it]
    if (arg.startsWith('-')) {
        switch (arg) {
            case '-a': append = true; break
            case '-i': ignore = true; break
            case '-n': includeStdout = false; break
            case '-u': buffer = false; break
            default:
                println "usage: tee [-ainu] [filenames] ..."
                System.exit(1)
        }
    } else targets += arg
}
targets = targets.collect{ new TeeTarget(it, append, buffer, ignore) }
if (includeStdout) targets += new TeeTarget(System.out)
def tee = new MultiStream(targets, ignore)
while (line = System.in.readLine()) {
    tee.println(line)
}
tee.close()
//----------------------------------------------------------------------------------


// @@PLEAC@@_8.20
//----------------------------------------------------------------------------------
// most of the functionality - uses an explicit uid - ran on ubuntu 6.10 on intel
lastlog = new File('Pleac/data/lastlog')
channel = new RandomAccessFile(lastlog, 'r').channel
uid = 1000
recsize = 4 + 32 + 256
channel.position(uid * recsize)
buf = ByteBuffer.allocate(recsize)
channel.read(buf)
buf.flip()
date = getDate(buf)
line = getString(buf,32)
host = getString(buf,256)
println "User with uid $uid last logged on $date from ${host?host:'unknown'} on $line"
// => User with uid 1000 last logged on Sat Jan 13 09:09:35 EST 2007 from unknown on :0
//----------------------------------------------------------------------------------


// @@PLEAC@@_9.0
//----------------------------------------------------------------------------------
// Groovy builds on Java's file and io classes which provide an operating
// system independent abstraction of a file system. The actual File class
// is the main class of interest. It represents a potential file or
// directory - which may or may not (yet) exist. In versions of Java up to
// and including Java 6, the File class was missing some of the functionality
// required to implement some of the examples in the Chapter (workarounds
// and alternatives are noted below). In Java 7, (also known as "Dolphin")
// new File abstraction facilities are being worked on but haven't yet been
// publically released. These new features are known as JSR 203 and are
// referred to when relevant to some of the examples. Thanks to Alan Bateman
// from Sun for clarification regarding various aspects of JSR 203. Apologies
// if I misunderstood any aspects relayed to me and also usual disclaimers
// apply regarding features which may change or be dropped before release.

// path='/usr/bin'; file='vi' // linux/mac os?
path='C:/windows'; file='explorer.exe' // windows
entry = new File("$path")
assert entry.isDirectory()
entry = new File("$path/$file")
assert entry.isFile()

println File.separator
// => \ (on Windows)
// => / (on Unix)
// however if you just stick to backslashes Java converts for you
// in most situations

// File modification time (no exact equivalent of ctime - but you can
// call stat() using JNI or use exec() of dir or ls to get this kind of info)
// JSR 203 also plans to provide such info in Java 7.
println new Date(entry.lastModified())
// => Wed Aug 04 07:00:00 EST 2004

// file size
println entry.size()
// => 1032192

// check if we have permission to read the file
assert entry.canRead()

// check if file is binary or text?
// There is no functionality for this at the file level.
// Java has the Java Activation Framework (jaf) which is used to
// associate files (and streams) with MIME Types and subsequently
// binary data streams or character encodings for (potentially
// multilanguage) text files. JSR-203 provides a method to determine
// the MIME type of a file. Depending on the platform the file type may
// be determined based on a file attribute, file name "extension", the
// bytes of the files (byte sniffing) or other means. It is service
// provider based so developers can plug in their own file type detection
// mechanisms as required. "Out of the box" it will ship with file type
// detectors that are appropriate for the platform (integrates with GNOME,
// Windows registry, etc.).

// Groovy uses File for directories and files
// displayAllFilesInUsrBin:
new File('/usr/bin').eachFile{ file ->
  println "Inside /usr/bin is something called $file.name"
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.1
//----------------------------------------------------------------------------------
file = new File("filename")
file << 'hi'
timeModified = file.lastModified()
println new Date(timeModified)
// => Sun Jan 07 11:49:02 EST 2007

MILLIS_PER_WEEK = 60 * 60 * 24 * 1000 * 7
file.setLastModified(timeModified - MILLIS_PER_WEEK)
println new Date(file.lastModified())
// => Sun Dec 31 11:49:02 EST 2006

// Java currently doesn't provide access to other timestamps but
// there are things that can be done:
// (1) You can use JNI to call to C, e.g. stat()
// (2) Use exec() and call another program, e.g. dir, ls, ... to get the value you are after
// (3) Here is a Windows specific patch to get lastAccessedTime and creationTime
//     http://forum.java.sun.com/thread.jspa?forumID=31&start=0&threadID=409921&range=100#1800193
// (4) There is an informal patch for Java 5/6 which gives lastAccessedTime on Windows and Linux
//     and creationTime on windows:
//     http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6314708
// (5) JSR 203 (currently targetted for Java 7) aims to provide
//     "bulk access to file attributes, change notification, escape to filesystem-specific APIs"
//     this is supposed to include creationTime and lastAccessedTime along with many
//     security-related file attributes

// viFileWithoutChangingModificationTimeScript:
//#!/usr/bin/groovy
// uvi - vi a file without changing it's last modified time
if (args.size() != 1) {
  println "usage: uvi filename"
  System.exit(1)
}
file = args[0]
origTime = new File(file).lastModified()
"vi $file".execute()
new File(file).setLastModified(origTime)
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.2
//----------------------------------------------------------------------------------
println new File('/doesnotexist').exists()  // => false
println new File('/doesnotexist').delete()  // => false

new File('/createme') << 'Hi there'
println new File('/createme').exists()  // => true
println new File('/createme').delete()  // => true

names = ['file1','file2','file3']
files = names.collect{ new File(it) }
// create 2 of the files
files[0..1].each{ f -> f << f.name }

def deleteFiles(files) {
    def problemFileNames = []
    files.each{ f ->
        if (!f.delete())
            problemFileNames += f.name
    }
    def delCnt = files.size() - problemFileNames.size()
    println "Successfully deleted $delCnt of ${files.size()} file(s)"
    if (problemFileNames)
        println "Problems file(s): " + problemFileNames.join(', ')
}

deleteFiles(files)
// =>
// Successfully deleted 2 of 3 file(s)
// Problems file(s): file3

// we can also set files for deletion on exit
tempFile = new File('/xxx')
assert !tempFile.exists()
tempFile << 'junk'
assert tempFile.exists()
tempFile.deleteOnExit()
assert tempFile.exists()
// To confirm this is working, run these steps multiple times in a row.

// Discussion:
// Be careful with deleteOnExit() as there is no way to cancel it.
// There are also mechanisms specifically for creating unqiuely named temp files.
// On completion of JSR 203, there will be additional methods available for
// deleting which throw exceptions with detailed error messages rather than
// just return booleans.
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.3
//----------------------------------------------------------------------------------
// (1) Copy examples

//shared setup
dummyContent = 'some content' + System.getProperty('line.separator')
setUpFromFile()
setUpToFile()

// built-in copy via memory (text files only)
to << from.text
checkSuccessfulCopyAndDelete()

// built-in as a stream (text or binary) with optional encoding
to << from.asWritable('US-ASCII')
checkSuccessfulCopyAndDelete()

// built-in using AntBuilder
// for options, see: http://ant.apache.org/manual/CoreTasks/copy.html
new AntBuilder().copy( file: from.canonicalPath, tofile: to.canonicalPath )
checkSuccessfulCopyAndDelete()
// =>
//     [copy] Copying 1 file to D:\


// use Apache Jakarta Commons IO (jakarta.apache.org)
//import org.apache.commons.io.FileUtils
class FileUtils{}
// Copies a file to a new location preserving the lastModified date.
FileUtils.copyFile(from, to)
checkSuccessfulCopyAndDelete()

// using execute()
// "cp $from.canonicalPath $to.canonicalPath".execute()      // unix
println "cmd /c \"copy $from.canonicalPath $to.canonicalPath\"".execute().text    // dos vms
checkSuccessfulCopyAndDelete()
// =>
//        1 file(s) copied.

// (2) Move examples
// You can just do copy followed by delete but many OS's can just 'rename' in place
// so you can additionally do using Java's functionality:
assert from.renameTo(to)
assert !from.exists()
checkSuccessfulCopyAndDelete()
// whether renameTo succeeds if from and to are on different platforms
// or if to pre-exists is OS dependent, so you should check the return boolean

// alternatively, Ant has a move task:
// http://ant.apache.org/manual/CoreTasks/move.html

//helper methods
def checkSuccessfulCopyAndDelete() {
    assert to.text == dummyContent
    assert to.delete()
    assert !to.exists()
}
def setUpFromFile() {
    from = new File('/from.txt') // just a name
    from << dummyContent         // now its a real file with content
    from.deleteOnExit()          // that will be deleted on exit
}
def setUpToFile() {
    to = new File('C:/to.txt')     // target name
    to.delete() // ensure not left from previous aborted run
    assert !to.exists()          // double check
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.4
//----------------------------------------------------------------------------------
// Groovy (because of its Java heritage) doesn't have an exact
// equivalent of stat - as per 9.2 there are numerous mechanisms
// to achieve the equivalent, in particular, JSR203 (still in draft)
// has specific SymLink support including a FileId class in the
// java.nio.filesystems package. This will allow (depending on the
// operating system capabilities) files to be uniquely identified.
// If you work on Unix or Linux then you'll recognize this as it device/inode.

// If you are not interested in the above workarounds/future features
// and you are on a unix system, you can compare the absolutePath and
// canonicalPath attributes for a file. If they are different it is
// a symbolic link. On other operating systems, this difference is not
// to be relied upon and even on *nix systems, this will only get you
// so far and will also be relatively expensive resource and timewise.

// process only unique files
seen = []
def myProcessing(file) {
    def path = file.canonicalPath
    if (!seen.contains(path)) {
        seen << path
        // do something with file because we haven't seen it before
    }
}

// find linked files
seen = [:]
filenames = ['/dummyfile1.txt','/test.lnk','/dummyfile2.txt']
filenames.each{ filename ->
    def file = new File(filename)
    def cpath = file.canonicalPath
    if (!seen.containsKey(cpath)) {
        seen[cpath] = []
    }
    seen[cpath] += file.absolutePath
}

println 'Files with links:'
println seen.findAll{ k,v -> v.size() > 1 }
//---------------------------------------------------------------------------------

// @@PLEAC@@_9.5
//----------------------------------------------------------------------------------
// general pattern is:
// new File('dirname').eachFile{ /* do something ... */ }

// setup (change this on your system)
basedir = 'Pleac/src'

// process all files printing out full name (. and .. auto excluded)
new File(basedir).eachFile{ f->
    if (f.isFile()) println f.canonicalPath
}
// also remove dot files such as '.svn' and '.cvs' etc.
new File(basedir).eachFileMatch(~'^[^.].*'){ f->
    if (f.isFile()) println f.canonicalPath
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.6
//----------------------------------------------------------------------------------
// Globbing via Apache Jakarta ORO
//import org.apache.oro.io.GlobFilenameFilter
class GlobFilenameFilter{}
dir = new File(basedir)
namelist = dir.list(new GlobFilenameFilter('*.c'))
filelist = dir.listFiles(new GlobFilenameFilter('*.h') as FilenameFilter)

// Built-in matching using regex's
files = []
new File(basedir).eachFileMatch(~/\.[ch]$/){ f->
    if (f.isFile()) files += f
}

// Using Ant's FileScanner (supports arbitrary nested levels using **)
// For more details about Ant FileSets, see here:
// http://ant.apache.org/manual/CoreTypes/fileset.html
scanner = new AntBuilder().fileScanner {
    fileset(dir:basedir) {
        include(name:'**/pleac*.groovy')
        include(name:'Slowcat.*y')
        exclude(name:'**/pleac??.groovy') // chaps 10 and above
        exclude(name:'**/*Test*', unless:'testMode')
    }
}
for (f in scanner) {
    println "Found file $f"
}

// find and sort directories with numeric names
candidateFiles = new File(basedir).listFiles()
allDigits = { it.name =~ /^\d+$/ }
isDir = { it.isDirectory() }
dirs = candidateFiles.findAll(isDir).findAll(allDigits)*.canonicalPath.sort()
println dirs
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.7
//----------------------------------------------------------------------------------
// find all files recursively
dir = new File(basedir)
files = []
dir.eachFileRecurse{ files += it }

// find total size
sum = files.sum{ it.size() }
println "$basedir contains $sum bytes"
// => Pleac/src contains 365676 bytes

// find biggest
biggest = files.max{ it.size() }
println "Biggest file is $biggest.name with ${biggest.size()} bytes"
// => Biggest file is pleac6.groovy with 42415 bytes

// find most recently modified
youngest = files.max{ it.lastModified() }
println "Most recently modified is $youngest.name, changed ${new Date(youngest.lastModified())}"
// => Most recently modified is pleac9.groovy, changed Tue Jan 09 07:35:39 EST 2007

// find all directories
dir.eachDir{ println 'Found: ' + it.name}

// find all directories recursively
dir.eachFileRecurse{ f -> if (f.isDirectory()) println 'Found: ' + f.canonicalPath}
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.8
//----------------------------------------------------------------------------------
base = new File('path_to_somewhere_to_delete')

// delete using Jakarta Apache Commons IO
FileUtils.deleteDirectory(base)

// delete using Ant, for various options see:
// http://ant.apache.org/manual/CoreTasks/delete.html
ant = new AntBuilder()
ant.delete(dir: base)
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.9
//----------------------------------------------------------------------------------
names = ['Pleac/src/abc.java', 'Pleac/src/def.groovy']
names.each{ name -> new File(name).renameTo(new File(name + '.bak')) }

// The Groovy way of doing rename using an expr would be to use a closure
// for the expr:
// groovySimpleRenameScript:
//#!/usr/bin/groovy
// usage rename closure_expr filenames
op = args[0]
println op
files = args[1..-1]
shell = new GroovyShell(binding)
files.each{ f ->
    newname = shell.evaluate("$op('$f')")
    new File(f).renameTo(new File(newname))
}

// this would allow processing such as:
//% rename "{n -> 'FILE_' + n.toUpperCase()}" files
// with param pleac9.groovy => FILE_PLEAC9.GROOVY
//% rename "{n -> n.replaceAll(/9/,'nine') }" files
// with param pleac9.groovy => pleacnine.groovy
// The script could also be modified to take the list of
// files from stdin if no args were present (not shown).

// The above lets you type any Groovy code, but instead you might
// decide to provide the user with some DSL-like additions, e.g.
// adding the following lines into the script:
sep = File.separator
ext = { '.' + it.tokenize('.')[-1] }
base = { new File(it).name - ext(it) }
parent = { new File(it).parent }
lastModified = { new Date(new File(it).lastModified()) }
// would then allow the following more succinct expressions:
//% rename "{ n -> parent(n) + sep + base(n).reverse() + ext(n) }" files
// with param Pleac/src/pleac9.groovy => Pleac\src\9caelp.groovy
//% rename "{ n -> base(n) + '_' + lastModified(n).year + ext(n) }" files
// with param pleac9.groovy => pleac9_07.groovy

// As a different alternative, you could hook into Ant's mapper mechanism.
// You wouldn't normally type in this from the command-line but it could
// be part of a script, here is an example (excludes the actual rename part)
ant = new AntBuilder()
ant.pathconvert(property:'result',targetos:'windows'){
    path(){ fileset(dir:'Pleac/src', includes:'pleac?.groovy') }
    compositemapper{
        globmapper(from:'*1.groovy', to:'*1.groovy.bak')
        regexpmapper(from:/^(.*C2)\.(.*)$/, to:/\1_beta.\2/, casesensitive:'no')
        chainedmapper{
            packagemapper(from:'*pleac3.groovy', to:'*3.xml')
            filtermapper(){ replacestring(from:'C:.', to:'') }
        }
        chainedmapper{
            regexpmapper(from:/^(.*)4\.(.*)$/, to:/\1_4.\2/)
            flattenmapper()
            filtermapper(){ replacestring(from:'4', to:'four') }
        }
    }
}
println ant.antProject.getProperty('result').replaceAll(';','\n')
// =>
// C:\Projects\GroovyExamples\Pleac\src\pleac1.groovy.bak
// C:\Projects\GroovyExamples\Pleac\src\pleac2_beta.groovy
// Projects.GroovyExamples.Pleac.src.3.xml
// pleac_four.groovy
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.10
//----------------------------------------------------------------------------------
// Splitting a Filename into Its Component Parts
path = new File('Pleac/src/pleac9.groovy')
assert path.parent == 'Pleac' + File.separator + 'src'
assert path.name == 'pleac9.groovy'
ext = path.name.tokenize('.')[-1]
assert ext == 'groovy'

// No fileparse_set_fstype() equivalent in Groovy/Java. Java's File constructor
// automatically performs such a parse and does so appropriately of the operating
// system it is running on. In addition, 3rd party libraries allow platform
// specific operations ot be performed. As an example, many Ant tasks are OS
// aware, e.g. the pathconvert task (callable from an AntBuilder instance) has
// a 'targetos' parameter which can be one of 'unix', 'windows', 'netware',
// 'tandem' or 'os/2'.
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.11
//----------------------------------------------------------------------------------
// Given the previous discussion regarding the lack of support for symlinks
// in Java's File class without exec'ing to the operating system or doing
// a JNI call (at least until JSR 203 arrives), I have modified this example
// to perform an actual replica forest of actual file copies rather than
// a shadow forest full of symlinks pointing back at the real files.
// Use Apache Jakarta Commons IO
srcdir = new File('Pleac/src') // path to src
destdir = new File('C:/temp') // path to dest
preserveFileStamps = true
FileUtils.copyDirectory(srcdir, destdir, preserveFileStamps)
//----------------------------------------------------------------------------------

// @@PLEAC@@_9.12
//----------------------------------------------------------------------------------
//#!/usr/bin/groovy
// lst - list sorted directory contents (depth first)
// Given the previous discussion around Java's more limited Date
// information available via the File class, this will be a reduced
// functionality version of ls
LONG_OPTION = 'l'
REVERSE_OPTION = 'r'
MODIFY_OPTION = 'm'
SIZE_OPTION = 's'
HELP_OPTION = 'help'

op = new joptsimple.OptionParser()
op.accepts( LONG_OPTION, 'long listing' )
op.accepts( REVERSE_OPTION, 'reverse listing' )
op.accepts( MODIFY_OPTION, 'sort based on modification time' )
op.accepts( SIZE_OPTION, 'sort based on size' )
op.accepts( HELP_OPTION, 'display this message' )

options = op.parse(args)
if (options.wasDetected( HELP_OPTION )) {
    op.printHelpOn( System.out )
} else {
    sort = {}
    params = options.nonOptionArguments()
    longFormat = options.wasDetected( LONG_OPTION )
    reversed = options.wasDetected( REVERSE_OPTION )
    if (options.wasDetected( SIZE_OPTION )) {
        sort = {a,b -> a.size()<=>b.size()}
    } else if (options.wasDetected( MODIFY_OPTION )) {
        sort = {a,b -> a.lastModified()<=>b.lastModified()}
    }
    displayFiles(params, longFormat, reversed, sort)
}

def displayFiles(params, longFormat, reversed, sort) {
    files = []
    params.each{ name -> new File(name).eachFileRecurse{ files += it } }
    files.sort(sort)
    if (reversed) files = files.reverse()
    files.each { file ->
        if (longFormat) {
            print (file.directory ? 'd' : '-' )
            print (file.canRead() ? 'r' : '-' )
            print (file.canWrite() ? 'w ' : '- ' )
            //print (file.canExecute() ? 'x' : '-' ) // Java 6
            print file.size().toString().padLeft(12) + ' '
            print new Date(file.lastModified()).toString().padRight(22)
            println '  ' + file
        } else {
            println file
        }
    }
}

// =>
// % lst -help
// Option Description
// ------ -------------------------------
// --help display this message
// -l     long listing
// -m     sort based on modification time
// -r     reverse listing
// -s     sort based on size
//
// % lst -l -m Pleac/src Pleac/lib
// ...
// drw            0 Mon Jan 08 22:33:00 EST 2007  Pleac\lib\.svn
// -rw        18988 Mon Jan 08 22:33:41 EST 2007  Pleac\src\pleac9.groovy
// -rw         2159 Mon Jan 08 23:15:41 EST 2007  Pleac\src\lst.groovy
//
// % -l -s -r Pleac/src Pleac/lib
// -rw      1034049 Sun Jan 07 19:24:41 EST 2007  Pleac\lib\ant.jar
// -r-      1034049 Sun Jan 07 19:40:27 EST 2007  Pleac\lib\.svn\text-base\ant.jar.svn-base
// -rw       421008 Thu Jun 02 15:15:34 EST 2005  Pleac\lib\ant-nodeps.jar
// -rw       294436 Sat Jan 06 21:19:58 EST 2007  Pleac\lib\geronimo-javamail_1.3.1_mail-1.0.jar
// ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_10.0
//----------------------------------------------------------------------------------
def hello() {
    greeted += 1
    println "hi there!"
}

// We need to initialize greeted before it can be used, because "+=" assumes predefinition
greeted = 0
hello()
println greeted
// =>
// hi there
// 1
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.1
//----------------------------------------------------------------------------------
// basic method calling examples
// In Groovy, parameters are named anyway
def hypotenuse(side1, side2) {
    Math.sqrt(side1**2 + side2**2)    // sqrt in Math package
}
diag = hypotenuse(3, 4)
assert diag == 5

// the star operator will magically convert an Array into a "tuple"
a = [5, 12]
assert hypotenuse(*a) == 13

// both = men + women

// In Groovy, all objects are references, so the same problem arises.
// Typically we just return a new object. Especially for immutable objects
// this style of processing is very common.
nums = [1.4, 3.5, 6.7]
def toInteger(n) {
    n.collect { v -> v.toInteger() }
}
assert toInteger(nums) == [1, 3, 6]

orignums = [1.4, 3.5, 6.7]
def truncMe(n) {
    (0..<n.size()).each{ idx -> n[idx] = n[idx].toInteger() }
}
truncMe(orignums)
assert orignums == [1, 3, 6]
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.2
//----------------------------------------------------------------------------------
// variable scope examples
def somefunc() {
    def variableInMethod  // private is default in a method
}

def name // private is default for variable in a script

bindingVar = 10 // this will be in the binding (sort of global)
globalArray = []

// In Groovy, run_check can't access a, b, or c until they are
// explicitely defined global (using leading $), even if they are
// both defined in the same scope

def checkAccess(x) {
    def y = 200
    return x + y + bindingVar // access private, param, global
}
assert checkAccess(7) == 217

def saveArray(ary) {
    globalArray << 'internal'
    globalArray += ary
}

saveArray(['important'])
assert globalArray == ["internal", "important"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.3
//----------------------------------------------------------------------------------
// you want a private persistent variable within a script method

// you could use a helper class for this
class CounterHelper {
    private static counter = 0
    def static next() { ++counter }
}
def greeting(s) {
    def n = CounterHelper.next()
    println "Hello $s  (I have been called $n times)"
}
greeting('tom')
greeting('dick')
greeting('harry')
// =>
// Hello tom  (I have been called 1 times)
// Hello dick  (I have been called 2 times)
// Hello harry  (I have been called 3 times)

// you could make it more fancy by having separate keys,
// using synchronisation, singleton pattern, ThreadLocal, ...
//----------------------------------------------------------------------------------


// @@PLEAC@@_10.4
//----------------------------------------------------------------------------------
// Determining Current Method Name
// Getting class, package and static info is easy. Method info is just a little work.
// From Java we can use:
//     new Exception().stackTrace[0].methodName
// or for Java 5 and above (saves relatively expensive exception creation)
//     Thread.currentThread().stackTrace[3].methodName
// But these give the Java method name. Groovy wraps its own runtime
// system over the top. It's still a Java method, just a little bit further up the
// stack from where we might expect. Getting the Groovy method name can be done in
// an implementation specific way (subject to change as the language evolves):
def myMethod() {
    names = new Exception().stackTrace*.methodName
    println groovyUnwrap(names)
}
def myMethod2() {
    names = Thread.currentThread().stackTrace*.methodName
    names = names[3..<names.size()] // skip call to dumpThread
    println groovyUnwrap(names)
}
def groovyUnwrap(names) { names[names.indexOf('invoke0')-1] }
myMethod()  // => myMethod
myMethod2() // => myMethod2

// Discussion: If what you really wanted was a tracing mechanism, you could overrie
// invokeMethod and print out method names before calling the original method. Or
// you could use one of the Aspect-Oriented Programming packages for Java.
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.5
//----------------------------------------------------------------------------------
// Passing Arrays and Hashes by Reference
// In Groovy, every value is a reference to an object, thus there is
// no such problem, just call: arrayDiff(array1, array2)

// pairwise add (altered so it doesn't require equal sizes)
def pairWiseAdd(a1, a2) {
    s1 = a1.size(); s2 = a2.size()
    (0..<[s1,s2].max()).collect{
        it > s1-1 ? a2[it] : (it > s2-1 ? a1[it] : a1[it] + a2[it])
    }
}
a = [1, 2]
b = [5, 8]
assert pairWiseAdd(a, b) == [6, 10]

// also works for unequal sizes
b = [5, 8, -1]
assert pairWiseAdd(a, b) == [6, 10, -1]
b = [5]
assert pairWiseAdd(a, b) == [6, 2]

// We could check if both arguments were of a particular type, e.g.
// (a1 instanceof List) or (a2.class.isArray()) but duck typing allows
// it to work on other things as well, so while wouldn't normally do this
// you do need to be a little careful when calling the method, e.g.
// here we call it with two maps of strings and get back strings
// the important thing here was that the arguments were indexed
// 0..size-1 and that the items supported the '+' operator (as String does)
a = [0:'Green ', 1:'Grey ']
b = [0:'Frog', 1:'Elephant', 2:'Dog']
assert pairWiseAdd(a, b) == ["Green Frog", "Grey Elephant", "Dog"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.6
//----------------------------------------------------------------------------------
// Detecting Return Context
// There is no exact equivalent of return context in Groovy but
// you can behave differently when called under different circumstances
def addValueOrSize(a1, a2) {
     b1 = (a1 instanceof Number) ? a1 : a1.size()
     b2 = (a2 instanceof Number) ? a2 : a2.size()
     b1 + b2
}
assert (addValueOrSize(10, 'abcd')) == 14
assert (addValueOrSize(10, [25, 50])) == 12
assert (addValueOrSize('abc', [25, 50])) == 5
assert (addValueOrSize(25, 50)) == 75

// Of course, a key feature of many OO languages including Groovy is
// method overloading so that responding to dofferent parameters has
// a formal way of being captured in code with typed methods, e.g.
class MakeBiggerHelper {
    def triple(List iList) { iList.collect{ it * 3 } }
    def triple(int i) { i * 3 }
}
mbh = new MakeBiggerHelper()
assert mbh.triple([4, 5]) == [12, 15]
assert mbh.triple(4) == 12

// Of course with duck typing, we can rely on dynamic typing if we want
def directTriple(arg) {
    (arg instanceof Number) ? arg * 3 : arg.collect{ it * 3 }
}
assert directTriple([4, 5]) == [12, 15]
assert directTriple(4) == 12
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.7
//----------------------------------------------------------------------------------
// Passing by Named Parameter
// Groovy supports named params or positional arguments with optional
// defaults to simplify method calling

// named arguments work by using a map
def thefunc(Map args) {
    // in this example, we just call the positional version
    thefunc(args.start, args.end, args.step)
}

// positional arguments with defaults
def thefunc(start=0, end=30, step=10) {
    ((start..end).step(step))
}

assert thefunc()                        == [0, 10, 20, 30]
assert thefunc(15)                      == [15, 25]
assert thefunc(0,40)                    == [0, 10, 20, 30, 40]
assert thefunc(start:5, end:20, step:5) == [5, 10, 15, 20]
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.8
//----------------------------------------------------------------------------------
// Skipping Selected Return Values
// Groovy 1.0 doesn't support multiple return types, so you always use
// a holder class, array or collection to return multiple values.
def getSystemInfo() {
    def millis = System.currentTimeMillis()
    def freemem = Runtime.runtime.freeMemory()
    def version = System.getProperty('java.vm.version')
    return [millis:millis, freemem:freemem, version:version]
    // if you are likely to want all the information use a list
    //     return [millis, freemem, version]
    // or dedicated holder class
    //     return new SystemInfo(millis, freemem, version)
}
result = getSystemInfo()
println result.version
// => 1.5.0_08-b03
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.9
//----------------------------------------------------------------------------------
// Returning More Than One Array or Hash
// As per 10.8, Groovy 1.0 doesn't support multiple return types but you
// just use a holder class, array or collection. There are no limitations
// on returning arbitrary nested values using this technique.
def getInfo() {
    def system = [millis:System.currentTimeMillis(),
                  version:System.getProperty('java.vm.version')]
    def runtime = [freemem:Runtime.runtime.freeMemory(),
                   maxmem:Runtime.runtime.maxMemory()]
    return [system:system, runtime:runtime]
}
println info.runtime.maxmem // => 66650112 (info automatically calls getInfo() here)
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.10
//----------------------------------------------------------------------------------
// Returning Failure
// This is normally done in a heavy-weight way via Java Exceptions
// (see 10.12) or in a lightweight way by returning null
def sizeMinusOne(thing) {
    if (thing instanceof Number) return
    thing.size() - 1
}
def check(thing) {
    result = sizeMinusOne(thing)
    println (result ? "Worked with result: $result" : 'Failed')
}
check(4)
check([1, 2])
check('abc')
// =>
// Failed
// Worked with result: 1
// Worked with result: 2
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.11
//----------------------------------------------------------------------------------
// Prototyping Functions: Not supported by Groovy but arguably
// not important given other language features.

// Omitting Parentheses Scenario: Groovy only lets you leave out
// parentheses in simple cases. If you had two methods sum(a1,a2,a3)
// and sum(a1,a2), there would be no way to indicate that whether
// 'sum sum 2, 3, 4, 5' meant sum(sum(2,3),4,5) or sum(sum(2,3,4),5).
// You would have to include the parentheses. Groovy does much less
// auto flattening than some other languages; it provides a *args
// operator, varargs style optional params and supports method
// overloading and ducktyping. Perhaps these other features mean
// that this scenario is always easy to avoid.
def sum(a,b,c){ a+b+c*2 }
def sum(a,b){ a+b }
// sum sum 1,2,4,5
// => compilation error
sum sum(1,2),4,5
sum sum(1,2,4),5
// these work but if you try to do anything fancy you will run into trouble;
// your best bet is to actually include all the parentheses:
println sum(sum(1,2),4,5) // => 17
println sum(sum(1,2,4),5) // => 16

// Mimicking built-ins scenario: this is a mechanism to turn-off
// auto flattening, Groovy only does flattening in restricted circumstances.
// func(array, 1, 2, 3) is never coerced into a single list but varargs
// and optional args can be used instead
def push(list, Object[] optionals) {
    optionals.each{ list.add(it) }
}
items = [1,2]
newItems = [7, 8, 9]
push items, 3, 4
push items, 6
push (items, *newItems) // brackets currently required, *=flattening
                        // without *: items = [1, 2, 3, 4, 6, [7, 8, 9]]
assert items == [1, 2, 3, 4, 6, 7, 8, 9]
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.12
//----------------------------------------------------------------------------------
// Handling Exceptions
// Same story as in Java but Groovy has some nice Checked -> Unchecked
// magic behind the scenes (Java folk will know what this means)
// When writing methods:
//     throw exception to raise it
// When calling methods:
//     try ... catch ... finally surrounds processing logic
def getSizeMostOfTheTime(s) {
    if (s =~ 'Full Moon') throw new RuntimeException('The world is ending')
    s.size()
}
try {
    println 'Size is: ' + getSizeMostOfTheTime('The quick brown fox')
    println 'Size is: ' + getSizeMostOfTheTime('Beware the Full Moon')
} catch (Exception ex) {
    println "Error was: $ex.message"
} finally {
    println 'Doing common cleanup'
}
// =>
// Size is: 19
// Error was: The world is ending
// Doing common cleanup
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.13
//----------------------------------------------------------------------------------
// Saving Global Values
// We can just save the value and restore it later:
def printAge() { println "Age is $age" }

age = 18         // binding "global" variable
printAge()       // => 18

if (age > 0) {
    def origAge = age
    age = 23
    printAge()   // => 23
    age = origAge
}
printAge()       // => 18

// Depending on the circmstances we could enhance this in various ways
// such as synchronizing, surrounding with try ... finally, using a
// memento pattern, saving the whole binding, using a ThreadLocal ...

// There is no need to use local() for filehandles or directory
// handles in Groovy because filehandles are normal objects.
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.14
//----------------------------------------------------------------------------------
// Redefining a Function
// This can be done via a number of ways:

// OO approach:
// The standard trick using OO is to override methods in subclasses
class Parent { def foo(){ println 'foo' } }
class Child extends Parent { def foo(){ println 'bar' } }
new Parent().foo()   // => foo
new Child().foo()    // => bar

// Category approach:
// If you want to redefine a method from an existing library
// you can use categories. This can be done to avoid name conflicts
// or to patch functionality with local mods without changing
// original code
println new Date().toString()
// => Sat Jan 06 16:44:55 EST 2007
class DateCategory {
    static toString(Date self) { 'not telling' }
}
use (DateCategory) {
    println new Date().toString()
}
// => not telling

// Closure approach:
// Groovy's closures let you have "anonymous methods" as objects.
// This allows you to be very flexible with "method" redefinition, e.g.:
colors = 'red yellow blue green'.split(' ').toList()
color2html = new Expando()
colors.each { c ->
    color2html[c] = { args -> "<FONT COLOR='$c'>$args</FONT>" }
}
println color2html.yellow('error')
// => <FONT COLOR='yellow'>error</FONT>
color2html.yellow = { args -> "<b>$args</b>" } // too hard to see yellow
println color2html.yellow('error')
// => <b>error</b>

// Other approaches:
// you could use invokeMethod to intercept the original method and call
// your modified method on just particular input data
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.15
//----------------------------------------------------------------------------------
// Trapping Undefined Function Calls
class FontHelper {
    // we could define all the important colors explicitly like this
    def pink(info) {
        buildFont('hot pink', info)
    }
    // but this method will catch any undefined ones
    def invokeMethod(String name, Object args) {
        buildFont(name, args.join(' and '))
    }
    def buildFont(name, info) {
        "<FONT COLOR='$name'>" + info + "</FONT>"
    }
}
fh = new FontHelper()
println fh.pink("panther")
println fh.chartreuse("stuff", "more stuff")
// =>
// <FONT COLOR='hot pink'>panther</FONT>
// <FONT COLOR='chartreuse'>stuff and more stuff</FONT>
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.16
//----------------------------------------------------------------------------------
// Simulating Nested Subroutimes: Using Closures within Methods
def outer(arg) {
    def x = arg + 35
    inner = { x * 19 }
    x + inner()
}
assert outer(10) == 900
//----------------------------------------------------------------------------------

// @@PLEAC@@_10.17
//----------------------------------------------------------------------------------
// Program: Sorting Your Mail
//#!/usr/bin/groovy
//import javax.mail.*
class URLName{}
// solution using mstor package (mstor.sf.net)
session = Session.getDefaultInstance(new Properties())
store = session.getStore(new URLName('mstor:/path_to_your_mbox_directory'))
store.connect()

// read messages from Inbox
inbox = store.defaultFolder.getFolder('Inbox')
inbox.open(Folder.READ_ONLY)
messages = inbox.messages.toList()

// extractor closures
subject = { m -> m.subject }
subjectExcludingReplyPrefix = { m -> subject(m).replaceAll(/(?i)Re:\\s*/,'') } // double slash to single outside triple quotes
date = { m -> d = m.sentDate; new Date(d.year, d.month, d.date) } // ignore time fields

// sort by subject excluding 'Re:' prefixs then print subject for first 6
println messages.sort{subjectExcludingReplyPrefix(it)}[0..5]*.subject.join('\n')
// =>
// Additional Resources for JDeveloper 10g (10.1.3)
// Amazon Web Services Developer Connection Newsletter #18
// Re: Ant 1.7.0?
// ARN Daily | 2007: IT predictions for the year ahead
// Big Changes at Gentleware
// BigPond Account Notification

// sort by date then subject (print first 6 entries)
sorted = messages.sort{ a,b ->
    date(a) == date(b) ?
        subjectExcludingReplyPrefix(a) <=> subjectExcludingReplyPrefix(b) :
        date(a) <=> date(b)
}
sorted[0..5].each{ m -> println "$m.sentDate: $m.subject" }
// =>
// Wed Jan 03 08:54:15 EST 2007: ARN Daily | 2007: IT predictions for the year ahead
// Wed Jan 03 15:33:31 EST 2007: EclipseSource: RCP Adoption, Where Art Thou?
// Wed Jan 03 00:10:11 EST 2007: What's New at Sams Publishing?
// Fri Jan 05 08:31:11 EST 2007: Building a Sustainable Open Source Business
// Fri Jan 05 09:53:45 EST 2007: Call for Participation: Agile 2007
// Fri Jan 05 05:51:36 EST 2007: IBM developerWorks Weekly Edition, 4 January 2007

// group by date then print first 2 entries of first 2 dates
groups = messages.groupBy{ date(it) }
groups.keySet().toList()[0..1].each{
    println it
    println groups[it][0..1].collect{ '    ' + it.subject }.join('\n')
}
// =>
// Wed Jan 03 00:00:00 EST 2007
//     ARN Daily | 2007: IT predictions for the year ahead
//     EclipseSource: RCP Adoption, Where Art Thou?
// Fri Jan 05 00:00:00 EST 2007
//     Building a Sustainable Open Source Business
//     Call for Participation: Agile 2007

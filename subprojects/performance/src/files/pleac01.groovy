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

// @@PLEAC@@_1.0
//----------------------------------------------------------------------------------
string = '\\n'                    // two characters, \ and an n
assert string.size() == 2
string = "\n"                     // a "newline" character
string = '\n'                     // a "newline" character

string = "Jon 'Maddog' Orwant"    // literal single quote inside double quotes
string = 'Jon \'Maddog\' Orwant'  // escaped single quotes

string = 'Jon "Maddog" Orwant'    // literal double quotes inside single quotes
string = "Jon \"Maddog\" Orwant"  // escaped double quotes

string = '''
This is a multiline string declaration
using single quotes (you can use double quotes)
'''
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.1
//----------------------------------------------------------------------------------
// accessing substrings
string = 'hippopotamus'
start = 5; end = 7; endplus1 = 8
assert string.substring(start, endplus1) == 'pot'
assert string[start..end] == 'pot'

assert string.substring(start) == 'potamus'
assert string[start..-1] == 'potamus'

// String is immutable but new strings can be created in various ways
assert string - 'hippo' - 'mus' + 'to' == 'potato'
assert string.replace('ppopotam','bisc') == 'hibiscus'
assert string.substring(0, 2) + 'bisc' + string[-2..-1] == 'hibiscus'
// StringBuffer is mutable
sb = new StringBuffer(string)
sb[2..-3] = 'bisc'
assert sb.toString() == 'hibiscus'

// No exact pack/unpack equivalents exist in Groovy. Examples here use a custom
// implementation to split an original string into chunks of specified length
// the method is a modified version of the Java PLEAC version

// get a 5-character string, skip 8, then grab 2 5-character strings
// skipping the trailing spaces, then grab the rest
data = 'hippopotamus means river horse'
def fields = unpack('A5 x8 A5 x1 A5 x1 A*', data)
assert fields == ['hippo', 'means', 'river', 'horse']

// On a Java 5 or 6 JVM, Groovy can also make use of Scanners:
s = new Scanner(data)
s.findInLine(/(.{5}).{8}(.{5}) (.{5}) (.*)/)
m = s.match()
fields = []
(1..m.groupCount()).each{ fields << m.group(it) }
assert fields == ['hippo', 'means', 'river', 'horse']

// another scanner example similar to the javadoc example
input = '1 fish 2 fish red fish blue fish'
s = new Scanner(input).useDelimiter(/\s*fish\s*/)
fields = []
2.times{ fields << s.nextInt() }
2.times{ fields << s.next() }
assert fields == [1, 2, 'red', 'blue']

// split at five characters boundaries
String[] fivers = unpack('A5 ' * (data.length() / 5), data)
assert fivers == ["hippo", "potam", "us me", "ans r", "iver ", "horse"]

// chop string into individual characters
assert 'abcd' as String[] == ['a', 'b', 'c', 'd']

string = "This is what you have"
// Indexing forwards  (left to right)
// tens   000000000011111111112
// units +012345678901234567890
// Indexing backwards (right to left)
// tens   221111111111000000000
// units  109876543210987654321-

assert string[0]          == 'T'
assert string[5..6]       == 'is'
assert string[13..-1]     == 'you have'
assert string[-1]         == 'e'
assert string[-4..-1]     == 'have'
assert string[-8, -7, -6] == 'you'

data = new StringBuffer(string)
data[5..6] = "wasn't"       ; assert data.toString() == "This wasn't what you have"
data[-12..-1] = "ondrous"   ; assert data.toString() == "This wasn't wondrous"
data[0..0] = ""             ; assert data.toString() == "his wasn't wondrous"
data[-10..-1]  = ""         ; assert data.toString() == "his wasn'"

string = "This wasn't wondrous"
// check last ten characters match some pattern
assert string[-10..-1] =~ /^t\sw.*s$/

string = 'This is a test'
assert string[0..4].replaceAll('is', 'at') + string[5..-1] == 'That is a test'

// exchange the first and last letters in a string
string = 'make a hat'
string = string[-1] + string[1..-2] + string[0]
assert string == 'take a ham'

// extract column with unpack
string = 'To be or not to be'

// skip 6, grab 6
assert unpack("x6 A6", string) == ['or not']

// forward 6, grab 2, backward 5, grab 2
assert unpack("x6 A2 X5 A2", string) == ['or', 'be']

assert cut2fmt([8, 14, 20, 26, 30]) == 'A7 A6 A6 A6 A4 A*'

// utility method (derived from Java PLEAC version)
def unpack(String format, String data) {
    def result = []
    int formatOffset = 0, dataOffset = 0
    int minDataOffset = 0, maxDataOffset = data.size()

    new StringTokenizer(format).each{ token ->
        int tokenLen = token.length()

        // count determination
        int count = 0
        if (tokenLen == 1) count = 1
        else if (token.charAt(1) == '*') count = -1
        else count = token[1..-1].toInteger()

        // action determination
        char action = token.charAt(0)
        switch (action) {
            case 'A':
                if (count == -1) {
                    start = [dataOffset, maxDataOffset].min()
                    result.add(data[start..-1])
                    dataOffset = maxDataOffset
                } else {
                    start = [dataOffset, maxDataOffset].min()
                    end = [dataOffset + count, maxDataOffset].min()
                    result.add(data[start..<end])
                    dataOffset += count
                }
                break
            case 'x':
                if (count == -1) dataOffset = maxDataOffset
                else dataOffset += count
                break
            case 'X':
                if (count == -1) dataOffset = minDataOffset
                else dataOffset -= count
                break
            default:
                throw new RuntimeException('Unknown action token', formatOffset)
        }
        formatOffset += tokenLen + 1
    }
    return result as String[]
}

// utility method
def cut2fmt(positions) {
    template = ''
    lastpos = 1
    for (pos in positions) {
        template += 'A' + (pos - lastpos) + ' '
        lastpos = pos
    }
    return template + 'A*'
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.2
//----------------------------------------------------------------------------------
// use b if b is true, else c
b = false; c = 'cat'
assert (b ? b : c) == 'cat'
b = true
assert (b ? b : c)
// can be simplified to 'b || c' if c is a boolean
// strictly speaking, b doesn't have to be a boolean,
// e.g. an empty list is coerced to boolean false
b = []
assert (b ? b : c) == 'cat'

// set x to y unless x is already true
x = false; y = 'dog'
if (!x) x = y
assert x == 'dog'
// can be simplified to 'x ||= y' if y is a boolean
// x doesn't need to be a boolean, e.g. a non-empty
// string is coerced to boolean true
x = 'cat'
if (!x) x = y
assert x == 'cat'

// JVM supplies user name
// otherwise could use exec or built-in Ant features for reading environment vars
assert System.getProperty('user.name')

// test for nullity then for emptyness
def setDefaultIfNullOrEmpty(startingPoint) {
    (!startingPoint || startingPoint.length() == 0) ? 'Greenwich' : startingPoint
}
assert setDefaultIfNullOrEmpty(null) == 'Greenwich'
assert setDefaultIfNullOrEmpty('') == 'Greenwich'
assert setDefaultIfNullOrEmpty('Something else') == 'Something else'
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.3
//----------------------------------------------------------------------------------
v1 = 'alpha'; v2 = 'omega'
// this can done with explicit swapping via a temp variable
// or in a slightly more interesting way with a closure
swap = { temp = v1; v1 = v2; v2 = temp }
swap()
assert v1 == 'omega' && v2 == 'alpha'
// a more generic swap() is also possible using Groovy's metaclass mechanisms
// but is not idiomatic of Groovy usage
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.4
//----------------------------------------------------------------------------------
// char and int are interchangeable, apart from precision difference
// char use 16 bits while int use 32, requiring a cast from int to char
char ch = 'e'
int num = ch         // no problem
ch = (char) num  // needs an explicit cast

s1 = "Number " + num + " is character " + (char) num
assert s1 == 'Number 101 is character e'
s2 = "Character " + ch + " is number " + (int) ch
assert s2 == 'Character e is number 101'

// easy conversion between char arrays, char lists and Strings
char[] ascii = "sample".toCharArray() // {115, 97, 109, 112, 108, 101}
assert new String(ascii) == "sample"
assert new String([115, 97, 109, 112, 108, 101] as char[]) == "sample"

// convert 'HAL' to 'IBM' (in increasing order of Grooviness)
assert "HAL".toCharArray().collect{new String(it+1 as char[])}.join() == 'IBM'
assert ("HAL" as String[]).collect{it.next()}.join() == 'IBM'
assert "HAL".replaceAll('.', {it.next()}) == 'IBM'
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.5
//----------------------------------------------------------------------------------
string = "an apple a day"
assert string[3..7].split('')[1..5] == ['a', 'p', 'p', 'l', 'e']
assert string.split('').toList().unique().sort().join() == ' adelnpy'

//----------------------------------------------------------------------------------
// CheckSum.groovy: Compute 16-bit checksum of input file
// Usage: groovy CheckSum <file>
// script:
checksum = 0
new File(args[0]).eachByte{ checksum += it }
checksum %= (int) Math.pow(2, 16) - 1
println checksum
//----------------------------------------------------------------------------------
// to run on its own source code:
//=> % groovy CheckSum CheckSum.groovy
//=> 9349
//----------------------------------------------------------------------------------
// Slowcat.groovy: Emulate a  s l o w  line printer
// Usage: groovy Slowcat <file> <delay_millis_between_each_char>
// script:
delay = args[1].toInteger()
new File(args[0]).eachByte{ print ((char) it); Thread.sleep(delay) }
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.6
//----------------------------------------------------------------------------------
assert 'string'.reverse() == 'gnirts'

string = 'Yoda said, "can you see this?"'
revwords = string.split(' ').toList().reverse().join(' ')
assert revwords == 'this?" see you "can said, Yoda'

words = ['bob', 'alpha', 'rotator', 'omega', 'reviver']
long_palindromes = words.findAll{ w -> w == w.reverse() && w.size() > 5 }
assert long_palindromes == ['rotator', 'reviver']
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.7
//----------------------------------------------------------------------------------
s1 = 'abc\t def\tghi \n\tx'
s2 = 'abc      def    ghi \n        x'
def expand(s) {
    s.split('\n').toList().collect{
        line = it
        while (line.contains('\t')) {
            line = line.replaceAll(/([^\t]*)(\t)(.*)/){
                all,pre,tab,suf -> pre + ' ' * (8 - pre.size() % 8) + suf
            }
        }
        return line
    }.join('\n')
}
def unexpand(s) {
    s.split('\n').toList().collect{
        line = it
        for (i in line.size()-1..1) {
            if (i % 8 == 0) {
                prefix = line[0..<i]
                if (prefix.trim().size() != prefix.size()) {
                    line = prefix.trim() + '\t' + line[i..-1]
                }
            }
        }
        return line
    }.join('\n')
}
assert expand(s1) == s2
assert unexpand(s2) == s1
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.8
//----------------------------------------------------------------------------------
debt = 150
assert "You owe $debt to me" == 'You owe 150 to me'

rows = 24; cols = 80
assert "I am $rows high and $cols wide" == 'I am 24 high and 80 wide'

assert 'I am 17 years old'.replaceAll(/\d+/, {2*it.toInteger()}) == 'I am 34 years old'
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.9
//----------------------------------------------------------------------------------
assert "bo peep".toUpperCase() == 'BO PEEP'
assert 'JOHN'.toLowerCase() == 'john'
def capitalize(s) {s[0].toUpperCase() + (s.size()<2 ? '' : s[1..-1]?.toLowerCase())}
assert capitalize('joHn') == 'John'

s = "thIS is a loNG liNE".replaceAll(/\w+/){capitalize(it)}
assert s == 'This Is A Long Line'

s1 = 'JOhn'; s2 = 'joHN'
assert s1.equalsIgnoreCase(s2)

Random rand
def randomCase(char ch) {
    (rand.nextInt(100) < 20) ? Character.toLowerCase(ch) : ch
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.10
//----------------------------------------------------------------------------------
n = 10
assert "I have ${n+1} guanacos." == 'I have 11 guanacos.'
assert "I have " + (n+1) + " guanacos." == 'I have 11 guanacos.'

// sending templated email is solved in two parts: templating and sending
// Part 1: creating an email template
naughty = 'Mr Bad Credit'
def get_manager_list(s) { 'The Big Boss' }
msg = """
To: $naughty
From: Your Bank
Cc: ${ get_manager_list(naughty) }
Date: ${ new Date() }

Dear $naughty,

Today, you bounced check number ${ 500 + new Random().nextInt(100) } to us.
Your account is now closed.

Sincerely,
the management
"""
expected = '''
To: Mr Bad Credit
From: Your Bank
Cc: The Big Boss
Date: XXX

Dear Mr Bad Credit,

Today, you bounced check number XXX to us.
Your account is now closed.

Sincerely,
the management
'''
sanitized = msg.replaceAll('(?m)^Date: (.*)$','Date: XXX')
sanitized = sanitized.replaceAll(/(?m)check number (\d+) to/,'check number XXX to')
assert sanitized == expected
// note: Groovy also has several additional built-in templating facilities
// Part 2: sending email
// SendMail.groovy: Send email
// Usage: groovy SendEmail <msgfile>
// script:
class AntBuilder{}
ant = new AntBuilder()
ant.mail(from:'manager@grumpybank.com', tolist:'innocent@poorhouse.com',
    encoding:'plain', mailhost:'mail.someserver.com',
    subject:'Friendly Letter', message:'this is a test message')
// Ant has many options for setting encoding, security, attachments, etc., see:
// http://ant.apache.org/manual/CoreTasks/mail.html
// Groovy could also use the Java Mail Api directly if required
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.11
//----------------------------------------------------------------------------------
def raw = '''
    your text
    goes here
'''

def expected = '''
your text
goes here
'''

assert raw.split('\n').toList().collect{
    it.replaceAll(/^\s+/,'')
}.join('\n') + '\n' == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.12
//----------------------------------------------------------------------------------
input = '''Folding and splicing is the work of an editor,
 not a mere collection of silicon
 and
 mobile electrons!'''

expected = '''Folding and splicing
is the work of an
editor, not a mere
collection of
silicon and mobile
electrons!'''

def wrap(text, maxSize) {
    all = []
    line = ''
    text.eachMatch(/\S+/) {
        word = it[0]
        if (line.size() + 1 + word.size() > maxSize) {
            all += line
            line = word
        } else {
            line += (line == '' ? word : ' ' + word)
        }
    }
    all += line
    return all.join('\n')
}
assert wrap(input, 20) == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.13
//----------------------------------------------------------------------------------
string = /Mom said, "Don't do that."/
// backslash special chars
assert string.replaceAll(/['"]/){'\\'+it[0]} == /Mom said, \"Don\'t do that.\"/
// double special chars
assert string.replaceAll(/['"]/){it[0]+it[0]} == /Mom said, ""Don''t do that.""/
//backslash quote all non-capital letters
//assert "DIR /?".replaceAll(/[^A-Z]/){/\\/+it[0]} == /DIR\ \/\?/
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.14
//----------------------------------------------------------------------------------
assert '     x     '.trim() == 'x'
// print what's typed, but surrounded by >< symbols
// script:
new BufferedReader(new InputStreamReader(System.in)).eachLine{
    println(">" + it.trim() + "<");
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.15
//----------------------------------------------------------------------------------
pattern = /"([^\"\\]*(?:\\.[^\"\\]*)*)",?|([^,]+),?|,/
line = /XYZZY,"","O'Reilly, Inc","Wall, Larry","a \"glug\" bit,",5,"Error, Core Dumped"/
m = line =~ pattern
expected = [/XYZZY/, '', /O'Reilly, Inc/, /Wall, Larry/,     //'
            /a \"glug\" bit,/, /5/, /Error, Core Dumped/]
for (i in 0..<m.size().toInteger())
    assert expected[i] == (m[i][2] ? m[i][2] : m[i][1])

//----------------------------------------------------------------------------------

// @@PLEAC@@_1.16
//----------------------------------------------------------------------------------
// A quick google search found several Java implementations.
// As an example, how to use commons codec is shown below.
// Just place the respective jar in your classpath.
// Further details: http://jakarta.apache.org/commons/codec
// require(groupId:'commons-codec', artifactId:'commons-codec', version:'1.3')
soundex = new org.apache.commons.codec.language.Soundex()
assert soundex.soundex('Smith') == soundex.soundex('Smyth')
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.17
//----------------------------------------------------------------------------------
input = '''I have analysed the new part. As long as you
aren't worried about the colour, it is a dropin replacement.'''        //'

expected = '''I have analyzed the new part. As long as you
aren't worried about the color, it is a drop-in replacement.'''        //'

translations = [colour:'color', analysed:'analyzed', dropin:'drop-in']

def fixstyle(s) {
    s.split('\n').toList().collect{
        line = it
        translations.each{ key, value ->
            line = line.replaceAll(/(?<=\W)/ + key + /(?=\W)/, value)
        }
        return line
    }.join('\n')
}
assert fixstyle(input) == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_1.18
//----------------------------------------------------------------------------------
// Solved in two parts: 'screenscrape' text stream and return stream from process
// Part 1: text scraping
input = '''
      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND
     4636       1    4636       4636  con 1005 08:24:50 /usr/bin/bash
      676    4636     676        788  con 1005 13:53:32 /usr/bin/ps
'''
select1 = '''
      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND
      676    4636     676        788  con 1005 13:53:32 /usr/bin/ps
'''
select2 = '''
      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND
     4636       1    4636       4636  con 1005 08:24:50 /usr/bin/bash
'''

// line below must be configured for your unix - this one's cygwin
format = cut2fmt([10, 18, 26, 37, 42, 47, 56])
def psgrep(s) {
    out = []
    lines = input.split('\n').findAll{ it.size() }
    vars = unpack(format, lines[0]).toList().collect{ it.toLowerCase().trim() }
    out += lines[0]
    lines[1..-1].each{
        values = unpack(format, it).toList().collect{
            try {
                return it.toInteger()
            } catch(NumberFormatException e) {
                return it.trim()
            }
        }
        vars.eachWithIndex{ var, i ->
            binding.setVariable(var, values[i])
        }
        if (new GroovyShell(binding).evaluate(s)) out += it
    }
    return '\n' + out.join('\n') + '\n'
}
assert psgrep('winpid < 800') == select1
assert psgrep('uid % 5 == 0 && command =~ /sh$/') == select2
// Part 2: obtaining text stream from process
// unixScript:
input = 'ps'.execute().text
// cygwinScript:
input = 'path_to_cygwin/bin/ps.exe'.execute().text
// windowsScript:
// can use something like sysinternal.com s pslist (with minor script tweaks)
input = 'pslist.exe'.execute().text
//----------------------------------------------------------------------------------

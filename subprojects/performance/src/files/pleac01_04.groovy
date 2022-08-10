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
// char and int are interchangable, apart from precision difference
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

// @@PLEAC@@_2.1
//----------------------------------------------------------------------------------
// four approaches possible (shown for Integers, similar for floats, double etc.):
// (1) NumberFormat.getInstance().parse(s)    // getInstance() can take locale
// (2) Integer.parseInt(s)
// (3) new Integer(s)
// (4) regex
import java.text.*
int nb = 0
try {
    nb = NumberFormat.getInstance().parse('33.5') // '.5' will be ignored
    nb = NumberFormat.getInstance().parse('abc')
} catch (ParseException ex) {
    assert ex.getMessage().contains('abc')
}
assert nb == 33

try {
    nb = Integer.parseInt('34')
    assert nb == 34
    nb = new Integer('35')
    nb = Integer.parseInt('abc')
} catch (NumberFormatException ex) {
    assert ex.getMessage().contains('abc')
}
assert nb == 35

integerPattern = /^[+-]?\d+$/
assert '-36' =~ integerPattern
assert !('abc' =~ integerPattern)
decimalPattern = /^-?(?:\d+(?:\.\d*)?|\.\d+)$/
assert '37.5' =~ decimalPattern
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.2
//----------------------------------------------------------------------------------
// Groovy defaults to BigDecimal if you don't use an explicit float or double
wage = 5.36
week = 40 * wage
assert "One week's wage is: \$$week" == /One week's wage is: $214.40/
// if you want to use explicit doubles and floats you can still use
// printf in version 5, 6 or 7 JVMs
// printf('%5.2f', week as double)
// => 214.40
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.3
//----------------------------------------------------------------------------------
a = 0.255
b = a.setScale(2, BigDecimal.ROUND_HALF_UP);
assert a.toString() == '0.255'
assert b.toString() == '0.26'

a = [3.3 , 3.5 , 3.7, -3.3] as double[]
// warning rint() rounds to nearest integer - slightly different to Perl's int()
rintExpected = [3.0, 4.0, 4.0, -3.0] as double[]
floorExpected = [3.0, 3.0, 3.0, -4.0] as double[]
ceilExpected = [4.0, 4.0, 4.0, -3.0] as double[]
a.eachWithIndex{ val, i ->
    assert Math.rint(val) == rintExpected[i]
    assert Math.floor(val) == floorExpected[i]
    assert Math.ceil(val) == ceilExpected[i]
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.4
//----------------------------------------------------------------------------------
assert Integer.parseInt('0110110', 2) == 54
assert Integer.toString(54, 2) == '110110'
// also works for other radix values, e.g. hex
assert Integer.toString(60, 16) == '3c'

//----------------------------------------------------------------------------------

// @@PLEAC@@_2.5
//----------------------------------------------------------------------------------
x = 3; y = 20
for (i in x..y) {
    //i is set to every integer from x to y, inclusive
}

(x..<y).each {
    //implicit closure variable it is set to every integer from x up to but excluding y
}

assert (x..y).step(7) == [3, 10, 17]

years = []
(5..<13).each{ age -> years += age }
assert years == [5, 6, 7, 8, 9, 10, 11, 12]
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.6
//----------------------------------------------------------------------------------
// We can add additional methods to the Integer class
class IntegerCategory {
    static def romanMap = [1000:'M', 900:'CM', 500:'D', 400:'CD', 100:'C', 90:'XC',
                           50:'L', 40:'XL', 10:'X', 9:'IX', 5:'V', 4:'IV', 1:'I']

    static getRoman(Integer self) {
        def remains = self
        def text = ''
        romanMap.keySet().sort().reverse().each{ key ->
            while (remains >= key) {
                remains -= key
                text += romanMap[key]
            }
        }
        return text
    }

    static int parseRoman(Object self, String input) {
        def ustr = input.toUpperCase()
        int sum = 0
        romanMap.keySet().sort().reverse().each{ key ->
            while (ustr.startsWith(romanMap[key])) {
                sum += key
                ustr -= romanMap[key]
            }
        }
        return sum
    }
}

use(IntegerCategory) {
    int fifteen = 15
    assert fifteen.roman == 'XV'
    assert parseRoman('XXVI') == 26
    for (i in 1..3900) {
        assert i == parseRoman(i.roman)
    }
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.7
//----------------------------------------------------------------------------------
random = new Random()
100.times{
    next = random.nextInt(50) + 25
    assert next > 24
    assert next < 76
}
chars = []
['A'..'Z','a'..'z','0'..'9',('!@$%^&*' as String[]).toList()].each{chars += it}
password = (1..8).collect{ chars[random.nextInt(chars.size())] }.join()
assert password.size() == 8
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.8
//----------------------------------------------------------------------------------
// By default Groovy uses Java's Random facilities which use the current time
// as the initial seed. This always changes but does so slowly over time.
// You are free to select a better seed if you want greater randomness or
// use the same one each time if you need repeatability.
long seed = System.currentTimeMillis()
random1 = new Random(seed)
random2 = new Random(seed)
assert random1.nextInt() == random2.nextInt()
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.9
//----------------------------------------------------------------------------------
// java.util.Random which Groovy uses already uses a 48-bit seed
// You can make use 64 not 48 bits (and make better use of the 48 bits) see here:
// http://alife.co.uk/nonrandom/
// You can choose a better seed, e.g. Ant uses:
seed = System.currentTimeMillis() + Runtime.runtime.freeMemory()
// You can accept input from the user, e.g.
// http://examples.oreilly.com/javacrypt/files/oreilly/jonathan/util/Seeder.java
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.10
//----------------------------------------------------------------------------------
// use Java's Random.nextGaussian() method
random = new Random()
mean = 25
sdev = 2
salary = random.nextGaussian() * sdev + mean
// script:
printf 'You have been hired at \$%.2f', salary
// => You have been hired at $25.05
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.11
//----------------------------------------------------------------------------------
// radians = Math.toRadians(degrees)
assert Math.toRadians(90) == Math.PI / 2
// degrees = Math.toDegrees(radians)
assert Math.toDegrees(Math.PI) == 180
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.12
//----------------------------------------------------------------------------------
// use Java's trigonometry methods in java.lang.Math
//----------------------------------------------------------------------------------
t = Math.tan(1.5)
assert t > 14.1 && t < 14.11
ac = Math.acos(0.1)
assert ac > 1.47 && ac < 1.48
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.13
//----------------------------------------------------------------------------------
assert Math.log(Math.E) == 1
assert Math.log10(10000) == 4
def logn(base, val) { Math.log(val)/Math.log(base) }
assert logn(2, 1024) == 10
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.14
//----------------------------------------------------------------------------------
// there are several Java Matrix packages available, e.g.
// http://math.nist.gov/javanumerics/jama
//import Jama.Matrix
class Matrix{}
matrix1 = new Matrix([
   [3, 2, 3],
   [5, 9, 8]
] as double[][])

matrix2 = new Matrix([
   [4, 7],
   [9, 3],
   [8, 1]
] as double[][])

expectedArray = [[54.0, 30.0], [165.0, 70.0]] as double[][]
productArray = matrix1.times(matrix2).array

for (i in 0..<productArray.size()) {
    assert productArray[i] == expectedArray[i]
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.15
//----------------------------------------------------------------------------------
// there are several Java Complex number packages, e.g.:
// http://jakarta.apache.org/commons/math/userguide/complex.html
//import org.apache.commons.math.complex.Complex
class Complex{}
a = new Complex(3, 5)  // 3 + 5i
b = new Complex(2, -2) // 2 - 2i
expected = new Complex (16, 4) // 16 + 4i
assert expected == a * b
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.16
//----------------------------------------------------------------------------------
assert Integer.parseInt('101', 16) == 257
assert Integer.parseInt('077', 8) == 63
//----------------------------------------------------------------------------------
// conversionScript:
print 'Gimme a number in decimal, octal, or hex: '
reader = new BufferedReader(new InputStreamReader(System.in))
input = reader.readLine().trim()
switch(input) {
    case ~'^0x\\d+':
        number = Integer.parseInt(input.substring(2), 16); break
    case ~'^0\\d+':
        number = Integer.parseInt(input.substring(1), 8); break
    default:
        number = Integer.parseInt(input)
}
println 'Decimal value: ' + number

// permissionScript:
print 'Enter file permission in octal: '
input = new BufferedReader(new InputStreamReader(System.in))
num = input.readLine().trim()
permission = Integer.parseInt(num, 8)
println 'Decimal value: ' + permission
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.17
//----------------------------------------------------------------------------------
nf = NumberFormat.getInstance()
assert nf.format(-1740525205) == '-1,740,525,205'
//----------------------------------------------------------------------------------
// @@PLEAC@@_2.18
//----------------------------------------------------------------------------------
def timeMessage(hour) { 'It took ' + hour + ' hour' + (hour == 1 ? '' : 's') }
assert 'It took 1 hour' == timeMessage(1)
assert 'It took 2 hours' == timeMessage(2)

// you can also use Java's ChoiceFormat
// overkill for this example but extensible and compatible with MessageFormat
limits = [1, ChoiceFormat.nextDouble(1)] as double[]
names = ['century', 'centuries'] as String[]
choice = new ChoiceFormat(limits, names)
numCenturies = 1
expected = 'It took 1 century'
assert expected == "It took $numCenturies " + choice.format(numCenturies)
// an alternate constructor syntax
choice = new ChoiceFormat('0#are no files|1#is one file|2#are multiple files')
assert choice.format(3) == 'are multiple files'

// more complex pluralization can be done with Java libraries, e.g.:
// http://www.elvis.ac.nz/brain?PluralizationMapping
// org.springframework.util.Pluralizer within the Spring Framework (springframework.org)
//----------------------------------------------------------------------------------

// @@PLEAC@@_2.19
//----------------------------------------------------------------------------------
// calculating prime factors
def factorize(BigInteger orig) {
    factors = [:]
    def addFactor = { x -> if (factors[x]) factors[x] += 1 else factors[x] = 1 }
    n = orig
    i = 2
    sqi = 4               // square of i
    while (sqi <= n) {
        while (n.remainder(i) == 0) {
            n /= i
            addFactor i
        }
        // we take advantage of the fact that (i+1)**2 = i**2 + 2*i + 1
        sqi += 2 * i + 1
        i += 1
    }
    if ((n != 1) && (n != orig)) addFactor n
    return factors
}

def pretty(factors) {
    if (!factors) return "PRIME"
    sb = new StringBuffer()
    factors.keySet().sort().each { key ->
        sb << key
        if (factors[key] > 1) sb << "**" + factors[key]
        sb << " "
    }
    return sb.toString().trim()
}

assert pretty(factorize(2178)) == '2 3**2 11**2'
assert pretty(factorize(39887)) == 'PRIME'
assert pretty(factorize(239322000000000000000000)) == '2**19 3 5**18 39887'
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.0
//----------------------------------------------------------------------------------
// use Date to get the current time
println new Date()
// => Mon Jan 01 07:12:32 EST 2007
// use Calendar to compute year, month, day, hour, minute, and second values
cal = Calendar.instance
println 'Today is day ' + cal.get(Calendar.DAY_OF_YEAR) + ' of the current year.'
// => Today is day 1 of the current year.
// there are other Java Date/Time packages with extended capabilities, e.g.:
//     http://joda-time.sourceforge.net/
// there is a special Grails (grails.org) time DSL (see below)
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.1
//----------------------------------------------------------------------------------
cal = Calendar.instance
Y = cal.get(Calendar.YEAR)
M = cal.get(Calendar.MONTH) + 1
D = cal.get(Calendar.DATE)
println "The current date is $Y $M $D"
// => The current date is 2006 04 28
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.2
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = Calendar.instance
// set time zone using long or short timezone values
cal.timeZone = TimeZone.getTimeZone("America/Los_Angeles")
cal.timeZone = TimeZone.getTimeZone("UTC")
// set date fields one at a time
cal.set(Calendar.MONTH, Calendar.DECEMBER)
// or several together
//calendar.set(year, month - 1, day, hour, minute, second)
// get time in seconds since EPOCH
long time = cal.time.time / 1000
println time
// => 1196522682
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.3
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = Calendar.instance
// set time
cal.time = new Date(time * 1000)
// get date fields
println('Dateline: '
    + cal.get(Calendar.HOUR_OF_DAY) + ':'
    + cal.get(Calendar.MINUTE) + ':'
    + cal.get(Calendar.SECOND) + '-'
    + cal.get(Calendar.YEAR) + '/'
    + (cal.get(Calendar.MONTH) + 1) + '/'
    + cal.get(Calendar.DATE))
// => Dateline: 7:33:16-2007/1/1
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.4
//----------------------------------------------------------------------------------
import java.text.SimpleDateFormat
long difference = 100
long after = time + difference
long before = time - difference

// any field of a calendar is incrementable via add() and roll() methods
cal = Calendar.instance
df = new SimpleDateFormat()
printCal = {cal -> df.format(cal.time)}
cal.set(2000, 0, 1, 00, 01, 0)
assert printCal(cal) == '1/01/00 00:01'
// roll minute back by 2 but don't adjust other fields
cal.roll(Calendar.MINUTE, -2)
assert printCal(cal) == '1/01/00 00:59'
// adjust hour back 1 and adjust other fields if needed
cal.add(Calendar.HOUR, -1)
assert printCal(cal) == '31/12/99 23:59'

// larger example
cal.timeZone = TimeZone.getTimeZone("UTC")
cal.set(1973, 0, 18, 3, 45, 50)
cal.add(Calendar.DATE, 55)
cal.add(Calendar.HOUR_OF_DAY, 2)
cal.add(Calendar.MINUTE, 17)
cal.add(Calendar.SECOND, 5)
assert printCal(cal) == '14/03/73 16:02'

// alternatively, work with epoch times
long birthTime = 96176750359       // 18/Jan/1973, 3:45:50 am
long interval = 5 +                // 5 second
                17 * 60 +          // 17 minute
                2  * 60 * 60 +     // 2 hour
                55 * 60 * 60 * 24  // and 55 day
then = new Date(birthTime + interval * 1000)
assert df.format(then) == '14/03/73 16:02'

// Alternatively, the Google Data module has a category with DSL-like time support:
// http://docs.codehaus.org/display/GROOVY/Google+Data+Support
// which supports the following syntax
// def interval = 5.seconds + 17.minutes + 2.hours + 55.days
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.5
//----------------------------------------------------------------------------------
bree = 361535725  // 16 Jun 1981, 4:35:25
nat  =  96201950  // 18 Jan 1973, 3:45:50
difference = bree - nat
println "There were $difference seconds between Nat and Bree"
// => There were 265333775 seconds between Nat and Bree
seconds    =  difference % 60
difference = (difference - seconds) / 60
minutes    =  difference % 60
difference = (difference - minutes) / 60
hours      =  difference % 24
difference = (difference - hours)   / 24
days       =  difference % 7
weeks      = (difference - days)    /  7
println "($weeks weeks, $days days, $hours:$minutes:$seconds)"
// => (438 weeks, 4 days, 23:49:35)
//----------------------------------------------------------------------------------
cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
cal.set(1981, 5, 16)  // 16 Jun 1981
date1 = cal.time
cal.set(1973, 0, 18)  // 18 Jan 1973
date2 = cal.time
difference = Math.abs(date2.time - date1.time)
days = difference / (1000 * 60 * 60 * 24)
assert days == 3071
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.6
//----------------------------------------------------------------------------------
// create a calendar with current time and time zone
cal = Calendar.instance
cal.set(1981, 5, 16)
yearDay = cal.get(Calendar.DAY_OF_YEAR);
year = cal.get(Calendar.YEAR);
yearWeek = cal.get(Calendar.WEEK_OF_YEAR);
df1 = new SimpleDateFormat("dd/MMM/yy")
df2 = new SimpleDateFormat("EEEE")
print(df1.format(cal.time) + ' was a ' + df2.format(cal.time))
println " and was day number $yearDay and week number $yearWeek of $year"
// => 16/Jun/81 was a Tuesday and was day number 167 and week number 25 of 1981
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.7
//----------------------------------------------------------------------------------
input = "1998-06-03"
df1 = new SimpleDateFormat("yyyy-MM-dd")
date = df1.parse(input)
df2 = new SimpleDateFormat("MMM/dd/yyyy")
println 'Date was ' + df2.format(date)
// => Date was Jun/03/1998
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.8
//----------------------------------------------------------------------------------
import java.text.DateFormat
df = new SimpleDateFormat('E M d hh:mm:ss z yyyy')
cal.set(2007, 0, 1)
println 'Customized format gives: ' + df.format(cal.time)
// => Mon 1 1 09:02:29 EST 2007 (differs depending on your timezone)
df = DateFormat.getDateInstance(DateFormat.FULL, Locale.FRANCE)
println 'Customized format gives: ' + df.format(cal.time)
// => lundi 1 janvier 2007
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.9
//----------------------------------------------------------------------------------
// script:
println 'Press return when ready'
before = System.currentTimeMillis()
input = new BufferedReader(new InputStreamReader(System.in)).readLine()
after = System.currentTimeMillis()
elapsed = (after - before) / 1000
println "You took $elapsed seconds."
// => You took2.313 seconds.

// take mean sorting time
size = 500; number = 100; total = 0
for (i in 0..<number) {
    array = []
    size.times{ array << Math.random() }
    doubles = array as double[]
    // sort it
    long t0 = System.currentTimeMillis()
    Arrays.sort(doubles)
    long t1 = System.currentTimeMillis()
    total += (t1 - t0)
}
println "On average, sorting $size random numbers takes ${total / number} milliseconds"
// => On average, sorting 500 random numbers takes 0.32 milliseconds
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.10
//----------------------------------------------------------------------------------
delayMillis = 50
Thread.sleep(delayMillis)
//----------------------------------------------------------------------------------

// @@PLEAC@@_3.11
//----------------------------------------------------------------------------------
// this could be done more simply using JavaMail's getAllHeaderLines() but is shown
// in long hand for illustrative purposes
sampleMessage = '''Delivered-To: alias-someone@somewhere.com.au
Received: (qmail 27284 invoked from network); 30 Dec 2006 15:16:26 -0000
Received: from unknown (HELO lists-outbound.sourceforge.net) (66.35.250.225)
  by bne012m.server-web.com with SMTP; 30 Dec 2006 15:16:25 -0000
Received: from sc8-sf-list2-new.sourceforge.net (sc8-sf-list2-new-b.sourceforge.net [10.3.1.94])
    by sc8-sf-spam2.sourceforge.net (Postfix) with ESMTP
    id D8CCBFDE3; Sat, 30 Dec 2006 07:16:24 -0800 (PST)
Received: from sc8-sf-mx1-b.sourceforge.net ([10.3.1.91]
    helo=mail.sourceforge.net)
    by sc8-sf-list2-new.sourceforge.net with esmtp (Exim 4.43)
    id 1H0fwX-0003c0-GA
    for pleac-discuss@lists.sourceforge.net; Sat, 30 Dec 2006 07:16:20 -0800
Received: from omta05ps.mx.bigpond.com ([144.140.83.195])
    by mail.sourceforge.net with esmtp (Exim 4.44) id 1H0fwY-0005D4-DD
    for pleac-discuss@lists.sourceforge.net; Sat, 30 Dec 2006 07:16:19 -0800
Received: from win2K001 ([138.130.127.127]) by omta05ps.mx.bigpond.com
    with SMTP
    id <20061230151611.XVWL19269.omta05ps.mx.bigpond.com@win2K001>;
    Sat, 30 Dec 2006 15:16:11 +0000
From: someone@somewhere.com
To: <pleac-discuss@lists.sourceforge.net>
Date: Sun, 31 Dec 2006 02:14:57 +1100
Subject: Re: [Pleac-discuss] C/Posix/GNU - @@pleac@@_10x
Content-Type: text/plain; charset="us-ascii"
Content-Transfer-Encoding: 7bit
Sender: pleac-discuss-bounces@lists.sourceforge.net
Errors-To: pleac-discuss-bounces@lists.sourceforge.net

----- Original Message -----
From: someone@somewhere.com
To: otherperson@somewhereelse.com
Cc: <pleac-discuss@lists.sourceforge.net>
Sent: Wednesday, December 27, 2006 9:18 AM
Subject: Re: [Pleac-discuss] C/Posix/GNU - @@pleac@@_10x

I really like that description of PLEAC.
'''
expected = '''
Sender                    Recipient                 Time              Delta
<origin>                  somewhere.com             01:14:57 06/12/31 
win2K001                  omta05ps.mx.bigpond.com   01:14:57 06/12/31 1m 14s
omta05ps.mx.bigpond.com   mail.sourceforge.net      01:16:11 06/12/31 8s
sc8-sf-mx1-b.sourceforge. sc8-sf-list2-new.sourcefo 01:16:19 06/12/31 1s
sc8-sf-list2-new.sourcefo sc8-sf-spam2.sourceforge. 01:16:20 06/12/31 4s
unknown                   bne012m.server-web.com    01:16:24 06/12/31 1s
'''

class MailHopDelta {
    def headers, firstSender, firstDate, out

    MailHopDelta(mail) {
        extractHeaders(mail)
        out = new StringBuffer()
        def m = (mail =~ /(?m)^Date:\s+(.*)/)
        firstDate = parseDate(m[0][1])
        firstSender = (mail =~ /(?m)^From.*\@([^\s>]*)/)[0][1]
        out('Sender Recipient Time Delta'.split(' '))
    }

    def parseDate(date) {
        try {
            return new SimpleDateFormat('EEE, dd MMM yyyy hh:mm:ss Z').parse(date)
        } catch(java.text.ParseException ex) {}
        try {
            return new SimpleDateFormat('dd MMM yyyy hh:mm:ss Z').parse(date)
        } catch(java.text.ParseException ex) {}
        try {
            return DateFormat.getDateInstance(DateFormat.FULL).parse(date)
        } catch(java.text.ParseException ex) {}
        DateFormat.getDateInstance(DateFormat.LONG).parse(date)
    }

    def extractHeaders(mail) {
        headers = []
        def isHeader = true
        def currentHeader = ''
        mail.split('\n').each{ line ->
            if (!isHeader) return
            switch(line) {
                case ~/^\s*$/:
                    isHeader = false
                    if (currentHeader) headers << currentHeader
                    break
                case ~/^\s+.*/:
                    currentHeader += line; break
                default:
                    if (currentHeader) headers << currentHeader
                    currentHeader = line
            }
        }
    }

    def out(line) {
        out << line[0][0..<[25,line[0].size()].min()].padRight(26)
        out << line[1][0..<[25,line[1].size()].min()].padRight(26)
        out << line[2].padRight(17) + ' '
        out << line[3] + '\n'
    }

    def prettyDate(date) {
        new SimpleDateFormat('hh:mm:ss yy/MM/dd').format(date)
    }

    def process() {
        out(['<origin>', firstSender, prettyDate(firstDate), ''])
        def prevDate = firstDate
        headers.grep(~/^Received:\sfrom.*/).reverseEach{ hop ->
            def from = (hop =~ /from\s+(\S+)|\((.*?)\)/)[0][1]
            def by   = (hop =~ /by\s+(\S+\.\S+)/)[0][1]
            def hopDate = parseDate(hop[hop.lastIndexOf(';')+2..-1])
            out([from, by, prettyDate(prevDate), prettyDelta(hopDate.time - prevDate.time)])
            prevDate = hopDate
        }
        return out.toString()
    }

    def prettyField(secs, sign, ch, multiplier, sb) {
        def whole = (int)(secs / multiplier)
        if (!whole) return 0
        sb << '' + (sign * whole) + ch + ' '
        return whole * multiplier
    }

    def prettyDelta(millis) {
        def sign = millis < 0 ? -1 : 1
        def secs = (int)Math.abs(millis/1000)
        def sb = new StringBuffer()
        secs -= prettyField(secs, sign, 'd', 60 * 60 * 24, sb)
        secs -= prettyField(secs, sign, 'h', 60 * 60, sb)
        secs -= prettyField(secs, sign, 'm', 60, sb)
        prettyField(secs, sign, 's', 1, sb)
        return sb.toString().trim()
    }
}

assert '\n' + new MailHopDelta(sampleMessage).process() == expected
//----------------------------------------------------------------------------------


// @@PLEAC@@_4.0
//----------------------------------------------------------------------------------
simple = [ "this", "that", "the", "other" ]
nested = [ "this", "that", [ "the", "other" ] ]
assert nested.size() == 3
assert nested[2].size() == 2

flattenNestedToSimple = [ "this", "that", [ "the", "other" ] ].flatten()
assert flattenNestedToSimple.size() == 4
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.1
//----------------------------------------------------------------------------------
a = [ "quick", "brown", "fox" ]
assert a.size() == 3
a = 'Why are you teasing me?'.split(' ')
assert a == ["Why", "are", "you", "teasing", "me?"]

removeLeadingSpaces = { it.trim() }
nonBlankLines = { it }
lines = '''
    The boy stood on the burning deck,
    It was as hot as glass.
'''.split('\n').collect(removeLeadingSpaces).findAll(nonBlankLines)

assert lines == ["The boy stood on the burning deck,",
                 "It was as hot as glass."]

// initialiseListFromFileScript:
lines = new File('mydata.txt').readLines()

// processFileScript:
new File('mydata.txt').eachLine{
    // dosomething
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.2
//----------------------------------------------------------------------------------
marbleColors = ['red', 'green', 'yellow']
assert marbleColors.join(', ') == 'red, green, yellow'

def commify(items) {
    if (!items) return items
    def sepchar = items.find{ it =~ /,/ } ? '; ' : ', '
    switch (items.size()) {
        case 1: return items[0]
        case 2: return items.join(' and ')
    }
    items[0..-2].join(sepchar) + sepchar + 'and ' + items[-1]
}

assert commify(marbleColors) == 'red, green, and yellow'

lists = [
    [ 'just one thing' ],
    [ 'Mutt', 'Jeff' ],
    'Peter Paul Mary'.split(' '),
    [ 'To our parents', 'Mother Theresa', 'God' ],
    [ 'pastrami', 'ham and cheese', 'peanut butter and jelly', 'tuna' ],
    [ 'recycle tired, old phrases', 'ponder big, happy thoughts' ],
    [ 'recycle tired, old phrases',
      'ponder big, happy thoughts',
      'sleep and dream peacefully' ],
]

expected = '''
just one thing
Mutt and Jeff
Peter, Paul, and Mary
To our parents, Mother Theresa, and God
pastrami, ham and cheese, peanut butter and jelly, and tuna
recycle tired, old phrases and ponder big, happy thoughts
recycle tired, old phrases; ponder big, happy thoughts; and sleep and dream peacefully
'''

assert expected == '\n' + lists.collect{commify(it)}.join('\n') + '\n'
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.3
//----------------------------------------------------------------------------------
// In Groovy, lists and arrays are more or less interchangeable
// here is the example using lists
people = ['Crosby', 'Stills', 'Nash']
assert people.size() == 3
people[3] = 'Young'
assert people.size() == 4
assert people == ['Crosby', 'Stills', 'Nash', 'Young']
// to use arrays simply do 'people = peopleArray.toList()' at the start
// and 'peopleArray = people as String[]' at the end
// if you attempt to do extension on a Java array you will get an
// ArrayIndexOutOfBoundsException - which is why Java has ArrayList et al
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.4
//----------------------------------------------------------------------------------
// list to process
people == ['Crosby', 'Stills', 'Nash', 'Young']
// helper
startsWithCapital = { word -> word[0] in 'A'..'Z' }

// various styles are possible for processing lists
// closure style
people.each { person -> assert startsWithCapital(person) }
// for loop style
for (person in people) { assert startsWithCapital(person) }

// unixScriptToFindAllUsersStartingWithLetterA:
all = 'who'.execute().text.replaceAll('\r', '').split('\n')
all.grep(~/^a.*/).each{ println it }

// printFileWithWordsReversedScript:
new File('Pleac/src/SlowCat.groovy').eachLine{ line ->
     line.split(' ').each{ print it.reverse() }
}

a = [0.5, 3]; b = [0, 1]
assert [a, b].flatten().collect{ it * 7 } == [3.5, 21, 0, 7]
// above doesn't modify original arrays
// instead use a = a.collect{ ... }
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.5
//----------------------------------------------------------------------------------
// not relevant in Groovy since we have always references
items = []
for (item in items) {
    // do something with item
}
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.6
//----------------------------------------------------------------------------------
assert [ 1, 1, 2, 2, 3, 3, 3, 5 ].unique() == [ 1, 2, 3, 5 ]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.7
//----------------------------------------------------------------------------------
assert [ 1, 1, 2, 2, 3, 3, 3, 4, 5 ] - [ 1, 2, 4 ]  ==  [3, 3, 3, 5]
assert [ 1, 1, 2, 2, 3, 3, 3, 4, 5 ].unique() - [ 1, 2, 4 ]  ==  [3, 5]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.8
//----------------------------------------------------------------------------------
a = [1, 3, 5, 6, 7, 8]
b = [2, 3, 5, 7, 9]
// intersection
assert a.intersect(b) == [3, 5, 7]
// union
assert (a + b).unique().sort() == [1, 2, 3, 5, 6, 7, 8, 9]
// difference
assert (a - b) == [1, 6, 8]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.9
//----------------------------------------------------------------------------------
members = [ "Time", "Flies" ]
initiates =  [ "An", "Arrow" ]
members += initiates
assert members == ["Time", "Flies", "An", "Arrow"]

members.add(2, "Like")
assert members == ["Time", "Flies", "Like", "An", "Arrow"]

members[0] = "Fruit"
members[3..4] = ["A", "Banana"]
assert members == ["Fruit", "Flies", "Like", "A", "Banana"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.10
//----------------------------------------------------------------------------------
items = ["the", "quick", "brown", "fox"]
assert items.reverse() == ["fox", "brown", "quick", "the"]

firstLetters = []
items.reverseEach{ firstLetters += it[0] }
assert firstLetters.join() == 'fbqt'

descending = items.sort().reverse()
assert descending == ["the", "quick", "fox", "brown"]
descendingBySecondLastLetter = items.sort { a,b -> b[-2] <=> a[-2] }
assert descendingBySecondLastLetter == ["brown", "fox", "the", "quick"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.11
//----------------------------------------------------------------------------------
// warning: not an exact equivalent, idiomatic use would return copies
def shift2 = {one = friends[0]; two = friends[1]; 2.times{friends.remove(0)}}
friends = 'Peter Paul Mary Jim Tim'.split(' ').toList()
shift2()
assert one == 'Peter'
assert two == 'Paul'
assert friends == ["Mary", "Jim", "Tim"]

def pop2(items) { items[0..1] }
beverages = 'Dew Jolt Cola Sprite Fresca'.split(' ').toList()
pair = pop2(beverages)
assert pair == ["Dew", "Jolt"]
//----------------------------------------------------------------------------------


// @@PLEAC@@_4.12
//----------------------------------------------------------------------------------
class Employee {
    def name
    def position
    def salary
}
staff = [new Employee(name:'Jim',position:'Manager',salary:26000),
         new Employee(name:'Jill',position:'Engineer',salary:24000),
         new Employee(name:'Jack',position:'Engineer',salary:22000)]
highestEngineer = staff.find { emp -> emp.position == 'Engineer' }
assert highestEngineer.salary == 24000
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.13
//----------------------------------------------------------------------------------
engineers = staff.findAll { e -> e.position == 'Engineer' }
assert engineers.size() == 2

highPaid = staff.findAll { e -> e.salary > 23000 }
assert highPaid*.name == ["Jim", "Jill"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.14
//----------------------------------------------------------------------------------
// sort works for numbers
assert [100, 3, 20].sort() == [3, 20, 100]
// strings representing numbers will be sorted alphabetically
assert ['100', '3', '20'].sort() == ["100", "20", "3"]
// closure style sorting allows arbitrary expressions for the comparison
assert ['100', '3', '20'].sort{ a,b -> a.toLong() <=> b.toLong()} == ["3", "20", "100"]

// obtain the following on unix systems using: 'ps ux'.execute().text
processInput = '''
      PID    PPID    PGID     WINPID  TTY  UID    STIME COMMAND
     3868       1    3868       3868  con 1005 06:23:34 /usr/bin/bash
     3456    3868    3456       3528  con 1005 06:23:39 /usr/bin/ps
'''
nonEmptyLines = {it.trim()}
lines = processInput.split("\n").findAll(nonEmptyLines)[1..-1]
def col(n, s) { s.tokenize()[n] }
commandIdx = 7
pidIdx = 0
ppidIdx = 1
linesByPid = lines.sort{ col(pidIdx,it).toLong() }
assert col(commandIdx, linesByPid[0]) == '/usr/bin/ps'
linesByPpid = lines.sort{ col(ppidIdx,it).toLong() }
assert col(commandIdx, linesByPpid[0]) == '/usr/bin/bash'
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.15
//----------------------------------------------------------------------------------
// sort staff from 4.12 by name
assert staff.sort { a,b -> a.name <=> b.name }*.name == ["Jack", "Jill", "Jim"]
// sort by first two characters of name and if equal by descending salary
assert staff.sort { a,b ->
    astart = a.name[0..1]
    bstart = b.name[0..1]
    if (astart == bstart) return b.salary <=> a.salary
    return astart <=> bstart
}*.name == ["Jack", "Jim", "Jill"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.16
//----------------------------------------------------------------------------------
items = [1, 2, 3, 4, 5]
processed = []
10.times{
    processed << items[0]
    items = items[1..-1] + items[0]
}
assert processed == [1, 2, 3, 4, 5, 1, 2, 3, 4, 5]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.17
//----------------------------------------------------------------------------------
import java.text.DateFormatSymbols as Symbols
items = new Symbols().shortWeekdays.toList()[1..7]
assert items == ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"]
// not as random as you might expect
println items.sort{ Math.random() }
// => ["Sat", "Tue", "Sun", "Wed", "Mon", "Thu", "Fri"]
// better to use the built-in method for this purpose
Collections.shuffle(items)
println items
// => ["Wed", "Tue", "Fri", "Sun", "Sat", "Thu", "Mon"]
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.18
//----------------------------------------------------------------------------------
symbols = new Symbols()
words = symbols.weekdays.toList()[1..7] +
    symbols.months.toList()[0..11] +
    symbols.eras.toList() +
    symbols.amPmStrings.toList()

expected = //
'AD        August    February  July      May       October   September Tuesday   \n' +
'AM        BC        Friday    June      Monday    PM        Sunday    Wednesday \n' +
'April     December  January   March     November  Saturday  Thursday  \n'

class WordFormatter {
    def cols

    def process(list) {
        def sb = new StringBuffer()
        def colWidth = list.max{it.size()}.size() + 1
        int columns = [cols/colWidth, 1].max()
        def numWords = list.size()
        int rows = (numWords + columns - 1) / columns
        for (row in 0..<rows) {
            for (col in 0..<columns) {
                def target = row + col * rows
                if (target < numWords)
                    sb << list[target].padRight(colWidth)
            }
            sb << '\n'
        }
        return sb.toString()
    }
}

// get nr of chars that fit in window or console, see PLEAC 15.4
// hard-coded here but several packages are available, e.g. in JLine
// use a concrete implementation of Terminal.getTerminalWidth()
def getWinCharWidth() { 80 }

// main script
actual = new WordFormatter(cols:getWinCharWidth()).process(words.sort())
assert actual == expected
//----------------------------------------------------------------------------------

// @@PLEAC@@_4.19
//----------------------------------------------------------------------------------
// recursive version is simplest but can be inefficient
def fact(n) { (n == 1) ? 1 : n * fact(n-1)}
assert fact(10) == 3628800
// unwrapped version: note use of BigInteger
def factorial(n) {
    def result = 1G // 1 as BigInteger
    while (n > 0) {
        result *= n
        n -= 1
    }
    return result
}
expected = 93326215443944152681699238856266700490715968264381621468592963895217599993229915608941463976156518286253697920827223758251185210916864000000000000000000000000
assert expected == factorial(100)
// println factorial(10000)
// => 284625... (greater than 35,000 digits)

// simple version but less efficient
def simplePermute(items, perms) {
    if (items.size() == 0)
        println perms.join(' ')
    else
        for (i in items) {
            newitems = items.clone()
            newperms = perms.clone()
            newperms.add(i)
            newitems.remove(i)
            simplePermute(newitems, newperms)
        }
}
simplePermute(['dog', 'bites', 'man'], [])
// =>
//dog bites man
//dog man bites
//bites dog man
//bites man dog
//man dog bites
//man bites dog

// optimised version below
expected = '''
man bites dog
man dog bites
bites man dog
bites dog man
dog man bites
dog bites man
'''

// n2pat(n, len): produce the N-th pattern of length len
def n2pat(n, length) {
    def pat = []
    int i = 1
    while (i <= length) {
        pat << (n % i)
        n = n.intdiv(i)
        i += 1
    }
    pat
}

// pat2perm(pat): turn pattern returned by n2pat() into
// permutation of integers.
def pat2perm(pat) {
    def source = (0 ..< pat.size()).collect{ it/*.toString()*/ }
    def perm = []
    while (pat.size() > 0) {
        def next = pat.remove(pat.size()-1)
        perm << source[next]
        source.remove(next)
    }
    perm
}

def n2perm(n, len) {
    pat2perm(n2pat((int)n,len))
}

data = ['man', 'bites', 'dog']
sb = new StringBuffer()
numPermutations = fact(data.size())
for (j in 0..<numPermutations) {
    def permutation = n2perm(j, data.size()).collect { k -> data[k] }
    sb << permutation.join(' ') + '\n'
}
assert '\n' + sb.toString() == expected
//----------------------------------------------------------------------------------

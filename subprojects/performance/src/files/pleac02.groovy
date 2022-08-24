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

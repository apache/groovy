/**
 * A local variable declaration statement declares one or more local variable names
 */
class Foo {
    def run() {
        runIntTests
        runDefIntTests
        runDefTests
        runStringTests
        runDefStringTests
        runModifierTests
        runNonTypeTests
    }
    // The type can be a builtin type keyword.
    def runIntTests() {
        int a
        int b = 1
        int c,d,e
        int f,g = 2
        int h = 3,i = 4  // extra permutations
        int j = 5,k      // extra permutations
        //int int p//@fail:parse
    }
    // With type keywords, the 'def' keyword is optional.
    def runDefIntTests() {
        def int a
        def int b = 1
        def int c,d,e
        def int f,g = 2
        //def int int p//@fail:parse
    }
    // With the 'def' the type is optional.
    // (It defaults to the type of the expression.)
    def runDefTests() {
        def a
        def b = 1
        def c,d,e
        def f,g = 2
        def h = "h"
        def java.lang.String i = 'i'
        //def def p//@fail:parse
    }
    // Capitalized names are taken to be declaration types.
    // With such names, the 'def' keyword is optional.
    def runStringTests() {
        Strange z
        String a
        String b = '1'
        String c,d,e
        String f,g = '2'
        String h = 3,i = 4  // extra permutations
        String j = 5,k      // extra permutations
        //String String p//@fail:parse
    }
    // With capitalized type names, the 'def' keyword is optional.
    def runDefStringTests() {
        def String a
        def String b = '1'
        def String c,d,e
        def String f,g = '2'
        //def String String p//@fail:parse
        def Strange z // Will parse, though not a type! //@fail:check
    }
    // When a modifier is present, the 'def' keyword is optional.
    def runModifierTests() {
        final a = 1, b = 2
        final int c = 12
        def final d = 3, e = 4
        def final int f = 5
        //int final p  //@fail:parse
        //final def q = 9//@fail:parse
    }
    def nonType =  // not capitalized...
        {println "nonType invoked on $it"}
    def one=1
    // The parser accepts uncapitalized type names only when required by context.
    // The requiring context is a modifier ('final') or the 'def' keyword.
    // Of course, built-in type keywords like 'int' are always types.
    // All other uncapitalized names are taken to be commands.
    def runNonTypeTests() {
        nonType one  // Parses as a command.
        def nonType a; // Will parse, though not a type! //@fail:check
        final nonType b = 0; //@fail:check  // Will parase, though not a type!
        //nonType p = 1 //@fail:parse
    }
}

import org.codehaus.groovy.tools.FileSystemCompiler as Compiler


def benchData = [
    ackermann       :   [5,6,7,8],
/*    ary             :   [1],
    binarytrees     :   [1],
    chameneos       :   [1],
    echo            :   [1],
    except          :   [1],
    fannkuch        :   [1],
    fannkuchredux   :   [1],
    fasta           :   [1],*/
    fibo            :   [30,31,32,33,34],
/*    harmonic        :   [1],
    hash            :   [1],
    hash2           :   [1],
    heapsort        :   [1],
    hello           :   [1],
    knucleotide     :   [1],
    lists           :   [1],
    magicsquares    :   [1],
    mandelbrot      :   [1],
    matrix          :   [1],
    message         :   [1],
    meteor          :   [1],
    methcall        :   [1],
    moments         :   [1],
    nbody           :   [1],
    nestedloop      :   [1],
    nsieve          :   [1],
    nsievebits      :   [1],
    objinst         :   [1],
    partialsums     :   [1],
    pidigits        :   [1],
    process         :   [1],
    prodcons        :   [1],
    random          :   [1],
    raytracer       :   [1],
    recursive       :   [1],
    regexdna        :   [1],
    revcomp         :   [1],
    sieve           :   [1],
    spectralnorm    :   [1],
    spellcheck      :   [1],
    strcat          :   [1],
    sumcol          :   [1],
    takfp           :   [1],
    threadring      :   [1],
    wc              :   [1],
    wordfreq        :   [1],*/
]

setGroovyLib()

println "-"*80
println "Groovy benchmarking test"
showJavaVersion()
println "Groovy lib: $GROOVY_LIB"
println "-"*80
benchData.each { bench, input ->
    println "Benchmark $bench"
    [".java", ".groovy"].each { ending ->
        println("\t$bench$ending :")
        
        boolean exists = prepare(bench, ending)
        if (exists) {
            execBenchmark(bench, input)
        } else {
            println "skipped"
        }
        cleanFolder()
    }
    println "-"*80
}

boolean prepare(bench, ending) {
    // copy file to exec folder it it exists
    def orig = new File("bench/$bench$ending")
    if (!orig.exists()) return false;

    // compile file using groovy compiler
    // in = orig, out = exec
    Compiler.main("-j","-d", "exec", orig.absolutePath)
    true
}

void cleanFolder(){
    def dir = new File("exec/")
    dir.eachFile { file -> file.delete() }
}

void showJavaVersion() {
    def p = [System.getenv("JAVA_HOME")+"bin/java","-version"].execute()
    p.consumeProcessErrorStream(System.out)
    p.consumeProcessOutputStream(System.out)
    p.waitForOrKill(10*60*1000)
}

void execBenchmark(bench, input) {
    input.each { param -> 
        def java = System.getenv("JAVA_HOME")+"bin/java"
        def cp = "./exec/"+File.pathSeparatorChar
        cp += GROOVY_LIB+File.pathSeparatorChar
        cp += "../target/lib/runtime/*"

        def time1 = System.nanoTime()

        print "\t\trunning  "
        20.times { n->
            printProgress(n)
            def p = [java, "-cp", cp, bench, param].execute()
            p.consumeProcessOutput()
            p.waitForOrKill(10*60*1000)
        }
        def time2 = System.nanoTime()
        long td = (time2-time1)/1000000/20
        print "\b"*9
        println "time ($param) = $td"
    }
}

void printProgress(it) {
  def n = it % 4
  if (n==0) print "\b/"
  if (n==1) print "\b-"
  if (n==2) print "\b\\"
  if (n==3) print "\b-"
}

void setGroovyLib() {
    def f = new File("../target/dist")
    f.eachFile { entry ->
        def name = entry.name
        if (!name.startsWith("groovy-all")) return
        if (name.contains("sources")) return
        GROOVY_LIB = entry.absolutePath
    }
}


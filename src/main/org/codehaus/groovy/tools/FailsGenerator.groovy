package org.codehaus.groovy.tools;

import java.io.*;
import groovy.util.XmlSlurper;

class FailsGenerator {

    def public conf;
    def public map = new HashMap();
    def public save=false
    public boolean hasChanged=false
    public int nr=0;
    public boolean skipIgnores=true
    
    public FailsGenerator(){}    

    static void main(args) {
        if (args.length<2) {
            println "usage: FailsGenerator [--save] [--skip-ignores] <conf-file> <reports-dir>"
            println "         save           is optional argument and let the script store the results"
            println "                        found in the xml files in the conf file"
            println "         skip-ignores   don't print information about ignored files "
            println "         conf-file      is the configuration file"
            println "         reports-dir    is the directory containing the xml reports from JUnit"
            System.exit(1)
        }
        def gen = new FailsGenerator();
        int argIndex=0
        while (args[argIndex].startsWith("--")) {
          switch (args[argIndex]) {
            case "--save":
              gen.save=true
              break;
            case "--skip-ignores":
              gen.skipIgnores=true
              break;
            default:
              println "unknown option "+ args[argIndex]
              System.exit(1);
          }
          argIndex++;
        }
        gen.conf = new File(args[argIndex])
        gen.readConf()
        gen.compareFiles(args[argIndex+1])
        gen.saveConf()
        println "DONE"
    }
    
    
    void compareFiles(dir) {
    
        def attReader = { name, attName, oldVal, newVal ->
            if (attName!=null) oldVal=Integer.parseInt(oldVal)
            boolean success = (oldVal!=0 && newVal==0)
            if (success) {
                println("${name}: well done, no more ${attName}");
            } else if (oldVal==-1) {
                // do nothing
            } else if (newVal<oldVal) {
                println("${name}: improved ${attName} from ${oldVal} to ${newVal}");
            } else if (newVal>oldVal) {
                println("${name}: more ${attName} (from ${oldVal} to ${newVal})");
            }
            hasChanged = hasChanged || newVal!=oldVal
            return success
        }


        dir = new File(dir)
        if (!dir.isDirectory()) throw new RuntimeException("${dir} has to be a directory containg the xml tests reports")
        dir.eachFileRecurse {
            file ->
            if (!file.getName().endsWith(".xml")) return
            if (file.getName().indexOf("\$")>-1) {
              if (!skipIgnores) println("${file.name} is ignored because it's output for a subclass")
              return
            }
            def node = new XmlSlurper().parse(file);
            def name = node['@name']
            def errorVal = Integer.parseInt(node['@errors'])
            def failureVal = Integer.parseInt(node['@failures'])

            def el = map.get(name)
            if (el==null && !save) throw new RuntimeException("unknown test ${name}, please add it to conf file ${conf.name}")
            if (el==null && save) {
                el = new HashMap()
                el.put("errors","-1");
                el.put("failures","-1");
                println "added configuration for test ${name}"
                addToMap(name,el)
                hasChanged=true
            }

            def err  = attReader(name,"errors",el.errors,errorVal)
            def fail = attReader(name,"failures",el.failures,failureVal)
            if (err && fail) {
                println(">>> Congratulations ${name} has passed the test <<<");
            }
            el.errors = errorVal
            el.failures = failureVal
        }
    }        
    
    void saveConf() {
        if (!save) return
        if (!hasChanged) {
            println "no changes to configuration"
            return
        }
        println "saving conf in ${conf}, old configuration is saved in ${conf}.old"
        println "WARNING: comments are not in the new file"
        def oldConf = new File(conf.absolutePath+".old")
        def bytes = conf.readBytes()
        def out = oldConf.newOutputStream()
        out << bytes
        out.close()
        out = conf.newWriter()
        try {
            map.each {
                 out.writeLine("[${it.key}]")
                 it.value.each {
                     out.writeLine("${it.key}=${it.value}")
                 }
            }
        } finally {
            out.close();
        }
    }

    void readConf() {
        def reader = new LineNumberReader(new FileReader(conf));
        def line = null;

        def readLine = {l ->
            if (l!=null) return l
            while (true) {
                l = reader.readLine()
                if (l==null) return null
                if (""==l) continue
                l=l.trim()
                if (l[0] == "#") continue
                return l
            }
        }

        def lineloop = {l,lbreak,func ->
            while(true) {
                if (l!=null && lbreak) return l
                if (l==null) l = readLine(l)
                if (l==null) return
                nr= reader.lineNumber;
                l = func(l)
            }
            return l
        }

        def attRead = {el,l->
            l = lineloop(l,true) {ll ->
                if (ll[0]=="[" || ll[-1]=="]") return ll
                int index = ll.indexOf('=');
                if (index==-1) throw new RuntimeException(" ${conf.name}:${nr} = expected somewhere, but got ${ll}");
                String name = ll.substring(0,index).trim();
                String value = "";
                if (ll.length()>=index) value = ll.substring(index+1).trim();
                el.put(name,value);
                return null
            }
            return l
        }


        def fileStart = {->
            lineloop(null,false) {l ->
                if (l[0]!="[" || l[-1]!="]") {
                    throw new RuntimeException("${conf.name}:${nr} filename inside of [] expected, but got ${line}")
                }
                def el = new HashMap()
                def file = line.substring(1,l.length()-1)
                addToMap(file,el);
                l = attRead(el,null)
                return l
            }
        }
        
        try {
            fileStart()
        } finally {
            reader.close();
        }
        if (map.size==0) throw new RuntimeException("${conf.name} was empty");
    }
    
    def addToMap(name, el) {
        map.put(name,el)
    }

    
}


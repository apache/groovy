package org.codehaus.groovy.tools;

import java.io.*;
import org.codehaus.groovy.sandbox.util.XmlSlurper;

class FailsGenerator {

    static def conf;
    static def map=new HashMap();
    
    public FailsGenerator(){}    

    static void main(args) {
        conf = new File(args[0])
        readConf()
        compareFiles(args[1])
    }
    
    static void compareFiles(dir) {
    
        attReader = {name,attName, oldVal, newVal::
            if (attName!=null) oldVal=Integer.parseInt(oldVal)
            boolean success = (oldVal!=0 && newVal==0)
            if (success) {
                println("${name}: well done, no more ${attName}");
            } else if (newVal<oldVal) {
                println("${name}: improved ${attName} from ${oldVal} to ${newVal}");
            } else if (newVal>oldVal) {
                println("${name}: more ${attName} (from ${oldVal} to ${newVal}");
            }
            return success 
        }

    
        dir = new File(dir)
        if (!dir.isDirectory() throw new RuntimeException("${dir} has to be a directory containg the xml tests reports")
        dir.eachFileRecurse {file::
            if (!file.getName().endsWith(".xml")) return
            if (!file.getName().indexOf("$")>-1) {
              println("${file.name) is ignored because it's output for a subclass")
              return
            }
            node = new XmlSlurper().parse(file);
            name = node['@name']
            errorVal = Integer.parseInt(node['@errors'])
            failureVal = Integer.parseInt(node['@failures'])
            
            el = map.get(name)
            if (el==null) throw new RuntimeException("unknown test ${name}, please add it to conf file ${conf.name}")
            
            err  = attReader(name,"errors",el.errors,errorVal)
            fail = attReader(name,"failures",el.failures,failureVal)
            if (err && fail) {
                println(">>> Congratualtions ${name} has passed the test <<<"); 
            }
        }
    }
    
    static int nr=0;
    
    static void readConf() {
        reader = new LineNumberReader(new FileReader(conf));
        line = null;
        
        def readLine = {line::
            if (line!=null) return line
            while (true) {
                line = reader.readLine()
                if (line==null) return null
                if (""==line) continue
                line=line.trim()
                if (line[0] == "#") continue
                return line
            }
        }
        
        def lineloop = {line,lbreak,func::
            while(true) {
                if (line!=null && lbreak) return line
                if (line==null) line = readLine(line)
                if (line==null) return
                nr= reader.lineNumber;
                line = func(line)
            }
            return line
        }
        
        def attRead = {el,line::
            line = lineloop(line,true) {line::
                if (line[0]=="[" || line[-1]=="]") return line
                int index = line.indexOf('=');
                if (index==-1) throw new RuntimeException(" ${conf.name}:${nr} = expected somewhere, but got ${line}");
                String name = line.substring(0,index).trim();
                String value = "";
                if (line.length()>=index) value = line.substring(index+1).trim();
                el.put(name,value);
                return null
            }
            return line
        }
        
        
        def fileStart = {::
            lineloop(null,false) {line::
                if (line[0]!="[" || line[-1]!="]") {
                    throw new RuntimeException("${conf.name}:${nr} filename inside of [] expected, but got ${line}")
                }
                el = new HashMap()
                file = line.substring(1,line.length()-1)
                addToMap(file,el);
                line = attRead(el,null)
                return line
            }
        }
        
        fileStart()
        if (map.size==0) throw new RuntimeException("${conf.name} was empty");
    }
    
    static addToMap(name, el) {
        map.put(name,el)
    }
    
}



import org.codehaus.groovy.scriptom.ActiveXProxy

sc = new ActiveXProxy("ScriptControl")
sc.Language = "VBScript"
println sc.Eval("1 + 1").value


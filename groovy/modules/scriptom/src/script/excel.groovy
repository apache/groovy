
import org.codehaus.groovy.scriptom.ActiveXProxy

xls = new ActiveXProxy("Excel.Application")
xls.Visible = true

Thread.sleep(1000)

workbooks = xls.Workbooks
workbook  = workbooks.Add()

sheet = workbook.ActiveSheet
a1 = sheet.Range('A1')
a2 = sheet.Range('A2')

a1.Value   = 123.456
a2.Formula = '=A1*2'

println "a1: ${a1.Value.value}"
println "a2: ${a2.Value.getValue()}"

workbook.Close(false, null, false)
Thread.sleep(1000)
xls.Quit()
Thread.sleep(1000)
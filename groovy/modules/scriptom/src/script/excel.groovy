
import org.codehaus.groovy.scriptom.ActiveXProxy

// create a proxy for Excel
xls = new ActiveXProxy("Excel.Application")
xls.Visible = true

Thread.sleep(1000)

// get the workbooks object
workbooks = xls.Workbooks
// add a new workbook
workbook  = workbooks.Add()

// select the active sheet
sheet = workbook.ActiveSheet

// get a handle on two cells
a1 = sheet.Range('A1')
a2 = sheet.Range('A2')

// sets a value for A1
a1.Value   = 123.456
// defines a formula in A2
a2.Formula = '=A1*2'

println "a1: ${a1.Value.value}"
println "a2: ${a2.Value.getValue()}"

// close the workbook without asking for saving the file
workbook.Close(false, null, false)
// quits excel
xls.Quit()

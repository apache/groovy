import org.codehaus.groovy.scriptom.ActiveXProxy

ALT 	= '%'
SHIFT 	= '+' 
CTRL 	= '^' 

'cmd /k notepad.exe'.execute()

// Windows Scripting Host shell
host = new ActiveXProxy("WScript.Shell")
host.AppActivate("Notepad")

Thread.sleep(1000)

// some keystrokes
host.sendKeys('0/0{TAB}0/1{ENTER}1/0{TAB}1/1{ENTER}')

// function key to insert current date
host.sendKeys('{F5}')

// replace all 0's by x
host.sendKeys(CTRL+'h')
host.sendKeys('0{TAB}x')
host.sendKeys(ALT+'a')
host.sendKeys('{ESC}')




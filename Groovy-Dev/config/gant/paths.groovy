//  Path specifications for the Gant build of Groovy.
//
//  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
//  compliance with the License. You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software distributed under the License is
//  distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
//  implied. See the License for the specific language governing permissions and limitations under the License.
//
//  This work is copyright by the author(s) and is part of a greater work collectively copyright by Codehaus on
//  behalf of the Groovy community. See the NOTICE.txt file distributed with this work for additional information.
//
//  Author : Russel Winder

bootstrapDirectory = 'bootstrap'
sourceDirectory = 'src'
wikiPdfDirectory = 'src'
mainSourceDirectory = sourceDirectory + '/main'
testSourceDirectory = sourceDirectory + '/test'
toolsSourceDirectory = sourceDirectory + '/tools'
examplesSourceDirectory = sourceDirectory + '/examples'
    
targetDirectory = 'target'
installDirectory = targetDirectory + '/install'
cruiseReportRootDirectory = targetDirectory + '/root'
stagingDirectory = targetDirectory + '/staging'
docsDirectory = targetDirectory + '/html'
mainClassesDirectory = targetDirectory + '/classes'
testClassesDirectory = targetDirectory + '/test-classes'
toolsClassesDirectory = targetDirectory + '/tools-classes'
mainStubsDirectory = targetDirectory + '/stubs'
testStubsDirectory = targetDirectory + '/test-stubs'

examplesClassesDirectory = targetDirectory + '/examples-classes'
instrumentedClassesDirectory = targetDirectory + '/instrumented-classes'
reportsDirectory = targetDirectory + '/reports'
targetLibDirectory = targetDirectory + '/lib'
targetDistDirectory = targetDirectory + '/dist'
    
antlrDirectory = mainSourceDirectory + '/org/codehaus/groovy/antlr'
groovyParserDirectory = antlrDirectory + '/parser'
javaParserDirectory = antlrDirectory + '/java'

compileLibDirectory = targetLibDirectory + '/compile'
testLibDirectory = targetLibDirectory + '/test'
runtimeLibDirectory = targetLibDirectory + '/runtime'
toolsLibDirectory = targetLibDirectory + '/tools'
examplesLibDirectory = targetLibDirectory + '/examples'
extrasLibDirectory = targetLibDirectory + '/extras'
junitRawDirectory = targetDirectory + '/test-reports'
junitReportsDirectory = reportsDirectory + '/junit'

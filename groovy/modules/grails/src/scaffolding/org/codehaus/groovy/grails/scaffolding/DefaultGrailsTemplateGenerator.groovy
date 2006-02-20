/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.grails.scaffolding;

import org.codehaus.groovy.grails.commons.GrailsDomainClass;
import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.codehaus.groovy.grails.scaffolding.GrailsTemplateGenerator;
import org.codehaus.groovy.grails.commons.GrailsClassUtils as GCU;
/**
 * Default implementation of the generator that generates grails artifacts (controllers, views etc.)
 * from the domain model
 *
 * @author Graeme Rocher
 * @since 09-Feb-2006
 */
class DefaultGrailsTemplateGenerator implements GrailsTemplateGenerator  {

    @Property String basedir
    @Property boolean overwrite = false
    def engine = new groovy.text.SimpleTemplateEngine()

    // a closure that uses the type to render the appropriate editor
    def renderEditor = { property ->
        def domainClass = property.domainClass
        def cp = domainClass.constrainedProperties[property.name]
        
        def display = (cp ? cp.display : true)        
        if(!display) return ''
        
	def buf = new StringBuffer("<div class='prop \${hasErrors(bean:${domainClass.propertyName},field:'${property.name}','errors')}'><label for='${property.name}'>${property.naturalName}:</label>")
            if(Number.class.isAssignableFrom(property.type))
                buf << renderNumberEditor(domainClass,property)
            else if(property.type == String.class)
                buf << renderStringEditor(domainClass,property)
            else if(property.type == Boolean.class)
                buf << renderBooleanEditor(domainClass,property)
            else if(property.type == Date.class)
                buf << renderDateEditor(domainClass,property)
            else if(property.type == TimeZone.class)
                buf << renderSelectTypeEditor("timeZone",domainClass,property)
            else if(property.type == Locale.class)
                buf << renderSelectTypeEditor("locale",domainClass,property)
            else if(property.type == Currency.class)
                buf << renderSelectTypeEditor("currency",domainClass,property)
            else if(property.type==([] as Byte[]).class) //TODO: Bug in groovy means i have to do this :(
                buf << renderByteArrayEditor(domainClass,property)
            else if(property.manyToOne || property.oneToOne)
                buf << renderManyToOne(domainClass,property)
            else if(property.oneToMany || property.manyToMany)
                buf << renderOneToMany(domainClass,property)
                
       buf << '</div>'
       return buf.toString()
    }

    public void generateViews(GrailsDomainClass domainClass, String destdir) {
        if(!destdir)
            throw new IllegalArgumentException("Argument [destdir] not specified")

        def viewsDir = new File("${destdir}/grails-app/views/${domainClass.propertyName}")
        if(!viewsDir.exists())
            viewsDir.mkdirs()


        generateListView(domainClass,viewsDir)
        generateShowView(domainClass,viewsDir)
        generateEditView(domainClass,viewsDir)
        generateCreateView(domainClass,viewsDir)
    }

    public void generateController(GrailsDomainClass domainClass, String destdir) {
        if(!destdir)
            throw new IllegalArgumentException("Argument [destdir] not specified")

        if(domainClass) {
            def destFile = new File("${destdir}/grails-app/controllers/${domainClass.shortName}Controller.groovy")
            if(destFile.exists()) {
                println "Controller ${destFile.name} already exists skipping"
                return
            }
            destFile.parentFile.mkdirs()

            def templateText = '''
class ${className}Controller {
    @Property index = { redirect(action:list,params:params) }

    @Property list = {
        if(!params['max']) params['max'] = 10
        [ ${propertyName}List: ${className}.list( params ) ]
    }

    @Property show = {
        [ ${propertyName} : ${className}.get( params['id'] ) ]
    }

    @Property delete = {
        def ${propertyName} = ${className}.get( params['id'] )
        if(${propertyName}) {
            ${propertyName}.delete()
            flash['message'] = "${className} \\${params['id']} deleted."
            redirect(action:list)
        }
        else {
            flash['message'] = "${className} not found with id \\${params['id']}"
            redirect(action:list)
        }
    }

    @Property edit = {
        def ${propertyName} = ${className}.get( params['id'] )

        if(!${propertyName}) {
                flash['message'] = "${className} not found with id \\${params['id']}"
                redirect(action:list)
        }
        else {
            return [ ${propertyName} : ${propertyName} ]
        }
    }

    @Property update = {
        def ${propertyName} = ${className}.get( params['id'] )
        if(${propertyName}) {
             ${propertyName}.properties = params
            if(${propertyName}.save()) {
                redirect(action:show,id:${propertyName}.id)
            }
            else {
                render(view:'edit',model:[${propertyName}:${propertyName}])
            }
        }
        else {
            flash['message'] = "${className} not found with id \\${params['id']}"
            redirect(action:edit,id:params['id'])
        }
    }

    @Property create = {
        def ${propertyName} = new ${className}()
        ${propertyName}.properties = params
        return ['${propertyName}':${propertyName}]
    }

    @Property save = {
        def ${propertyName} = new ${className}()
        ${propertyName}.properties = params
        if(${propertyName}.save()) {
            redirect(action:show,id:${propertyName}.id)
        }
        else {
            render(view:'create',model:[${propertyName}:${propertyName}])
        }
    }

}'''

            def binding = [ className: domainClass.shortName, propertyName:domainClass.propertyName ]
            def t = engine.createTemplate(templateText)

            destFile.withWriter { w ->
                t.make(binding).writeTo(w)
            }

            println "Controller generated at ${destFile}"
        }
    }

    private renderStringEditor(domainClass, property) {
        def cp = domainClass.constrainedProperties[property.name]
        if(!cp) {
            return "<input type='text' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}' />"
        }
        else {
            if(cp.maxLength > 250 && !cp.password) {
                return "<textarea rows='1' cols='1' name='${property.name}'>\${${domainClass.propertyName}?.${property.name}}</textarea>"
            }
            else {
                def sb = new StringBuffer('<input ')
                cp.password ? sb << 'type="password" ' : sb << 'type="text" '
                if(!cp.editable) sb << 'readonly="readonly" '
                if(cp.maxLength < Integer.MAX_VALUE ) sb << "maxlength='${cp.maxLength}' "
                sb << "name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></input>"
                return sb.toString()
            }
        }
    }

    private renderByteArrayEditor(domainClass,property) {
        return "<input type='file' name='${property.name}'></input>"
    }

    private renderManyToOne(domainClass,property) {
        if(property.association) {            
            return "<g:select optionKey=\"id\" from=\"\${${property.type.name}.list()}\" name='${property.name}.id' value='\${${domainClass.propertyName}?.${property.name}?.id}'></g:select>"
        }
    }

    private renderOneToMany(domainClass,property) {
        return "Number of ${property.name}: \${${property.type.name}.count()}"
    }

    private renderNumberEditor(domainClass,property) {
        def cp = domainClass.constrainedProperties[property.name]
        if(!cp) {
            if(property.type == Byte.class) {
                return "<g:select from='\${-128..127}' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:select>"
            }
            else {
                return "<input type='text' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></input>"
            }
        }
        else {
            if(cp.range) {
                return "<g:select from='\${${cp.range.from}..${cp.range.to}}' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:select>"
            }
            else if(cp.size) {
                return "<g:select from='\${${cp.size.from}..${cp.size.to}}' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:select>"
            }
            else {
                return "<input type='text' name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></input>"
            }
        }
    }

    private renderBooleanEditor(domainClass,property) {

        def cp = domainClass.constrainedProperties[property.name]
        if(!cp) {
            return "<g:checkBox name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:checkBox>"
        }
        else {
            def buf = new StringBuffer('<g:checkBox ')
            if(cp.widget) buf << "widget='${cp.widget}'";

            buf << "name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}' "
            cp.attributes.each { k,v ->
                  buf << "${k}=\"${v}\" "
            }
            buf << '></g:checkBox>'
            return buf.toString()
        }

    }

    private renderDateEditor(domainClass,property) {
        def cp = domainClass.constrainedProperties[property.name]
        if(!cp) {
            return "<g:datePicker name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:datePicker>"
        }
        else {
            def buf = new StringBuffer('<g:datePicker ')
            if(cp.widget) buf << "widget='${cp.widget}' ";

            if(cp.format) buf << "format='${cp.format}' ";
            cp.attributes.each { k,v ->
                  buf << "${k}=\"${v}\" "
            }
            buf << "name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:datePicker>"
            return buf.toString()
        }
    }

    private renderSelectTypeEditor(type,domainClass,property) {
       def cp = domainClass.constrainedProperties[property.name]
        if(!cp) {
            return "<g:${type}Select name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:${type}Select>"
        }
        else {
            def buf = new StringBuffer('<g:${type}Select ')
            if(cp.widget) buf << "widget='${cp.widget}' ";
            cp.attributes.each { k,v ->
                  buf << "${k}=\"${v}\" "
            }
            buf << "name='${property.name}' value='\${${domainClass.propertyName}?.${property.name}}'></g:${type}Select>"
            return buf.toString()
        }
    }




    private generateListView(domainClass, destDir) {
        def listFile = new File("${destDir}/list.gsp")
        if(!listFile.exists() || overwrite) {
            def templateText = '''
<html>
    <head>
         <title>${className} List</title>
         <link rel="stylesheet" href="\\${createLinkTo(dir:'css',file:'main.css')}"></link>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link action="index">Home</g:link></span>
            <span class="menuButton"><g:link action="create">New ${className}</g:link></span>
        </div>
        <div class="body">
           <h1>${className} List</h1>
            <g:if test="flash['message']">
                 <div class="message">
                       \\${flash['message']}
                 </div>
            </g:if>
           <table>
               <tr>
                   <%
                        props = domainClass.properties.findAll { it.name != 'version' }
                        Collections.sort(props, new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domainClass))
                   %>
                   <%props.eachWithIndex { p,i ->
                   	if(i < 6) {%>                   
                        <th>${p.naturalName}</th>
                   <%}}%>
                   <th></th>
               </tr>
               <g:each in="\\${${propertyName}List}">
                    <tr>
                       <%props.eachWithIndex { p,i ->
                             if(i < 6) {%>
                            <td>\\${it.${p.name}}</td>
                       <%}}%>
                       <td class="actionButtons">
                            <span class="actionButton"><g:link action="show" id="\\${it.id}">Show</g:link></span>
                            <span class="actionButton"><g:link action="delete" id="\\${it.id}">Delete</g:link></span>
                       </td>
                    </tr>
               </g:each>
           </table>
        </div>
    </body>
</body>
            '''

            def t = engine.createTemplate(templateText)
            def binding = [ domainClass: domainClass, className:domainClass.shortName,propertyName:domainClass.propertyName ]

            listFile.withWriter { w ->
                t.make(binding).writeTo(w)
            }

        }
    }

    private generateShowView(domainClass,destDir) {
        def showFile = new File("${destDir}/show.gsp")
        if(!showFile.exists() || overwrite) {
            def templateText = '''
<html>
    <head>
         <title>Show ${className}</title>
         <link rel="stylesheet" href="\\${createLinkTo(dir:'css',file:'main.css')}"></link>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link action="index">Home</g:link></span>
            <span class="menuButton"><g:link action="list">${className} List</g:link></span>
        </div>
        <div class="body">
           <h1>Show ${className}</h1>
           <g:if test="\\${flash['message']}">
                 <div class="message">\\${flash['message']}</div>
           </g:if>
           <div class="dialog">

                   <%
                        props = domainClass.properties.findAll { it.name != 'version' }
                        Collections.sort(props, new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domainClass))
                   %>
                   <%props.each { p ->%>
                        <div class="prop">
                              <span class="name">${p.naturalName}:</span>
                              <span class="value">\\${${propertyName}.${p.name}}</span>
                        </div>
                   <%}%>
           </div>
           <div class="buttons">
                 <span class="button"><button onclick="location.href='\\${createLink(action:'edit',id:${propertyName}?.id)}'">Edit</button></span>
                 <span class="button"><button onclick="location.href='\\${createLink(action:'delete',id:${propertyName}?.id)}'">Delete</button></span>
                 <span class="button"><button onclick="location.href='\\${createLink(action:'list')}'">Back</button></span>
           </div>
        </div>
    </body>
</body>
            '''

            def t = engine.createTemplate(templateText)
            def binding = [ domainClass: domainClass, className:domainClass.shortName,propertyName:domainClass.propertyName ]

            showFile.withWriter { w ->
                t.make(binding).writeTo(w)
            }

        }
    }

    private generateEditView(domainClass,destDir) {
        def editFile = new File("${destDir}/edit.gsp")
        if(!editFile.exists() || overwrite) {
            def templateText = '''
<html>
    <head>
         <title>Edit ${className}</title>
         <link rel="stylesheet" href="\\${createLinkTo(dir:'css',file:'main.css')}"></link>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link action="index">Home</g:link></span>
            <span class="menuButton"><g:link action="list">${className} List</g:link></span>
        </div>
        <div class="body">
           <h1>Edit ${className}</h1>
           <g:if test="\\${flash['message']}">
                 <div class="message">\\${flash['message']}</div>
           </g:if>
           <g:hasErrors bean="\\${${propertyName}}">
                <div class="errors">
                    <g:renderErrors bean="\\${${propertyName}}" as="list" />
                </div>
           </g:hasErrors>
           <div class="prop">
	      <span class="name">Id:</span>
	      <span class="value">\\${${propertyName}?.id}</span>
	      <input type="hidden" name="${propertyName}.id" value="\\${${propertyName}?.id}" />
           </div>           
           <g:form url="[action:'update',id:${propertyName}?.id]" method="post">
               <div class="dialog">

                       <%
                            props = domainClass.properties.findAll { it.name != 'version' && it.name != 'id' }
                            Collections.sort(props, new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domainClass))
                       %>
                       <%props.each { p ->%>
				${renderEditor(p)}
                       <%}%>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Update"></input>
                     </span>
               </div>
            </g:form>
        </div>
    </body>
</body>
            '''

            def t = engine.createTemplate(templateText)
            def binding = [ domainClass: domainClass,
                            className:domainClass.shortName,
                            propertyName:domainClass.propertyName,
                            renderEditor:renderEditor ]

            editFile.withWriter { w ->
                t.make(binding).writeTo(w)
            }

        }
    }

    private generateCreateView(domainClass,destDir) {
        def createFile = new File("${destDir}/create.gsp")
        if(!createFile.exists() || overwrite) {
            def templateText = '''
<html>
    <head>
         <title>Create ${className}</title>
         <link rel="stylesheet" href="\\${createLinkTo(dir:'css',file:'main.css')}"></link>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><g:link action="index">Home</g:link></span>
            <span class="menuButton"><g:link action="list">${className} List</g:link></span>
        </div>
        <div class="body">
           <h1>Create ${className}</h1>
           <g:if test="\\${flash['message']}">
                 <div class="message">\\${flash['message']}</div>
           </g:if>
           <g:hasErrors bean="\\${${propertyName}}">
                <div class="errors">
                    <g:renderErrors bean="\\${${propertyName}}" as="list" />
                </div>
           </g:hasErrors>
           <g:form action="save" method="post">
               <div class="dialog">

                       <%
                            props = domainClass.properties.findAll { it.name != 'version' && it.name != 'id' }
                            Collections.sort(props, new org.codehaus.groovy.grails.scaffolding.DomainClassPropertyComparator(domainClass))
                       %>
                       <%props.each { p ->%>                                                              
                                  ${renderEditor(p)}
                       <%}%>
               </div>
               <div class="buttons">
                     <span class="formButton">
                        <input type="submit" value="Create"></input>
                     </span>
               </div>
            </g:form>
        </div>
    </body>
</body>
            '''

            def t = engine.createTemplate(templateText)
            def binding = [ domainClass: domainClass,
                            className:domainClass.shortName,
                            propertyName:domainClass.propertyName,
                            renderEditor:renderEditor ]

            createFile.withWriter { w ->
                t.make(binding).writeTo(w)
            }

        }
    }
}
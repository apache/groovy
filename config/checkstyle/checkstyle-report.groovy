/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

/**
 * A template which generates an HTML report from the checkstyle XML report
 */
yieldUnescaped '<!DOCTYPE html>'

def severityMapping = [
        error  : 'danger',
        warning: 'warning',
        info   : 'info',
        ignore : 'success'
]

def github = { path, line=null ->
    def localPath = (path - project.rootDir).replaceAll('\\\\','/')
    def link = """https://github.com/groovy/groovy-core/blob/master$localPath${line?"#L$line":""}"""

    if (line) {
        "<a href='$link'>$line</a>"
    } else {
        "<i class='fa fa-github'></i>&nbsp;<a href='$link'>$link</a>"
    }
}

html {
    head {
        meta 'charset': "utf-8"
        meta 'http-equiv': "content-type", content: "text/html; charset=utf-8"
        meta 'http-equiv': "X-UA-Compatible", content: "IE=edge"
        meta name: "viewport", content: "width=device-width, initial-scale=1"

        title "Checkstyle report for ${project.name}"
        link href: "http://maxcdn.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css", rel: "stylesheet"
        link href: "http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css", rel: "stylesheet"
        link href: "http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap-theme.min.css", rel: "stylesheet"
    }

    body {
        div(class:'navbar navbar-inverse navbar-fixed-top', role:'navigation') {
            div(class:'container') {
                div(class:'navbar-header') {
                    button(type:'button', class:'navbar-toggle', 'data-toggle':'collapse', 'data-target':'navbar-collaspe') {
                        span(class:'sr-only', 'Toggle navigation')
                        span(class:'icon-bar'){}
                        span(class:'icon-bar'){}
                        span(class:'icon-bar'){}
                    }
                    a(class:'navbar-brand',href:'#', 'Checkstyle report')
                }
                div(class:'navbar-collapse collapse') {
                    ul(class:"nav navbar-nav") {
                        li(class: 'dropdown') {
                            a(id: 'severityDropdown', href: '#', class: 'dropdown-toggle', 'data-toggle': 'dropdown', 'Severity <span class="caret"></span>')
                            ul(class: "dropdown-menu dropdown-severity", role: "menu") {
                                li(role: 'presentation', class: 'active') {
                                    a(role: 'menuitem', tabindex: '-1', href: '#', 'All levels')
                                }
                                li(role: 'presentation') { a(role: 'menuitem', tabindex: '-1', href: '#', 'Error') }
                                li(role: 'presentation') { a(role: 'menuitem', tabindex: '-1', href: '#', 'Warning') }
                                li(role: 'presentation') { a(role: 'menuitem', tabindex: '-1', href: '#', 'Info') }
                                li(role: 'presentation') { a(role: 'menuitem', tabindex: '-1', href: '#', 'Ignore') }
                            }
                        }

                        Set rules = files.collect { it.errors.collect { it.source } }.flatten()

                        li(class: 'dropdown') {
                            a(id: 'rulesDropdown', href: '#', class: 'dropdown-toggle', 'data-toggle': 'dropdown', 'Rules <span class="caret"></span>')
                            ul(class: "dropdown-menu dropdown-rule", role: "menu") {
                                li(role: 'presentation', class: 'active') {
                                    a(role: 'menuitem', tabindex: '-1', href: '#', 'All rules')
                                }
                                rules.each { rule ->
                                    li(role: 'presentation') { a(role: 'menuitem', tabindex: '-1', href: '#', rule) }
                                }
                            }
                        }
                    }
                }
            }
        }


        div(class: 'container') {
            div(class:'page-header') {
                h1("Checkstyle report for project ${project.name}")
            }
            files.each { file ->
                def errors = file.errors
                Set severities = errors.collect { "severity-${it.severity}" }
                Set panelRules = errors.collect { "rule-${it.source.toLowerCase()}" }
                div(class: "panel panel-default ${severities.join(' ')} ${panelRules.join(' ')}") {
                    div(class: "panel-heading") {
                        h3(class: 'panel-title', github(file.name))
                    }
                    div(class: 'panel-body') {
                        table(class: "table table-striped table-bordered") {
                            tbody {
                                errors.each { err ->
                                    tr(class:"checkstyle-error severity-${err.severity} rule-${err.source.toLowerCase()}") {
                                        td {
                                            h4 {
                                                span(class: "label label-${severityMapping[err.severity]}", err.severity.capitalize())
                                            }
                                        }
                                        td { span "At line ${github(file.name, err.line)}, $err.message" }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            script(src: "http://code.jquery.com/jquery-1.11.0.min.js") {}
            script(src: "http://maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min.js") {}
            script {
                yieldUnescaped '''$(document).ready(function () {
        var severity = null;
        var rule = null;
        doFilter();
        function doFilter() {
          var severityClass = "severity-" + severity;
          var ruleClass = "rule-" + rule;
          $('.panel').hide();
          $('.checkstyle-error').hide();
          $('.checkstyle-error').filter(function () {
              return (severity==null || $(this).hasClass(severityClass)) && (rule==null || $(this).hasClass(ruleClass));
          }).show();
          $('.panel').filter(function () {
              return (severity==null || $(this).hasClass(severityClass)) && (rule==null || $(this).hasClass(ruleClass));
          }).show();
        }
        $(".dropdown-severity li a").click(function() {
          rule = null;
          severity = $(this).text().toLowerCase();
          if (severity==="all levels") {
            severity = null;
          }
          doFilter();
        });
        $(".dropdown-rule li a").click(function() {
          severity = null;
          rule = $(this).text().toLowerCase();
          if (rule==="all rules") {
            rule = null;
          }
          doFilter();
        });

});'''
            }
        }
    }
}
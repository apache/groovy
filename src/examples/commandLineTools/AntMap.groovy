#!/bin/env groovy
// 
// convert an ant build file into a format suitable for http://sf.net/projects/freemind
//
// by Jeremy Rayner - 2 Dec 2004
// inspired by Sam Newman ( http://www.magpiebrain.com/archives/2004/12/02/antgui )
//
// usage:   groovy AntMap > build.mm

import groovy.util.XmlParser

buildFileName = "build.xml"  // default

// handle command line params
if (args.length > 0) {
	buildFileName = args[0]
}

// header
println "<map version='0.7.1'>"
project = new XmlParser().parse(buildFileName)
name = project['@name']
println "<node TEXT='${name}'>"
level = 0

printChildren(project,level)



def void printChildren(node,level) {
	level++
	node.each {
		name = huntForName(it)
		if (name != null) {
			if (level > 1) {
				println "<node TEXT='${name}' POSITION='right'>"
			} else if (it.name() == 'property' || it.name() == 'path' ) {
				if (it.children().size() > 0) {
					println "<node TEXT='${name}' POSITION='left' FOLDED='true'>"
				} else {
					println "<node TEXT='${name}' POSITION='left'>"
				}
			} else if (it.children().size() > 0) {
				println "<node TEXT='${name}' POSITION='right' FOLDED='true'>"
			} else {
				println "<node TEXT='${name}' POSITION='right'>"
			}
		}
		if (it.children().size() > 0) printChildren(it,level)
		if (name!=null)	println "</node>"
	}
}

// footer
println "</node></map>"


def String huntForName(node) {
	preferNodeNames = ["junitreport"]
	if (node == null) return null
	if (preferNodeNames.contains(node.name())) return node.name()
	if (node['@name'] != null) return node['@name']
	if (node['@todir'] != null) return node['@todir']
	if (node['@dir'] != null) return node['@dir']
	if (node['@refid'] != null) return node['@refid']
	return node.name()
}

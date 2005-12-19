package org.codehaus.groovy.grails.orm.hibernate;
class HibernateMappedClass {
	
	private Integer id;
	private String myProp;
	
	public void setId(Integer id) {
		this.id = id;		
	}
	
	public Integer getId() {
		return this.id;
	}
	
	public void setMyProp(String myProp) {
		this.myProp = myProp;
	}
	
	public String getMyProp() {
		return this.myProp;
	}
}
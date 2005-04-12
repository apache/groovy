/*
 * Groovy script to be read as a file to test the file based codesource features of groovy security.
 * The file extension of .gvy is used to prevent this script from being treated as a groovy script by maven.
 */
 
new GroovyShell().evaluate("1+2", "forbiddenCodeBaseTest", "/groovy/security/forbiddenCodeBase");

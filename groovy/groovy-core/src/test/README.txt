TEST ORGANISATION
=================

We'll be mainaining Classic and New Groovy for a couple of releases so if you are gonna add/modify a test, please
follow these guidelines...

groovy-core/src/test      is for Classic Groovy test cases
groovy-core/src/test-new  is for New Groovy test cases


* all new tests go in their regular place in groovy-core/src/test

* if need be a new test could be duplicated into the groovy-core/src/test-new directory but this is optional

* when we are removing the old parser in 2 releases time, we copy the test-new tests over the top of the test
  directory (so we'll keep any new tests added in the meantime)

* any significant modifications to tests, should where possible be done in new test classes
  (so we don't loose the change when we do the above merge)

* we only migrate tests from these areas after we've migrated to New Groovy
  (or we carefully remove the tests from both 'test' and 'test-new' if we're really keen).

* before introducing new tests in /src/test , we need to check that a test
  under that name does not yet exists in /src/test-new.

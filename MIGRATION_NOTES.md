# Migration Notes

All scanner hits were reviewed against the g94-to-PAPI-20260204 migration data. Every
real migration site (6 Cat-C operator mutations on Gradle `list` properties plus the
single Cat-A `JavaExec.setArgs` call) was rewritten in place and is not listed here.
The entries below are the remaining hits, all confirmed **false positives** — the
receiver at each call site is not a migrated Gradle property.

## False positives — `excludes` on the third-party RAT plugin task

The `rat` task comes from `org.nosphere.apache:creadur-rat-gradle` (applied as
`org.nosphere.apache.rat`). Its `excludes` property belongs to `org.nosphere.apache.rat.RatTask`,
a non-`org.gradle` type, so it is out of scope. The scanner matched it only against
`JacocoTaskExtension.excludes` by name.

### `subprojects/groovy-groovydoc/build.gradle`
- line 67 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), stylesheet.css exclusion, not a Gradle property
- line 68 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), prism js/css exclusion, not a Gradle property

### `subprojects/groovy-groovysh/build.gradle`
- line 68 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), GroovyEngine.java exclusion, not a Gradle property
- line 69 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), ObjectInspector.groovy exclusion, not a Gradle property
- line 70 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), Utils.groovy exclusion, not a Gradle property
- line 71 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), JrtJavaBasePackages.java exclusion, not a Gradle property
- line 72 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), PackageHelper.java exclusion, not a Gradle property

### `subprojects/groovy-json/build.gradle`
- line 49 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), groovy9802.json test-resource exclusion, not a Gradle property

### `subprojects/groovy-swing/build.gradle`
- line 41 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), generated _swing-builder-widgets.adoc exclusion, not a Gradle property

### `subprojects/groovy-templates/build.gradle`
- line 39 [Cat-C]: `excludes` — RatTask.excludes (org.nosphere.apache.rat), template test-resource exclusions, not a Gradle property

## False positive — local Groovy list variable

### `build-logic/src/main/groovy/org.apache.groovy-tested.gradle`
- line 269 [Cat-C]: `excludes` — local `def excludes = []` plain Groovy list inside the buildExcludeFilter() closure, not a Gradle property

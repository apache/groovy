# Migration Notes

This file records the Category-C scanner hits that were **not** rewritten because,
on inspection, their receivers are not migrated Gradle lazy properties. The six real
`list`-kind hits (compile/fork `jvmArgs`, `CompileOptions.compilerArgs`,
`AntlrTask.arguments`) were rewritten in place and removed from this file.

All remaining hits below are confirmed false positives. The scanner could only guess
`JacocoTaskExtension.excludes` because DSL files carry no typed imports; in every case
below the actual receiver is either a plain Groovy local list or the third-party Apache
RAT task, neither of which is `org.gradle.testing.jacoco.plugins.JacocoTaskExtension`.

## Confirmed false positives

### Plain Groovy local variable (not a Gradle property)

- line 269 [Cat-C]: `excludes` in `org.apache.groovy-tested.gradle` — receiver is a local `def excludes = []` declared in `buildExcludeFilter(boolean)`, an ordinary Groovy `ArrayList`, not a Gradle property
  - source: `        excludes << 'groovy/grape/'`

### Third-party Apache RAT task (`org.nosphere.apache.rat`, creadur-rat-gradle 0.8.1)

These all sit inside `tasks.named('rat') { … }`. `RatTask.getExcludes()` returns a plain
`List<String>` owned by the non-`org.gradle` plugin package, so the lazy `ListProperty`
rule does not apply and rewriting would break the third-party task.

- line 67 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovydoc/build.gradle` excluding `stylesheet.css` — third-party `org.nosphere.apache.rat.RatTask`, not JacocoTaskExtension
  - source: `    excludes << '**/stylesheet.css' // MIT license as per NOTICE/LICENSE files`
- line 68 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovydoc/build.gradle` excluding `prism*.js`/`prism*.css` — third-party RatTask, plain `List<String>` receiver
  - source: `    excludes << '**/prism*.js' << '**/prism*.css' // Prism, MIT license as per NOTICE/LICENSE files`
- line 68 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovysh/build.gradle` excluding `jline/GroovyEngine.java` — third-party RatTask, not a Gradle lazy property
  - source: `    excludes << '**/jline/GroovyEngine.java' // BSD license as per NOTICE/LICENSE files`
- line 69 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovysh/build.gradle` excluding `jline/ObjectInspector.groovy` — third-party RatTask receiver
  - source: `    excludes << '**/jline/ObjectInspector.groovy' // BSD license as per NOTICE/LICENSE files`
- line 70 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovysh/build.gradle` excluding `jline/Utils.groovy` — third-party RatTask, non-`org.gradle` package
  - source: `    excludes << '**/jline/Utils.groovy' // BSD license as per NOTICE/LICENSE files`
- line 71 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovysh/build.gradle` excluding `jline/JrtJavaBasePackages.java` — third-party RatTask, plain list
  - source: `    excludes << '**/jline/JrtJavaBasePackages.java' // BSD license as per NOTICE/LICENSE files`
- line 72 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-groovysh/build.gradle` excluding `jline/PackageHelper.java` — third-party RatTask, not JacocoTaskExtension
  - source: `    excludes << '**/jline/PackageHelper.java' // BSD license as per NOTICE/LICENSE files`
- line 49 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-json/build.gradle` excluding `groovy9802.json` — third-party RatTask receiver, plain `List<String>`
  - source: `    excludes += [`
- line 41 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-swing/build.gradle` excluding `_swing-builder-widgets.adoc` — third-party RatTask, non-Gradle type
  - source: `    excludes += [`
- line 39 [Cat-C]: `excludes` on RatTask in `subprojects/groovy-templates/build.gradle` excluding spec/test resources — third-party RatTask, not a migrated Gradle property
  - source: `    excludes += [`

#!/bin/sh

# Ensure these paths are correct

#JAVA_HOME=/usr/local/java
GROOVY_HOME=~/groovy-1.0-beta-2
GROOVY_SRC=$GROOVY_HOME/src/main
ASM_SRC=~/ASM/dev/src

# End of paths




BUILD_DIR=build
LIB_NAME=groovy

rm -rf $BUILD_DIR lib$LIB_NAME.{jar,so}
mkdir -p $BUILD_DIR

BASE_DIR=`pwd`
cd $BUILD_DIR
BUILD_DIR=`pwd`
cd $BASE_DIR

# Copy mimimum required source files to temp directory
(cd $GROOVY_SRC && cp --parents `grep -v '#' $BASE_DIR/groovy-src` $BUILD_DIR)
(cd $ASM_SRC && cp --parents `grep -v '#' $BASE_DIR/asm-src` $BUILD_DIR)

# Patch the source files
patch --silent -f -p0 -d $BUILD_DIR < patch.diff

# .java -> .class
find $BUILD_DIR -name \*.java | xargs gcj -C

# .class -> .jar
jar -cf lib$LIB_NAME.jar -C $BUILD_DIR .

# .class -> .so
gcj -shared -o lib$LIB_NAME.so lib$LIB_NAME.jar

# clean up
rm -rf $BUILD_DIR


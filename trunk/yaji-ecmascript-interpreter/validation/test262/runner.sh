#!/bin/bash
#vim: tw=80:sw=4

JAVA=`which java`
TEST262_ROOT_DIR=${1-"./test262"}
YAJI_BUILD_DIR=${2-"../../bin"}
LOG_DIR="./logs"

$JAVA -cp $YAJI_BUILD_DIR org.yaji.test.Runner --testDir=$TEST262_ROOT_DIR/converted --logFile=$LOG_DIR/$0.$$.log --excludes=$TEST262_ROOT_DIR/test/config/excludelist.xml --originals=$TEST262_ROOT_DIR/test/suite | tee $LOG_DIR/$0.$$.out

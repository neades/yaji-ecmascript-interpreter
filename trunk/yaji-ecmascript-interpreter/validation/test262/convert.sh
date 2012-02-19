#!/bin/bash

find ./test/suite -name '*.js' -print > /tmp/convert.$$

for i in `cat /tmp/convert.$$` ; do
  testName=`echo $i | sed -e "s%./test/suite/%%" -e "s%.js%%"`
  dirName=`dirname $testName`
  echo $testName [converted/$dirName]
  mkdir -pv converted/$dirName
  echo "python tools/packaging/test262.py --command 'echo {{path}}' $testName --cat > converted/${testName}.js"
  python tools/packaging/test262.py --command 'echo {{path}}' $testName --cat |  sed 's/\(^[[:blank:]][[:blank:]]*\)function[[:blank:]]*\([a-zA-Z0-9_$][a-zA-Z0-9_$]*\)/\1var \2 = function \2/' > converted/${testName}.js

done

rm /tmp/convert.$$

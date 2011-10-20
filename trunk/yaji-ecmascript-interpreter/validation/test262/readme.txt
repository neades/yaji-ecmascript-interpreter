To run test262 tests:

Install python.

Download tests: http://hg.ecmascript.org/tests/test262/

(Instructions: http://wiki.ecmascript.org/doku.php?id=test262:command)

You can run the tests directly using the syntax:

cd <test262 download directory>
python python tools/packaging/test262.py --command 'java -jar <yaji-ecmascript-interpreter>/lib/fesi.jar -f {{path}}'

However this is very slow!

org.yaji.test.Runner provides a way of running the tests without reinvoking the JVM for every test.

First the tests need to be converted so that they are executable. To do this

cd <test262 download directory>
<yaji-ecmascript-interpreter>/validation/test262/convert.sh 

The Runner can then be invoked using:

java -cp <yaji-ecmascript-interpreter>/build org.yaji.test.Runner --testDir=<test262 download directory>/converted --logFile=/tmp/test.txt --excludes=<test262 download directory>/test/config/excludelist.xml --originals=<test262 download directory>/test/suite

To explain the options:

--testDir - directory containing converted tests
--logFile - filename of report file. 
--excludes - a list of tests to exclude (shipped with test262 download)
--originals - directory containing the unconverted tests - needed to determine if it is "negative" test i.e. expected to fail.

To run test262 tests:

Install python (version 2).

Download or checkout tests:
- http://hg.ecmascript.org/tests/test262/
- hg clone http://hg.ecmascript.org/tests/test262 test262


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

java -cp <yaji-ecmascript-interpreter>/build org.yaji.test.Runner \
     --testDir=<test262 download directory>/converted \
     --logFile=/tmp/test.txt \
     --excludes=<test262 download directory>/test/config/excludelist.xml:<project.dir>/validation/test262/excludelist.xml \
     --originals=<test262 download directory>/test/suite \
     --overrideDir=<project.dir>/validation/test262/converted

To explain the options:

--excludes - a list of files containing lists of tests to exclude (shipped with test262 download)
             There is also a list of additional excludes where the tests are faulty
--logFile - filename of report file. 
--originals - directory containing the unconverted tests - needed to determine if it is "negative" test i.e. expected to fail.
--overrideDir - directory containing modified versions of test262 tests which override the downloaded version 
--testDir - directory containing converted tests
--threads=<n> Number of concurrent tests to run


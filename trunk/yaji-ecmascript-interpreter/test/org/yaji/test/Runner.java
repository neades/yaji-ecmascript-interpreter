package org.yaji.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import FESI.Interpreter.Evaluator;

public class Runner {

    private static SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
    
    private final Arguments arguments;
    private PrintWriter logger;
    private int testCount;
    private int testPassed;
    private HashSet<String> excludes;

    private File originalDirectory;

    private static class Arguments extends HashMap<String,String> {

        private static final long serialVersionUID = -45926745254286446L;

        public Arguments(String[] args) {
            for (String arg : args) {
                if (arg.startsWith("--")) {
                    String[] components = arg.substring(2).split("=");
                    if (components.length == 2) {
                        put(components[0],components[1]);
                    } else {
                        throw new Error("Invalid argument "+arg);
                    }
                } else {
                    throw new Error("Invalid argument "+arg);
                }
            }
        }

    }

    public Runner(Arguments arguments) throws IOException {
        this.arguments = arguments;
        String logFile = arguments.get("logFile");
        this.logger = createLogger(logFile);
        String excludesFile = arguments.get("excludes");
        this.excludes = new HashSet<String>();
        if (excludesFile != null) {
            readExcludesFile(excludesFile,this.excludes);
        }
        String originalDirectoryName = arguments.get("originals");
        if (originalDirectoryName != null) {
            originalDirectory = new File(originalDirectoryName);
        } else {
            originalDirectory = null;
        }
    }

    private void readExcludesFile(String excludesFile, final HashSet<String> excludesSet) {
        try {
            SAXParser parser = saxParserFactory.newSAXParser();
            parser.parse(new File(excludesFile), new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes)
                        throws SAXException {
                    if ("test".equals(qName)) {
                        String id = attributes.getValue("id");
                        if (id != null) {
                            excludesSet.add(id);
                        }
                    }
                }
            });
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private PrintWriter createLogger(String logFile) throws IOException {
        PrintWriter logger;
        if (logFile != null) {
            logger = new PrintWriter(new FileWriter(logFile));
        } else {
            logger = new PrintWriter(System.out) {
                @Override
                public void close() {
                    // Ignore
                }
            };
        }
        return logger;
    }
    
    public void run() {
        File rootDirectory = new File(arguments.get("testDir"));
        
        runTestsInDirectory(rootDirectory);
        
        logger.close();
    }

    private static class ThreadRunner extends Thread {

        private final Queue<TestFile> tests;

        public ThreadRunner(Queue<TestFile> tests) {
            this.tests = tests;
        }
        
        @Override
        public void run() {
            TestFile test = tests.poll();
            while (test != null) {
                test.execute();
                test = tests.poll();
            }
        }
    }
    
    private void runTestsInDirectory(File rootDirectory) {
        List<TestFile> tests = getListOfTests(rootDirectory);
        
        Queue<TestFile> testQueue = new ConcurrentLinkedQueue<TestFile>(tests);
        long start = System.currentTimeMillis();
        Thread[] threads = new Thread[2];
        for(int i=0; i<threads.length; i++) {
            threads[i] = new ThreadRunner(testQueue);
            threads[i].start();
        }
        waitForThreadsToExit(threads);
        long duration = System.currentTimeMillis() - start;
        System.out.println();
        System.out.println("Run completed: "+testPassed+"/"+testCount+" ("+toReadableDuration(duration)+") "+(new Date()).toString());
    }

    private void waitForThreadsToExit(Thread[] threads) {
        for(int i=0; i<threads.length; i++) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private List<TestFile> getListOfTests(File rootDirectory) {
        List<TestFile> tests = new ArrayList<TestFile>();
        List<File> directoriesToProcess = new LinkedList<File>();
        directoriesToProcess.add(rootDirectory);
        do {
            File directory = directoriesToProcess.remove(0);
            File [] files = directory.listFiles();
            for (File file : files) {
                if (file.isDirectory()) {
                    directoriesToProcess.add(file);
                } else if (file.getName().endsWith(".js")){
                    TestFile test = new TestFile(rootDirectory,file);
                    if (!excludes.contains(test.getTestId())) {
                        tests.add(test);
                    }
                }
            }
        } while (directoriesToProcess.size() > 0);
        return tests;
    }

    private String toReadableDuration(long duration) {
        StringBuilder sb = new StringBuilder();
        toReadableDuration(sb, duration);
        return sb.toString();
    }

    private void toReadableDuration(Appendable sb, long duration) {
        Formatter formatter = new Formatter(sb);
        long seconds = duration/1000L;
        formatter.format("%d:%02d.%03d", Long.valueOf(seconds/60),Long.valueOf(seconds%60),Long.valueOf(duration%1000L));
    }

    
    private class TestFile {
        private String testName;
        private String testId;
        private boolean negative;
        private final File testFile;

        public TestFile(File rootDirectory, File testFile) {
            this.testFile = testFile;
            testName = getTestName(rootDirectory, testFile);
            testId = testName.substring(testName.lastIndexOf(File.separatorChar)+1);
            negative = isNegative(testName);
        }
        
        public String getTestId() {
            return testId;
        }
    
        public void execute() {
            long start = System.currentTimeMillis();
            boolean passed = executeTest(testFile, testName);
            if (negative) {
                passed = !passed;
            }
            logTestResult(testName, passed, System.currentTimeMillis()-start);
        }
    }
    
    private boolean executeTest(File file, String testName) {
        Evaluator evaluator = new Evaluator();
        evaluator.setDefaultTimeZone(TimeZone.getTimeZone("UTC"));
        boolean passed = false;
        logStart(testName);
        try {
            evaluator.evaluate(file);
            passed = true;
        } catch (Throwable e) {
            logException(e);
        }
        return passed;
    }

    private synchronized void logException(Throwable e) {
        try {
            e.printStackTrace(logger);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

    private synchronized void logStart(String testName) {
        logger.append("Executing ").append(testName).append(" ");
    }

    private String getTestName(File rootDirectory, File file) {
        int rootLength = rootDirectory.getAbsolutePath().length();
        String testPath = file.getAbsolutePath();
        return testPath.substring(rootLength, testPath.length()-3);
    }

    private synchronized void logTestResult(String testName, boolean passed, long duration) {
        testCount ++;
        logger.println();
        if (passed) {
            logger.print("Result " + testName + " SUCCESS (");
            testPassed ++;
            System.out.print('.');
        } else {
            logger.print("Result " + testName + " FAIL (");
            System.out.print('E');
        }
        if (testCount % 80 == 0) {
            System.out.println();
        }
        toReadableDuration(logger, duration);
        logger.println(")");
        System.out.flush();
    }
    
    private boolean isNegative(String testName) {
        File originalFile = new File(originalDirectory,testName+".js");
        boolean isNegative = false;
        if (originalFile.isFile()) {
            try {
                BufferedReader reader = new BufferedReader(new FileReader(originalFile));
                String line = reader.readLine();
                do {
                    if (line.contains("@negative")) {
                        isNegative = true;
                    }
                    line = reader.readLine();
                } while (line != null && !isNegative);
                reader.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Could not locate file "+originalFile.getAbsolutePath());
        }
        return isNegative;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            Runner runner = new Runner(new Arguments(args));
            runner.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

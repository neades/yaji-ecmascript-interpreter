package FESI.Integration;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import FESI.Interpreter.Interpret;

@RunWith(Parameterized.class)
public class ESTestFilesTest {
    private final String filename;
    private final class TestInterpret extends Interpret {
        boolean hasErrored() {
            return anyError;
        }
        @Override
        public void exit() {
            // Don't exit the VM!
        }
    }

    @Parameters
    public static List<Object[]> data() {
        File validationDirectory = findValidationDirectory();
        if (validationDirectory == null) {
            fail("Could not locate validation directory");
        }
        File[] files = validationDirectory.listFiles(new FilenameFilter() {
            
            public boolean accept(File dir, String name) {
                return name.endsWith(".estest");
            }
        });
        ArrayList<Object[]> filesToProcess = new ArrayList<Object[]>(files.length);
        for (File file : files) {
            filesToProcess.add(new Object[] { file.getAbsolutePath() });
        }
        return filesToProcess;
    }

    private static File findValidationDirectory() {
        File cwd = new File(System.getProperty("user.dir"));
        do {
            File dir = new File(cwd,"validation");
            if (dir.isDirectory()) {
                return dir;
            }
            cwd = dir.getParentFile();
        } while (cwd != null);
        return null;
    }
    
    public ESTestFilesTest(String filename) {
        this.filename = filename;
    }

    @Test
    public void runFile() {
        TestInterpret interpreter = new TestInterpret();
        interpreter.doWork(new String[] { "-T" , filename });
        assertFalse("Testing "+filename,interpreter.hasErrored());
    }
}

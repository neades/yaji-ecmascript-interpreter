// FileIO.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
// Advanced FESI Copyright (c) Graham Technology, 2002
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation; either
// version 2 of the License, or (at your option) any later version.

// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.

// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

package FESI.Extensions;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;

import FESI.Data.ArrayPrototype;
import FESI.Data.BuiltinFunctionObject;
import FESI.Data.DatePrototype;
import FESI.Data.ESBoolean;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.FunctionPrototype;
import FESI.Data.GlobalObject;
import FESI.Data.ObjectPrototype;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.ScopeChain;

/**
 * An EcmaScript FileIO 'File' object
 */
class ESFile extends ESObject {
    private static final long serialVersionUID = -2502912532866913867L;
    File file = null;
    /**
     * Advanced FESI GT Modified: 5/10/2002 Added the transient modifier to
     * allow serialization.
     */
    transient Object readerWriter = null;
    boolean atEOF = false;
    String lastLine = null;
    Throwable lastError = null;

    ESFile(ESObject prototype, Evaluator evaluator, String fileName) {
        super(prototype, evaluator);
        file = new File(fileName);
    }

    ESFile(ESObject prototype, Evaluator evaluator, String pathName,
            String fileName) {
        super(prototype, evaluator);
        file = new File(pathName, fileName);
    }

    // for subclass
    protected ESFile(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
    }

    public String getESClassName() {
        return "File";
    }

    public String toString() {
        if (file == null)
            return "<null>";
        return file.toString();
    }

    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + ((file == null) ? "null" : file.toString()) + "]";
    }

    protected void setError(Throwable e) {
        lastError = e;
    }

    public boolean exists() {
        if (file == null)
            return false;
        return file.exists();
    }

    public boolean open() {
        if (readerWriter != null) {
            setError(new IllegalStateException("File already open"));
            return false;
        }
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }

        // We assume that the BufferedReader and PrintWriter creation
        // cannot fail except if the FileReader/FileWriter fails.
        // Otherwise we have an open file until the reader/writer
        // get garbage collected.
        try {
            if (file.exists()) {
                readerWriter = new BufferedReader(new FileReader(file));
            } else {
                readerWriter = new PrintWriter(new FileWriter(file));
            }
            return true;
        } catch (IOException e) {
            setError(e);
            return false;
        }
    }

    public boolean isOpened() {
        return (readerWriter != null);
    }

    public boolean close() {
        if (readerWriter == null)
            return false;
        try {
            if (readerWriter instanceof Reader) {
                ((Reader) readerWriter).close();
            } else {
                ((Writer) readerWriter).close();
            }
            readerWriter = null;
            return true;
        } catch (IOException e) {
            setError(e);
            readerWriter = null;
            return false;
        }
    }

    public boolean write(boolean ln, ESValue[] arguments) {
        if (readerWriter == null) {
            setError(new IllegalStateException("File not opened"));
            return false;
        }
        if (!(readerWriter instanceof PrintWriter)) {
            setError(new IllegalStateException("File not opened for writing"));
            return false;
        }
        PrintWriter writer = (PrintWriter) readerWriter;
        for (int i = 0; i < arguments.length; i++) {
            writer.print(arguments[i].toString());
        }
        if (ln)
            writer.println();
        return true;
    }

    public String readln() {
        if (readerWriter == null) {
            setError(new IllegalStateException("File not opened"));
            return null;
        }
        if (!(readerWriter instanceof BufferedReader)) {
            setError(new IllegalStateException("File not opened for reading"));
            return null;
        }
        if (atEOF) {
            setError(new EOFException());
            return null;
        }
        if (lastLine != null) {
            String line = lastLine;
            lastLine = null;
            return line;
        }
        BufferedReader reader = (BufferedReader) readerWriter;
        // Here lastLine is null, return a new line
        try {
            String line = reader.readLine();
            if (line == null) {
                atEOF = true;
                setError(new EOFException());
            }
            return line;
        } catch (IOException e) {
            setError(e);
            return null;
        }
    }

    public boolean eof() {
        if (readerWriter == null) {
            setError(new IllegalStateException("File not opened"));
            return true;
        }
        if (!(readerWriter instanceof BufferedReader)) {
            setError(new IllegalStateException("File not opened for read"));
            return true;
        }
        if (atEOF)
            return true;
        if (lastLine != null)
            return false;
        BufferedReader reader = (BufferedReader) readerWriter;
        try {
            lastLine = reader.readLine();
            if (lastLine == null)
                atEOF = true;
            return atEOF;
        } catch (IOException e) {
            setError(e);
            return true;
        }
    }

    public boolean isFile() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }
        return file.isFile();
    }

    public boolean isDirectory() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }
        return file.isDirectory();
    }

    public boolean flush() {
        if (readerWriter == null) {
            setError(new IllegalStateException("File not opened"));
            return false;
        }
        if (readerWriter instanceof Writer) {
            try {
                ((Writer) readerWriter).flush();
            } catch (IOException e) {
                setError(e);
                return false;
            }
        } else {
            setError(new IllegalStateException("File not opened for write"));
            return false; // not supported by reader
        }
        return true;
    }

    public long getLength() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return -1;
        }
        return file.length();
    }

    public long lastModified() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return 0L;
        }
        return file.lastModified();
    }

    public String error() {
        if (lastError == null) {
            return "";
        }
        String exceptionName = lastError.getClass().getName();
        int l = exceptionName.lastIndexOf(".");
        if (l > 0)
            exceptionName = exceptionName.substring(l + 1);
        return exceptionName + ": " + lastError.getMessage();

    }

    public void clearError() {
        lastError = null;
    }

    public boolean remove() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }
        if (readerWriter != null) {
            setError(new IllegalStateException(
                    "An openened file cannot be removed"));
            return false;
        }
        return file.delete();
    }

    public boolean renameTo(ESFile toFile) {
        if (file == null) {
            setError(new IllegalArgumentException(
                    "Uninitialized source File object"));
            return false;
        }
        if (toFile.file == null) {
            setError(new IllegalArgumentException(
                    "Uninitialized target File object"));
            return false;
        }
        if (readerWriter != null) {
            setError(new IllegalStateException(
                    "An openened file cannot be renamed"));
            return false;
        }
        if (toFile.readerWriter != null) {
            setError(new IllegalStateException(
                    "You cannot rename to an openened file"));
            return false;
        }
        return file.renameTo(toFile.file);
    }

    public boolean canRead() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }
        return file.canRead();
    }

    public boolean canWrite() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return false;
        }
        return file.canWrite();
    }

    public String getParent() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return "";
        }
        String parent = file.getParent();
        return (parent == null ? "" : parent);
    }

    public String getName() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return "";
        }
        String name = file.getName();
        return (name == null ? "" : name);
    }

    public String getPath() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return "";
        }
        String path = file.getPath();
        return (path == null ? "" : path);
    }

    public String getAbsolutePath() {
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return "";
        }
        String absolutPath = file.getAbsolutePath();
        return (absolutPath == null ? "" : absolutPath);
    }

    public boolean isAbsolute() {
        if (file == null)
            return false;
        return file.isAbsolute();
    }

    public boolean mkdir() {
        if (file == null)
            return false;
        if (readerWriter != null)
            return false;
        return file.mkdirs(); // Using multi directory version
    }

    public String[] list() {
        if (file == null)
            return null;
        if (readerWriter != null)
            return null;
        if (!file.isDirectory())
            return null;
        return file.list();
    }

    public String readAll() {
        // Open the file for readAll
        if (readerWriter != null) {
            setError(new IllegalStateException("File already open"));
            return null;
        }
        if (file == null) {
            setError(new IllegalArgumentException("Uninitialized File object"));
            return null;
        }
        try {
            if (file.exists()) {
                readerWriter = new BufferedReader(new FileReader(file));
            } else {
                setError(new IllegalStateException("File does not exist"));
                return null;
            }
            if (!file.isFile()) {
                setError(new IllegalStateException("File is not a regular file"));
                return null;
            }

            // read content line by line to setup properl eol
            StringBuilder buffer = new StringBuilder(
                    (int) (file.length() * 1.10));
            BufferedReader reader = (BufferedReader) readerWriter;
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                buffer.append(line);
                buffer.append('\n'); // EcmaScript EOL
            }

            // Close the file
            ((Reader) readerWriter).close();
            readerWriter = null;
            return buffer.toString();
        } catch (IOException e) {
            readerWriter = null;
            setError(e);
            return null;
        }
    }

} // class ESFile

/**
 * An EcmaScript FileIO File 'constant' as in, err or out
 */
class ESStdFile extends ESFile {
    private static final long serialVersionUID = -4492825896310456818L;
    String name;
    /**
     * Advanced FESI GT Modified: 5/10/2002 Added the transient modifier to
     * allow serialization.
     */
    transient InputStream ins = null;
    transient PrintStream outs = null;

    ESStdFile(ESObject prototype, Evaluator evaluator, String name,
            InputStream ins) {
        super(prototype, evaluator);
        this.name = name;
        this.ins = ins;
    }

    ESStdFile(ESObject prototype, Evaluator evaluator, String name,
            PrintStream outs) {
        super(prototype, evaluator);
        this.name = name;
        this.outs = outs;
    }

    public String toString() {
        return name;
    }

    public boolean exists() {
        return true;
    }

    public boolean open() {
        return false;
    }

    public boolean isOpened() {
        return true;
    }

    public boolean close() {
        return false;
    }

    public boolean write(boolean ln, ESValue[] arguments) {
        if (outs == null) {
            setError(new IllegalStateException("File not opened for writing"));
            return false;
        }
        for (int i = 0; i < arguments.length; i++) {
            outs.println(arguments[i].toString());
        }
        if (ln)
            outs.println();
        outs.flush();
        return true;
    }

    public String readln() {
        if (ins == null) {
            setError(new IllegalStateException("File not opened for reading"));
            return null;
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(ins));
        // Here lastLine is null, return a new line
        try {
            String line = reader.readLine();
            if (line == null) {
                atEOF = true;
                setError(new EOFException());
            }
            atEOF = false;
            return line;
        } catch (IOException e) {
            setError(e);
            return null;
        }
    }

    public boolean eof() {
        if (ins == null) {
            setError(new IllegalStateException("File not opened for read"));
            return true;
        }
        return atEOF;
    }

    public boolean isFile() {
        return true;
    }

    public boolean isDirectory() {
        return false;
    }

    public boolean flush() {
        if (outs == null) {
            setError(new IllegalStateException("File not opened for write"));
            return false;
        }
        return true; // done at each write anyhow
    }

    public long getLength() {
        return -1;
    }

    public long lastModified() {
        return 0L;
    }

    public boolean remove() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return false;
    }

    public boolean renameTo(ESFile toFile) {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return false;
    }

    public boolean canRead() {
        return (ins != null);
    }

    public boolean canWrite() {
        return (outs != null);
    }

    public String getParent() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return "";
    }

    public String getName() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return "";
    }

    public String getPath() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return "";
    }

    public String getAbsolutePath() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return "";
    }

    public boolean isAbsolute() {
        return true;
    }

    public boolean mkdir() {
        setError(new IllegalArgumentException(
                "Operation invalid on standard input/output"));
        return false;
    }

    public String[] list() {
        return null;
    }

} // class ESStdFile

public class FileIO extends Extension {
    private static final long serialVersionUID = 2861456587190109112L;

    class GlobalObjectFile extends BuiltinFunctionObject {
        private static final long serialVersionUID = 8414668332754933785L;

        GlobalObjectFile(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            return doConstruct(thisObject, arguments);
        }

        public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = null;
            if (arguments.length == 0) {
                throw new EcmaScriptException("File requires 1 or 2 arguments");
            } else if (arguments.length == 1) {
                file = new ESFile(esFilePrototype, this.getEvaluator(),
                        arguments[0].toString());
            } else if (arguments.length > 1) {
                file = new ESFile(esFilePrototype, this.getEvaluator(),
                        arguments[0].toString(), arguments[1].toString());
            }
            return file;
        }

        public ESValue getPropertyInScope(String propertyName,
                ScopeChain previousScope, int hash) throws EcmaScriptException {
            if (propertyName.equals("separator")) {
                return new ESString(File.separator);
            }
            return super.getPropertyInScope(propertyName, previousScope, hash);
        }

        public ESValue getProperty(String propertyName, int hash)
                throws EcmaScriptException {
            if (propertyName.equals("separator")) {
                return new ESString(File.separator);
            }
            return super.getProperty(propertyName, hash);

        }

        public String[] getSpecialPropertyNames() {
            String[] ns = { "separator" };
            return ns;
        }

    } // class GlobalObjectFile

    static class FileWriteln extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1212940361346379188L;

        FileWriteln(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.write(true, arguments));
        }
    }

    static class FileReadln extends BuiltinFunctionObject {
        private static final long serialVersionUID = -6563844058409689916L;

        FileReadln(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            String line = file.readln();
            if (line == null) {
                return ESNull.theNull;
            }
            return new ESString(line);

        }
    }

    static class FileEof extends BuiltinFunctionObject {
        private static final long serialVersionUID = -4173944880733445209L;

        FileEof(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.eof());
        }
    }

    static class FileExists extends BuiltinFunctionObject {
        private static final long serialVersionUID = 7326672957348254206L;

        FileExists(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.exists());
        }
    }

    static class FileIsOpened extends BuiltinFunctionObject {
        private static final long serialVersionUID = 8354315134724021693L;

        FileIsOpened(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.isOpened());
        }
    }

    static class FileIsAbsolute extends BuiltinFunctionObject {
        private static final long serialVersionUID = 686656951279531101L;

        FileIsAbsolute(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.isAbsolute());
        }
    }

    static class FileIsFile extends BuiltinFunctionObject {
        private static final long serialVersionUID = 9013756116339087947L;

        FileIsFile(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.isFile());
        }
    }

    static class FileIsDirectory extends BuiltinFunctionObject {
        private static final long serialVersionUID = 353394458055715978L;

        FileIsDirectory(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.isDirectory());
        }
    }

    static class FileWrite extends BuiltinFunctionObject {
        private static final long serialVersionUID = 5761934263495777587L;

        FileWrite(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.write(false, arguments));
        }
    }

    static class FileOpen extends BuiltinFunctionObject {
        private static final long serialVersionUID = 3197833025539110487L;

        FileOpen(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;

            return ESBoolean.makeBoolean(file.open());
        }
    }

    static class FileClose extends BuiltinFunctionObject {
        private static final long serialVersionUID = -8172955647017598492L;

        FileClose(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.close());
        }
    }

    static class FileFlush extends BuiltinFunctionObject {
        private static final long serialVersionUID = -6292904766666156870L;

        FileFlush(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.flush());
        }
    }

    static class FileGetLength extends BuiltinFunctionObject {
        private static final long serialVersionUID = -5649442104693910207L;

        FileGetLength(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESNumber.valueOf((file.getLength()));
        }
    }

    static class FileLastModified extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6586666475850937489L;

        FileLastModified(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            long lmDate = file.lastModified();
            DatePrototype theDate = new DatePrototype(this.getEvaluator(),
                    lmDate);
            return theDate;
        }
    }

    static class FileError extends BuiltinFunctionObject {
        private static final long serialVersionUID = -2034139369850182826L;

        FileError(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return new ESString(file.error());
        }
    }

    static class FileClearError extends BuiltinFunctionObject {
        private static final long serialVersionUID = 8499703287448417367L;

        FileClearError(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            file.clearError();
            return ESUndefined.theUndefined;
        }
    }

    static class FileRemove extends BuiltinFunctionObject {
        private static final long serialVersionUID = 2926292978390363433L;

        FileRemove(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.remove());
        }
    }

    class FileRenameTo extends BuiltinFunctionObject {
        private static final long serialVersionUID = 2614804455764561224L;

        FileRenameTo(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            if (arguments.length <= 0)
                return ESBoolean.makeBoolean(false);
            ESFile toFile = null;
            if (arguments[0] instanceof ESFile) {
                toFile = (ESFile) arguments[0];
            } else {
                toFile = new ESFile(esFilePrototype, this.getEvaluator(),
                        arguments[0].toString());
            }
            return ESBoolean.makeBoolean(file.renameTo(toFile));
        }
    }

    static class FileCanWrite extends BuiltinFunctionObject {
        private static final long serialVersionUID = 6165625127683518191L;

        FileCanWrite(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.canWrite());
        }
    }

    static class FileCanRead extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1390115713208254039L;

        FileCanRead(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.canRead());
        }
    }

    static class FileGetName extends BuiltinFunctionObject {
        private static final long serialVersionUID = 5290629150488957956L;

        FileGetName(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return new ESString(file.getName());
        }
    }

    static class FileGetParent extends BuiltinFunctionObject {
        private static final long serialVersionUID = 5056689866244089478L;

        FileGetParent(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return new ESString(file.getParent());
        }
    }

    // Equivallent to toString
    static class FileGetPath extends BuiltinFunctionObject {
        private static final long serialVersionUID = -5427120519594023452L;

        FileGetPath(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return new ESString(file.getPath());
        }
    }

    static class FileGetAbsolutePath extends BuiltinFunctionObject {
        private static final long serialVersionUID = -6901269335876770825L;

        FileGetAbsolutePath(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return new ESString(file.getAbsolutePath());
        }
    }

    static class FileMkdir extends BuiltinFunctionObject {
        private static final long serialVersionUID = 2139015631181199033L;

        FileMkdir(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            return ESBoolean.makeBoolean(file.mkdir());
        }
    }

    static class FileList extends BuiltinFunctionObject {
        private static final long serialVersionUID = 41287224913507091L;

        FileList(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 0);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;
            String[] l = file.list();
            if (l == null) {
                return ESBoolean.makeBoolean(false);
            }
            ESObject ap = this.getEvaluator().getArrayPrototype();
            ArrayPrototype theArray = new ArrayPrototype(ap, this
                    .getEvaluator());
            theArray.setSize(l.length);
            for (int i = 0; i < l.length; i++) {
                theArray.setElementAt(new ESString(l[i]), i);
            }
            return theArray;

        }
    }

    static class FileReadAll extends BuiltinFunctionObject {
        private static final long serialVersionUID = 778630085159043813L;

        FileReadAll(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESFile file = (ESFile) thisObject;

            return new ESString(file.readAll());
        }
    }

    ESObject esFilePrototype = null;

    public FileIO() {
        super();
    }

    public void initializeExtension(Evaluator evaluator)
            throws EcmaScriptException {

        GlobalObject go = evaluator.getGlobalObject();
        ObjectPrototype op = (ObjectPrototype) evaluator.getObjectPrototype();

        esFilePrototype = new ObjectPrototype(op, evaluator);

        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();

        ESObject file = new GlobalObjectFile("File", evaluator, fp);

        ESObject infile = new ESStdFile(esFilePrototype, evaluator, "<stdin>",
                System.in);
        ESObject outfile = new ESStdFile(esFilePrototype, evaluator,
                "<stdout>", System.out);
        ESObject errfile = new ESStdFile(esFilePrototype, evaluator,
                "<stderr>", System.err);

        esFilePrototype.putHiddenProperty("open", new FileOpen("open",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("flush", new FileFlush("flush",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("close", new FileClose("close",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("exists", new FileExists("exists",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("isFile", new FileIsFile("isFile",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("isOpened", new FileIsOpened(
                "isOpened", evaluator, fp));
        esFilePrototype.putHiddenProperty("isAbsolute", new FileIsAbsolute(
                "isAbsolute", evaluator, fp));
        esFilePrototype.putHiddenProperty("isDirectory", new FileIsDirectory(
                "isDirectory", evaluator, fp));
        esFilePrototype.putHiddenProperty("eof", new FileEof("eof", evaluator,
                fp));
        esFilePrototype.putHiddenProperty("write", new FileWrite("write",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("writeln", new FileWriteln("writeln",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("readln", new FileReadln("readln",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("error", new FileError("error",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("clearError", new FileClearError(
                "clearError", evaluator, fp));
        esFilePrototype.putHiddenProperty("getLength", new FileGetLength(
                "getLength", evaluator, fp));

        esFilePrototype.putHiddenProperty("lastModified", new FileLastModified(
                "lastModified", evaluator, fp));
        esFilePrototype.putHiddenProperty("remove", new FileRemove("remove",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("renameTo", new FileRenameTo(
                "renameTo", evaluator, fp));
        esFilePrototype.putHiddenProperty("canWrite", new FileCanWrite(
                "canWrite", evaluator, fp));
        esFilePrototype.putHiddenProperty("canRead", new FileCanRead("canRead",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("getParent", new FileGetParent(
                "getParent", evaluator, fp));
        esFilePrototype.putHiddenProperty("getName", new FileGetName("getName",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("getPath", new FileGetPath("getPath",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("toString", new FileGetPath(
                "toString", evaluator, fp));
        esFilePrototype.putHiddenProperty("getAbsolutePath",
                new FileGetAbsolutePath("getAbsolutePath", evaluator, fp));
        esFilePrototype.putHiddenProperty("mkdir", new FileMkdir("mkdir",
                evaluator, fp));
        esFilePrototype.putHiddenProperty("list", new FileList("list",
                evaluator, fp));

        esFilePrototype.putHiddenProperty("readAll", new FileReadAll("readAll",
                evaluator, fp));

        file.putHiddenProperty("stdin", infile);
        file.putHiddenProperty("stdout", outfile);
        file.putHiddenProperty("stderr", errfile);

        go.putHiddenProperty("File", file);
    }
}

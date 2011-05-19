// Database.java
// FESI Copyright (c) Jean-Marc Lugrin, 1999
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

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;

import FESI.Data.BuiltinFunctionObject;
import FESI.Data.ESBoolean;
import FESI.Data.ESLoader;
import FESI.Data.ESNull;
import FESI.Data.ESNumber;
import FESI.Data.ESObject;
import FESI.Data.ESString;
import FESI.Data.ESUndefined;
import FESI.Data.ESValue;
import FESI.Data.ESWrapper;
import FESI.Data.FunctionPrototype;
import FESI.Data.GlobalObject;
import FESI.Data.ObjectPrototype;
import FESI.Exceptions.EcmaScriptException;
import FESI.Interpreter.Evaluator;
import FESI.Interpreter.LocalClassLoader;

/**
 * A Database object, representing a connection to a JDBC Driver
 */
class ESDatabase extends ESObject {
    private static final long serialVersionUID = 3834507683804498054L;
    private transient Connection connection = null; // Null if not connected
    private transient DatabaseMetaData databaseMetaData = null;
    private transient String driverName = null;
    private transient ClassLoader driverLoader = null;
    private transient Exception lastError = null;
    private transient ESObject esRowSetPrototype = null;
    private transient boolean driverOK = false;

    /**
     * Create a new database object based on a driver name, with driver on the
     * classpath
     * 
     * @param prototype
     *            The prototype object for all database object
     * @param evaluator
     *            The current evaluator
     * @param esRowSetPrototype
     *            The prototype to use to create row set
     * @param driverName
     *            The class name of the JDBC driver
     */

    ESDatabase(ESObject prototype, Evaluator evaluator,
            ESObject esRowSetPrototype, String driverName) {
        super(prototype, evaluator);
        this.driverName = driverName;
        this.esRowSetPrototype = esRowSetPrototype; // specific to an evaluator
        try {
            Class<?> driverClass = Class.forName(driverName);
            if (!Driver.class.isAssignableFrom(driverClass)) {
                // System.err.println("##Bad class " + driverClass);
                lastError = new EcmaScriptException("Class " + driverClass
                        + " is not a JDBC driver");
            }
            driverClass.newInstance(); // may be needed by some drivers,
                                       // harmless for others
        } catch (ClassNotFoundException e) {
            // System.err.println("##Cannot find driver class: " + e);
            // e.printStackTrace();
            lastError = e;
        } catch (InstantiationException e) {
            // System.err.println("##Cannot instantiate driver class: " + e);
            // e.printStackTrace();
            lastError = e;
        } catch (IllegalAccessException e) {
            // ignore as this may happen depending on driver, it may not be
            // severe
            // for example because the instance was created at class
            // initialization time
        }
        driverOK = true;
    }

    /**
     * Create a new database object based on a driver name and its classpath
     * 
     * @param prototype
     *            The prototype object for all database object
     * @param evaluator
     *            The current evaluator
     * @param esRowSetPrototype
     *            The prototype to use to create row set
     * @param driverName
     *            The class name of the JDBC driver
     * @param pathName
     *            The path to the classes of the JDBC driver
     */
    ESDatabase(ESObject prototype, Evaluator evaluator,
            ESObject esRowSetPrototype, String driverName, String pathName) {
        super(prototype, evaluator);
        this.driverName = driverName;
        this.esRowSetPrototype = esRowSetPrototype;
        try {
            this.driverLoader = LocalClassLoader.makeLocalClassLoader(pathName);
        } catch (EcmaScriptException e) {
            // System.err.println("##Cannot find driver class: " + e);
            // e.printStackTrace();
            lastError = e;
            return;
        }
        try {
            Class<?> driverClass = driverLoader.loadClass(driverName);
            if (!Driver.class.isAssignableFrom(driverClass)) {
                // System.err.println("##Bad class " + driverClass);
                // e.printStackTrace();
                lastError = new EcmaScriptException("Class " + driverClass
                        + " is not a JDBC driver");
                return;
            }
            driverClass.newInstance();
        } catch (ClassNotFoundException e) {
            // System.err.println("##Cannot find driver class: " + e);
            // e.printStackTrace();
            lastError = e;
        } catch (InstantiationException e) {
            // System.err.println("##Cannot instantiate driver class: " + e);
            // e.printStackTrace();
            lastError = e;
        } catch (IllegalAccessException e) {
            // System.err.println("##Cannot access driver class: " + e);
            // e.printStackTrace();
            lastError = e;
        }
        driverOK = true;
    }

    /**
     * Create the database prototype object which cannot be connected
     * 
     * @param prototype
     *            The prototype object for the database prototype object
     * @param evaluator
     *            The current evaluator
     */

    ESDatabase(ESObject prototype, Evaluator evaluator) {
        super(prototype, evaluator);
        this.driverName = null;
        this.esRowSetPrototype = null;
        driverOK = false; // Avoid usage of this object
    }

    public String getESClassName() {
        return "Database";
    }

    public String toString() {
        if (driverName == null)
            return "[database protoype]";
        return "[Database: '"
                + driverName
                + (driverOK ? (connection == null ? "' - disconnected] "
                        : " - connected]") : " - in error]");
    }

    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + this.toString() + "]";
    }

    ESValue getLastError() throws EcmaScriptException {
        if (lastError == null) {
            return ESNull.theNull;
        }
        return ESLoader.normalizeValue(lastError, getEvaluator());
    }

    /**
     * Connect to the database, using the specific url, optional user name and
     * password
     * 
     * @param arguments
     *            The argument list
     * @return true if successful, false otherwise
     */
    ESValue connect(ESValue arguments[]) throws EcmaScriptException {
        if (!driverOK) {
            throw new EcmaScriptException(
                    "Driver not initialized properly - cannot connect");
        }
        lastError = null;
        String url = (arguments.length > 0) ? arguments[0].toString() : null;
        String userName = (arguments.length > 1) ? arguments[1].toString()
                : null;
        String password = (arguments.length > 2) ? arguments[2].toString()
                : null;
        try {
            if (userName == null) {
                connection = DriverManager.getConnection(url);
            } else {
                connection = DriverManager.getConnection(url, userName,
                        password);
            }
        } catch (Exception e) {
            // System.err.println("##Cannot connect: " + e);
            // e.printStackTrace();
            lastError = e;
            return ESBoolean.makeBoolean(false);
        }
        return ESBoolean.makeBoolean(true);
    }

    /**
     * Disconnect from the database, nop if not conected
     * 
     * @return true if successful, false if error during idsconnect
     */
    ESValue disconnect() throws EcmaScriptException {
        if (!driverOK) {
            throw new EcmaScriptException(
                    "Driver not initialized properly - cannot disconnect");
        }
        lastError = null;
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                lastError = null;
            } catch (SQLException e) {
                // System.err.println("##Cannot disonnect: " + e);
                // e.printStackTrace();
                lastError = e;
                return ESBoolean.makeBoolean(false);
            }
        }
        return ESBoolean.makeBoolean(true);
    }

    ESValue release() {
        if (driverOK) {
            try {
                disconnect();
            } catch (EcmaScriptException e) {
                // ignored
            }
        }
        return ESUndefined.theUndefined;
    }

    ESValue executeRetrieval(ESValue arguments[]) throws EcmaScriptException {
        String sql = (arguments.length > 0) ? arguments[0].toString() : null;

        if (connection == null) {
            throw new EcmaScriptException("JDBC driver not connected");
        }
        Statement statement = null;
        ResultSet resultSet = null;

        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql); // will return true if
                                                     // first result is a result
                                                     // set
        } catch (SQLException e) {
            // System.err.println("##Cannot retrieve: " + e);
            // e.printStackTrace();
            lastError = e;
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception ignored) {
                // do nothing
            }
            statement = null;
            return ESBoolean.makeBoolean(false);
        }
        ESRowSet rowSet = new ESRowSet(esRowSetPrototype, getEvaluator(), sql,
                this, statement, resultSet);
        return rowSet;
        // return ESBoolean.makeBoolean(true);
    }

    ESValue executeCommand(ESValue arguments[]) throws EcmaScriptException {
        String sql = (arguments.length > 0) ? arguments[0].toString() : null;
        int count = 0;

        if (connection == null) {
            throw new EcmaScriptException("JDBC driver not connected");
        }

        Statement statement = null;
        try {

            statement = connection.createStatement();
            count = statement.executeUpdate(sql); // will return true if first
                                                  // result is a result set
        } catch (SQLException e) {
            // System.err.println("##Cannot retrieve: " + e);
            // e.printStackTrace();
            lastError = e;
            try {
                if (statement != null)
                    statement.close();
            } catch (Exception ignored) {
                // do nothing
            }
            statement = null;
            return ESBoolean.makeBoolean(false);
        }
        try {
            statement.close();
        } catch (SQLException e) {
            // ignored
        }
        return ESNumber.valueOf(count);
        // return ESBoolean.makeBoolean(true);
    }

    public Object getMetaData() {
        if (databaseMetaData == null)
            try {
                databaseMetaData = connection.getMetaData();
            } catch (SQLException e) {
                // ignored
            }
        return databaseMetaData;
    }
}

/**
 * A RowSet object
 */
class ESRowSet extends ESObject {
    private static final long serialVersionUID = 7043284303624735346L;
    private transient String sql = null;
    private transient Statement statement = null;
    private transient ResultSet resultSet = null;
    private transient ResultSetMetaData resultSetMetaData = null;
    private transient ArrayList<String> colNames = null;
    private transient boolean lastRowSeen = false;
    private transient boolean firstRowSeen = false;
    private transient Exception lastError = null;

    ESRowSet(ESObject prototype, Evaluator evaluator, String sql,
            ESDatabase database, Statement statement, ResultSet resultSet)
            throws EcmaScriptException {
        super(prototype, evaluator);
        this.sql = sql;
        this.statement = statement;
        this.resultSet = resultSet;

        if (sql == null)
            throw new NullPointerException("sql");
        if (resultSet == null)
            throw new NullPointerException("resultSet");
        if (statement == null)
            throw new NullPointerException("statement");
        if (database == null)
            throw new NullPointerException("database");

        try {

            this.resultSetMetaData = resultSet.getMetaData();
            int numcols = resultSetMetaData.getColumnCount();
            // System.out.println("$$NEXT : " + numcols);
            colNames = new ArrayList<String>(numcols);
            for (int i = 0; i < numcols; i++) {
                String colName = resultSetMetaData.getColumnLabel(i + 1);
                // System.out.println("$$COL : " + colName);
                colNames.add(colName);
            }
        } catch (SQLException e) {
            colNames = new ArrayList<String>(); // An empty one
            throw new EcmaScriptException("Could not get column names", e);

            // System.err.println("##Cannot get column names: " + e);
            // e.printStackTrace();
        }
    }

    public String getESClassName() {
        return "RowSet";
    }

    public String toDetailString() {
        return "ES:[Object: builtin " + this.getClass().getName() + ":"
                + this.toString() + "]";
    }

    public int getColumnCount() {
        return colNames.size();
    }

    public Object getMetaData() {
        return resultSetMetaData;
    }

    ESValue getLastError() throws EcmaScriptException {
        if (lastError == null) {
            return ESUndefined.theUndefined;
        }
        return ESLoader.normalizeValue(lastError, getEvaluator());

    }

    ESValue release() {
        try {
            if (statement != null)
                statement.close();
            if (resultSet != null)
                resultSet.close();
        } catch (SQLException e) {
            // ignored
        }
        statement = null;
        resultSet = null;
        resultSetMetaData = null;
        return ESUndefined.theUndefined;
    }

    public boolean hasMoreRows() {
        return !lastRowSeen; // Simplistic implementation
    }

    public String getColumnName(int idx) throws EcmaScriptException {
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        if (idx > 0 && idx <= colNames.size()) {
            return (String) colNames.get(idx - 1); // to base 0
        }
        throw new EcmaScriptException("Column index (base 1) " + idx
                + " out of range, max: " + colNames.size());

    }

    public int getColumnDatatypeNumber(int idx) throws EcmaScriptException {
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        if (idx > 0 && idx <= colNames.size()) {
            try {
                return resultSetMetaData.getColumnType(idx);
            } catch (SQLException e) {
                lastError = e;
                return -1;
            }
        }
        throw new EcmaScriptException("Column index (base 1) " + idx
                + " out of range, max: " + colNames.size());

    }

    public String getColumnDatatypeName(int idx) throws EcmaScriptException {
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        if (idx > 0 && idx <= colNames.size()) {
            try {
                return resultSetMetaData.getColumnTypeName(idx);
            } catch (SQLException e) {
                lastError = e;
                return null;
            }
        }
        throw new EcmaScriptException("Column index (base 1) " + idx
                + " out of range, max: " + colNames.size());

    }

    public ESValue getColumnItem(String propertyName)
            throws EcmaScriptException {
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        if (!firstRowSeen) {
            throw new EcmaScriptException(
                    "Attempt to access data before the first row is read");
        }
        try {
            int index = -1; // indicates not a valid index value
            try {
                char c = propertyName.charAt(0);
                if ('0' <= c && c <= '9') {
                    index = Integer.parseInt(propertyName);
                }
            } catch (NumberFormatException e) {
                // do nothing
            } catch (StringIndexOutOfBoundsException e) { // for charAt
                // do nothing
            }
            if (index >= 0) {
                return getProperty(index);
            }
            Object o = resultSet.getObject(propertyName);
            ESValue value = ESLoader.normalizeValue(o, getEvaluator());
            // System.out.println("&& @VALUE : " + value);
            lastError = null;
            return value;
        } catch (SQLException e) {
            // System.err.println("##Cannot get property '" + propertyName +
            // "' " + e);
            // e.printStackTrace();
            lastError = e;
        }
        return ESUndefined.theUndefined;
    }

    public ESValue getProperty(String propertyName, int hash)
            throws EcmaScriptException {
        // System.err.println(" &&& Getting property '" + propertyName + "'");

        // Length property is firsy checked

        // First return system or or prototype properties
        if (propertyName.equals("length")) {
            return ESNumber.valueOf(colNames.size());
        } else if (super.hasProperty(propertyName, hash)) {
            return super.getProperty(propertyName, hash);
        } else {
            if (resultSet == null) {
                throw new EcmaScriptException(
                        "Attempt to access a released result set");
            }
            if (!firstRowSeen) {
                throw new EcmaScriptException(
                        "Attempt to access data before the first row is read");
            }
            try {
                int index = -1; // indicates not a valid index value
                try {
                    char c = propertyName.charAt(0);
                    if ('0' <= c && c <= '9') {
                        index = Integer.parseInt(propertyName);
                    }
                } catch (NumberFormatException e) {
                    // do nothing
                } catch (StringIndexOutOfBoundsException e) { // for charAt

                }
                if (index >= 0) {
                    return getProperty(index);
                }
                Object o = resultSet.getObject(propertyName);
                ESValue value = ESLoader.normalizeValue(o, getEvaluator());
                // System.out.println("&& @VALUE : " + value);
                lastError = null;
                return value;
            } catch (SQLException e) {
                // System.err.println("##Cannot get property '" + propertyName +
                // "' " + e);
                // e.printStackTrace();
                lastError = e;
            }
        }
        return ESUndefined.theUndefined;
    }

    public ESValue getProperty(int index) throws EcmaScriptException {
        if (!firstRowSeen) {
            throw new EcmaScriptException(
                    "Attempt to access data before the first row is read");
        }
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        try {
            Object o = resultSet.getObject(index);
            ESValue value = ESLoader.normalizeValue(o, getEvaluator());
            lastError = null;
            return value;
        } catch (SQLException e) {
            // System.err.println("##Cannot get property: " + e);
            // e.printStackTrace();
            lastError = e;
        }
        return ESUndefined.theUndefined;
    }

    /*
     * Returns an enumerator for the key elements of this object.
     * 
     * @return the enumerator - may have 0 length of coulmn names where not
     * found
     */
    public Enumeration<String> getProperties() {
        if (resultSet == null) {
            return (new Vector<String>()).elements();
        }
        return new Vector<String>(colNames).elements();
    }

    /**
     * Get all properties (including hidden ones), for the command
     * 
     * @listall of the interpreter. Include the visible properties of the
     *          prototype (that is the one added by the user) but not the hidden
     *          ones of the prototype (otherwise this would list all functions
     *          for any object).
     * 
     * @return An enumeration of all properties (visible and hidden).
     */
    public Enumeration<String> getAllProperties() {
        return new Enumeration<String>() {
            String[] specialProperties = getSpecialPropertyNames();
            int specialEnumerator = 0;
            Enumeration<String> props = getProperties(); // all of object
                                                         // properties
            String currentKey = null;
            int currentHash = 0;
            boolean inside = false; // true when examing prototypes properties

            public boolean hasMoreElements() {
                // OK if we already checked for a property and one exists
                if (currentKey != null)
                    return true;
                // Loop on special properties first
                if (specialEnumerator < specialProperties.length) {
                    currentKey = specialProperties[specialEnumerator];
                    currentHash = currentKey.hashCode();
                    specialEnumerator++;
                    return true;
                }
                // loop on standard or prototype properties
                while (props.hasMoreElements()) {
                    currentKey = (String) props.nextElement();
                    currentHash = currentKey.hashCode();
                    if (inside) {
                        try {
                            if (hasProperty(currentKey, currentHash))
                                continue;
                        } catch (EcmaScriptException ignore) {
                            // do nothing
                        }
                        // SHOULD CHECK IF NOT IN SPECIAL
                    }
                    return true;
                }
                // If prototype properties have not yet been examined, look for
                // them
                if (!inside && getPrototype() != null) {
                    inside = true;
                    props = getPrototype().getProperties();
                    while (props.hasMoreElements()) {
                        currentKey = (String) props.nextElement();
                        currentHash = currentKey.hashCode();
                        try {
                            if (hasProperty(currentKey, currentHash))
                                continue;
                        } catch (EcmaScriptException ignore) {
                            // do nothing
                        }
                        return true;
                    }
                }
                return false;
            }

            public String nextElement() {
                if (hasMoreElements()) {
                    String key = currentKey;
                    currentKey = null;
                    return key;
                }
                throw new java.util.NoSuchElementException();

            }
        };
    }

    public String[] getSpecialPropertyNames() {
        String[] ns = { "length" };
        return ns;
    }

    ESValue next() throws EcmaScriptException {
        boolean status = false;
        if (lastRowSeen) {
            throw new EcmaScriptException(
                    "Attempt to access a next row after last row has been returned");
        }
        if (resultSet == null) {
            throw new EcmaScriptException(
                    "Attempt to access a released result set");
        }
        try {
            status = resultSet.next();
            lastError = null;
        } catch (SQLException e) {
            // System.err.println("##Cannot do next:" + e);
            // e.printStackTrace();
            lastError = e;
        }
        if (status)
            firstRowSeen = true;
        else
            lastRowSeen = true;
        return ESBoolean.makeBoolean(status);
    }

    public String toString() {
        return "[RowSet: '"
                + sql
                + "'"
                + (resultSet == null ? " - released]"
                        : (lastRowSeen ? " - at end]" : (firstRowSeen ? "]"
                                : " - at start]")));
    }

}

public class Database extends Extension {
    private static final long serialVersionUID = 7948096908366103321L;
    ESObject esDatabasePrototype = null;
    ESObject esRowSetPrototype = null;

    public Database() {
        super();
    }

    class GlobalObjectDatabase extends BuiltinFunctionObject {
        private static final long serialVersionUID = 1476645330609136920L;

        GlobalObjectDatabase(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            return doConstruct(thisObject, arguments);
        }

        public ESObject doConstruct(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = null;
            // System.out.println("esDatabasePrototype="+esDatabasePrototype);
            if (arguments.length == 0) {
                throw new EcmaScriptException(
                        "Database requires 1 or 2 arguments");
            } else if (arguments.length == 1) {
                database = new ESDatabase(esDatabasePrototype, this
                        .getEvaluator(), esRowSetPrototype, arguments[0]
                        .toString());
            } else if (arguments.length > 1) {
                database = new ESDatabase(esDatabasePrototype, this
                        .getEvaluator(), esRowSetPrototype, arguments[0]
                        .toString(), arguments[1].toString());
            }
            return database;
        }

    }

    class DatabaseGetLastError extends BuiltinFunctionObject {
        private static final long serialVersionUID = -817855141956128587L;

        DatabaseGetLastError(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.getLastError();
        }
    }

    class DatabaseRelease extends BuiltinFunctionObject {
        private static final long serialVersionUID = -2788460251115154264L;

        DatabaseRelease(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.release();
        }
    }

    class DatabaseConnect extends BuiltinFunctionObject {
        private static final long serialVersionUID = -160119919076836632L;

        DatabaseConnect(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.connect(arguments);
        }
    }

    class DatabaseDisconnect extends BuiltinFunctionObject {
        private static final long serialVersionUID = -2124435785806562065L;

        DatabaseDisconnect(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.disconnect();
        }
    }

    class DatabaseExecuteRetrieval extends BuiltinFunctionObject {
        private static final long serialVersionUID = 553887088268457954L;

        DatabaseExecuteRetrieval(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.executeRetrieval(arguments);
        }
    }

    class DatabaseExecuteCommand extends BuiltinFunctionObject {
        private static final long serialVersionUID = 7421773759587066619L;

        DatabaseExecuteCommand(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return database.executeCommand(arguments);
        }
    }

    class DatabaseGetMetaData extends BuiltinFunctionObject {
        private static final long serialVersionUID = 8387083082824146713L;

        DatabaseGetMetaData(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESDatabase database = (ESDatabase) thisObject;
            return new ESWrapper(database.getMetaData(), this.getEvaluator());
        }
    }

    class RowSetRelease extends BuiltinFunctionObject {
        private static final long serialVersionUID = -6169075232727703900L;

        RowSetRelease(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            return rowSet.release();
        }
    }

    class RowSetNext extends BuiltinFunctionObject {
        private static final long serialVersionUID = -2799372218763475297L;

        RowSetNext(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            return rowSet.next();
        }
    }

    class RowSetGetColumnCount extends BuiltinFunctionObject {
        private static final long serialVersionUID = -7034764698384513090L;

        RowSetGetColumnCount(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            return ESNumber.valueOf(rowSet.getColumnCount());
        }
    }

    class RowSetGetColumnName extends BuiltinFunctionObject {
        private static final long serialVersionUID = -5955002519715567919L;

        RowSetGetColumnName(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            if (arguments.length < 1) {
                throw new EcmaScriptException("Missing parameter in function "
                        + this);
            }
            int idx = arguments[0].toUInt32();
            String name = rowSet.getColumnName(idx); // base 1
            if (name == null) {
                return ESUndefined.theUndefined;
            }
            return new ESString(name);

        }
    }

    class RowSetGetColumnItem extends BuiltinFunctionObject {
        private static final long serialVersionUID = -8695359080807943092L;

        RowSetGetColumnItem(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            if (arguments.length < 1) {
                throw new EcmaScriptException("Missing parameter in function "
                        + this);
            }
            String name = arguments[0].toString();
            return rowSet.getColumnItem(name);
        }
    }

    class RowSetGetColumnDatatypeNumber extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1921386817405372093L;

        RowSetGetColumnDatatypeNumber(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            if (arguments.length < 1) {
                throw new EcmaScriptException("Missing parameter in function "
                        + this);
            }
            int idx = arguments[0].toUInt32();
            return ESNumber.valueOf(rowSet.getColumnDatatypeNumber(idx));
        }
    }

    class RowSetGetColumnDatatypeName extends BuiltinFunctionObject {
        private static final long serialVersionUID = -5178877749779743085L;

        RowSetGetColumnDatatypeName(String name, Evaluator evaluator,
                FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            if (arguments.length < 1) {
                throw new EcmaScriptException("Missing parameter in function "
                        + this);
            }
            int idx = arguments[0].toUInt32();
            String name = rowSet.getColumnDatatypeName(idx);
            // System.out.println("Datat type name for col " + idx + " is "
            // +name);
            if (name == null) {
                return ESUndefined.theUndefined;
            }
            return new ESString(name);

        }
    }

    class RowSetHasMoreRows extends BuiltinFunctionObject {
        private static final long serialVersionUID = -1322566915756923713L;

        RowSetHasMoreRows(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            return ESBoolean.makeBoolean(rowSet.hasMoreRows());
        }
    }

    class RowSetGetMetaData extends BuiltinFunctionObject {
        private static final long serialVersionUID = 3826426631039858729L;

        RowSetGetMetaData(String name, Evaluator evaluator, FunctionPrototype fp) {
            super(fp, evaluator, name, 1);
        }

        public ESValue callFunction(ESObject thisObject, ESValue[] arguments)
                throws EcmaScriptException {
            ESRowSet rowSet = (ESRowSet) thisObject;
            return new ESWrapper(rowSet.getMetaData(), this.getEvaluator());
        }
    }

    public void initializeExtension(Evaluator evaluator)
            throws EcmaScriptException {

        GlobalObject go = evaluator.getGlobalObject();
        ObjectPrototype op = (ObjectPrototype) evaluator.getObjectPrototype();
        FunctionPrototype fp = (FunctionPrototype) evaluator
                .getFunctionPrototype();

        esRowSetPrototype = new ObjectPrototype(op, evaluator);
        esDatabasePrototype = new ESDatabase(op, evaluator); // No driver

        ESObject globalDatabaseObject = new GlobalObjectDatabase("Database",
                evaluator, fp);
        globalDatabaseObject
                .putHiddenProperty("prototype", esDatabasePrototype);
        globalDatabaseObject.putHiddenProperty("length", ESNumber.valueOf(2));

        esDatabasePrototype.putHiddenProperty("constructor",
                globalDatabaseObject);
        esDatabasePrototype.putHiddenProperty("getLastError",
                new DatabaseGetLastError("getLastError", evaluator, fp));
        esDatabasePrototype.putHiddenProperty("release", new DatabaseRelease(
                "release", evaluator, fp));
        esDatabasePrototype.putHiddenProperty("connect", new DatabaseConnect(
                "connect", evaluator, fp));
        esDatabasePrototype.putHiddenProperty("disconnect",
                new DatabaseDisconnect("disconnect", evaluator, fp));
        esDatabasePrototype
                .putHiddenProperty("executeRetrieval",
                        new DatabaseExecuteRetrieval("executeRetrieval",
                                evaluator, fp));
        esDatabasePrototype.putHiddenProperty("executeCommand",
                new DatabaseExecuteCommand("executeCommand", evaluator, fp));
        esDatabasePrototype.putHiddenProperty("getMetaData",
                new DatabaseGetMetaData("getMetaData", evaluator, fp));

        esRowSetPrototype.putHiddenProperty("release", new RowSetRelease(
                "release", evaluator, fp));
        esRowSetPrototype.putHiddenProperty("next", new RowSetNext("next",
                evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getColumnCount",
                new RowSetGetColumnCount("getColumnCount", evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getColumnName",
                new RowSetGetColumnName("getColumnName", evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getColumnItem",
                new RowSetGetColumnItem("getColumnItem", evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getColumnDatatypeNumber",
                new RowSetGetColumnDatatypeNumber("getColumnDatatypeNumber",
                        evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getColumnDatatypeName",
                new RowSetGetColumnDatatypeName("getColumnDatatypeName",
                        evaluator, fp));
        esRowSetPrototype.putHiddenProperty("hasMoreRows",
                new RowSetHasMoreRows("hasMoreRows", evaluator, fp));
        esRowSetPrototype.putHiddenProperty("getMetaData",
                new RowSetGetMetaData("getMetaData", evaluator, fp));

        go.putHiddenProperty("Database", globalDatabaseObject);
    }
}

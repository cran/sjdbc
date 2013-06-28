import java.sql.*;
import java.util.Hashtable;
import java.util.ArrayList;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


public class SJDBCBridge {

	/**
	 * NA values
	 */
	private final static int SPLUS_32BIT_NA = 0x7ff00001;
	private final static long SPLUS_64BIT_NA = 0x7fffffff7fffffffL;
	private final static int R_INTEGER_NA = 0x80000000;
	private final static double R_NUMERIC_NA = Double.longBitsToDouble(0x7ff00000000007a2L);
	private final static double R_NUMERIC_NAN = Double.NaN;

	private static boolean is64Bit() {
		return System.getProperty("os.arch").indexOf("64") != -1 || System.getProperty("sun.arch.data.model").indexOf("64") != -1;
	}

	/**
	 * Hashtable of registered stuff to use.
	 */

    private final static Hashtable<String, SJDBCDataFrame> g_dfTable = new Hashtable<String, SJDBCDataFrame>();
    private final static Hashtable<String, PreparedStatement> g_insertStatementHash = new Hashtable<String, PreparedStatement>();
	private static Connection g_conn;
	private static String[] g_connInfo = new String[] {"","","","",""};
	private static URLClassLoader g_ucl = null;
	private static ArrayList<String> g_regDrivers = new ArrayList<String>();

	public enum IdentifierCase {
		UPPER, // UPPERCASE identifiers
		LOWER, // lowercase identifiers
		MIXED  // MixedCase (or case-insensitive) identifiers
	}

	public static void sjdbcImportData(String driverClass, String conInfo,
			String conUser, String conPassword, String sql, String id)
			throws Exception {

		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;
		ResultSet rs = null;

		con = establishConnection(driverClass, conInfo, conUser, conPassword);

		// Create and execute a SQL statement that returns a
		// set of data and then display it.
		stmt = con.createStatement();

		// Perform a fetch for every row in the result set.
		rs = stmt.executeQuery(sql);

		// Put the ResultSet in a hash table in which the S-PLUS
		// function javaGetResultSet() will look.
		SJDBCResultSetUtilities.register(id, rs);

	}

	public static void sjdbcTypeInfo(String driverClass, String conInfo,
			String conUser, String conPassword, String id)
			throws Exception {

		// Declare the JDBC objects.
		Connection con = null;
		ResultSet rs = null;

		con = establishConnection(driverClass, conInfo, conUser, conPassword);

		rs = con.getMetaData().getTypeInfo();

		// Put the ResultSet in a hash table in which the S-PLUS
		// function javaGetResultSet() will look.
		SJDBCResultSetUtilities.register(id, rs);

	}

	public static void sjdbcCloseConnection()
		throws java.sql.SQLException {
		if (g_conn != null && !g_conn.isClosed()) g_conn.close();
		g_connInfo = new String[] {"","","","",""};
	}

	/* Add new driver jars to the local classloader chain
	 * so they can be made available to the DriverManager. */
	public static void sjdbcAddDrivers(String[] driverJars) throws Exception {

		if (driverJars==null) driverJars = new String[0];
		URL[] urls = new URL[driverJars.length];
		for (int i=0; i < driverJars.length; i++) {
			urls[i] = (new File(driverJars[i])).toURI().toURL();
		}
		if (g_ucl == null) {
			g_ucl = new URLClassLoader(urls);
		}
		else {
			g_ucl = new URLClassLoader(urls, g_ucl);
		}
	}

	private static Connection establishConnection(String driverClass,
			String conInfo, String conUser, String conPassword)
			throws Exception {

		// Try to reuse existing connection if available and open
		if (g_conn != null && !g_conn.isClosed() &&
			driverClass.equals(g_connInfo[0]) &&
			conInfo.equals(g_connInfo[1]) &&
			conUser.equals(g_connInfo[2]) &&
			conPassword.equals(g_connInfo[3])) {

			return g_conn;
		}
		// Close any existing connection
		sjdbcCloseConnection();

		// Establish the connection.

		// register driver with DriverManger if not already registered
		if (!g_regDrivers.contains(driverClass)) {
			Driver drv = (Driver) g_ucl.loadClass(driverClass).newInstance();
			DriverManager.registerDriver(new SJDBCDriverShim(drv));
			g_regDrivers.add(driverClass);
		}

		if (conUser.length() == 0) {
			g_conn = DriverManager.getConnection(conInfo);
			g_connInfo = new String[] {driverClass, conInfo, "", "", ""};
		} else {
			g_conn = DriverManager.getConnection(conInfo, conUser, conPassword);
			g_connInfo = new String[] {driverClass, conInfo, conUser, conPassword, ""};
		}
		return g_conn;
	}

	public static int sjdbcExecute(String driverClass, String conInfo,
			String conUser, String conPassword, String sql) throws Exception {
		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;

		con = establishConnection(driverClass, conInfo, conUser, conPassword);

		// Create and execute a SQL statement that returns a
		// set of data and then display it.
		stmt = con.createStatement();

		// Execute the SQL query.
		stmt.executeUpdate(sql);

		return stmt.getUpdateCount();
	}

	public static void sjdbcExportStart(String id, String driverClass,
			String conInfo, String conUser, String conPassword, String table, boolean appendToTable,
			boolean preserveColumnCase)
			throws Exception {
		// Declare the JDBC objects.
		Connection con = null;
		Statement stmt = null;
		String stmtStr = "";
		Hashtable<Integer, String[]> sqlTypes = new Hashtable<Integer, String[]>();

		if (!g_dfTable.containsKey(id))
			throw new Exception("Data id does not exist.");
		SJDBCDataFrame df = g_dfTable.get(id);

		con = establishConnection(driverClass, conInfo, conUser, conPassword);

		// Get database-specific info from metadata
		DatabaseMetaData dmd = con.getMetaData();
		g_connInfo[4] = Boolean.toString(dmd.supportsBatchUpdates());
		String identifierQuoteString = dmd.getIdentifierQuoteString();
		String literalPrefix = "\'";
		String literalSuffix = "\'";

		// loop through TypeInfo to get database table info
		ResultSet typeInfo = dmd.getTypeInfo();
		while(typeInfo.next()) {
			int dataType = typeInfo.getInt("DATA_TYPE");
			String typePrecision = (typeInfo.getString("PRECISION") == null) ? "0" : typeInfo.getString("PRECISION");
			String typeName = typeInfo.getString("TYPE_NAME");

			switch(dataType) {
			case Types.VARCHAR:
				literalPrefix = typeInfo.getString("LITERAL_PREFIX");
				literalSuffix = typeInfo.getString("LITERAL_SUFFIX");
				break;
			}

			// store type/precision.  If multiple db types for same SQL standard type, choose the
			// one with higher precision (so we don't lose data).
			if (!sqlTypes.containsKey(dataType) ||
				Integer.parseInt(sqlTypes.get(dataType)[1]) < Integer.parseInt(typePrecision))
				sqlTypes.put(dataType,
					new String[] { typeName, typePrecision } );
		}

		//String reservedWords = dmd.getSQLKeywords();
		IdentifierCase columnCase = IdentifierCase.MIXED;
		if (!preserveColumnCase) {
			// convert to database-specific column names to aid in name matching
			if (dmd.storesLowerCaseIdentifiers()) columnCase = IdentifierCase.LOWER;
			else if (dmd.storesUpperCaseIdentifiers()) columnCase = IdentifierCase.UPPER;
		}

        String colNames = createSQLColNames(df.getColNames(), identifierQuoteString, columnCase);

        // build prepared statement
        stmtStr = "INSERT INTO " + table + " " + colNames + " VALUES " +
            createSQLRowTemplate(df.columns());
        PreparedStatement pstmt = con.prepareStatement(stmtStr);
        g_insertStatementHash.put(id, pstmt);

        // Disable autocommit (we want this as one transaction)
		con.setAutoCommit(false);
		try {
			if (!appendToTable) {
	            stmt = con.createStatement();
				// drop existing table and create a new one.
				try {
					stmt.executeUpdate("DROP TABLE " + table);
				}
				catch (SQLException e) {
					// ignore and move on if cannot drop table,
					// perhaps the table doesn't exist.
					con.rollback();  // restart the transaction
					/* JR: this seems to be necessary at least on PostgreSQL otherwise all
					       other commands in the transaction are ignored.  */
				}
				// create new table
				stmtStr = createSQLCreateTable(df.getColNames(), df.getColTypes(), sqlTypes, table, identifierQuoteString, columnCase);
				stmt.executeUpdate(stmtStr);
				stmt.close();
			}
		}
		catch (Exception e) {
		    // on error, undo changes
            try {
    			con.rollback(); // rollback the transaction
    			con.setAutoCommit(true);
            } catch (Exception ex) {
            }
			// close connection, in case it is in weird state
            try {
			    sjdbcCloseConnection();
            } catch (Exception ex) {
            }
			throw e; // throw the Exception up
		}
	}

	/* Bug 51498 modified signature of sjdbcExportData to add
	 * batchSize argument.  Add stub function here to handle
	 * Miner JDBC nodes that call without extra arg.
	 * Can probably be removed after next Miner release.  */
	public static int sjdbcExportData(String id, boolean commit)
		throws Exception {
		return sjdbcExportData(id, commit, 1000);
	}

    public static int sjdbcExportData(String id, boolean commit, int batchSize)
            throws Exception {
        if (!g_dfTable.containsKey(id))
            throw new Exception("Data id does not exist.");
        SJDBCDataFrame df = g_dfTable.get(id);
		boolean is64bit = is64Bit();

        // use previously-opened connection
        if (g_conn == null || g_conn.isClosed())
            throw new Exception("Connection closed");
        Connection con = g_conn;

		// does driver support batch, if so then use it
		boolean useBatch = batchSize > 0 && Boolean.valueOf(g_connInfo[4]);

        // fetch column types
        Integer[] colTypes = df.getColTypes();

        try {
            PreparedStatement pstmt = g_insertStatementHash.get(id);

            // insert each row
            for (int row = 0; row < df.length(); row++) {

                // set parameter for each column
                for (int col = 0; col < df.columns(); col++) {
                    // 1-indexed parameters, 0-indexed columns!
                    switch (colTypes[col]) {
                    case Types.INTEGER:
                    	/*
                    	if (is64bit) {
							long longVal = df.getLongData(col, row);
							if (longVal == SPLUS_64BIT_NA)
								pstmt.setNull(col+1, Types.INTEGER);
							else
								pstmt.setLong(col+1, longVal);
						}
						else {
							int intVal = df.getIntegerData(col, row);
							if (intVal == SPLUS_32BIT_NA)
								pstmt.setNull(col+1, Types.INTEGER);
							else
								pstmt.setInt(col+1, intVal);
						}
						*/
						int intVal = df.getIntegerData(col, row);
						if (intVal == R_INTEGER_NA)
							pstmt.setNull(col+1, Types.INTEGER);
						else
							pstmt.setInt(col+1, intVal);
                        break;
                    case Types.DOUBLE:
                        double doubleVal = df.getDoubleData(col, row);
                        if (Double.isNaN(doubleVal)) {
                            pstmt.setNull(col+1, Types.DOUBLE);
						}
                        else
                            pstmt.setDouble(col+1, doubleVal);
                        break;
                    case Types.VARCHAR:
                        String stringVal = df.getStringData(col, row);
                        if (stringVal == null) {
                        	pstmt.setNull(col+1, Types.VARCHAR);
						}
                        else
                        	pstmt.setString(col+1, stringVal);
                        break;
                    case Types.TIMESTAMP:
                        Timestamp dateVal = df.getDateData(col, row);
                        if (dateVal == null)
                            pstmt.setNull(col+1, Types.TIMESTAMP);
                        else
                            pstmt.setTimestamp(col+1, dateVal);
                        break;
                    }
                }
				if (useBatch) {
					pstmt.addBatch();

					if(((row + 1) % batchSize)  == 0)
					{
						pstmt.executeBatch();
					}
				}
				else pstmt.executeUpdate();
            }
			if (useBatch) {
				// Check if some batch statements are left
				if((df.length() % batchSize) != 0)
				{
					pstmt.executeBatch();
				}
			}

            // Commit changes
            if (commit) {
                con.commit();
                con.setAutoCommit(true);
            }
        }
        catch (Exception e) {
            // on error, undo changes
            try {
                con.rollback(); // rollback the transaction
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
            // close connection, in case it is in weird state
            try {
                sjdbcCloseConnection();
            } catch (Exception ex) {
            }
            throw e; // throw the Exception up
        }

        return df.length();
    }

    public static void sjdbcCreateData(String id) throws Exception {
        g_dfTable.put(id, new SJDBCDataFrame());
    }

    public static void sjdbcClearData(String id) throws Exception {
        g_dfTable.remove(id);
        g_insertStatementHash.remove(id);
    }

	public static void sjdbcAddIntegerColumn(String id, String colName,
			int[] colData) throws Exception {
		// make sure the data array exists in hashtable
		if (!g_dfTable.containsKey(id))
			throw new Exception("Data id does not exist.");

		// fetch the data array
		SJDBCDataFrame data = g_dfTable.get(id);

		data.addIntegerColumn(colName, colData);

	}
	public static void sjdbcAddLongColumn(String id, String colName,
				long[] colData) throws Exception {
			// make sure the data array exists in hashtable
			if (!g_dfTable.containsKey(id))
				throw new Exception("Data id does not exist.");

			// fetch the data array
			SJDBCDataFrame data = g_dfTable.get(id);

			data.addLongColumn(colName, colData);

	}
	public static void sjdbcAddDateColumn(String id, String colName,
			String[] colData) throws Exception {
		// make sure the data array exists in hashtable
		if (!g_dfTable.containsKey(id))
			throw new Exception("Data id does not exist.");

		// fetch the data array
		SJDBCDataFrame data = g_dfTable.get(id);

		data.addDateColumn(colName, colData);

	}
	public static void sjdbcAddDoubleColumn(String id, String colName,
			double[] colData) throws Exception {
		// make sure the data array exists in hashtable
		if (!g_dfTable.containsKey(id))
			throw new Exception("Data id does not exist.");

		// fetch the data array
		SJDBCDataFrame data = g_dfTable.get(id);

		data.addDoubleColumn(colName, colData);

	}

	public static void sjdbcAddStringColumn(String id, String colName,
			String[] colData) throws Exception {
		// make sure the data array exists in hashtable
		if (!g_dfTable.containsKey(id))
			throw new Exception("Data id does not exist.");
		// fetch the data array
		SJDBCDataFrame data = g_dfTable.get(id);
		data.addStringColumn(colName, colData);

	}

	private static String createSQLColNames(String[] colNames, String quoteString, IdentifierCase columnCase) {
		String out = "( ";
		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				out += ", ";
			String colName;
			switch(columnCase) {
				case UPPER:
					colName = colNames[i].toUpperCase();
					break;
				case LOWER:
					colName = colNames[i].toLowerCase();
					break;
				default:
				colName = colNames[i];
			}
			out += quoteString + colName + quoteString;
			out += " ";
		}
		out += ")";
		return out;
	}

	private static String createSQLCreateTable(String[] colNames, Integer[] colTypes,
		Hashtable<Integer, String[]> sqlTypes, String table, String quoteString, IdentifierCase columnCase)
		throws SQLException {
		String out = "CREATE TABLE " + table + " ( ";
		for (int i = 0; i < colNames.length; i++) {
			if (i > 0)
				out += ", ";
			String colName;
			switch(columnCase) {
				case UPPER:
					colName = colNames[i].toUpperCase();
					break;
				case LOWER:
					colName = colNames[i].toLowerCase();
					break;
				default:
				colName = colNames[i];
			}
			String colType = "";
			switch(colTypes[i]) {
			case Types.INTEGER:
				if (sqlTypes.containsKey(Types.INTEGER)) colType = sqlTypes.get(Types.INTEGER)[0];
				break;
			case Types.DOUBLE:
				// DOUBLE and FLOAT seems interchangable, and some databases use one or the other.
				if (sqlTypes.containsKey(Types.DOUBLE))	colType = sqlTypes.get(Types.DOUBLE)[0];
				else if (sqlTypes.containsKey(Types.FLOAT))	colType = sqlTypes.get(Types.FLOAT)[0];
				break;
			case Types.VARCHAR:
				// VARCHAR seems to be standard across all databases.  Ran into some problems
				// with MySQL using ENUM and VARCHAR with same type code and different precisions
				// so chose to always choose "VARCHAR(255)" as the type.
				/*if (sqlTypes.containsKey(Types.VARCHAR)) {
					colType = sqlTypes.get(Types.VARCHAR)[0];
					colType += "(255)";
				}
				*/
				colType = "VARCHAR(255)";
				break;
			case Types.TIMESTAMP:
				if (sqlTypes.containsKey(Types.TIMESTAMP)) colType = sqlTypes.get(Types.TIMESTAMP)[0];
				break;
			}
			if (colType.length() == 0)
				throw new SQLException("Could not determine column type for '" + colNames[i] + "' with SQL Type '" + colTypes[i] + "'.");
			out += quoteString + colName + quoteString + " " + colType;
		}
		out += " )";

		return out;
	}
	private static String createSQLRowTemplate(int columns) {
			String out = "( ";
			for (int i = 0; i < columns; i++) {
				if (i != 0) out += ", ";
				out += "?";
			}
			out += ")";
			return out;
	}
}

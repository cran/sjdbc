import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Hashtable;

/**
 * [Adapted from ResultSetUtilities class in S-PLUS 8.0.4]
 * <P>
 * Provides static methods for accessing a ResultSet from S-PLUS.
 * <P>
 * This code uses the following data type mappings from SQL Types to Java Types to S-PLUS Types
 * <PRE>
 * SQL TYPES    Java Type        S-PLUS Type
 * BIGINT        double           numeric
 * DECIMAL       double           numeric
 * NUMERIC       double           numeric
 * DOUBLE        double           numeric
 * FLOAT         float            single
 * REAL          float            single
 * DATE          String           timeDate
 * TIME          String           timeDate
 * TIMESTAMP     String           timeDate
 * TINYINT       integer          integer
 * SMALLINT      integer          integer
 * INTEGER       integer          integer
 * BOOLEAN       boolean          logical
 * BIT           boolean          logical
 * All Others    String           character
 * </PRE>
 */

public class SJDBCResultSetUtilities {

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
	 * Hashtable of registered ResultSets.
	 */

	private final static Hashtable<String, ResultSet> g_rsTable = new Hashtable<String, ResultSet>();
	private final static Hashtable<String, Object[]> g_dataTable = new Hashtable<String, Object[]>();

	/*
	 * Default number of rows when this can't be determined prior to retrieving
	 * the data.
	 */

	private static int g_defaultNumRows = 100;

	/**
	 * Class is a singleton with static methods. An instance cannot be created.
	 */

	private SJDBCResultSetUtilities() {

	}

	/**
	 * Register a ResultSet in the Hashtable of known sets.
	 */

	public static void register(String key, ResultSet set) {
		g_rsTable.put(key, set);
	}

	/**
	 * Unregister a ResultSet from the Hashtable of known sets.
	 */

	public static void unregister(String key) {
		g_rsTable.remove(key);
	}

	/**
	 * Specify the default number of rows. If the ResultSet is of type
	 * ResultSet.TYPE_FORWARD_ONLY, then the number of rows cannot be determined
	 * prior to retrieving the data. In this case the default number of rows
	 * will be used, and the arrays containing the retrieved values will be
	 * doubled in size when the number of rows exceeds the current array size.
	 */

	public static void setDefaultNumberOfRows(int nrow) {
		g_defaultNumRows = nrow;
	}

	public static int getDefaultNumberOfRows() {
		return g_defaultNumRows;
	}

	/**
	 * Get data from a registered ResultSet.
	 *
	 * @param key
	 *            used to register the ResultSet in the Hashtable of known
	 *            ResultSets.
	 * @return Object array with four elements: column names, column type code
	 *         as a String, column type code as an int, and column values. The
	 *         first three items are the values returned by the
	 *         ResultSetMetaData methods getColumnName(), getColumnTypeName(),
	 *         and getColumnType(). The column values element is itself an array
	 *         of Vectors with one Vector of values for each column. Returns
	 *         null if the key does not match a registered ResultSet.
	 * @throws Throws
	 *             an SQLException if an error occurs accessing the ResultSet.
	 */

	public static Object[] getData(String key) throws SQLException {
	    return getData(key, true, -1);
	}

    /**
     * Get data from a registered ResultSet.
     *
     * @param key
     *            used to register the ResultSet in the Hashtable of known
     *            ResultSets.
     * @param startAtFirst
     *            if true, reset ResultSet to start at the first row of the data.
     *            If false, read from the current position in the ResultSet.
     * @param rowsToRead
     *            if zero or greater, the max number of rows that will be read
     *            from the ResultSet.
     * @return Object array with four elements: column names, column type code
     *         as a String, column type code as an int, and column values. The
     *         first three items are the values returned by the
     *         ResultSetMetaData methods getColumnName(), getColumnTypeName(),
     *         and getColumnType(). The column values element is itself an array
     *         of Vectors with one Vector of values for each column. Returns
     *         null if the key does not match a registered ResultSet.
     * @throws Throws
     *             an SQLException if an error occurs accessing the ResultSet.
     */

	public static Object[] getData(String key, boolean startAtFirst, int rowsToRead) throws SQLException {
		boolean is64bit = is64Bit();

		ResultSet curSet = (ResultSet) g_rsTable.get(key);
		if (curSet == null) {
			return null;
		}

		// Get the meta data
		ResultSetMetaData metaData = curSet.getMetaData();

		// Get the column count
		int colCount = metaData.getColumnCount();

		// Allocate the top-level arrays
		String[] colNames = new String[colCount];
		String[] colTypeNames = new String[colCount];
		int[] colTypeCodes = new int[colCount];
		Object[] colValues = new Object[colCount];

		// Identify the particular types we'll want in S-PLUS
		int[] splusTypeCodes = new int[colCount];
		int curCode = 0;

		// Get the column names and types

		for (int i = 0; i < colCount; i++) {
			// Use 1 to n indexing for method
			colNames[i] = metaData.getColumnName(i + 1);
			colTypeNames[i] = metaData.getColumnTypeName(i + 1);
			colTypeCodes[i] = metaData.getColumnType(i + 1);
			switch (colTypeCodes[i]) {
			case Types.BIGINT:
			case Types.DECIMAL:
			case Types.NUMERIC:
			case Types.FLOAT:
			case Types.REAL:
			case Types.DOUBLE: {
				curCode = Types.DOUBLE;
			}
				break;
			/*
			case Types.FLOAT:
			case Types.REAL: {
				curCode = Types.FLOAT;
			}
				break;
			*/
			case Types.DATE:
			case Types.TIME:
			case Types.TIMESTAMP: {
				curCode = Types.DATE;
			}
				break;
			case Types.TINYINT:
			case Types.SMALLINT:
			case Types.INTEGER: {
				curCode = Types.INTEGER;
			}
				break;
			case Types.BOOLEAN:
			case Types.BIT: {
				curCode = Types.BIT;
			}
				break;
			default: {
				curCode = Types.OTHER;
			}
				break;
			}
			splusTypeCodes[i] = curCode;
		}

		// Get the values from the ResultSet.

		// If the set is scrollable, use this to count the number
		// of rows and make sure we are at the start. Otherwise
		// start with a default number of rows and grow the arrays
		// as needed.

		int numRows = g_defaultNumRows;

		if (startAtFirst) {
		    // DB2 driver was throwing an AbstractMethodError at curSet.getType()
		    // Skip over this stuff if Error thrown, it's optional anyway.
		    boolean errorcheck = false;
		    try {

		        curSet.getType();
		    }
		    catch (java.lang.Error e) {
		        errorcheck = true;
		    }

		    if (errorcheck == false && curSet.getType() != ResultSet.TYPE_FORWARD_ONLY) {
		        curSet.last();
		        numRows = curSet.getRow();
		        curSet.beforeFirst();
		    }
		}


		// Allocate the initial arrays
		for (int i = 0; i < colCount; i++) {
			// Create the appropriate result type based on the column type
			colValues[i] = newArrayOfType(splusTypeCodes[i], numRows);
		}

		int curRow = 0;
		String curStrVal = "";

		while ((rowsToRead<0 || curRow<rowsToRead) && curSet.next()) {
			if (curRow == numRows) {
				// Double the array size and copy the values
				numRows = 2 * numRows;

				for (int i = 0; i < colCount; i++) {
					colValues[i] = newArrayOfType(splusTypeCodes[i], numRows,
							colValues[i]);
				}
			}
			for (int i = 0; i < colCount; i++) {
				switch (splusTypeCodes[i]) {
				case Types.DOUBLE: {
					((double[]) colValues[i])[curRow] = curSet.getDouble(i + 1);
					if (curSet.wasNull()) ((double[]) colValues[i])[curRow] = R_NUMERIC_NA;
				}
					break;
				/*
				case Types.FLOAT: {
					((float[]) colValues[i])[curRow] = curSet.getFloat(i + 1);
					if (curSet.wasNull()) ((float[]) colValues[i])[curRow] = R_NUMERIC_NA;
				}
					break;
				*/
				case Types.DATE: {
					java.sql.Timestamp ts = curSet.getTimestamp(i + 1);
					if (ts != null)
						((String[]) colValues[i])[curRow] = ts.toString();
					else
						((String[]) colValues[i])[curRow] = null;
				}
					break;
				case Types.INTEGER: {
					/*
					if (is64bit) {
						((long[]) colValues[i])[curRow] = curSet.getLong(i + 1);
						if (curSet.wasNull()) ((long[]) colValues[i])[curRow] = SPLUS_64BIT_NA;
					}
					else {
						((int[]) colValues[i])[curRow] = curSet.getInt(i + 1);
						if (curSet.wasNull()) ((int[]) colValues[i])[curRow] = SPLUS_32BIT_NA;
					}
					*/

					((int[]) colValues[i])[curRow] = curSet.getInt(i + 1);
					if (curSet.wasNull()) ((int[]) colValues[i])[curRow] = R_INTEGER_NA;

				}
					break;
				case Types.BIT: {
					((boolean[]) colValues[i])[curRow] = curSet.getBoolean(i + 1);
				}
					break;
				default: {
					curStrVal = curSet.getString(i + 1);
					/*
					if (curStrVal == null)
						curStrVal = "";
					*/
					((String[]) colValues[i])[curRow] = curStrVal;
				}
					break;
				}

			}
			curRow++;
		}

		// Trim the extra elements from the arrays
		for (int i = 0; i < colCount; i++) {
			colValues[i] = newArrayOfType(splusTypeCodes[i], curRow,
					colValues[i]);
		}

		// Combine the info we want to return into a single Object array

		Object[] result = new Object[] { colNames, colTypeNames, splusTypeCodes,
				colValues };

		return result;
	}

	/**
	 * Create a new array of the specified type and size.
	 */

	private static Object newArrayOfType(int typeCode, int size) {
		return newArrayOfType(typeCode, size, null);
	}

	/**
	 * Create a new array of the specified size, initialize by copying values
	 * from the specified array. Note that the length of curArray must be less
	 * than or equal to "size".
	 */
	private static Object newArrayOfType(int typeCode, int size, Object curArray) {
		Object result = null;

		switch (typeCode) {
		case Types.DOUBLE: {
			double[] newArray = new double[size];
			if (curArray != null) {
				double[] oldArray = (double[]) curArray;
				for (int i = 0; i < Math.min(oldArray.length, size); i++) {
					newArray[i] = oldArray[i];
				}
			}
			result = newArray;
		}
			break;
		/*
		case Types.FLOAT: {
			float[] newArray = new float[size];
			if (curArray != null) {
				float[] oldArray = (float[]) curArray;
				for (int i = 0; i < Math.min(oldArray.length, size); i++) {
					newArray[i] = oldArray[i];
				}
			}
			result = newArray;
		}
			break;
		*/
		case Types.INTEGER: {
			/*
			if (is64Bit()) {
				long[] newArray = new long[size];
				if (curArray != null) {
					long[] oldArray = (long[]) curArray;
					for (int i = 0; i < Math.min(oldArray.length, size); i++) {
						newArray[i] = oldArray[i];
					}
				}
				result = newArray;
			}
			else {
				int[] newArray = new int[size];
				if (curArray != null) {
					int[] oldArray = (int[]) curArray;
					for (int i = 0; i < Math.min(oldArray.length, size); i++) {
						newArray[i] = oldArray[i];
					}
				}
				result = newArray;
			}
			*/
			int[] newArray = new int[size];
			if (curArray != null) {
				int[] oldArray = (int[]) curArray;
				for (int i = 0; i < Math.min(oldArray.length, size); i++) {
					newArray[i] = oldArray[i];
				}
			}
			result = newArray;
		}
			break;
		case Types.BIT: {
			boolean[] newArray = new boolean[size];
			if (curArray != null) {
				boolean[] oldArray = (boolean[]) curArray;
				for (int i = 0; i < Math.min(oldArray.length, size); i++) {
					newArray[i] = oldArray[i];
				}
			}
			result = newArray;
		}
			break;
		default: {
			String[] newArray = new String[size];
			if (curArray != null) {
				String[] oldArray = (String[]) curArray;
				for (int i = 0; i < Math.min(oldArray.length, size); i++) {
					newArray[i] = oldArray[i];
				}
			}
			result = newArray;
		}
			break;
		}

		return result;
	}

	/***********************************************************
	 * Additional functions to support retrieving data by parts
	 ***********************************************************/
    public static void snextPopulateData(String key, boolean startAtFirst, int rowsToRead) throws java.sql.SQLException {
		g_dataTable.put(key, getData(key, startAtFirst, rowsToRead));
 	}

 	public static void snextReleaseData(String key) {
		g_dataTable.remove(key);
 	}

	public static String[] snextGetColNames(String key) {
		Object[] data = g_dataTable.get(key);
		return (String[]) data[0];
	}

	public static int[] snextGetColTypeCodes(String key) {
		Object[] data = g_dataTable.get(key);
		return (int[]) data[2];
	}

	public static String[] snextGetStringColumn(String key, int col) {
		Object[] data = g_dataTable.get(key);
		return (String[]) ((Object[]) data[3])[col];
	}
	public static int[] snextGetIntegerColumn(String key, int col) {
		Object[] data = g_dataTable.get(key);
		return (int[]) ((Object[]) data[3])[col];
	}
	public static boolean[] snextGetBooleanColumn(String key, int col) {
		Object[] data = g_dataTable.get(key);
		return (boolean[]) ((Object[]) data[3])[col];
	}
	public static double[] snextGetDoubleColumn(String key, int col) {
		Object[] data = g_dataTable.get(key);
		return (double[]) ((Object[]) data[3])[col];
	}
	/*
	public static float[] snextGetFloatColumn(String key, int col) {
		Object[] data = g_dataTable.get(key);
		return (float[]) ((Object[]) data[3])[col];
	}
	public static long[] snextGetLongColumn(String key, int col) {
			Object[] data = g_dataTable.get(key);
			return (long[]) ((Object[]) data[3])[col];
	}
	*/

}


import java.util.ArrayList;
import java.util.Vector;
import java.sql.Timestamp;
import java.sql.Types;

/*
 * Represents a simple data.frame object.
 */

public class SJDBCDataFrame {

	private ArrayList<Object> data;
	private Vector<String> colNames;
	private Vector<Integer> colTypes; // java.sql.Types (why could they not use an enum?)
	private int rows;
	private int columns;
	private static final int INIT_COLUMNS = 5;

	public SJDBCDataFrame() {
		data = new ArrayList<Object>(INIT_COLUMNS);
		colNames = new Vector<String>(INIT_COLUMNS);
		colTypes = new Vector<Integer>(INIT_COLUMNS); // java.sql.Types
		rows = 0;
		columns = 0;

	}

	public void addIntegerColumn(String colName, int[] colData)
			throws Exception {
		if (columns == 0)
			rows = colData.length;
		if (colData.length != rows)
			throw new Exception(
					"Length of column data does not match existing data.");
		data.add(colData);
		colNames.add(colName);
		colTypes.add(Types.INTEGER);
		columns++;
	}
	public void addLongColumn(String colName, long[] colData)
			throws Exception {
		if (columns == 0)
			rows = colData.length;
		if (colData.length != rows)
			throw new Exception(
					"Length of column data does not match existing data.");
		data.add(colData);
		colNames.add(colName);
		colTypes.add(Types.INTEGER);
		columns++;
	}
	public void addDoubleColumn(String colName, double[] colData)
			throws Exception {
		if (columns == 0)
			rows = colData.length;
		if (colData.length != rows)
			throw new Exception(
					"Length of column data does not match existing data.");
		data.add(colData);
		colNames.add(colName);
		colTypes.add(Types.DOUBLE);
		columns++;
	}

	public void addStringColumn(String colName, String[] colData)
			throws Exception {
		if (columns == 0)
			rows = colData.length;
		if (colData.length != rows)
			throw new Exception(
					"Length of column data does not match existing data.");
		data.add(colData);
		colNames.add(colName);
		colTypes.add(Types.VARCHAR);
		columns++;
	}

	public void addDateColumn(String colName, String[] colData)
			throws Exception {
		if (columns == 0)
			rows = colData.length;
		if (colData.length != rows)
			throw new Exception(
					"Length of column data does not match existing data.");
		// create java.sql.Timestamp column based on input string
		Timestamp[] tsData = new Timestamp[colData.length];
		for (int i=0; i < colData.length; i++) {
			try {
				tsData[i] = Timestamp.valueOf(colData[i]);
			}
			catch (IllegalArgumentException e) {
				// unreadable Timestamp format, assume null
				tsData[i] = null;
			}
		}
		data.add(tsData);
		colNames.add(colName);
		colTypes.add(Types.TIMESTAMP);
		columns++;
	}

	public String[] getColNames() {
		return (String[]) colNames.toArray(new String[0]);
	}

	public Integer[] getColTypes() {
		return (Integer[]) colTypes.toArray(new Integer[0]);
	}

	public int length() {
		return rows;
	}

	public int columns() {
		return columns;
	}

	public int getIntegerData(int col, int row) {
		return ((int[]) data.get(col))[row];
	}
	public long getLongData(int col, int row) {
		return ((long[]) data.get(col))[row];
	}
	public double getDoubleData(int col, int row) {
		return ((double[]) data.get(col))[row];
	}

	public String getStringData(int col, int row) {
		return ((String[]) data.get(col))[row];
	}

	public Timestamp getDateData(int col, int row) {
		return ((Timestamp[]) data.get(col))[row];
	}


}

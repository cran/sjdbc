import java.util.Properties;
import java.sql.*;

/*
 * Wrap dynamically-loaded JDBC drivers in a wrapper class
 * to avoid problems with classes loaded after this one.
 */
class SJDBCDriverShim implements Driver {
    private Driver driver;

    SJDBCDriverShim(Driver d) {
        this.driver = d;
    }

    public boolean acceptsURL(String u) throws SQLException {
        return this.driver.acceptsURL(u);
    }

    public Connection connect(String u, Properties p) throws SQLException {
        return this.driver.connect(u, p);
    }

    public int getMajorVersion() {
        return this.driver.getMajorVersion();
    }

    public int getMinorVersion() {
        return this.driver.getMinorVersion();
    }

    public DriverPropertyInfo[] getPropertyInfo(String u, Properties p) throws SQLException {
        return this.driver.getPropertyInfo(u, p);
    }

    public boolean jdbcCompliant() {
        return this.driver.jdbcCompliant();
    }

    public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

}
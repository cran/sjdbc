\name{executeJDBC}
\alias{executeJDBC}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Execute SQL Query on a JDBC-Compatible Database }
\description{
  Executes a SQL command on a JDBC-Compatible database.  
}
\usage{
executeJDBC(sqlQuery, driverClass, con, user, password, keepAlive)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{sqlQuery}{a string containing the SQL query to execute. }
  \item{driverClass}{a string containing the name of the Java class for required JDBC driver. }
  \item{con}{a string containing the JDBC connection string. }
  \item{user}{a string containing the user name with access to database. }
  \item{password}{a string containing the password for the given user name on the database. }
  \item{keepAlive}{a logical. If \code{TRUE}, keeps the database connection alive after executing the query. The default is \code{FALSE}.}
}
\details{
  Executes the command on the database using the JDBC driver specified in \code{driverClass}. The required JDBC driver must be
  loaded in \code{sjdbc} before it is used.  See \code{loadJDBCDriver} for details.  

  Database connections are closed by default after executing the query, unless \code{keepAlive} is set to \code{TRUE}.  
  If \code{keepAlive = TRUE}, the connection remains open, and successive database commands can reuse the open connection if and only if 
  the same values for \code{driverClass}, \code{con}, \code{user}, and \code{password} are supplied.  
}
\note{
  Some arguments can also be set using \code{sjdbcOptions}. 
}
\value{
  returns the number of rows affected, if applicable.  
}
\seealso{
\code{\link{loadJDBCDriver}}, \code{\link{sjdbcOptions}}
}
\examples{\dontrun{
executeJDBC(driverClass="com.microsoft.sqlserver.jdbc.SQLServerDriver", 
            con="jdbc:sqlserver://qadb-s2k:1433;databaseName=testdb;user=testqa;password=testqa;", 
            user="testqa", password="testqa",
            sqlQuery="UPDATE TEST1 SET Weight = NULL WHERE Weight < 2500")

executeJDBC(driverClass="com.microsoft.sqlserver.jdbc.SQLServerDriver", 
            con="jdbc:sqlserver://qadb-s2k:1433;databaseName=testdb;user=testqa;password=testqa;", 
            user="testqa", password="testqa",
            sqlQuery="DROP TABLE TEST1")
}}
\keyword{ interface }


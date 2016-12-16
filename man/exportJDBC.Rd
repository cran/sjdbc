\name{exportJDBC}
\alias{exportJDBC}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Export To a JDBC-Compatible Database }
\description{
  Exports data to a database using JDBC drivers.  
}
\usage{
exportJDBC(data, table, appendToTable = TRUE,
    driverClass = sjdbcOptions()$driverClass, con = sjdbcOptions()$con,
    user = sjdbcOptions()$user, password = sjdbcOptions()$password,
    keepAlive = sjdbcOptions()$keepAlive, preserveColumnCase = FALSE,
    batchSize = sjdbcOptions()$batchSize, 
    useTransaction = sjdbcOptions()$useTransaction)
}
\arguments{
  \item{data}{the \code{data.frame} object to export. }
  \item{table}{a string containing the name of the database table. }
  \item{appendToTable}{a logical. If \code{TRUE} (the default), rows are appended to the existing table; if \code{FALSE}, any existing table is dropped and an
  empty table is created prior to exporting the data. }  
  \item{driverClass}{a string containing the name of the Java class for the required JDBC driver. }
  \item{con}{a string specifying the JDBC connection string. }
  \item{user}{a string containing the user name with access to database. }
  \item{password}{a string containing the password for the given user name on the database. }
  \item{keepAlive}{a logical. If \code{TRUE}, keeps the database connection alive after executing the query. The default is \code{FALSE}.}
  \item{preserveColumnCase}{a logical. If \code{TRUE}, preserves case-sensitive column names, if supported by database.  
  If \code{FALSE} (the default), column name case is converted to the database-specific default. }
  \item{batchSize}{an integer specifying the number of rows sent to the database in each batch, if batch updates are supported by the JDBC driver.  
  Default value is \code{1000}.  A value of \code{0} disables batch exporting.}
  \item{useTransaction}{ If \code{TRUE}, exports the data as a single transaction, otherwise commits throughout export. }
}
\details{
  Exports data to the database using the JDBC driver specified in \code{driverClass}.  The required JDBC driver must be
  loaded in \code{sjdbc} before use.  See \code{loadJDBCDriver} for details.  

  Database connections are closed by default after the query executes, unless \code{keepAlive} is set to \code{TRUE}.  If \code{keepAlive = TRUE}, 
  the connection remains open, and successive database commands can reuse the open connection if and only if the same values
  for \code{driverClass}, \code{con}, \code{user}, and \code{password} are supplied.    
  
  Setting a larger value for the \code{batchSize} argument can improve efficiency when you need to export large data tables, if batch updates are supported by 
  the JDBC driver.  
  
}
\value{
  returns the number of rows exported.  
}
\seealso{
\code{\link{loadJDBCDriver}}, \code{\link{sjdbcOptions}}, \code{\link{executeJDBC}}
}
\note{
  Some arguments can also be set using \code{sjdbcOptions}. 
  
  When you export to a new table (\code{appendToTable=FALSE}), you might find that the column types of the resulting table are not as desired.  Columns containing
  text data are of type \code{VARCHAR(255)} (or database equivalent), and numeric and \code{timeDate} columns attempt to use appropriate 
  database-specific column types.  If you want a specific column type or precision in your tables, you should create the table manually using 
  \code{executeJDBC}, and then append your data to the existing table.  
}
\examples{\dontrun{
exportJDBC(data=fuel.frame, driverClass="com.microsoft.sqlserver.jdbc.SQLServerDriver", 
            con="jdbc:sqlserver://qadb-s2k:1433;databaseName=testdb;user=testqa;password=testqa;", 
            user="testqa", password="testqa",
            table="TEST1", append=F)
}}
\keyword{ file }
\keyword{ interface }

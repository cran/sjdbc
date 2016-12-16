\name{importJDBC}
\alias{importJDBC}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Import From a JDBC-Compatible Database }
\description{
  Imports data from a database using JDBC drivers.  
}
\usage{
importJDBC(sqlQuery, table, driverClass = sjdbcOptions()$driverClass,
    con = sjdbcOptions()$con, user = sjdbcOptions()$user,
    password = sjdbcOptions()$password,
    keepAlive = sjdbcOptions()$keepAlive, bigdata = FALSE)
}
\arguments{
  \item{sqlQuery}{the SQL query string describing the data to be retreived from the database.  Required if \code{table} is not provided. }
  \item{table}{a string specifying the name of the table to import.  Required if \code{sqlQuery} is not provided.  Implies \code{sqlQuery="SELECT * FROM <table>"}.  }
  \item{driverClass}{a string containing the name of the Java class for the required JDBC driver. }
  \item{con}{the JDBC connection string. }
  \item{user}{a string specifying the user name with access to the database. }
  \item{password}{a string containing the password for the given user name on the database. }
  \item{keepAlive}{a logical. If \code{TRUE}, keeps the database connection alive after executing the query.  The default is \code{FALSE}.}
  \item{bigdata}{unsupported in this version.  Exists for compatibility with Spotfire S+. }
}
\details{
  Imports data from the database using the JDBC driver specified in \code{driverClass}.  The required JDBC driver must be
  loaded in \code{sjdbc} before use.  See \code{loadJDBCDriver} for details.  

  Database connections are closed by default after the query executes, unless \code{keepAlive} is set to \code{TRUE}.  If \code{keepAlive = TRUE}, 
  the connection remains open, and successive database commands can reuse the open connection if and only if the same values
  for \code{driverClass}, \code{con}, \code{user}, and \code{password} are supplied.    
}
\value{
  returns a \code{data.frame} containing the requested data.  
}
\note{
Character data can be imported either as \code{character} or as \code{factor}.  \code{importJDBC} uses the 
value of \code{options(stringsAsFactors)} to determine how to import the data.  

Some arguments can also be set using \code{sjdbcOptions}. 
}
\section{Time Zone Handling}{
Times, Dates, and Timestamps that the datebase returns are assumed to be GMT.  The resulting \code{timeDate} objects 
are created in GMT, without conversion.  If you know the time zone of the incoming data, you can specify an alternative time 
zone for the \code{timeDate} objects by setting \code{options("time.zone")} prior to import.  For further details, see 
the \code{class.timeDate} help file.
}
\seealso{
\code{\link{loadJDBCDriver}}, \code{\link{sjdbcOptions}} 
}
\examples{\dontrun{
importJDBC(driverClass="com.microsoft.sqlserver.jdbc.SQLServerDriver", 
	    con="jdbc:sqlserver://qadb-s2k:1433;databaseName=testdb;user=testqa;password=testqa;", 
	    sqlQuery="SELECT * FROM FUEL_FRAME")
	    
importJDBC(driverClass="COM.ibm.db2.jdbc.net.DB2Driver", 
	    con="jdbc:db2://qadb1:6789/QATESTDB", 
	    user="testqa", 
	    password="testqa", 
	    sqlQuery="SELECT * FROM FUEL_FRAME")
}}
\keyword{ file }
\keyword{ interface }


\name{jdbcTypeInfo}
\alias{jdbcTypeInfo}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Retrieve Supported Column Type Info from a Database }
\description{
  Retrieves a table containing the data types supported by the connected database.  
}
\usage{
jdbcTypeInfo(driverClass, con, user, password, keepAlive)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{driverClass}{a string specifying the name of the Java class for the required JDBC driver. }
  \item{con}{the JDBC connection string. }
  \item{user}{a string specifying the user name with access to the database. }
  \item{password}{a string containing the password for given the user name on the database. }
  \item{keepAlive}{a logical. If \code{TRUE}, keeps the database connection alive after executing the query. The default is \code{FALSE}.}
}
\value{
  returns a \code{data.frame} containing the entire table.  
}
\details{
  A direct interface to the \code{java.sql.DatabaseMetaData.getTypeInfo()} method.  See 
  the Java documentation for description of the fields in the table.  Useful for debugging.  
}
\references{
 2004. \url{http://java.sun.com/j2se/1.5.0/docs/api/java/sql/DatabaseMetaData.html#getTypeInfo()}. \emph{Java SE Developer Documentation}. Redwood Shores, CA: Oracle Corporation.
}
\examples{\dontrun{
jdbcTypeInfo(driverClass="com.microsoft.sqlserver.jdbc.SQLServerDriver", 
            con="jdbc:sqlserver://qadb-s2k:1433;databaseName=testdb;user=testqa;password=testqa;", 
            user="testqa", password="testqa")
}
}
\keyword{ interface }
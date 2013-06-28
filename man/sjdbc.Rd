\name{sjdbc}
\alias{sjdbc}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ SJDBC Package Documentation }
\description{
  The SJDBC Package provides an interface to databases using Java's JDBC connectivity.  
}
\details{
  Provides an interface to a databases using JDBC drivers.  You can get JDBC drivers from the software providers.  
  Place the JAR or ZIP file containing the JDBC drivers in the \code{drivers} folder under the package 
  installation directory. All files placed in this directory are added automatically to the Java \code{CLASSPATH} when
  the package is loaded.  Alternatively, drivers can be loaded explicitly at runtime using \code{\link{loadJDBCDriver}}.  
  
  The interface has been tested with the following drivers:  
  \itemize{
  \item Microsoft SQL Server 2005 
      \itemize{\item Connection String:  \code{jdbc:sqlserver://<host>:1433;databaseName=<database>;user=<username>;password=<password>;}
               \item Driver Class:  \code{com.microsoft.sqlserver.jdbc.SQLServerDriver}
    }
  \item IBM DB2 Universal Database 7.2 
      \itemize{\item Connection String:  \code{jdbc:db2://<host>:6789/<database>}
               \item Driver Class:  \code{COM.ibm.db2.jdbc.net.DB2Driver}
    }
  \item MySQL Connector/J 3.1.14
    \itemize{\item Connection String:  \code{jdbc:mysql://<host>:3306/<database>}
             \item Driver Class:  \code{com.mysql.jdbc.Driver}
    }
  \item Oracle 10g Release 2 10.2.0.4 (ojdbc14.jar)
    \itemize{\item Connection String:  \code{jdbc:oracle:thin:@<host>:1521:<databaseSID>}
             \item Driver Class:  \code{oracle.jdbc.OracleDriver}
    }   
  \item PostgreSQL 8.3 (JDBC3 driver 8.3-603)
    \itemize{\item Connection String:  \code{jdbc:postgresql://<host>:5432/<database>}
             \item Driver Class:  \code{org.postgresql.Driver}
    }       
  }
}
\section{Known Issues}{
\itemize{
\item Missing values might not be handled correctly in all cases.  \code{exportJDBC} handles missing (NA) values for \code{integer} 
and \code{numeric} class columns by creating \code{NULL} values in the database table.
Currently, this does not work for \code{character} or \code{factor} columns.  NA values are stored as \dQuote{NA} in the table, but empty strings 
(\dQuote{}) are stored as empty strings.  
}
}
\keyword{ file }
\keyword{ interface }


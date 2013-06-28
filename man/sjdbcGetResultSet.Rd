\name{sjdbcGetResultSet}
\alias{sjdbcGetResultSet}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Get a ResultSet From Static Java Class }
\description{
  Retreive a ResultSet previously stored in a static instance of \code{SJDBCResultSetUtilities} class as a \code{data.frame}.  
}
\usage{
sjdbcGetResultSet(key, unregister = TRUE, default.num.rows = NULL,
                  start.at.first=TRUE, rows.to.read=-1)
}
%- maybe also 'usage' for other objects documented here.
\arguments{
  \item{key}{a string containing the key into the hash table in \code{SJDBCResultSetUtilities} where the result was stored previously. }
  \item{unregister}{a logical. If \code{TRUE} (the default), specifies that the ResultSet should be removed from the hash 
      table after the data is returned. }
  \item{default.num.rows}{an integer containing the number of rows. 

      When the ResultSet is of type \code{ResultSet.TYPE_FORWARD_ONLY},
      the number of rows cannot be determined until after all of
      the data has been retrieved.  If the ResultSet has more than
      the default number, the array sizes are doubled whenever the
      current capacity is reached.  If the ResultSet is not of
      \code{TYPE_FORWARD_ONLY}, this argument is not used.    }
  \item{start.at.first}{a logical. If \code{TRUE} (the default), set the ResultSet to start
      with the first row before reading.
      if \code{FALSE}, start with the current row.  }
  \item{rows.to.read}{an integer specifying the maximum number of rows to read. If 
      less than zero, read all rows in the result set.}
}
\details{
  This function is called by \code{importJDBC} and usually is not called directly.  
}
\value{
  returns a \code{data.frame} containing the ResultSet.  
}
\references{ Adapted from \code{javaGetResultSet} in Spotfire S+ 8.0.4. }
\seealso{ \code{\link{importJDBC}} }
\examples{\dontrun{
sjdbcGetResultSet("resultid")
}
}
\keyword{ file }
\keyword{ interface }


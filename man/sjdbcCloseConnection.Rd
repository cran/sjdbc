\name{sjdbcCloseConnection}
\alias{sjdbcCloseConnection}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Close a Persistent Database Connection }
\description{
  Closes any open persistent database connection.  
}
\usage{
  sjdbcCloseConnection()
}
\details{
  Closes a connection that was made persistent previously using the \code{keepAlive} argument to one of the database functions.  
 
  This function is used primarily by \code{importJDBC}, \code{exportJDBC}, and \code{executeJDBC} to
  close connections after execution. It rarely needs to be called directly.  
}
\value{
  returns no value. 
}
\seealso{
\code{\link{importJDBC}}, \code{\link{exportJDBC}}, \code{\link{executeJDBC}} 
}
\examples{\dontrun{
  # close an open connection
  sjdbcCloseConnection()
}}
\keyword{ interface }


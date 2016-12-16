\name{jdbcTimeDate}
\alias{jdbcTimeDate}
\title{ Convert an splusTimeDate::timeDate object to standard JDBC Timestamp string  }
\description{
  Converts a \code{timeDate} vector to a \code{character} vector in the standard format
  expected by \code{java.sql.Timestamp}:  
  \code{yyyy-mm-dd hh:mm:ss.fffffffff}  (in GMT)
}
\usage{
jdbcTimeDate(data)
}

\arguments{
  \item{data}{a \code{timeDate} vector. }
}
\value{
  returns a \code{character} vector in the specified format.  
}
\seealso{ \code{\link{exportJDBC}} }
\examples{
my.td <- as.POSIXct("2011/1/1")
jdbcTimeDate(my.td)
}
\keyword{ interface }
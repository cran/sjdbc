\name{loadJDBCDriver}
\alias{loadJDBCDriver}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Load a JDBC Driver }
\description{
  Makes a JDBC Driver available to the \code{sjdbc} package.  
}
\usage{
loadJDBCDriver(driverJar)
}
\arguments{
  \item{driverJar}{a vector of one or more strings containing the full paths to JDBC driver jars. }
}
\details{
  Makes the specified driver jars available to the \code{sjdbc} package.  The driver must be loaded prior to its first use
  in the TIBCO Enterprise Runtime for R session.  
}
\note{ 
  The JDBC drivers need to be loaded each time you use the \code{sjdbc} package.  To load a driver automatically when
  loading the \code{sjdbc} package, place it in the in the \code{drivers} folder where the 
  \code{sjdbc} package is installed.
}
\examples{\dontrun{
loadJDBCDriver(file.path("C:", "sqljdbc.jar"))
}}
\keyword{ interface }


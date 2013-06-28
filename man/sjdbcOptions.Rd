\name{sjdbcOptions}
\alias{sjdbcOptions}
%- Also NEED an '\alias' for EACH other topic documented here.
\title{ Package Options and Defaults}
\description{
  Store presistent options and defaults for \code{sjdbc} package functions.  
}
\usage{
sjdbcOptions(...)
}
\arguments{
  \item{...}{  you can provide no arguments. You can provide a list or vector of character strings 
               as the only argument, or you can provide arguments in \code{name=value} form. 
               See the \bold{VALUE} and \bold{SIDE EFFECTS} sections for more information.  }
}

\value{

  The \code{sjdbcOptions} function always returns a list, even if the list is of length 1. 
  \itemize{
      \item{if no arguments are given, returns a list of current values for all options.}
      \item{if a character vector is given as the only argument, returns a list of current values 
       for the options named in the character vector.}
      \item{If an object of mode \code{"list"} is given as the only argument, 
       its components become the values for options with the corresponding names. The function returns a list of the 
       option values before they were modified. Usually, the list given as an argument is the return value 
       of a previous call to \code{sjdbcOptions}.}
     \item{If arguments are given in \code{name=value} form, \code{sjdbcOptions} changes the values of the 
      specified options and returns a list of the option values before they were modified.}

  }
}
\section{Side Effects}{
  When options are set, the \code{sjdbcOptions} function changes a list named \code{.sjdbcOptions} in the session 
  frame (frame 0). The components of \code{.sjdbcOptions} are all of the currently defined options. 
  If \code{sjdbcOptions} is called with either a list as the single argument or with one or more arguments 
  in \code{name=value} form, the options specified are changed or created. 
}
\section{Supported Options}{
\itemize{
\item{\code{driverClass}}{  a string containing the name of the Java class for the required JDBC driver. }
\item{\code{con}}{  the JDBC connection string. }
\item{\code{user}}{  a string specifying the user name with access to database.  \emph{Note:} Some drivers do not require this option. }
\item{\code{password}}{  a string containing the password for the given user name on the database.  \emph{Note:} Some drivers do not require this option. }
\item{\code{keepAlive}}{  a logical. if \code{TRUE}, keeps the database connection alive after executing the query. Defaults to \code{FALSE}.}
\item{\code{batchSize}}{  an integer containing the number of rows exported per batch in \code{exportJDBC}.  Defaults to \code{1000}.}
}
}

\section{See Also}{
This function closely mimics the behavior of the \code{options} function in base TIBCO Enterprise Runtime for R.  
}
\examples{
# set a single option
sjdbcOptions(driverClass="COM.ibm.db2.jdbc.net.DB2Driver")

# set multiple options
sjdbcOptions(driverClass="COM.ibm.db2.jdbc.net.DB2Driver", 
	    con="jdbc:db2://qadb1:6789/QATESTDB", 
	    user="testqa", 
	    password="testqa")
}
 
\keyword{ interface }


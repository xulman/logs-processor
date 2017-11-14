This is a repository with Java source codes for simple log processor that
reads in one log file (which can itself be well a concatenation of multiple
log files) and displays it in "2D". That said, the log file is expected to
consists of logical tripples (x,y,msg) and is displayed anyhow 2D flattish,
e.g., as Excel sheet or as plot inside a SVG graphics. The 'x' is assumed
to represent a source of the 'mgs' (message), e.g. a threadID,
communication peer etc; it is merely a 'x' axis label of some column.
Obviously, the same 'x'-related messages are aggregated in the same column.
The 'y' is assumed to represent a time stamp when 'x' emitted the 'msg'; it
is truly an y-coordinate. The message 'msg' can be any text that shall be
displayed "at" 'x'-labeled column and 'y'-coordinate row.

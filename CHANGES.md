# CHANGELOG

## `0.1.0`

Changes from `cljs.repl.browser`, from which this codebase was started:

* Multiple concurrent browser-REPLs can be safely used
* The browser-REPL's HTTP server is now always-on
* Each browser-REPL session supports a new top-level "entry" URL that can be
  used to easily start the REPL in a browser or other JS runtime (i.e. you don't
  need to have a separate webapp running to initiate the browser-REPL
  connection)
* The entry (and REPL) URLs are available in slots on the browser-REPL's
  environment, making it trivial to automate browser-REPL sessions with e.g.
  phantomjs (see the added `exec-env` for an easy automated browser-REPL option)
* Replaced the custom HTTP server with `com.sun.net.httpserver.*` bits (AFAICT, a
  part of J2SE 6+, not random implementation details:
  http://docs.oracle.com/javase/7/docs/technotes/guides/net/enhancements-6.0.html)
* The `:port` argument to `repl-env` is no longer supported; the lifecycle of the
  server is not tied to the creation of a browser-REPL environment.  If you need
  to get the port of the running browser-REPL server, use
  `(get-browser-repl-port)`; if you need a URL you can use with
  `clojure.browser.repl.connect` as shown in existing browser-REPL tutorials,
  it's available under `:repl-url` from the browser-REPL environment you want to
  connect to.


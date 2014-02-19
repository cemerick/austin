# CHANGELOG

## [`0.1.4`](https://github.com/cemerick/austin/issues?milestone=2&state=closed)

* Austin REPLs now have ClojureScript source maps turned on by default. (gh-33)
* `cemerick.austin/repl-env` (and all helper functions that delegate to it) now
  accept a `:host` option (defaults to `"localhost"`, can be any
  network-addressable hostname) (gh-41)

## `0.1.3`

Released to address a derp in the upstream Piggieback dependency.

## `0.1.2`

* Adds support for ClojureScript compiler environments introduced in `0.0-2014`.
  Now requires that version of ClojureScript or higher.

## [`0.1.1`](https://github.com/cemerick/austin/issues?milestone=1&page=1&state=closed)

* The port that Austin's HTTP server starts on can now be configured via system
  property, environment variable, or by explicitly starting it with a given port
  number. (gh-4, gh-5)
* A comprehensible error message is now emitted if the executable named when
  creating a new `cemerick.austin/exec-env` (or running the
  `cemerick.austin.repls/exec` shortcut) (`phantomjs` by default) is not
  available. (gh-12, gh-13)
* The `:static-dir` option is now properly utilized (gh-2)

## `0.1.0`

Changes from `cljs.repl.browser`, from which this codebase was started:

* Multiple concurrent browser-REPLs can be safely used from the same project
* Austin's HTTP server is now always-on, and auto-selects an open port; this
  means you can have multiple concurrent browser-REPLs running from _different_
  projects without faffing around with `:port` arguments, etc.
* Each browser-REPL session supports a new top-level "entry" URL that can be
  used to easily start the REPL in a browser or other JS runtime (i.e. you don't
  need to have a separate webapp running to initiate the browser-REPL
  connection)
* The entry (and REPL) URLs are available in slots on the browser-REPL's
  environment, making it trivial to automate browser-REPL sessions with e.g.
  phantomjs (see `exec-env` for an easy automated browser-REPL option)
* Replaced the custom HTTP server with `com.sun.net.httpserver.*` bits ([a
  standard part of J2SE
  6+](http://docs.oracle.com/javase/7/docs/technotes/guides/net/enhancements-6.0.html))
* The `:port` argument to `repl-env` is no longer supported; the lifecycle of
  the server is not tied to the creation of a browser-REPL environment.  If you
  need to get the port of the running browser-REPL server, use
  `(get-browser-repl-port)`; if you need a URL you can use with
  `clojure.browser.repl/connect` as shown in existing browser-REPL tutorials,
  it's available under `:repl-url` from the browser-REPL environment you want to
  connect to.

# Austin

<!-- [![Travis CI status](https://secure.travis-ci.org/cemerick/austin.png)](http://travis-ci.org/#!/cemerick/austin/builds) -->

A significant refactoring of the ClojureScript-standard browser-repl environment
that's as easy to "configure" and use as a Clojure REPL.

[![](https://dl.dropboxusercontent.com/u/35498822/austin-6-large.png)](http://youtu.be/HoLs0V8T5AA?t=41s)

## Why?

Austin has one objective: to get you into a fast ClojureScript REPL suited for
your project running in a browser environment as quickly and painlessly as
possible, with full support for the nREPL toolchain.

[Check out the screencast demonstrating how Austin is
used](http://www.youtube.com/watch?v=a1Bs0pXIVXc&feature=youtu.be), or forge
ahead for detailed documentation.


## Status

I've been using this browser-repl alternative for ~six months with good results,
and others have banged it around some as well.  That said, I've only recently
begun to think about the API around its configuration and project integration
(in particular, the `cemerick.austin.repls` namespace).  It all works nice, but
changes in that department are almost surely going to happen.

### Compatibility

When using Austin via nREPL, it depends upon
[Piggieback](https://github.com/cemerick/piggieback). Please refer to
[Piggieback's compatibility notes](https://github.com/cemerick/piggieback#compatibility-notes)
to see if there are any known problems with using it (and therefore Austin)
with your preferred toolchain.

## Changelog

Available [here](http://github.com/cemerick/austin/blob/master/CHANGES.md).

Austin is largely a refactoring of the original ClojureScript REPL.  These
changes include:

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
  phantomjs.  See ['Project REPLs'](#project-repls) for easy-mode "project"
  REPLs.
* Replaced the custom HTTP server with `com.sun.net.httpserver.*` bits ([a
  standard part of J2SE
  6+](http://docs.oracle.com/javase/7/docs/technotes/guides/net/enhancements-6.0.html))
* The `:port` argument to `repl-env` is no longer supported; the lifecycle of
  the server is not tied to the creation of a browser-REPL environment.  If you
  need to get the port of the running browser-REPL server, use
  `(get-browser-repl-port)`; if you need a URL you can use with
  `clojure.browser.repl/connect` as shown in existing browser-REPL tutorials,
  it's available under `:repl-url` from the browser-REPL environment you want to
  connect to. See ['Browser-connected REPLs'](#browser-connected-repls) for
  easy-mode browser-connected REPLs

## "Installation"

Austin is available in Maven Central. Add it to your `project.clj`'s list of
`:plugins`, probably in your `:dev` profile:

```clojure
:profiles {:dev {:plugins [[com.cemerick/austin "0.1.4"]]}}
```

**WARNING Austin is not compatible with the (currently) latest ClojureScript release.**
([context](https://github.com/cemerick/austin/issues/55)) Use ClojureScript `0.0-2156`
until the a new ClojureScript release is cut that includes the resolution of the
underlying issue.

Also, just like in Clojure development, your ClojureScript source roots must be
listed in e.g. `:source-paths` and/or `:test-source-paths` in order for
ClojureScript source files to be picked up properly.  i.e. just having them
enumerated in your lein-cljsbuild configuration(s) is not sufficient.

**Note that Austin requires ClojureScript `0.0-2014` or higher.**

Austin contains some Leiningen middleware that does the following:

* Adds a _dependency_ on Austin to your project, which transitively brings in
  ClojureScript and Piggieback.
* Modifies your project's `:repl-options` to include Piggieback's
  `wrap-cljs-repl` middleware.
* Adds `(require '[cemerick.austin.repls :refer (exec) :rename {exec
  austin-exec}])` to your project's `:injections`, thus making
  `cemerick.austin.repls/exec` available as `austin-exec` in the `user`
  namespace for your fast'n'easy ClojureScript browser REPL pleasure.

## Usage

_If you're impatient, [skip on down](#project-repls) to start a ClojureScript
REPL using phantomjs/slimerjs/Chrome/etc in about 10 seconds._

Austin provides two types of ClojureScript REPL environments.  One, returned by
calls to `cemerick.austin/repl-env`, is analogous to the standard ClojureScript
browser-REPL environment implemented in `cljs.repl.browser`, with various
usability improvements.  The other, returned by calls to
`cemerick.austin/exec-env`, provides all the same functionality as `repl-env`,
but also fully manages the lifecycle of an external JavaScript runtime that is
used to service all REPL interactions (i.e. you don't need to have an app
running in a GUI browser to get a browser-REPL going).  Either of these REPL
environments can be used with either of:

* `cljs.repl/repl`, described in the various "core" ClojureScript tutorials as
  the primary entry point for all things REPL.  This is suitable in terminal
  settings, can be used w/ e.g. `inferior-lisp` in emacs, and so on, but cannot
  be used with nREPL.
* `cemerick.piggieback/cljs-repl`, the nREPL-compatible analogue to
  `cljs.repl/repl`, provided by Piggieback

Austin's two types of REPL environments roughly correspond to the two primary
scenarios for ClojureScript REPLs:

* _Project_ REPLs, where you want a ClojureScript REPL that has all of your
  project's dependencies, sources, and other resources available, but is
  generally not using or requiring your application's front-end to be running in
  a GUI browser, i.e. a headless JavaScript runtime is sufficient, which may or
  may not have a DOM.  This is generally when `exec-env` is used.
* _Browser-connected REPLs_, the original use case of ClojureScript
  browser-REPLs, where you want a ClojureScript REPL connected to a browser
  runtime within which you've loaded your front-end application.

This nomenclature is a bit hand-wavy, since the JavaScript runtimes used by
project REPLs are almost always _also_ browsers; hopefully I'll come up with a
better term for the first category eventually.

### Project REPLs

To start a project REPL, just pass the result of calling `exec-env` to the
ClojureScript REPL function that corresponds with your environment:

* If you're using nREPL, `(cemerick.piggieback/cljs-repl :repl-env
  (cemerick.austin/exec-env))`
* If you're not using nREPL, `(cljs.repl/repl (cemerick.austin/exec-env))`

Alternatively, you can use `cemerick.austin.repls/cljs-repl`, a convenience
function that will detect whether you're using nREPL or not, and pass a new exec
environment to the correct ClojureScript REPL function.  So,
`(cemerick.austin.repls/cljs-repl (cemerick.austin/exec-env))` is equivalent to
the two examples above; this particular combination is so commonly used that
it's wrapped up into a single function, `(cemerick.austin.repls/exec)`, probably
the easiest way to start a ClojureScript REPL that uses a browser JavaScript
runtime.  _Note that `cemerick.austin.repls/exec` passes all of its arguments
along to `exec-env`._

#### `exec-env`'s browser runtimes

Any of the above options will give you a headless ClojureScript REPL that has all
of your project's dependencies, sources, and other resources available.
`exec-env` uses `phantomjs` by default, so you'll need to have that installed
and on your `PATH`.  If you are using a different _phantomjs-compatible_
headless browser implementation (e.g. slimerjs, or perhaps your package manager
installs phantomjs with a different name?), you can pass the name of that binary
as :phantom-cmd, e.g. `(exec-env :phantom-cmd "slimerjs")`.

Whichever process is started will be automatically terminated when you stop the
ClojureScript REPL (via `:cljs/quit`), or the parent Clojure REPL.

##### Using other browser runtimes

I've been saying "headless" here because it's often most convenient to avoid
using "headed" browsers, which necessarily open a new window for each
ClojureScript REPL you start. But, if you really want to, you _can_ use a full
GUI browser with `exec-env`, which can be handy if you need to see the results
of DOM manipulations, etc., without having to set up and connect to a browser
running your application.  To do this, just pass the terminal commands necessary
to start your preferred browser (such that Austin can append the browser-repl
URL to the command) to `exec-env` or `exec` as a `:exec-cmds` vector keyword
argument:

```clojure
user=> (cemerick.austin.repls/exec
         :exec-cmds ["open" "-ga" "/Applications/Google Chrome.app"])
Browser-REPL ready @ http://localhost:59423/4877/repl/start
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=> (apply + (js/Array 1 2 3))
6
```

The command strings passed to `exec` in this example will open the browser-REPL
endpoint URL in a new Chrome window in the background on Mac OS X.  Substitute
whatever invocation you like for your preferred browser / operating system.

### Browser-connected REPLs

This was always the primary use case for the original browser-repl: load your
application up in a browser, have it connect back to your Clojure /
ClojureScript compiler environment, and you can develop/debug/inspect/etc your
running ClojureScript application as it runs in its target environment.

This repo provides a completely self-contained sample project demonstrating and
documenting how to use Austin for your browser-connected REPL'ing needs.  [Check
it
out](https://github.com/cemerick/austin/blob/master/browser-connected-repl-sample).

### Other usage tidbits

#### Server port selection

By default, Austin's embedded HTTP server (which is what accepts requests from
all JavaScript runtimes hosting a ClojureScript REPL) starts on a random
system-assigned port.  If you're using the provided facilities for generating
Javascript to insert into your app's HTML to connect back to the HTTP server
(i.e. `cemerick.austin.repls/browser-connected-repl-js`), then this is ideal:
the server will always find an open port, and running multiple applications,
each with N browser-REPLs, will always work.

However, if you need to fix the port used by the HTTP server, there are three
ways to go about it:

* set the `AUSTIN_DEFAULT_SERVER_PORT` environment variable before starting your
  Clojure process
* set the `cemerick.austin.default-server-port` system property; _this will only
  take effect if you have not yet caused the server to start automatically by
  creating a browser-REPL environment_.
* explicitly start Austin's server, providing the desired port number, e.g.
  `(cemerick.austin/start-server 9000)`.

## TODO

* ISO a reasonable automated test strategy

## Need Help?

Ping `cemerick` on freenode irc or
[twitter](http://twitter.com/cemerick) if you have questions or would
like to contribute.

## License and credits

Big shout out to Brenton Ashworth, Alex Redington, and Bobby Calderwood (the
authors of the original browser-repl), Brandon Bloom for pushing hard on making
ClojureScript easier to use, and everyone else in #clojure and on the mailing
list(s) that took the time to take Austin for a spin when it was still just a
[gist](https://gist.github.com/cemerick/5091059) and then a `.patch` file.

Copyright ©2013 [Chas Emerick](http://cemerick.com) and other contributors.

```
Copyright (c) Rich Hickey. All rights reserved. The use and
distribution terms for this software are covered by the Eclipse
Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
which can be found in the file epl-v10.html at the root of this
distribution. By using this software in any fashion, you are
agreeing to be bound by the terms of this license. You must
not remove this notice, or any other, from this software.
```

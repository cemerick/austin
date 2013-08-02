# Austin

<!-- [![Travis CI status](https://secure.travis-ci.org/cemerick/austin.png)](http://travis-ci.org/#!/cemerick/austin/builds) -->

A significant refactoring of the ClojureScript-standard browser-repl environment
that's as easy to "configure" and use as a Clojure REPL.

[![](https://dl.dropboxusercontent.com/u/35498822/austin-6-large.png)](http://youtu.be/HoLs0V8T5AA?t=41s)

## Why?

Austin has one objective: to get you into a fast ClojureScript REPL suited for
your project running in a browser environment as quickly and painlessly as
possible, with full support for the nREPL toolchain.  

## Status

I've been using this browser-repl alternative for ~six months with good results,
and others have banged it around some as well.  That said, I've only recently
begun to think about the "UX" around its configuration and project integration,
so changes in that department are almost surely going to happen.

## Changelog

Available [here](http://github.com/cemerick/austin/blob/master/CHANGES.md); the
oldest entries summarize the changes made based on the original ClojureScript
browser-repl.

## "Installation"

Austin is available in Maven Central. Add it to your `project.clj`'s list of
`:plugins`, probably in your `:dev` profile:

```clojure
:profiles {:dev {:plugins [[com.cemerick/austin "0.1.0-SNAPSHOT"]]}}
```

Austin contains some Leiningen middleware that does the following:

* Adds a _dependency_ on Austin to your project, which transitively brings in
  ClojureScript and Piggieback.
* Modifies your project's `:repl-options` to include Piggieback's
  `wrap-cljs-repl` middleware.

## Usage

**All examples and discussions currently here assume the use of an nREPL
toolchain (e.g. nrepl.el, vim-fireplace, Counterclockwise, etc).** Austin can be
used without nREPL, I just haven't documented it yet.

There are approximately two ClojureScript REPL use cases: one where you want to connect a ClojureScript REPL to your front-end running in a full GUI browser, and one 

### Headless REPL

This is where all you really want is a "headless" ClojureScript REPL that has all of your project's dependencies, sources, and other resources available.  

Start your project's Clojure REPL, e.g. `lein repl`, or the equivalent in
any other nREPL environment (or, you can clone an nREPL session
of an already-running REPL, thus making it easy to have a session plugged into
"both sides", Clojure and ClojureScript), and call
`cemerick.austin.repls/start`:

```clojure
user=> (require '[cemerick.austin.repls :refer (start)])
nil
user=> (start)
Browser-REPL ready @ http://localhost:59423/5546/repl/start
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=> (apply + (js/Array 1 2 3))
6
```

Austin uses uses `phantomjs` by default, so you'll need to have that installed
and on your `PATH`.  If you are using a different _phantomjs-compatible_
headless browser implementation (e.g. slimerjs, or perhaps your package manager
installs phantomjs with a different name?), you can pass the name of that binary
as :phantom-cmd, e.g. `(start :phantom-cmd "slimerjs")`.

Whichever process is started will be automatically terminated when you stop the
ClojureScript REPL (via `:cljs/quit`), or the parent Clojure REPL.

#### Using other browser runtimes

I've been saying "headless" here because it's often most convenient to avoid
using "headed" browsers, which necessarily open a new window for each
ClojureScript REPL you start. But, if you really want to, you _can_ use a full
GUI browser in this scenario.  To do this, just pass the terminal commands
necessary to start your preferred browser (such that Austin can append the
browser-repl URL to the command) to `cemerick.austin.repls/start` as a
`:exec-cmds` vector keyword argument:

```clojure
user=> (start :exec-cmds ["open" "-ga" "/Applications/Google Chrome.app"])
Browser-REPL ready @ http://localhost:59423/4877/repl/start
Type `:cljs/quit` to stop the ClojureScript REPL
nil
cljs.user=> (apply + (js/Array 1 2 3))
6
```

The command strings passed to `start` will open the browser-REPL endpoint URL in
a new Chrome window in the background on Mac OS X.  Substitute whatever
invocation you like for your preferred browser / operating system.

### Browser-connected REPL

You want a ClojureScript REPL connected to a browser runtime within which you've
loaded your front-end application.  This was always the primary use case for the
original browser-repl.

**This is fully implemented, but I haven't written up docs for it yet.  Coming
shortly!**

##

## TODO

* docs for browser-connected REPL use case
* Sample web app project using Austin
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

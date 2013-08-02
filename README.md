# Austin

<!-- [![Travis CI status](https://secure.travis-ci.org/cemerick/austin.png)](http://travis-ci.org/#!/cemerick/austin/builds) -->

A significant refactoring of the ClojureScript standard browser-repl environment
that's as easy to "configure" and use as a Clojure REPL.

<iframe width="420" height="315" src="//www.youtube.com/embed/HoLs0V8T5AA?start=41" frameborder="0" allowfullscreen></iframe>

## Why?

Austin has one objective: to get you into a fast ClojureScript REPL suited for
your project running in a browser environment as quickly and painlessly as
possible, with full support for the nREPL toolchain.  There are two primary use
cases:

1. You want a ClojureScript REPL connected to a browser runtime within which
   you've loaded your front-end application.  This was always the primary use
   case for the original browser-repl.
2. You want a ClojureScript REPL with all of your project's dependencies and
   such available, but not necessarily using a browser that has your app loaded.
   In many cases, a headless browser runtime (like phantomjs or slimerjs) is
   perfectly acceptable here.

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

Austin is available in Maven Central. Add this `:dependency` to your Leiningen
`project.clj`:

```clojure
[com.cemerick/austin "0.1.0"]
```

Or, add this to your Maven project's `pom.xml`:

```xml
<dependency>
  <groupId>com.cemerick</groupId>
  <artifactId>austin</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Usage


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

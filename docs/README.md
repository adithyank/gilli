# Introduction

A Platform that has lot of [DSL](https://en.wikipedia.org/wiki/Domain-specific_language)s for day to day purpose

`lsdsl-lang` is a  **library** with lot of built-in DSLs and platform 
for creating lot of DLSs using `Groovy` and `Java` languages.

This platform is built using the DSL facilities offered by
[Groovy Programming language](http://groovy.apache.org/).

In addition to support creating lot of DSLs, the platform itself will
offer lot of builtin DSLs and lot of command line facilities like
custom commands, awk like text file processing, mysql/oracle query
execution from command line itself, etc. The tag line is `Java/Groovy
Power at command line`

This project can also be integrated with any Java based applications to
use the builtin DSLs for the common tasks to drastically improve 
developer productivity. Also, development teams can create their own
DSLs to solve their domain problems at hand.

`lsdsl-lang` platform is also targetting non-development (testers, software
support personnel, system administrators) teams to better
do their routine works using quick easy scripts.

Fundamentally, it is great platform for people who does automation using
simple scripts.

Keep following this project for new features, updates...!

# Design

**LsDSL** does not require you to write parsers or grammer to compile your DSL code. It is over the facilities of Groovy only.

The DSL Script code that you write is a legal groovy code requiring additional classpath entries. That is it !

# Note on Other JVM Languages

You can use Java or Groovy for using and writing DSLs in this paltform.
Technically, you should be able to use kotlin or scala or any other
JVM languages also to implement DSLs. But, we have not attempted so far.

Now we are good only with Java and Groovy...! :)

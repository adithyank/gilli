# I AM ACTIVELY BUILDING IT. PLS COME BACK AFTER SOME TIME. YOU CAN USE IT

# Introduction

1. A Platform that has lot of [DSL](https://en.wikipedia.org/wiki/Domain-specific_language)s for day to day purpose
1. You can create your own DSL for solving your domain problems as per the guidelines documented below and use it along with existing built-in DSLs
   * Currently below programming languages are supported
       * Java
       * Groovy

# Groovy

This **LsDSL** library exploits the facilities provided by [Groovy](http://groovy-lang.org/) Language in implementing the DSLs. Since Groovy is one of the JVM languages, you can use also java to implement DSL.

# Design

**LsDSL** does not require you to write parsers or grammer to compile your DSL code. It is over the facilities of Groovy only.

The DSL Script code that you write is a legal groovy code requiring additional classpath entries. That is it !

# Note on Other JVM Languages

Technically, you should be able to use kotlin or scala or any other JVM languages also to implement DSLs. But, we have not attempted so far. We will attempt and update this page


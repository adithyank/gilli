---
title: Quick Feel :)
permalink: lang_quickfeel.html
---

Sample usages of the {{ page.gilli_project_title }} is here...

# Simple Command Line Examples

## Simple Printing

```bash
$ gilli -p ' "Gilli is awesome !" '
Gilli is awesome !
```
1. `-p` just prints the output of the given expression in the console
1. The given expression inside pair of single-quotes is a valid `groovy`
   code. In addition to plain groovy, {{ page.gilli_project_title }} offers lot of
   additional utilities... We will see in the below sections
1. Inner double-quotes are given as `String` is printed
1. The expression can be a simple string or any arithmetics as given below


## Simple Arithmetics

```bash
$ gilli -p '4/5'
0.8

$ gilli -p '(4 * 12) + 12'
60
```

## String Processing

`String` processing at command line is so easy now with {{ page.gilli_project_title }}

```bash
$ gilli -p '"Internationalization".take(5)'
Inter
```
Where from the `take` operation comes? You remember, I said that any valid groovy
expression can be given. `take` is a method offered by groovy on `String`s

## Piping from OS commands

Assume that the content of `names.txt` is as below

```
India
US
England
Africa
```


Do you want to print the length of each line along side...? See the command below...

```bash
$ cat names.txt | gilli -i -p '"$line.length : $line"'
5 : India
2 : US
7 : England
6 : Africa
```

1. The expression used here uses the [String Interpolation](https://groovy-lang.org/syntax.html#_string_interpolation)
feature of Groovy
1. `-i` option indicates that the expression will be called for each `i`nput
line
1. `-p` indicates that the output of the expression will be printed in the console. Since `-i` is given
the printing is done for each line... Very cool
1. The expression indicates that the `length` of the line followed by `colon` symbol
   followed by the actual line itself will be printed. 
1. This piping opens up lot of opportunities to play in the command line itself with
`Power of Java, Groovy, Gilli`. You can pipe the output back to OS commands like below

```bash
$ cat names.txt | gilli -i -p '"$line.length : $line"' | sort
2 : US
5 : India
6 : Africa
7 : England
```

Sky is the limit! Enjoy !!


## Power of Java, Groovy, Gilli at command line

Apply a condition in the command line itself.

Print the input line only if it contains the character `a`

```bash
$ cat names.txt | gilli -i -e 'if (line.contains("a")) println line'
India
England
Africa
```

The example is trivial here. But, the power can be felt with this.

# Gilli Specific Examples

Some of the above things can be done with `groovy` command also. Below are examples
of value addition from {{ page.gilli_project_title }}

## Unit conversions

Unit conversions at the command line itself...!

```bash
$ gilli -p 'convert.length(1000).meter.toMile'
0.6213712000 mile

$ gilli -p 'convert.length(1000).meter.tomILe'
0.6213712000 mile

$ gilli -p 'convert.area(1).acre.toAre'
40.468564224 are

```

There are lot of such unit conversions available in gilli...


TODO : Documentation is yet to be completed 

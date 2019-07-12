---
title: Contribution
permalink: lang_contribution.html
---

## Introduction

Pls read the full content in this page before starting your development
in {{ page.gilli_project_title }} project

{{ page.gilli_project_title }} project uses Gradle as its build tool
   

Pls run `gradle cD` to build {{ page.gilli_project_title }} project
locally and do some sanity checks with the
{{ page.gilli_project_title }} project runtime.


## Development Hints

Please read all the below sections before starting your development on
this project

## Running Tests

{{ page.gilli_project_title }} project uses `TestNG` as its testing
   framework

Pls run `gradle test` locally at the directory `gilli` and see whether
all the tests are passing after your every change.


## Hints For Documentation

### Push Auto-generated files

After your development and testing, you will run `gradle` without any
task name or `gradle createDistributions` for taking local build.

This will create local build along with any auto generated files (primarily
for documentation). So, pls ensure you run `git status` to check for any
auto-generated files and make sure that you `git commit` and `git push` 
them too.

### Your own auto-generation

When you develop some feature, you may want the docs to be auto-generated
similar to some existing ones. 

To achieve this, pls add a new class in `gilli-cover` project and call it
from `main()` method of `CoverMain.java`

See `PrepareUnitsList.groovy` for a working example.

## Inter-Module Dependency

The current design of the gradle scripts is in such a way that the
inter-module dependencies are to be entered in the `dependencies`
closure of the individual modules' `build.gradle`

The dependencies that are common to all the modules are to be put in
{{ page.gilli_project_title }}`/modules/gilli-cover/build.gradle`


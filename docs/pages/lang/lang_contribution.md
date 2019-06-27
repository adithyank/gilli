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


## Inter-Module Dependency

The current design of the gradle scripts is in such a way that the
inter-module dependencies are to be entered in the `dependencies`
closure of the individual modules' `build.gradle`

The dependencies that are common to all the modules are to be put in
{{ page.gilli_project_title }}`/build.gradle`


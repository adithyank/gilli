---
title: Contribution
permalink: lang_contribution.html
---

## Introduction

Pls read the full content in this page before starting your development
in `lsdsl-lang` project


## Development Hints

Please read all the below sections before starting your development on
this project

### Inter-Module Dependency

The current design of the gradle scripts is in such a way that the
inter-module dependencies are to be entered in the `dependencies`
closure of the individual modules' `build.gradle`

The dependencies that are common to all the modules are to be put in
`lsdsl-lang/build.gradle`


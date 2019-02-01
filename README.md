#  Gradle R plugin

[![Join the chat at https://gitter.im/gradle-R-plugin/community](https://badges.gitter.im/gradle-R-plugin/community.svg)](https://gitter.im/gradle-R-plugin/community?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Waffle.io - Columns and their card count](https://badge.waffle.io/arekbee/gradle-R-plugin.svg?columns=all)](https://waffle.io/arekbeey/gradle-R-plugin)

[![Build Status](https://travis-ci.com/arekbee/gradle-R-plugin.svg?branch=master)](https://travis-ci.com/arekbee/gradle-R-plugin)
[![Build status](https://ci.appveyor.com/api/projects/status/agg9on9mydpl19e1?svg=true)](https://ci.appveyor.com/project/arekbee/gradle-r-plugin)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=arekbee_gradle-R-plugin&metric=alert_status)](https://sonarcloud.io/dashboard?id=arekbee_gradle-R-plugin)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.arekbee/gradle-R-plugin.svg)](https://search.maven.org/artifact/com.github.arekbee/gradle-R-plugin)
[![Gradle Plugin](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/arekbee/gradle-R-plugin/com.github.arekbee.gradle-R-plugin.gradle.plugin/maven-metadata.xml.svg?label=gradle-plugin)](https://plugins.gradle.org/plugin/com.github.arekbee.gradle-R-plugin)


This Gradle plugin provides tasks to build and deploy R scripts and packages.

gradle-R-plugin has been also uploaded into [gradle plugin portal](https://plugins.gradle.org/plugin/com.github.arekbee.gradle-R-plugin)



# How to use it?

## Initialize gradle for R project
``` 
gradle init
```


## Configuration

In your R project, add the following plugin declaration to your build.gradle file:
``` groove
plugins {
  id "com.github.arekbee.gradle-R-plugin" version "0.3"
}
```

If you are using older version of Gradle then you need to use build script snippet
``` groove
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath "com.github.arekbee:gradle-R-plugin:0.3"
  }
}

apply plugin: "com.github.arekbee.gradle-R-plugin"
```

Alternatively to gradle plugin portal, the plugin JAR is also available in directly available on Maven Central.
``` groove
buildscript {
  repositories {
    mavenCentral()
  }
}
```


Alternatively, you can download it from GitHub and deploy it to your local repository. If you have local maven repository then you can

``` groove
buildscript {
  repositories {
     maven {
           url = 'c:\\my_local_repos\\maven-repo'
       }
  }

  dependencies {
    classpath 'com.github.arekbee:gradle-R-plugin:0.3'
  }
}
```


### Settings for R project

If your project is in special directory then you need to set src property for r closure:
``` groove
r {
    src = './src/my_project'
}
```

If your project should be run on different R versions than R version specified by system:    
``` groove
r {
    interpreter = 'C:\\R\\R-3.5.2\\bin\\x64\\R.exe'
}
```

### Settings for R package
R package is also a R project, so properties form r closure are also available for rpacakge closure.

If your R package should be generated in special location then you need to set rpackage closure:
``` groove
rpackage {
    dest = './outputs'
}
```

By default, when you build R package integrated with gradle (build.gradle file and .gradle fodler) then you will have gradle file in tar.gz. 
You should add manually .gradle regex selector into .Rbuildignore file or (better) run this command:
```
gradle rPackageUseBuildIgnoreGradle 
```


## Running defined tasks
After setting the R plugin, you can now run commands like ./gradlew version in the root folder of your R project.



## Supported tasks
| Task                       | Description
|----------------------------|---
| rhome  | Show the version of R
| rgetwd | Show the working directory
| rPackageInit | Initialize R package
| rPackageBuild | Builds a package file from package sources
| rPackageBuildVignettes | Builds package vignettes using the same algorithm that R CMD build does. This means including non-Sweave vignettes, using makefiles (if present), and copying over extra files
| rPackageBuildWin | Bundling source package, and then uploading to http://win-builder.r-project.org/
| rPackageCheck | Updates the package documentation, then builds and checks the package locally.
| rPackageCleanVignettes | This uses a fairly rudimentary algorithm where any files in inst/doc with a name that exists in vignettes are removed
| rPackageDocument | Build all documentation for a package
| rPackageLint | The default linters correspond to the style guide at http://r-pkgs.had.co.nz/r.html#style
| rPackageRelease | Updates the package documentation, then builds and checks the package locally.
| rPackageSpellCheck | Runs a spell check on text fields in the package description file, manual pages, and optionally vignettes. Wraps the spelling package.
| rPackageSubmitCran | This uses the new CRAN web-form submission process. After submission, you will receive an email asking you to confirm submission - this is used to check that the package is submitted by the maintainer.
| rPackageTest | Reloads package code then runs all testthat tests
| rPackageTestCoverage | Runs test coverage on your package
| rSessionInfo | Print version information about R, the OS and attached or loaded packages.

## Custom tasks

It is possible to write your own custom tasks. We need to import RCode:
``` groove
import org.arekbee.RCode
```

### Simple custom task:
``` groove
task task_rhome(type:RCode) {
    expression = "R.home()"
}
```


### Running task for different R versions

``` groove
task task_r352(type:RCode) {
    expression = "R.home()"
    interpreter = 'C:\\R\\R-3.5.2\\bin\\x64\\R.exe'
}
task task_r324(type:RCode) {
    expression = "R.home()"
    interpreter = 'C:\\R\\R-3.2.4revised\\bin\\x64\\R.exe'
}
task task_r300(type:RCode) {
    expression = "R.home()"
    interpreter = 'C:\\R\\R-3.0.0\\bin\\x64\\R.exe'
}
```


### Running task from file 
#### By file prop
``` groove
task task_file_r(type:RCode) {
    file = file('test.R')
}
```
#### By source function
``` groove
task task_file_rsource(type:RCode) {
    src = '.'
    expression = 'source(\'test.R\');'
}
```
#### By running RScript
``` groove
task task_file_rscript(type:RCode) {
    interpreter = 'C:\\R\\R-3.5.2\\bin\\x64\\Rscript.exe'
    preArgs = file('test.R')
}
```





## Documentation
Task documentation is available by running:

    ./gradlew help --task [task]


## Contributing

If you wish to build this plugin from source, please see the [contributor instructions](CONTRIBUTING.md).



#  Gradle R plugin

This Gradle plugin provides tasks to build and deploy R scripts and packages.


# How to use

## Configuration

In your R project, add the following plugin to your build.gradle:
``` groove
apply plugin: 'org.arekbee.gradle-R-plugin'
```


The plugin JAR needs to be defined in your build script. It is directly available on Maven Central.

``` groove
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'org.arekbee:gradle-R-plugin:0.1'
  }
}
```

Alternatively, you can download it from GitHub and deploy it to your local repository. If you have local maven repository then you can

``` groove
buildscript {
    repositories {
        maven {
            url = '/path_to_maven_repo/'
        }
    }
    dependencies {
        classpath 'org.arekbee:gradle-R-plugin:0.1'
    }
}
```


## Running defined tasks
After setting the R plugin, you can now run commands like ./gradlew version in the root folder of your R project.


## Supported tasks
| Task                       | Description
|----------------------------|---
| version  | Show the version of R


## Custom tasks



## Documentation
Task documentation is available by running:

    ./gradlew help --task [task]


## Contributing

If you wish to build this plugin from source, please see the [contributor instructions](CONTRIBUTING.md).



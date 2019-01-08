# gradle-R-plugin


# Usage 
``` groove
buildscript {
    repositories {
        maven {
            url = 'D:\\Code\\git\\consuming\\maven-repo'
        }
    }
    dependencies {
        classpath 'org.arekbee:gradle-R-plugin:0.1'
    }
}

apply plugin: 'org.arekbee.gradle-R-plugin'
import org.arekbee.RCode

task restore(type:RCode) {
    group = 'packrat'
    expression = "packrat::restore()"
    onlyIf {
        file('packrat/packrat.lock').exists()
    }
}

task version {
    rcode {
        codeExpression = 'version'
    }
}
```


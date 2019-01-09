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
r {
    interpreter = 'D:\\Programs\\Microsoft\\ROpen\\R-3.5.0\\bin\\x64\\R.exe'
    src = './src1/lintr-master/lintr-master'
}

rpackage {
    dest = './outputs'
}



import org.arekbee.RCode
task task_build(type:RCode) {
    println("Client interpreter:${interpreter}")
    println("client workingDir: ${currentDir}")
    group = 'std'
    expression = "devtools::build()"

}
```


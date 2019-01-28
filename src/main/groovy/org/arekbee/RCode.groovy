package org.arekbee

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction


class RCode extends DefaultTask {

    String  interpreter,src,preArgs
    InputStream standardInput = null
    OutputStream standardOutput = null
    String command = null
    String expression = null
    File file = null

    RCode() {
        super()
        group = 'rbase'
    }

    @TaskAction
    def exec() {
        println("r ${project.r.src} rpackage: ${project.rpackage.src}")

        interpreter = interpreter ?:  project.r.interpreter.get()
        src = src ?: project.r.src.get()
        preArgs = preArgs ?: project.r.preArgs.get()

        def myargs = [interpreter]
        if (preArgs != null)
        {
            myargs.add(preArgs)
        }

        if (command != null){
            myargs.add("CMD")
            myargs.add(command)
        }else if (expression != null){
            myargs.add("-q")
            myargs.add("-e")
            myargs.add(expression)
        }else if (file != null){
            println("Running file: ${file} path: ${file.getAbsolutePath()}")
            myargs.add("-f")
            myargs.add(file.getAbsolutePath())
        }

        project.exec {
            workingDir src
            commandLine myargs
            standardInput = standardInput?: standardInput
            standardOutput = standardOutput?: standardOutput

        }
    }
}

class DevtoolsRCode extends RCode {
    DevtoolsRCode() {
        super()
        group = "devtools"
        onlyIf {
            new File("${project.r.src.get()}").exists()
        }
    }
}

class PackratRCode extends RCode {
    PackratRCode() {
        super()
        group = "packrat"
        onlyIf {
            new File("${project.r.src.get()}/packrat").exists()
        }
    }
}

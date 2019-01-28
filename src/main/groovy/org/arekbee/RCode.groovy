package org.arekbee

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input

class RCode extends DefaultTask {

    @Optional @Input
    String  interpreter,src,preArgs

    @Optional
    InputStream standardInput = null

    @Optional
    OutputStream standardOutput = null

    @Optional
    String command = null

    @Optional @Input
    String expression = null

    @Optional @InputFile
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

        def cmdargs = [interpreter]
        if (preArgs != null)
        {
            cmdargs.add(preArgs)
        }

        if (command != null){
            cmdargs.add("CMD")
            cmdargs.add(command)
        }else if (expression != null){
            cmdargs.add("-q")
            cmdargs.add("-e")
            cmdargs.add(expression)
        }else if (file != null){
            println("Running file: ${file} path: ${file.getAbsolutePath()}")
            cmdargs.add("-f")
            cmdargs.add(file.getAbsolutePath())
        }

        project.exec {
            workingDir src
            commandLine cmdargs
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

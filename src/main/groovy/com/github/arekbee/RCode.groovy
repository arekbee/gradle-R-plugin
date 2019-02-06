package com.github.arekbee

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Input
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class RTask extends DefaultTask {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Optional @Input
    String src
    RTask() {
        super()
        group = 'rbase'
    }

    def checkSrc() {
        logger.debug("Checking src on value $src")
        def dir = new File(src)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }
}

class RCode extends RTask {
    @Optional @Input
    String  interpreter,preArgs

    @Optional @Input
    InputStream standardInput = null

    @Optional @Input
    OutputStream standardOutput = null

    @Optional @Input
    String command = null

    @Optional @Input
    String expression = null

    @Optional @InputFile
    File file = null

    @TaskAction
    def exec() {
        logger.info("r ${project.r.src} rpackage: ${project.rpackage.src}")

        interpreter = interpreter ?:  project.r.interpreter.get()
        src = src ?: project.r.src.get()
        preArgs = preArgs ?: project.r.preArgs.get()

        logger.info("Src is $src")
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
            logger.info("Running file: ${file} path: ${file.getAbsolutePath()}")
            cmdargs.add("-f")
            cmdargs.add(file.getAbsolutePath())
        }

        logger.debug("Before checking src")
        checkSrc()
        logger.debug("After checking src")
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
        group = "devtools"
    }
}

class PackageRCode extends  DevtoolsRCode {
    PackageRCode ()    {
        onlyIf {
            new File("${project.r.src.get()}").exists()
        }
    }
}

class TestedPackageRCode extends  PackageRCode {
    TestedPackageRCode ()    {
        onlyIf {
            new File("${project.r.src.get()}/inst/tests").exists() || new File("${project.r.src.get()}/tests/testthat").exists()
        }
    }
}

class PackratRCode extends RCode {
    PackratRCode() {
        group = "packrat"
        onlyIf {
            new File("${project.r.src.get()}/packrat").exists()
        }
    }
}

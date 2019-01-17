package org.arekbee

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Exec

import org.gradle.api.plugins.BasePlugin
import org.gradle.initialization.LoadProjectsBuildOperationType



class RCode extends DefaultTask {

    String  interpreter,src,preArgs
    InputStream standardInput = null
    OutputStream standardOutput = null
    String command = null
    String expression = null
    File file = null

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



class RPlugin implements Plugin<Project> {
    void apply(Project project) {
        println("in GreetingPlugin.apply : ${project}")

        project.extensions.create("rpackage",RPackagePluginExtension, project)
        project.extensions.create("r",RPluginExtension, project)



        project.task('rhome', type:RCode) {
            group = 'rbase'
            expression = "R.home()"
        }
        project.task('getwd', type:RCode) {
            group = 'rbase'
            expression = "getwd()"
        }

        project.task('restore', type:RCode) {
            group = 'packrat'
            expression = 'packrat::restore()'
            onlyIf {
                new File("${project.r.src.get()}/packrat").exists()
            }
        }
        project.task('test', type:RCode) {
            group = 'devtools'
            expression = 'devtools::test(reporter=testthat::TeamcityReporter)'
            onlyIf {
                new File("${project.r.src.get()}/tests").exists()
            }
        }

    }
}


class RPluginExtension {
    final Property<String> interpreter
    final Property<String> preArgs
    final Property<String> src



    RPluginExtension(Project project) {
        interpreter = project.getObjects().property(String)
        interpreter.set('R')
        preArgs = project.getObjects().property(String)
        preArgs.set('--no-save')

        src = project.getObjects().property(String)
        src.set('.')
    }

}

class RPackagePluginExtension extends RPluginExtension {

    final Property<String> dest

    RPackagePluginExtension(Project project) {
        super(project)
        dest = project.objects.property(String)
        dest.set('.')
    }
}




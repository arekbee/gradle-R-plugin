package org.arekbee

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Exec

import org.gradle.api.plugins.BasePlugin
import org.gradle.initialization.LoadProjectsBuildOperationType


class RPluginExtension {
    final Property<String> interpreter
    final Property<String> preArgs
    final Property<String> src

    RPluginExtension(Project project) {
        interpreter = project.objects.property(String)
        interpreter.set('R')
        preArgs = project.objects.property(String)
        preArgs.set('--no-save')

        src = project.objects.property(String)
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


/*
class RExec extends Exec {
    RExec() {
        println("In constructor RExec")
        workingDir "${project.projectDir}"

        if (Os.isFamily(Os.FAMILY_WINDOWS)){
            executable 'cmd'
            args = ['/c', 'D:\\Programs\\Microsoft\\ROpen\\R-3.5.0\\bin\\x64\\R.exe --no-save -e']
        }else{

            executable 'R'
            args = ['--no-save -e']
        }
        println("out constructor RExec")
    }
}
*/


class RCode extends DefaultTask {

        String  interpreter
        String  currentDir
        String preArgs

        RCode() {
            super()

            interpreter = project.r.interpreter.getOrElse('R')
            currentDir = project.r.src.getOrElse('.')
            preArgs = project.r.preArgs.getOrNull()

            println("RCode: project.rpackage.src.get(): ${project.rpackage.src.get()}")
            println("RCode: project.r.get(): ${project.r.interpreter.get()}")
            println("RCode project.r.src is ${project.r.src.get()}")
        }

        InputStream standardInput = null
        OutputStream standardOutput = null
        String command = null
        String expression = null
        File file = null

        @TaskAction
        def exec() {
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
                myargs.add("-f")
                myargs.add(file.getAbsolutePath())
            }
            project.exec {
                workingDir currentDir
                commandLine myargs
                if (standardInput != null) {
                    standardInput = standardInput
                }
                if (standardOutput != null) {
                    standardOutput = standardOutput
                }
            }
        }
    }

class GreetingPlugin implements Plugin<Project> {
    void apply(Project project) {
        println("in GreetingPlugin.apply : ${project}")

        def rpackage = project.extensions.create("rpackage",RPackagePluginExtension, project)
        def r = project.extensions.create("r",RPluginExtension, project)

        println("project.rpackage.src is: ${project.rpackage.src}")
        println("project.r.interpreter is: ${project.r.interpreter}")
        println("ASD")
        println("rpackage: $rpackage.src")
        println("r: $r.src")



        project.tasks.create('version', type:RCode) {
            println('in version')
            println("interpreter:${interpreter}")
            expression = "version"

        }

        project.task('build', type:RCode) {
            println('in load_all')
            println("interpreter:${interpreter}")
            group = 'devtools'
            expression = "devtools::build()"

        }


        project.task('restore', type:RCode) {
            println('in restore')
            group = 'packrat'
            onlyIf {
                file("${project.rpackage.src.get()}/packrat").exists()
            }
            expression = 'packrat::restore()'
        }


    }
}

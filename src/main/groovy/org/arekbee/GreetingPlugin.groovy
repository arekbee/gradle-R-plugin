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



class RPluginExtension {
    final Property<String> codeExpression
    final Property<String> codeFilePath

    RPluginExtension(Project project) {
        println("in RPluginExtension")
        codeExpression = project.objects.property(String)
        codeExpression.set('version')
        codeFilePath = project.objects.property(String)
        codeFilePath.set('')
        println("out RPluginExtension")
    }

}


class RPackagePluginExtension {
    final Property<String> src
    final Property<String> desc

    RPackagePluginExtension(Project project) {
        println("in RPackagePluginExtension")
        src = project.objects.property(String)
        src.set('.')
        src = project.objects.property(String)
        src.set('.')
        println("out RPackagePluginExtension")
    }
}



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

class RCode extends DefaultTask {
        File workingDir = project.rootDir
        InputStream standardInput = null
        OutputStream standardOutput = null
        String command = null
        String expression = null
        File file = null

        @TaskAction
        def exec() {
            def myargs = ['R']
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
                workingDir workingDir
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

class DevtoolsRExec extends RExec{
    DevtoolsRExec() {
        group = 'devtools'
        println("in DevtoolsRExec")
    }
}
class PackratRExec extends RExec{
    PackratRExec() {
        group = 'packrat'
        println("in PackratRExec")
    }
}



class GreetingPlugin implements Plugin<Project> {
    void apply(Project project) {
        println("in GreetingPlugin.apply ")
        def extensionrpackage = project.extensions.create("rpackage",RPackagePluginExtension, project)
        def extensionrcode = project.extensions.create("rcode",RPluginExtension, project)

        println("in GreetingPlugin.apply after extension")
        project.tasks.create('hello') {
            println("in hello")
            doLast{
                println("in hello doLast")
                def codeExpression = extensionrcode.codeExpression
                println(codeExpression)
            }
        }


        project.task('load_all', type:DevtoolsRExec) {
            println("in load_all")
            doFirst {
                println("in load_all doFirst")
                args += "devtools::load_all()"
            }
        }
        project.task('code', type:RExec) {
            println("in code")
            doFirst {
                println("in code doFirst")
                def codeExpression = extensionrcode.codeExpression
                args += codeExpression.get()
                println(codeExpression.get())
            }
        }


    }


}
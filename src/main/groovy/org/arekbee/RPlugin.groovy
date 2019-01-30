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
import org.arekbee.RCode
import org.arekbee.DevtoolsRCode
import org.arekbee.PackratRCode


class RPlugin implements Plugin<Project> {
    void apply(Project project) {
        println("in RPlugin.apply : ${project}")

        project.extensions.create("rpackage",RPackagePluginExtension, project)
        project.extensions.create("r",RPluginExtension, project)

        project.task('rhome', type:RCode) {
            expression = "R.home()"
        }

        project.task('rhome', type:RCode) {
            expression = "R.home()"
        }
        project.task('rgetwd', type:RCode) {
            expression = "getwd()"
        }

        project.task('rSessionInfo', type:RCode) {
            description = 'Print version information about R, the OS and attached or loaded packages.'
            expression = 'sessioninfo::sessionInfo()'
        }

        project.task('rPackratRestore', type:PackratRCode) {
            expression = 'packrat::restore()'
        }
        project.task('rPackratStatus', type:PackratRCode) {
            expression = 'packrat::status()'
        }
        
        project.task('rPackratInit', type:PackratRCode) {
            expression = 'packrat::init()'
        }
        
        project.task('rPackratSnapshot', type:PackratRCode) {
            expression = 'packrat::.snapshotImpl(snapshot.sources=FALSE,\'.\')'
        }
        
        project.task('rPackageCleanVignettes', type:DevtoolsRCode) {
            description = 'This uses a fairly rudimentary algorithm where any files in inst/doc with a name that exists in vignettes are removed'
            expression = 'devtools::clean_vignettes()'
        }
        project.task('rPackageInit', type:DevtoolsRCode) {
            description = 'Initialize R package'
            expression = "devtools::create('.'); devtools::use_readme_md(); devtools::use_testthat()"
        }
        
        project.task('rPackageBuild', type:DevtoolsRCode) {
            description = 'Builds a package file from package sources'
            expression = 'devtools::build(vignettes=FALSE,args=\'--keep-empty-dirs\',path=\'build\')'
        }

        project.task('rPackageBuildWin', type:DevtoolsRCode) {
            description = 'Bundling source package, and then uploading to http://win-builder.r-project.org/'
            expression = 'devtools::build_win()'
        }

        project.task('rPackageBuildVignettes', type:DevtoolsRCode) {
            description = 'Builds package vignettes using the same algorithm that R CMD build does. This means including non-Sweave vignettes, using makefiles (if present), and copying over extra files'
            expression = 'devtools::build_vignettes()'
        }

        project.task('rPackageDocument', type:DevtoolsRCode) {
            description = 'Build all documentation for a package'
            expression = 'devtools::document()'

        }

        project.task('rPackageTest', type:DevtoolsRCode) {
            description = 'Reloads package code then runs all testthat tests'
            expression = 'devtools::test(reporter=testthat::TeamcityReporter)'
            onlyIf {
                new File("${project.r.src.get()}/inst/tests").exists() && new File("${project.r.src.get()}/tests/testthat").exists()
            }

        }
        project.task('rPackageTestCoverage', type:DevtoolsRCode) {
            description = 'Runs test coverage on your package'
            expression = 'devtools::test_coverage()'
        }
        project.task('rPackageCheck', type:DevtoolsRCode) {
            description = 'Updates the package documentation, then builds and checks the package locally.'
            expression = 'devtools::check()'

        }
        project.task('rPackageRelease', type:DevtoolsRCode) {
            description = 'Updates the package documentation, then builds and checks the package locally.'
            expression = 'devtools::release()'
        }

        project.task('rPackageSubmitCran', type:DevtoolsRCode) {
            description = 'This uses the new CRAN web-form submission process. After submission, you will receive an email asking you to confirm submission\n'+
                    '- this is used to check that the package is submitted by the maintainer.'
            expression = 'devtools::submit_cran()'
        }

        project.task('rPackageSpellCheck', type:DevtoolsRCode) {
            description = 'Runs a spell check on text fields in the package description file, manual pages, and optionally\n' +
                    'vignettes. Wraps the spelling package.'
            expression = 'devtools::spell_check()'
        }

        project.task('rPackageLint', type:DevtoolsRCode) {
            description = 'The default linters correspond to the style guide at http://r-pkgs.had.co.nz/r.html#style,\n' +
                    'however it is possible to override any or all of them using the linters paramete'
            expression = 'devtools::lint()'
        }

        project.task('rPackageUseBuildIgnoreGradle', type:DevtoolsRCode) {
            description = 'Adds gradle files into .Rbuildignore file'
            expression = 'devtools::use_build_ignore(c(\'.gradle\',\'gradle*\',\'build.cmd\',\'build/\',\'tests/\',\'packrat/\',\'gradle.properties\'),escape=FALSE)'
        }

        
        project.task('rPackageVersion', type:DevtoolsRCode) {
            description = 'Sets version of R package'
            def versionRelease = 0.0.1
            expression = "x=read.dcf('DESCRIPTION');x[,'Version']='" + versionRelease + "';write.dcf(x,file='DESCRIPTION')"
        }
        
         project.task('rRepoWritePackagesFile',  type:RCode) {
             def destLocalRepoPath = ""
             expression = "tools::write_PACKAGES(\'$destLocalRepoPath\',type=\'source\',verbose=TRUE,subdirs=FALSE,addFiles=TRUE)"
        }
        
        
         project.task('rRepoCopy',  type:Copy) {
            def distPath = ""
            def destLocalRepoPath = ""
            println "Copy files from $distPath to $destLocalRepoPath repo"
            from distPath
            include '*.tar.gz'
            into destLocalRepoPath
            fileMode 0755
        }
        
        
         project.task('rRepoArchive') {
            println "It should archive old packages"
         }
        
        
        project.task('rPackageBuildUnzip') {
            def buildDir = ""
            def unzippedDir = ""
            FileTree tree = fileTree(dir: "$buildDir")
            tree.include "*.tar.gz"
            tree.each { File file -> println "tar.gz file:  $file"}
            if (!tree.isEmpty()) {
                def fileToUnzip  = tree.getAt(-1)
                println "unzip file $fileToUnzip into $unzippedDir"
                copy {
                    from tarTree(resources.gzip("$fileToUnzip"))
                    into unzippedDir
                }
            }else {
                println "There is not file in $buildDir for pattern $prefixModelName"
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
    final Property<String> name     // name of package
    final Property<String> unzipDir // for unzipping r package after build
    final Property<String> version // version of package
    
    
    RPackagePluginExtension(Project project) {
        super(project)
        dest = project.objects.property(String)
        dest.set('.')
    }
}

class RRepositoryPluginExtension {
    final Property<String> local //location of local repository
        
    RRepositoryPluginExtension(Project project) {
    
    }
}




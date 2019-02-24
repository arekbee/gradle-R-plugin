package com.github.arekbee

import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.DefaultTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileTree
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.AbstractExecTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.Exec

import org.gradle.api.plugins.BasePlugin
import org.gradle.initialization.LoadProjectsBuildOperationType
import com.github.arekbee.RCode
import com.github.arekbee.DevtoolsRCode
import com.github.arekbee.PackratRCode
import org.gradle.api.tasks.Copy
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.nio.file.Path
import java.nio.file.Paths

class RPlugin implements Plugin<Project> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    void apply(Project project) {
        logger.debug("in RPlugin.apply for: ${project}")

        project.extensions.create("rpackage",RPackagePluginExtension, project)
        project.extensions.create("r",RPluginExtension, project)
        project.extensions.create("rrepo", RRepositoryPluginExtension, project)


        project.task('rgetwd', type: RCode) {
            expression = "getwd()"
        }

        project.task('rhome', type: RCode) {
            expression = "R.home()"
        }

        project.task('rSessionInfo', type: RCode) {
            description = 'Print version information about R, the OS and attached or loaded packages.'
            expression = 'sessionInfo()'
        }

        project.task('rInstallPackages', type: RCode) {
            def packagesList = "'DT','devtools','sessioninfo','covr','testthat','packrat','rversions','hunspell','xtable', 'roxygen2', 'lintr'"
            description = 'Installs R packages which are required for gradle-R-plugin like $packagesList'
            expression = "install.packages(c($packagesList))"
        }

        project.task('rPackratRestore', type: PackratRCode) {
            expression = 'packrat::restore()'
        }
        project.task('rPackratStatus', type: PackratRCode) {
            expression = 'packrat::status()'
        }

        project.task('rPackratInit', type: RCode) {
            group = 'packrat'
            expression = 'packrat::init()'
        }

        project.task('rPackratSnapshot', type: PackratRCode) {
            expression = 'packrat::.snapshotImpl(snapshot.sources=FALSE,\'.\')'
        }

        project.task('rPackageCleanVignettes', type: PackageRCode) {
            description = 'This uses a fairly rudimentary algorithm where any files in inst/doc with a name that exists in vignettes are removed'
            expression = 'devtools::clean_vignettes()'
        }

        def rPackageDocument = project.task('rPackageDocument', type: PackageRCode) {
            description = 'Build all documentation for a package'
            expression = 'devtools::document()'
        }

        def rPackageTest = project.task('rPackageTest', type: TestedPackageRCode) {
            description = 'Reloads package code then runs all testthat tests'
            expression = 'devtools::test(reporter=testthat::TeamcityReporter)'
        }

        def rPackageCheck = project.task('rPackageCheck', type: PackageRCode) {
            description = 'Updates the package documentation, then builds and checks the package locally.'
            expression = 'devtools::check()'
        }

        project.task('rPackageBuildVignettes', type: PackageRCode) {
            description = 'Builds package vignettes using the same algorithm that R CMD build does. This means including non-Sweave vignettes, using makefiles (if present), and copying over extra files'
            expression = 'devtools::build_vignettes()'
        }

        project.task('rPackageRelease', type: PackageRCode) {
            description = 'Updates the package documentation, then builds and checks the package locally.'
            expression = 'devtools::release()'
        }

        project.task('rPackageSubmitCran', type: PackageRCode) {
            description = 'This uses the new CRAN web-form submission process. After submission, you will receive an email asking you to confirm submission\n' +
                    '- this is used to check that the package is submitted by the maintainer.'
            expression = 'devtools::submit_cran()'
        }

        project.task('rPackageSpellcheck', type: DevtoolsRCode) {
            description = 'Runs a spell check on text fields in the package description file, manual pages, and optionally\n' +
                    'vignettes. Wraps the spelling package.'
            expression = 'devtools::spell_check()'
        }

        project.task('rPackageUseBuildIgnoreGradle', type: DevtoolsRCode) {
            description = 'Adds gradle files into .Rbuildignore file'
            expression = 'devtools::use_build_ignore(c(\'.gradle\',\'gradle*\',\'build.cmd\',\'build/\',\'tests/\',\'packrat/lib*\',\'packrat/src*\',\'gradle.properties\',\'.*report.html$\',\'README.md\'),escape=FALSE)'
        }

        def rPackageDest = project.task('rPackageDest', type:RTask)
        def rPackageInit = project.task('rPackageInit', type:DevtoolsRCode)
        def rPackageTestCoverage = project.task('rPackageTestCoverage', type: TestedPackageRCode)
        def rPackageLint = project.task('rPackageLint', type: PackageRCode)
        def rPackageBuild = project.task('rPackageBuild', type: PackageRCode)
        def rPackageBuildWin = project.task('rPackageBuildWin', type: PackageRCode)
        def rPackageVersion  = project.task('rPackageVersion', type: PackageRCode)
        def rRepoWritePackagesFile = project.task('rRepoWritePackagesFile', type: RCode)
        def rPackageBuildUnzip = project.task('rPackageBuildUnzip', type:Copy )

        project.afterEvaluate {
            rPackageDest.doFirst {
                description ="Creates destination/build folder. By default it is '..'"
                def src = project.r.src.get()
                def dest = project.rpackage.dest.get()
                def destFolder = new File(src, dest)
                if (!destFolder.exists()){
                    logger.warn("Destination/build folder ${destFolder.canonicalPath} does not exists. We will creat it.")
                    destFolder.mkdirs()
                }
            }

            rPackageInit.doFirst {
                description = 'Initialize R package in empty directory'
                def name = project.rpackage.name.get()
                logger.info("Package name is $name")
                expression = "devtools::setup(description=list(Package=\'$name\'));devtools::use_readme_md();devtools::use_testthat();devtools::use_vignette(\'$name\')"
            }

            rPackageTestCoverage.doFirst {
                description = 'Runs test coverage on your package'
                def dest = project.rpackage.dest.get()
                expression = "covr::report(x=covr::package_coverage(),file=normalizePath(file.path(\'$dest\','code-cov-report.html'),winslash=\'/\'),browse=FALSE)"
            }.dependsOn(rPackageDest)

            rPackageLint.doFirst {
                description = 'The default linters correspond to the style guide at http://r-pkgs.had.co.nz/r.html#style,\n' +
                        'however it is possible to override any or all of them using the linters paramete'
                def lintTypes = project.rpackage.lintTypes.get()
                def dest = project.rpackage.dest.get()
                expression = "print(xtable::xtable(subset(as.data.frame(devtools::lint()),type%in%c($lintTypes))), type=\'html\',file=normalizePath(file.path(\'$dest\',\'lint-report.html\'),winslash=\'/\'))"
            }.dependsOn(rPackageDest)

            rPackageBuild.doFirst {
                description = 'Builds a package file from package sources'
                def dest = project.rpackage.dest.get()
                expression = "devtools::build(vignettes=FALSE,args=\'--keep-empty-dirs\',path=\'$dest\')"
            }.dependsOn(rPackageDest )

            rPackageBuildWin.doFirst {
                description = 'Bundling source package, and then uploading to http://win-builder.r-project.org/'
                def dest = project.rpackage.dest.get()
                expression = "devtools::build_win(vignettes=FALSE,args=\'--keep-empty-dirs\',path=\'$dest\')"
            }.dependsOn(rPackageDest )

            rPackageVersion.doFirst {
                description = 'Sets version of R package'
                def versionRelease = project.rpackage.version.get()
                expression = "x=read.dcf('DESCRIPTION');x[,'Version']='" + versionRelease + "';write.dcf(x,file='DESCRIPTION')"
                println "expresion for  rPackageVersion is %expression"
            }

            rRepoWritePackagesFile.doFirst{
                def destLocalRepoPath = project.rrepo.local.get()
                def src = project.r.src.get()
                def destFolder = new File(src, destLocalRepoPath)
                if (!destFolder.exists()){
                    logger.warn("Local Repository dir ${destFolder.canonicalPath} does not exists. We will creat it.")
                    destFolder.mkdirs()
                }
                expression = "tools::write_PACKAGES(\'$destLocalRepoPath\',type=\'source\',verbose=TRUE,subdirs=FALSE,addFiles=TRUE)"
            }

            rPackageBuildUnzip.configure {
                    group = 'rbase'
                    def src = project.r.src.get()
                    def dest = project.rpackage.dest.get()
                    def unzipDir = project.rpackage.unzipDir.get()

                    def destFolder = new File(src, dest)
                    if (!destFolder.exists()) {
                        logger.warn("Dest dir ${destFolder.canonicalPath} does not exists. Please run ${rPackageDest.name} task")
                    } else {
                        def p = Paths.get(destFolder.canonicalPath, unzipDir)
                        def unzipDirFromDesc = new File(p.toString())
                        if (!unzipDirFromDesc.exists()) {
                            logger.warn("Unziped directory under ${unzipDirFromDesc.canonicalPath} does not exists. We will creat it.")
                            unzipDirFromDesc.mkdirs()
                        }

                        FileTree tree = project.fileTree(dir: destFolder.canonicalPath)
                        tree.include "*.tar.gz"
                        tree.each { File file -> println("tar.gz file:  $file") }
                        if (!tree.isEmpty()) {
                            def fileToUnzip = tree.getAt(-1)
                            logger.info("unzip file $fileToUnzip into $unzipDirFromDesc.canonicalPath")
                            from project.tarTree(fileToUnzip)
                            into unzipDirFromDesc.canonicalPath
                        } else {
                            logger.warn "There are not files in $tree"
                        }
                    }
            }

            /*
         project.task('rRepoCopy',  type:Copy) {
            def distPath = ""
            def destLocalRepoPath = ""
            logger.debug "Copy files from $distPath to $destLocalRepoPath repo"
            from distPath
            include '*.tar.gz'
            into destLocalRepoPath
            fileMode 0755
        }

        
         project.task('rRepoArchive') {
            logger.debug "It should archive old packages"
         }

        

        */

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
    final Property<String> lintTypes

    RPackagePluginExtension(Project project) {
        super(project)
        dest = project.objects.property(String)
        dest.set('..')

        unzipDir = project.objects.property(String)
        unzipDir.set('./unzip')

        name = project.objects.property(String)
        name.set(new File(System.getProperty("user.dir")).getName())

        version = project.objects.property(String)
        version.set('0.0.1')

        lintTypes = project.objects.property(String)
        lintTypes.set("'warning','error','style'")
    }
}

class RRepositoryPluginExtension {
    final Property<String> local //location of local repository
        
    RRepositoryPluginExtension(Project project) {
        local = project.objects.property(String)
        local.set('../Repository')
    }
}




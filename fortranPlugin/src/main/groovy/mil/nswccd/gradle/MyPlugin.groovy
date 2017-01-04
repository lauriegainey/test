package mil.nswccd.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask

class MyPlugin implements Plugin<Project> {
	
    void apply(Project project) {
        project.with { 
            tasks.create(name: 'compileModel', type: MyCompileTask) {
                description = "Compiles files into outputDir"                
                srcDir = "${project.projectDir}/src/model"
                includeFiles = ['**/*.f','**/*.for','**/*.F','**/*.FOR','**/*.f90','**/*.F90','**/*.c','**/*.C']
                excludeFiles = ['']
                outputDir = file("${project.projectDir}/objs")
                inclDir = 'incls'
                fortranCompileFlags = ['-cpp', '-fno-backslash', '-falign-commons', '-fno-automatic', 
                                       '-funderscoring', '-fno-second-underscore']
                cCompileFlags = ['-cpp', '-D_HPUX_SOURCE', '-DBYTE_RECORDS', '-DLINUX', '-D_BSD_SOURCE', 
                                 '-D_XOPEN_SOURCE', '-DUNIX',
                                 '-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE=1']
                fortranCompiler = 'gfortran'
                cCompiler = 'gcc'
            }
 
            tasks.create(name: 'linkModel', type: MyLinkTask) {
                dependsOn compileModel
                description = "Links object files into executable"                
                linkFlags = ['-g','-O2']
                linker = 'c++'
                exeName = "${project.name}.exe"
                objDir = compileModel.outputDir
                binPath = file("${project.projectDir}/bin")

            }

            def testSrcDir = "${project.projectDir}/src/test"
            tasks.create(name: 'compileTest', type: MyCompileTask) {
                dependsOn linkModel
                description = "Compiles files into outputDir"                
                srcDir = "${project.projectDir}/src/test"
                includeFiles = ['*.f', '*.c']
                excludeFiles = ['']
                outputDir = file("${compileModel.outputDir}/test")
                outputDir = file("${project.projectDir}/objsTest")
                inclDir = 'incls'
                fortranCompileFlags = ['-cpp', '-fno-backslash', '-falign-commons', '-fno-automatic', 
                                       '-funderscoring', '-fno-second-underscore']
                cCompileFlags = ['-cpp', '-D_HPUX_SOURCE', '-DBYTE_RECORDS', '-DLINUX', '-D_BSD_SOURCE', 
                                 '-D_XOPEN_SOURCE', '-DUNIX',
                                 '-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE=1']
                fortranCompiler = 'gfortran'
                cCompiler = 'gcc'
            }

            tasks.create(name: 'linkTest', type: MyLinkTask) {
                dependsOn compileTest
                description = "Links object files into test executable"                
                linkFlags = ['-g','-O2']
                linker = 'c++'
                binPath = file("${project.projectDir}/bin")
                objDir = file(compileTest.outputDir)
                exeName = "test${project.name}.exe"
            }
            tasks.create(name: 'runTest', type: MyRunTestTask) {
                dependsOn linkTest
                description = "Run Tests against STD Testcases"                
                stdPath = new File("${project.projectDir}/testcases")
                // TODO: I think I need an 'afterEvaluate' here
                // because it is not picking up the new exeName and binPath
                modelFullPathName = file(linkModel.exeName )
                testFullPathName = file("${linkTest.binPath}/${linkTest.exeName}")

                tolerance = 2.0
                testRunPath =  new File(project.projectDir, "tmp")
            }
            tasks.create(name: 'saveTestResults', type: MySaveTestTask){
            	dependsOn runTest
	            saveTestResults.buildHistoryFile=runTest.buildHistoryFile
	            description "Save test results to the remote repository"
            }
            tasks.create(name: 'buildTask', type: DefaultTask) {
                dependsOn runTest
                description "Main build task"
            }
        }
    }
}
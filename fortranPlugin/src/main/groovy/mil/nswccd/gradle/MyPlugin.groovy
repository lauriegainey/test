package mil.nswccd.gradle
import org.gradle.internal.os.OperatingSystem;

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask

class MyPlugin implements Plugin<Project> {
	
    void apply(Project project) {
        project.with { 
            tasks.create(name: 'compileSingleFile', type: MyCompileTask) {
                description = "Compiles a single f90 file so that it's *.mod file can be used for others"
                srcDir = "${project.projectDir}/src/model"
                includeFiles = ['**/test.f90']
                excludeFiles = ['']
                outputDir = file("${project.projectDir}/objs")
                inclDir = 'incls'
                STAPLE = 'true'
                x64 = 'false'
                fortranCompileFlags = ['-cpp', '-fno-backslash', '-falign-commons', '-fno-automatic', 
                                       '-funderscoring', '-fno-second-underscore']
                cCompileFlags = ['-cpp', '-D_HPUX_SOURCE', '-DBYTE_RECORDS', '-DLINUX', '-D_BSD_SOURCE', 
                                 '-D_XOPEN_SOURCE', '-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE=1']
                if (OperatingSystem.current().isWindows()) {
                   cCompileFlags = cCompileFlags + "-D_WIN32"
                } else {
                   cCompileFlags = cCompileFlags + "-D_UNIX"
                }
                fortranCompiler = 'gfortran'
                cCompiler = 'gcc'
            }
            tasks.create(name: 'compileModel', type: MyCompileTask) {
                description = "Compiles files into outputDir"                
                srcDir = "${project.projectDir}/src/model"
                includeFiles = ['**/*.f','**/*.for','**/*.F','**/*.FOR','**/*.f90','**/*.F90','**/*.c','**/*.C','**/*.cpp']
                excludeFiles = ['']
                outputDir = file("${project.projectDir}/objs")
                inclDir = 'incls'
                STAPLE = 'true'
                x64 = 'false'
                fortranCompileFlags = ['-cpp', '-fno-backslash', '-falign-commons', '-fno-automatic', 
                                       '-funderscoring', '-fno-second-underscore']
                cCompileFlags = ['-cpp', '-D_HPUX_SOURCE', '-DBYTE_RECORDS', '-DLINUX', '-D_BSD_SOURCE', 
                                 '-D_XOPEN_SOURCE', '-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE=1']
                if (OperatingSystem.current().isWindows()) {
                   cCompileFlags = cCompileFlags + "-D_WIN32"
                } else {
                   cCompileFlags = cCompileFlags + "-D_UNIX"
                }
                fortranCompiler = 'gfortran'
                cCompiler = 'gcc'
            }
 
            tasks.create(name: 'linkModel', type: MyLinkTask) {
                dependsOn compileModel
                description = "Links object files into executable"                
                linkFlags = ['-g','-O2']
                linker = 'c++'
                x64 = 'false'
                exeName = "${project.name}.exe"
                objDir = compileModel.outputDir
                binPath = file("${project.projectDir}/bin")

            }

            tasks.create(name: 'compileTest', type: MyCompileTask) {
                dependsOn linkModel
                description = "Compiles files into outputDir"                
                srcDir = "${project.projectDir}/src/test"
                includeFiles = ['*.f', '*.c','*.cpp']
                excludeFiles = ['']
                outputDir = file("${compileModel.outputDir}/test")
                outputDir = file("${project.projectDir}/objsTest")
                inclDir = 'incls'
                STAPLE = 'false'
                x64 = 'false'
                fortranCompileFlags = ['-cpp', '-fno-backslash', '-falign-commons', '-fno-automatic', 
                                       '-funderscoring', '-fno-second-underscore']
                cCompileFlags = ['-cpp', '-D_GNU_SOURCE', '-DBYTE_RECORDS', '-D_BSD_SOURCE', 
                                 '-D_XOPEN_SOURCE', '-D_FILE_OFFSET_BITS=64','-D_LARGEFILE64_SOURCE=1']
                if (OperatingSystem.current().isWindows()) {
                   cCompileFlags = cCompileFlags + "-D_WIN32"
                } else {
                   cCompileFlags = cCompileFlags + "-D_UNIX"
                }
                fortranCompiler = 'gfortran'
                cCompiler = 'gcc'
            }

            tasks.create(name: 'linkTest', type: MyLinkTask) {
                dependsOn compileTest
                description = "Links object files into test executable"                
                linkFlags = ['-g','-O2']
                linker = 'c++'
                x64 = 'false'
                binPath = file("${project.projectDir}/bin")
                objDir = file(compileTest.outputDir)
                exeName = "test${project.name}.exe"
            }
            tasks.create(name: 'runTest', type: MyRunTestTask) {
                dependsOn linkTest
                description = "Run Tests against STD Testcases"                
                stdPath = new File("${project.projectDir}/testcases")
                modelFullPathName = file(linkModel.exeName )
                testFullPathName = file("${linkTest.binPath}/${linkTest.exeName}")

                tolerance = 2.0
                verbose = false
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

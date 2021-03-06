package mil.nswccd.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Input
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.TaskAction

class MyCompileTask extends DefaultTask {
    String srcDir
    List<String> includeFiles
    List<String> excludeFiles
    @Input List<String> fortranCompileFlags
    @Input List<String> cCompileFlags
    @Input String fortranCompiler
    @Input String cCompiler
    @Input String inclDir
    @Input String STAPLE
    @Input String x64
    @InputFiles 
       def getFiles() {
          return project.fileTree(dir: srcDir, includes: includeFiles, excludes: excludeFiles)
       }
    @OutputDirectory File outputDir

    @TaskAction
    def compile(IncrementalTaskInputs inputs) {
      inputs.outOfDate {
          String baseName = it.file.name.split("\\.")[0]
          String fileExt = it.file.name.split("\\.")[1]
          String pathName = it.file.getParentFile()
          def compiler = fortranCompiler
          def  compileFlags = fortranCompileFlags
          if (fileExt.equals('c') || fileExt.equals('cpp')) {
            compiler = cCompiler
            compileFlags = cCompileFlags
          } 
          project.exec {
            workingDir = pathName 
            executable = compiler
            def argsList = ["-o", "${outputDir}/${baseName}.o", "-c", "${pathName}/${baseName}.${fileExt}", 
                        "-I${project.projectDir}/${inclDir}"]
            if (compiler == 'gfortran') {
              argsList = argsList +  "-DGFORTRAN"
           }
            if (STAPLE == 'true') {
                argsList = argsList + "-DSTAPLE_BUILD"
            }
            if (x64 == 'true') {
                argsList = argsList + "-m64"
            }
            argsList = argsList + compileFlags
            args = argsList 
          }
      }
      inputs.removed {
          String baseName = it.file.name.split("\\.")[0]
          def objFile = project.file("${outputDir}/${baseName}.o")
          if (objFile.exists()) {
            objFile.delete()
          }
      }
    }
}
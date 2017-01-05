package mil.nswccd.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class MyLinkTask extends DefaultTask {
    @Input String linker
    @Input String x64
    @Input List<String> linkFlags
    @Input String exeName
    @InputDirectory objDir 
    @InputFiles 
       def getFiles() {
          return project.fileTree(dir: objDir)
       }
    @OutputDirectory File binPath


    @TaskAction
    def link(IncrementalTaskInputs inputs) {
        project.exec {
          workingDir binPath
          executable = linker
          def argsList = ["-o", exeName, "-DGFORTRAN", "-DSTAPLE_BUILD"]
          if (x64 == 'true') {
              argsList = argsList + "-m64"
          }
          argsList = argsList + getFiles() + linkFlags
          args = argsList 
        }
    }
}

package mil.nswccd.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class MyRunTestTask extends DefaultTask {
    @InputDirectory stdPath
    @InputFile modelFullPathName
    @InputFile testFullPathName
    @Input String tolerance
    @Input String verbose
    @OutputDirectory File testRunPath

        File buildHistoryFile = new File("${project.rootDir}/build-history/build-results-"+getDate()+".txt")
	public String getDate() {
	    return new Date().format('yyyy-MM-dd-HHmmss')
	}
	
    @TaskAction
    public void runTests() {
    	new ByteArrayOutputStream().withStream { os ->
	         project.exec {
	              environment "STD_DIR", stdPath
	              environment "STD_EXE", modelFullPathName.getAbsolutePath()
	              workingDir testRunPath
	              executable = testFullPathName.getAbsolutePath() 
	              def argsList = [tolerance, "REPO",verbose ]
	              args = argsList
	              standardOutput = os
	         }
                 buildHistoryFile.getParentFile().mkdirs()
	         buildHistoryFile.createNewFile()
         	 buildHistoryFile.write os.toString()
             println os.toString()
        }
    }
}

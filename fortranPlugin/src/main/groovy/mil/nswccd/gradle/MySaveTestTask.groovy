package mil.nswccd.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import groovy.swing.SwingBuilder
import java.net.URLEncoder

class MySaveTestTask extends DefaultTask {
	File buildHistoryFile
	
	@TaskAction
    public void saveTests() {
    	def console = System.console()
        def username = ""
        def password = ""
        if(console == null) {
	        new SwingBuilder().edt {
	            dialog(modal: true, title: 'Enter GitHub Credentials', alwaysOnTop: true, resizable: false, locationRelativeTo: null, pack: true, show: true) {
	                vbox { // Put everything below each other
	                    label(text: "Username:")
	                    def input1 = textField()
	                    label(text: "Password:")
	                    def input2 = passwordField()
	                    button(defaultButton: true, text: 'OK', actionPerformed: {
	                        def encoder=new URLEncoder()
	                        username = input1.text
	                        password = encoder.encode(""+input2.password, "UTF-8")
	                        dispose()
	                    })
	                }
	            }
	        }
	    } else {
	        username = console.readLine('> Please enter your github username: ')
        	password = console.readPassword('> Please enter your github password: ')
	    }
        def remoteRepo = "https://"+username+":"+password+"@github.com/dquelch/CASSGRAB3D.git"
    	def result = project.exec {
            executable = 'git'
            args = ['add',buildHistoryFile.getAbsolutePath()]
        }
        result = project.exec {
        	def message="Saving build test results to repository."
            executable = 'git'
            args = ['commit','-m',message]
        }
    	result = project.exec {
            executable = 'git'
            args = ['push',remoteRepo]
        }
    }
}
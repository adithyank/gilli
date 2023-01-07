package gilli.process

import gilli.internal.main.Gilli
import gilli.util.ConsoleColor
import gilli.util.dataframe.DataFrame
import groovy.transform.stc.ClosureParams
import groovy.transform.stc.SimpleType
import groovy.util.logging.Log4j2

class OSProcess
{
	static ExitStatus exec(@DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		exec([], false, closure)
	}
	
	static ExitStatus execbg(@DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		exec([], true, closure)
	}

	static ExitStatus exec(String cmd, @DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		return exec(cmd.split(' ').toList(), false, closure)
	}

	static ExitStatus execbg(String cmd, @DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		return exec(cmd.split(' ').toList(), true, closure)
	}

	static ExitStatus exec(List<String> cmds, String dirName)
	{
		exec(cmds, false, dirName)
	}

	static ExitStatus execbg(List<String> cmds, String dirName)
	{
		exec(cmds, true, dirName)
	}

	static ExitStatus exec(List<String> cmds, boolean background, String dirName)
	{
		exec(cmds, background) {

			if (dirName)
				dir dirName
		}
	}

	static ExitStatus exec(List<String> cmds, @DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		exec(cmds, false, closure)
	}

	static ExitStatus execbg(List<String> cmds, @DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		exec(cmds, true, closure)
	}

	static ExitStatus exec(List<String> cmds, boolean background, @DelegatesTo(ProcessDelegate) Closure closure = {})
	{
		ProcessDelegate pd = new ProcessDelegate(cmds)
		
		closure.delegate = pd
		closure.resolveStrategy = Closure.DELEGATE_FIRST
		
		closure()

		pd.bg(background)
		return pd.start()
	}

	static ExitStatus execAndPrint(String cmd, String dirName = null)
	{
		return execAndPrint(cmd.split(' ').toList(), dirName)
	}
	
	static ExitStatus execAndPrint(List<String> cmds, String dirName = null)
	{
		//println cmds.getClass().name + " : " + cmds
		//cmds.each {println it.class.name + " : " + it}
		
		exec(cmds, false) {

			inheritIO
			
			if(dirName)
				dir dirName
			
			outLine { Gilli.stdoutWithoutTime.info it}
			
			errLine {Gilli.stdoutWithoutTime.info ConsoleColor.foreRed(it)}
		}
	}

	static DataFrame currenttable(String filterCommand = "")
	{
		DataFrame tempTable = new DataFrame()
		tempTable.header("UID", "PID", "PPID",  "C", "STIME","TTY", "TIME", "CMD")
		exec{
			cmd "ps"
			cmd "-few"
		}.text.eachLine { l ->
			l = l.replaceAll("( )+", " ").split()
			List<String> temp_row = l[0..tempTable.header.size()-2]
			String temp_command = l[tempTable.header.size()-1..l.size()-1].join(" ")
			temp_row.add temp_command
			if (temp_command.contains("currenttable(") && temp_command.contains(filterCommand))
				return
			if (!filterCommand || temp_command.contains(filterCommand))
				tempTable.row temp_row
		}
		tempTable
	}
}

@Log4j2
class ProcessDelegate
{
	private List<String> cmds = []

	private Closure onSuccess
	private Closure onFailure

	private Closure out
	private Closure err

	private Closure outLine
	private Closure errLine

	private boolean inheritIO = false

	private File workingDir

	private boolean printCmdBeforeExec = false

	private boolean background = false

	ProcessDelegate(List<String> cmds)
	{
		if (cmds)
			this.cmds = cmds
	}

	void bg(boolean background)
	{
		this.background = background
	}

	void cmd(String item)
	{
		cmds << item
	}

	void success(Closure closure)
	{
		onSuccess = closure
	}

	boolean getPrintCommandBeforeExecution()
	{
		printCmdBeforeExec = true
	}

	void dir(String workingDir)
	{
		this.workingDir = new File(workingDir)
	}

	boolean getInheritIO()
	{
		inheritIO = true
	}

	void failure(Closure closure)
	{
		onSuccess = closure
	}

	void out(@ClosureParams(value = SimpleType, options = 'java.io.InputStream') Closure closure)
	{
		out = closure
	}

	void err(@ClosureParams(value = SimpleType, options = 'java.io.InputStream') Closure closure)
	{
		err = closure
	}

	void outLine(@ClosureParams(value = SimpleType, options = 'java.lang.String') Closure closure)
	{
		outLine = closure
	}

	void errLine(@ClosureParams(value = SimpleType, options = 'java.lang.String') Closure closure)
	{
		errLine = closure
	}

	ExitStatus start()
	{
		ProcessBuilder pb = new ProcessBuilder(cmds)

		pb.directory(workingDir)

		if (printCmdBeforeExec)
			Gilli.stdout.info("Executing below command\n{}\n-----------", cmds.join(' '))

		File outFile
		File errFile

		if (inheritIO)
		{
			pb.inheritIO()
		}
		else
		{
			outFile = File.createTempFile("gilli_process", ".out")
			errFile = File.createTempFile("gilli_process", ".err")

			pb.redirectOutput(outFile)
			pb.redirectError(errFile)
		
			log.debug "outfile = $outFile.absolutePath, errfile = $errFile.absolutePath"
		}
		long start = System.currentTimeMillis()

		log.trace "Executing command : " + cmds.join(" ")

		Process process = pb.start()
		
		ExitStatus exitStatus = new ExitStatus()

		exitStatus.ioInherited = inheritIO
		exitStatus.cmd = cmds
		
		if (outFile)
			exitStatus.out = outFile
		
		if (errFile)
			exitStatus.err = errFile

		if (!background)
		{
			if (outFile)
				outFile.deleteOnExit()

			if (errFile)
				errFile.deleteOnExit()

			exitStatus.exitCode = process.waitFor()
			long stop = System.currentTimeMillis()
			exitStatus.elapsed = stop - start

			if (exitStatus.exitCode == 0) {
				onSuccess?.call()
				exitStatus.success = true
			} else {
				onFailure?.call()
				exitStatus.success = false

				throw new RuntimeException("${errFile.text.trim()}. Command : [$cmds]")
			}

			if (outLine && outFile)
				outFile.newReader().each { outLine.call(it) }

			if (errLine && errFile)
				errFile.newReader().each { errLine.call(it) }

			if (out && outFile)
				out.call(outFile.newInputStream())

			if (err && errFile)
				err.call(errFile.newInputStream())
		}

		return exitStatus
	}
		}

class ExitStatus
{
	List<String> cmd
	int exitCode
	boolean success
	boolean ioInherited
	long elapsed
	
	File out
	File err
	
	String toString()
	{
		return (success ? "success" : "fail") + "/" + exitCode
	}
	
	String getOutText()
	{
		return out?.text
	}
	
	String getErrText()
	{
		return err?.text
	}
	
	String getCommandString()
	{
		return cmd.join(' ')
	}
	
	String getFirstOutLine()
	{
		def lines = out?.readLines()
		return lines ? lines.get(0) : null
	}

	String getText()
	{
		getOutText() ? getOutText() : getErrText()
	}

	String getElapsedStr()
	{
		DateTimeDSL.getElapsedstr elapsed
	}

	String printOutErr()
	{
		if (out)
		{
			out.eachLine {
				println it
			}
		}

		if (err)
		{
			err.eachLine {
				System.err.println(it)
			}
		}
	}

}

package gilli.flutter

import gilli.process.OSProcess
import picocli.CommandLine

@CommandLine.Command(name = "flutter", sortOptions = false, description = "Flutter Development Related Tools")
class FlutterCmd
{
    public static final String DISABLE_WEB_SECURITY = '--disable-web-security'

    @CommandLine.Command(name = "dwsac", sortOptions = false, synopsisHeading = "Disable Web Security At Chrome\n\n", description = "Disable Web Security At Chrome")
    void disableWebSecurity()
    {
        String out = OSProcess.exec(["flutter", "doctor", "-v"]).outText

        String flutterPath = out.readLines().find {it.contains('Flutter version')}.takeAfter(' at ')

        println flutterPath

        File chromeDartFile = new File(flutterPath, 'packages/flutter_tools/lib/src/web/chrome.dart')
        String chromeDartFileText = chromeDartFile.text

        if (chromeDartFileText.contains(DISABLE_WEB_SECURITY))
        {
            println "$DISABLE_WEB_SECURITY already available in file : $chromeDartFile.absolutePath"
        }
        else
        {
            chromeDartFile.text = chromeDartFileText.replace("'--disable-extensions'", "'--disable-extensions', '--disable-web-security'")
            println "$DISABLE_WEB_SECURITY added to file  file : $chromeDartFile.absolutePath"
        }

        File toolsStampFile = new File(flutterPath, 'bin/cache/flutter_tools.stamp')
        def deleted = toolsStampFile.delete()
        println "File deletion result: $toolsStampFile.absolutePath : $deleted"
    }

}

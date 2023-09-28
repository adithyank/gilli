package gilli.firebase

import gilli.process.OSProcess
import picocli.CommandLine

@CommandLine.Command(name = "firebase", sortOptions = false, description = "Firebase Related Tools")
class FIrebaseCmd
{
    public static final String DISABLE_WEB_SECURITY = '--disable-web-security'

    @CommandLine.Command(name = "showUpdateCmd", sortOptions = false, description = "Command for Firebase update")
    void updateFirebase()
    {
        println 'curl -sL https://firebase.tools | upgrade=true bash'
    }

}

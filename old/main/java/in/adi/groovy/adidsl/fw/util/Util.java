package in.adi.groovy.adidsl.fw.util;

import java.io.File;

public class Util
{
	private static final String CYGDSL_HOME = System.getProperty("CYGDSL_HOME");
	
	public static File fileFromHome(String path)
	{
		return new File(CYGDSL_HOME + File.separator + path);	
	}
}

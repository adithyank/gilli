package in.adi.groovy.adidsl.fw;

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.ImportCustomizer;

public interface DSLInit
{
	void compilerConfiguration(CompilerConfiguration compilerConfiguration, ImportCustomizer imports);
	void init();
}

package gilli.extras.panchapakshi;

import picocli.CommandLine;

@CommandLine.Command(name = "5b", sortOptions = false, description = "Panchapakshi options",
subcommands = {PanchaPakshiDayChartCmd.class})
public class PanchaPakshiCmd
{
}

package gilli.extras.panchapakshi;

import picocli.CommandLine;

@CommandLine.Command(name = "5b", sortOptions = false, synopsisHeading = "Panchapakshi options\n\n",
subcommands = {PanchaPakshiDayChartCmd.class})
public class PanchaPakshiCmd
{
}

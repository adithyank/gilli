package gilli.extras.panchapakshi;

import picocli.CommandLine;

@CommandLine.Command(name = "daychart", sortOptions = false, synopsisHeading = "Prints Panchapakshi Day chart\n\n", description = "Prints Panchapakshi Day chart")
public class PanchaPakshiDayChartCmd implements Runnable
{
    @CommandLine.Option(names = {"-a", "--address"}, description = "Address to which the chart needs to be printed", required = true)
    private String location;

    @CommandLine.Option(names = {"-d", "--date"}, description = "Date for which the chart needs to be printed", required = true)
    private String date_yyyy_mm_dd;

    @CommandLine.Option(names = {"--valarpirai"}, description = "Given date is valar pirai. [true|false]. default: true")
    private boolean valarPirai = true;

    @Override
    public void run()
    {
        PanchaPakshiCharter.printChart(location, date_yyyy_mm_dd, valarPirai);
    }
}

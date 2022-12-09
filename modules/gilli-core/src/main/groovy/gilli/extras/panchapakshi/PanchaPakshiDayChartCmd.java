package gilli.extras.panchapakshi;

import picocli.CommandLine;

import java.util.Arrays;

@CommandLine.Command(name = "daychart", sortOptions = false, synopsisHeading = "Prints Panchapakshi Day chart\n\n", description = "Prints Panchapakshi Day chart")
public class PanchaPakshiDayChartCmd implements Runnable
{
    @CommandLine.Option(names = {"-a", "--address"}, description = "Address to which the chart needs to be printed")
    private String location;

    @CommandLine.Option(names = {"-d", "--date"}, description = "Date for which the chart needs to be printed")
    private String date_yyyy_mm_dd;

    @CommandLine.Option(names = {"--valarpirai"}, description = "Given date is valar pirai. [true|false]. default: true")
    private boolean valarPirai = true;

    private String bwFilter;

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec; // injected by picocli

    @Override
    public void run()
    {
        PanchaPakshiCharter.printChart(location, date_yyyy_mm_dd, valarPirai, bwFilter);
    }

    @CommandLine.Option(names = {"-f", "--bwfilter"}, description = "Filter for bird and work. This arg value should have exactly 3 letters. 1st 2 letters of the filter would be 1st 2 letters of bird and 3rd letter of the filter will be 1st letter of the work")
    public void setBwFilter(String bwFilter)
    {
        if (bwFilter == null || bwFilter.length() != 3)
            throw new CommandLine.ParameterException(spec.commandLine(), "Should have exactly 3 letters");

        String bird = bwFilter.substring(0, 2);
        String work = bwFilter.substring(2, 3);

        if (!Pakshi.first2CharsSet().contains(bird))
            throw new CommandLine.ParameterException(spec.commandLine(), "First 2 chars must be first 2 chars of any one of " + Arrays.toString(Pakshi.values()));

        if (!Thozil.firstCharSet().contains(work))
            throw new CommandLine.ParameterException(spec.commandLine(), "Third char must be first char of any one of " + Arrays.toString(Thozil.values()));

        this.bwFilter = bwFilter;
    }
}

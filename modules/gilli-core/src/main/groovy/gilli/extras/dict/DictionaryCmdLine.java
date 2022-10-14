package gilli.extras.dict;

import gilli.http.HttpClient;
import gilli.internal.main.GilliMain;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.util.List;

@CommandLine.Command(name = "dict", sortOptions = false, synopsisHeading = "Meaning of English words\n\n", description = "Meaning of English Words")
public class DictionaryCmdLine implements Runnable
{
    @Parameters(index = "0..*")
    private List<String> words;

    @Override
    public void run()
    {
        //System.out.println("run() method of dictionary : " + words);
        if (words == null || words.isEmpty())
            System.err.println("No words given!");

        DictCaller.meaning(words);
    }
}

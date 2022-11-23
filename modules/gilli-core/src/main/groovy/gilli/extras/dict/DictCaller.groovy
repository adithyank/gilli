package gilli.extras.dict

import gilli.http.HttpClient
import gilli.internal.main.Gilli
import gilli.util.ConsoleColor
import gilli.util.GeneralUtil
import groovy.json.JsonOutput

class DictCaller
{
    static final String INDENT_STR = " "

    static void meaning(List<String> words)
    {
        words.each {word ->
            HttpClient.GET {
                host 'https://api.dictionaryapi.dev'
                path '/api/v2/entries/en/' + word

                response {
                    json {
                        DictCaller.printCompressed('', it)
                    }
                }
            }
        }
    }

    static String coloredKey(String str)
    {
        return ConsoleColor.foreBlue(str)
    }

    private static void printCompressed(String prefix, Object o)
    {
        String header = prefix + INDENT_STR
        //println "$header:::"
        if (o == null) {
            println "$header$o"
            return
        }

        if (o instanceof List)
        {
            List l = (List) o
            if (l.isEmpty())
                return

            if (GeneralUtil.isScalar(l.get(0)))
            {
                println header + l.join(", ")
                return
            }

            l.eachWithIndex { Object entry, int i ->
                printCompressed(header, entry)
                if (i != l.size() - 1)
                    println "$header--"
            }
        }
        else if (o instanceof Map)
        {
            ((Map) o).each {
                printCompressed(header, it)
            }
        }
        else if (o instanceof Map.Entry)
        {
            Map.Entry e = o

            if (GeneralUtil.isEmpty(e.value))
            {
                //dont print anything
            }
            else if (GeneralUtil.isScalar(e.value))
            {
                println "$header${coloredKey(e.key.toString())}: $e.value"
            }
            else
            {
                println "$header${coloredKey(e.key.toString())}"
                printCompressed(header, e.value)
            }
        }
        else
        {
            if (GeneralUtil.isScalar(o))
                println "$header$o"
        }
    }
}

class DictResp
{
    List<DictEntry> entries;
}

class DictEntry
{
    DictLicense license
    List<DictMeaning> meanings
    String phonetic
    List<DictPhonetic> phonetics
    List<String> sourceUrls
    String word
}

class DictLicense
{
    String name;
    String url;
}

class DictMeaning
{
    List<String> antonyms
    List<DictDef> definitions
    String partOfSpeech
    List<String> synonyms
}

class DictDef
{
    List<String> antonyms
    String definition
    List<String> synonyms
    String example;
}

class DictPhonetic
{
    String audio
    DictLicense license
    String sourceUrl
    String text
}
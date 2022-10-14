package gilli.extras.dict

import gilli.http.HttpClient
import groovy.json.JsonOutput

class DictCaller
{
    static void meaning(List<String> words)
    {
        words.each {word ->
            HttpClient.GET {
                host 'https://api.dictionaryapi.dev'
                path '/api/v2/entries/en/' + word

                response {
                    text {
                        println JsonOutput.prettyPrint(it)
                    }
                }
            }
        }

    }
}

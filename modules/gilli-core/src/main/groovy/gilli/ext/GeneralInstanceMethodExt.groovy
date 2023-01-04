package gilli.ext

import gilli.util.GeneralUtil
import gilli.util.RecursiveObjectSearch;

public class GeneralInstanceMethodExt
{
    static Object searchKey(Object self, String key)
    {
        new RecursiveObjectSearch().searchKey(self, key)
    }

    static String toJson(Object self)
    {
        GeneralUtil.prettyJson(self)
    }

    static void printAsLines(Map self)
    {
        if (self == null)
            println self
        else
            self.entrySet().each {println it}
    }

    static void printAsLines(Collection self)
    {
        if (self == null)
            println self
        else
            self.each {println it}
    }
}

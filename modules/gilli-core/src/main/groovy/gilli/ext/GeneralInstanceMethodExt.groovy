package gilli.ext

import gilli.ext.fw.ExtModule
import gilli.internal.main.Gilli
import gilli.util.GeneralUtil
import gilli.util.RecursiveObjectSearch;

@ExtModule
class GeneralInstanceMethodExt
{
    static Object searchKey(Object self, String key)
    {
        new RecursiveObjectSearch().searchKey(self, key)
    }

    static String toJson(Object self)
    {
        GeneralUtil.prettyJson(self)
    }

    static void printAsLines(Map self, String prefix = '')
    {
        if (self == null)
            println self
        else
            self.entrySet().each {println "$prefix$it"}
    }

    static void printAsLines(Collection self, String prefix = '')
    {
        if (self == null)
            println self
        else
            self.each {println "$prefix$it"}
    }

    static void ifDir(String dir, Closure closure)
    {
        File fd = Gilli.fileFromInvocationDir(dir)
        if (fd.directory)
            closure.call(fd)
    }

    static File getAsFile(String self)
    {
        return Gilli.fileFromInvocationDir(self)
    }

    static List<String> splitc(String self, String str)
    {
        String regex = "[$str]+"
        return self.split(regex).findAll {it && !it.empty}
    }

    static List<String> getSplitcs(String self)
    {
        return self.splitc(' ')
    }

    static String padleft(Object o, Number size)
    {
        return String.valueOf(o).padLeft(size)
    }

    static String padleft(Number n)
    {
        return padleft(n, n)
    }

    static String getPadleft(Number n)
    {
        return padleft(n)
    }
}

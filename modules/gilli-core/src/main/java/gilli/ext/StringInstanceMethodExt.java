package gilli.ext;

import gilli.ext.fw.ExtModule;

@ExtModule
public class StringInstanceMethodExt
{
    public static int getLength(String s)
    {
        return s.length();
    }
}

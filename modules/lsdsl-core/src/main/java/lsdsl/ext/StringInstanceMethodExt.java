package lsdsl.ext;

import lsdsl.ext.fw.ExtModule;

@ExtModule
public class StringInstanceMethodExt
{
    public static int getLength(String s)
    {
        return s.length();
    }
}

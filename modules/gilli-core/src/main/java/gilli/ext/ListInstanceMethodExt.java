package gilli.ext;

import gilli.ext.fw.StaticExtModule;
import gilli.util.GeneralUtil;

import java.util.List;
import java.util.stream.Collectors;

@StaticExtModule
public class ListInstanceMethodExt
{
    public static List<Number> numbers(List<?> list)
    {
        return list.stream().map(GeneralUtil::convertToBigDecimal).collect(Collectors.toList());
    }
}

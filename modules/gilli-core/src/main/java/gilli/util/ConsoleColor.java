package gilli.util;

public class ConsoleColor
{
    public static String get(ForeColor color, String text)
    {
        return "\033[0;" + color.code + ";1m" + text + "\033[0m";
    }

    public static String foreRed(String text)
    {
        return get(ForeColor.RED, text);
    }

    public static String foreBlue(String text)
    {
        return get(ForeColor.BLUE, text);
    }

    public enum ForeColor
    {
        BLUE("34"),
        RED("31");

        private String code;

        ForeColor(String code)
        {
            this.code = code;
        }
    }

    public static void main(String[] args)
    {
        System.out.println(get(ForeColor.RED, "Red america"));
        System.out.println(get(ForeColor.BLUE, "Blue India"));
    }

}

package gilli.util;

public class GilliRTException extends RuntimeException
{
    public GilliRTException()
    {
    }

    public GilliRTException(String message)
    {
        super(message);
    }

    public GilliRTException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public GilliRTException(Throwable cause)
    {
        super(cause);
    }

    public GilliRTException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace)
    {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}

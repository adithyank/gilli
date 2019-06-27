package gilli.ext

import org.testng.Assert
import org.testng.annotations.Test

class StringInstanceMethodExtTest
{
    @Test
    void stringLength()
    {
        Assert.assertEquals("India".length, 5)
        Assert.assertEquals("".length, 0)
    }
}

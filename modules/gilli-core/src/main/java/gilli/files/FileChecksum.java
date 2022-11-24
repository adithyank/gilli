package gilli.files;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;

public class FileChecksum
{
    private static final Logger logger = LogManager.getLogger();

    public static String md5(String fileName)
    {
        FileChannel fc = null;
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            Path path = Paths.get(fileName);
            fc = FileChannel.open(path, StandardOpenOption.READ);

            ByteBuffer bb = ByteBuffer.allocate((int) fc.size());

            fc.read(bb);
            bb.position(0);
            md.update(bb);

            byte[] digest = md.digest();
            return convertByteArrayToHexString(digest);
        }
        catch (Exception ex)
        {
            logger.error("Exception while finding the MD5 of the file [" + fileName + "]", ex);
            return null;
        }
        finally
        {
            if (fc != null)
            {
                try
                {
                    fc.close();
                }
                catch (IOException ex)
                {
                    logger.error("", ex);
                }
            }
        }
    }

    public static String convertByteArrayToHexString(byte[] array)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < array.length; i++)
        {
            String hex = Integer.toHexString(0xff & array[i]);

            if (hex.length() == 1)
                sb.append('0');

            sb.append(hex);
        }
        return sb.toString();
    }



}

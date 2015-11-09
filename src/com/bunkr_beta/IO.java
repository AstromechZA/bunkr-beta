package com.bunkr_beta;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

public class IO
{

    public static String readNByteString(InputStream dis, int n) throws IOException
    {
        byte[] buffer = new byte[n];
        int r = dis.read(buffer);
        return new String(buffer).substring(0, r);
    }

    public static String readString(DataInputStream dis) throws IOException
    {
        return readNByteString(dis, dis.readInt());
    }

    public static void reliableSkip(InputStream is, long n) throws IOException
    {
        long stillToSkip = n;
        while (stillToSkip > 0)
        {
            stillToSkip -= is.skip(stillToSkip);
        }
    }

    public static String convertToJson(Object object) throws IOException
    {
        ObjectMapper om = new ObjectMapper();
        StringWriter sw = new StringWriter();
        om.writeValue(sw, object);
        return sw.toString();
    }

}

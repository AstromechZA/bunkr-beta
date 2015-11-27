package com.bunkr_beta.streams.output;

import org.bouncycastle.crypto.BufferedBlockCipher;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by benmeier on 15/10/26.
 *
 * borrowed heavily from spongy castle
 * Borrowed from org/spongycastle/crypto/io/CipherOutputStream.java
 */
public class CustomCipherOutputStream extends FilterOutputStream
{
    private final BufferedBlockCipher bufferedBlockCipher;

    private final byte[] oneByte = new byte[1];
    private final byte[] buf;

    /**
     * Constructs a CipherOutputStream from an OutputStream and a
     * BufferedBlockCipher.
     */
    public CustomCipherOutputStream(
            OutputStream os,
            BufferedBlockCipher cipher)
    {
        super(os);
        this.bufferedBlockCipher = cipher;
        this.buf = new byte[cipher.getBlockSize()];
    }

    /**
     * Writes the specified byte to this output stream.
     *
     * @param b the <code>byte</code>.
     * @exception java.io.IOException if an I/O error occurs.
     */
    public void write(
            int b)
            throws IOException
    {
        oneByte[0] = (byte)b;
        int len = bufferedBlockCipher.processBytes(oneByte, 0, 1, buf, 0);
        if (len != 0)
        {
            out.write(buf, 0, len);
        }
    }

    /**
     * Writes <code>b.length</code> bytes from the specified byte array
     * to this output stream.
     * <p>
     * The <code>write</code> method of
     * <code>CipherOutputStream</code> calls the <code>write</code>
     * method of three arguments with the three arguments
     * <code>b</code>, <code>0</code>, and <code>b.length</code>.
     *
     * @param b the data.
     * @exception java.io.IOException if an I/O error occurs.
     * @see #write(byte[], int, int)
     */
    public void write(
            byte[] b)
            throws IOException
    {
        write(b, 0, b.length);
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     *
     * @param b the data.
     * @param off the start offset in the data.
     * @param len the number of bytes to write.
     * @exception java.io.IOException if an I/O error occurs.
     */
    public void write(
            byte[] b,
            int off,
            int len)
            throws IOException
    {
        byte[] buf = new byte[bufferedBlockCipher.getOutputSize(len)];

        int outLen = bufferedBlockCipher.processBytes(b, off, len, buf, 0);

        if (outLen != 0)
        {
            out.write(buf, 0, outLen);
        }
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with this stream.
     * <p>
     * This method invokes the <code>doFinal</code> method of the encapsulated
     * cipher object, which causes any bytes buffered by the encapsulated
     * cipher to be processed. The result is written out by calling the
     * <code>flush</code> method of this output stream.
     * <p>
     * This method resets the encapsulated cipher object to its initial state
     * and calls the <code>close</code> method of the underlying output
     * stream.
     *
     * @exception java.io.IOException if an I/O error occurs.
     */
    public void close()
            throws IOException
    {
        try
        {
            byte[] buf = new byte[bufferedBlockCipher.getOutputSize(0)];

            int outLen = bufferedBlockCipher.doFinal(buf, 0);

            if (outLen != 0)
            {
                out.write(buf, 0, outLen);
            }
        }
        catch (Exception e)
        {
            throw new IOException("Error closing stream: " + e.toString());
        }

        flush();
        super.close();
    }

}

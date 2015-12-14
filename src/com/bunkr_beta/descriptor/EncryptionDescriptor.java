package com.bunkr_beta.descriptor;

import com.bunkr_beta.RandomMaker;

/**
 * Creator: benmeier
 * Created At: 2015-12-03
 */
public class EncryptionDescriptor
{
    private static final int MINIMUM_AES_KEY_LENGTH = 256;
    private static final int DEFAULT_AES_KEY_LENGTH = 256;
    private static final int MINIMUM_PBKD2_ITERS = 4096;
    private static final int DEFAULT_PBKD2_ITERS = 10000;
    private static final int DEFAULT_PBKD2_SALT_LEN = 128;

    public final int pbkdf2Iterations;
    public final int aesKeyLength;
    public final byte[] pbkdf2Salt;

    public EncryptionDescriptor(int pbkdf2Iterations, int aesKeyLength, byte[] pbkdf2Salt)
    {
        if (pbkdf2Iterations < MINIMUM_PBKD2_ITERS)
            throw new IllegalArgumentException(String.format("pbkdf2Iterations must be at least %d", MINIMUM_PBKD2_ITERS));

        if (aesKeyLength != MINIMUM_AES_KEY_LENGTH)
            throw new IllegalArgumentException(String.format("aesKeyLength must be %d", MINIMUM_AES_KEY_LENGTH));

        this.pbkdf2Iterations = pbkdf2Iterations;
        this.aesKeyLength = aesKeyLength;
        this.pbkdf2Salt = pbkdf2Salt;
    }

    public static EncryptionDescriptor makeDefaults()
    {
        return new EncryptionDescriptor(DEFAULT_PBKD2_ITERS, DEFAULT_AES_KEY_LENGTH, RandomMaker.get(DEFAULT_PBKD2_SALT_LEN));
    }
}

package com.bunkr_beta.inventory;

import com.bunkr_beta.KeyMaker;
import com.bunkr_beta.fragmented_range.FragmentedRange;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.security.SecureRandom;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FileInventoryItem extends InventoryItem
{
    private long sizeOnDisk;
    private long modifiedAt;
    private byte[] encryptionKey;
    private byte[] encryptionIV;
    private FragmentedRange blocks;
    private long actualSize;

    @JsonCreator
    public FileInventoryItem(
            @JsonProperty("name") String name,
            @JsonProperty("uuid") UUID uuid,
            @JsonProperty("blocks") FragmentedRange blocks,
            @JsonProperty("sizeOnDisk") long sizeOnDisk,
            @JsonProperty("actualSize") long actualSize,
            @JsonProperty("modifiedAt") long modifiedAt,
            @JsonProperty("encryptionKey") byte[] encryptionKey,
            @JsonProperty("encryptionIV") byte[] encryptionIV
    )
    {
        super(name, uuid);
        this.encryptionKey = encryptionKey;
        this.encryptionIV = encryptionIV;
        this.sizeOnDisk = sizeOnDisk;
        this.actualSize = actualSize;
        this.modifiedAt = modifiedAt;
        this.blocks = blocks;
    }

    public FileInventoryItem(String name)
    {
        super(name, UUID.randomUUID());
        this.sizeOnDisk = 0;
        this.blocks = new FragmentedRange();
        this.modifiedAt = System.currentTimeMillis();
        this.encryptionKey = KeyMaker.get(256);
        this.encryptionIV = KeyMaker.get(256);
    }

    public FragmentedRange getBlocks()
    {
        return blocks;
    }

    public void setBlocks(FragmentedRange blocks)
    {
        this.blocks = blocks;
    }

    public byte[] getEncryptionIV()
    {
        return encryptionIV;
    }

    public void setEncryptionIV(byte[] encryptionIV)
    {
        this.encryptionIV = encryptionIV;
    }

    public byte[] getEncryptionKey()
    {
        return encryptionKey;
    }

    public void setEncryptionKey(byte[] encryptionKey)
    {
        this.encryptionKey = encryptionKey;
    }

    public long getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt)
    {
        this.modifiedAt = modifiedAt;
    }

    public long getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    public void setSizeOnDisk(long sizeOnDisk)
    {
        this.sizeOnDisk = sizeOnDisk;
    }

    public void setActualSize(long actualSize)
    {
        this.actualSize = actualSize;
    }

    public long getActualSize()
    {
        return this.actualSize;
    }
}

package org.bunkr.inventory;

import org.bunkr.utils.RandomMaker;
import org.bunkr.fragmented_range.FragmentedRange;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class FileInventoryItem extends InventoryItem implements ITaggable, IFFTraversalTarget
{
    private long sizeOnDisk;
    private long modifiedAt;
    private byte[] encryptionKey;
    private byte[] encryptionIV;
    private byte[] integrityHash;
    private FragmentedRange blocks;
    private long actualSize;
    private Set<String> tags;

    public FileInventoryItem(String name, UUID uuid, FragmentedRange blocks, long sizeOnDisk, long actualSize, long modifiedAt, byte[] encryptionKey, byte[] encryptionIV, byte[] integrityHash, HashSet<String> tags)
    {
        super(name, uuid);
        this.encryptionKey = encryptionKey;
        this.encryptionIV = encryptionIV;
        this.integrityHash = integrityHash;
        this.sizeOnDisk = sizeOnDisk;
        this.actualSize = actualSize;
        this.modifiedAt = modifiedAt;
        this.blocks = blocks;
        this.tags = tags;
    }

    public FileInventoryItem(String name)
    {
        super(name, UUID.randomUUID());
        this.sizeOnDisk = 0;
        this.blocks = new FragmentedRange();
        this.modifiedAt = System.currentTimeMillis();
        this.encryptionKey = RandomMaker.get(256);
        this.encryptionIV = RandomMaker.get(256);
        this.integrityHash = null;
        this.tags = new HashSet<>();
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

    public byte[] getIntegrityHash()
    {
        return integrityHash;
    }

    public void setIntegrityHash(byte[] integrityHash)
    {
        this.integrityHash = integrityHash;
    }

    public long getModifiedAt()
    {
        return modifiedAt;
    }

    public void setModifiedAt(long modifiedAt)
    {
        if (modifiedAt < 0) throw new IllegalArgumentException("Cannot set modifiedAt < 0");
        this.modifiedAt = modifiedAt;
    }

    public Date getModifiedAtDate()
    {
        return new Date(this.getModifiedAt());
    }

    public long getSizeOnDisk()
    {
        return sizeOnDisk;
    }

    public void setSizeOnDisk(long sizeOnDisk)
    {
        if (sizeOnDisk < 0) throw new IllegalArgumentException("Cannot set sizeOnDisk < 0");
        this.sizeOnDisk = sizeOnDisk;
    }

    public void setActualSize(long actualSize)
    {
        if (actualSize < 0) throw new IllegalArgumentException("Cannot set actualSize < 0");
        this.actualSize = actualSize;
    }

    public long getActualSize()
    {
        return this.actualSize;
    }

    @Override
    public void setTags(Set<String> tags)
    {
        this.tags = tags;
    }

    @Override
    public Set<String> getTags()
    {
        return this.tags;
    }

    @Override
    public boolean isAFile()
    {
        return true;
    }
}

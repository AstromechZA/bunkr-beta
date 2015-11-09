package com.bunkr_beta;

import com.bunkr_beta.inventory.Inventory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.security.NoSuchAlgorithmException;

public class ArchiveInfoContext
{
    public final File filePath;
    private Inventory archiveInventory;
    private Descriptor archiveDescriptor;
    private int blockSize;
    private long blockDataOffset;
    private long blockDataLength;
    private boolean fresh = false;

    public ArchiveInfoContext(File filePath) throws IOException, NoSuchAlgorithmException
    {
        this.filePath = filePath;
        this.refresh();
    }

    public void refresh() throws IOException, NoSuchAlgorithmException
    {
        try(FileInputStream fis = new FileInputStream(this.filePath))
        {
            try(DataInputStream dis = new DataInputStream(fis))
            {
                String fivebytes = IO.readNByteString(dis, 5);
                if (! fivebytes.equals("BUNKR")) throw new IOException("File format header does not match 'BUNKR'");
                dis.readByte();
                dis.readByte();
                dis.readByte();
                this.blockSize = dis.readInt();
                this.blockDataOffset = 5 + 3 + 4;
                this.blockDataLength = dis.readLong();
                IO.reliableSkip(dis, this.blockDataLength);
                String invjson = IO.readString(dis);
                String descjson = IO.readString(dis);
                this.archiveInventory = new ObjectMapper().readValue(invjson, Inventory.class);
                this.archiveDescriptor = new ObjectMapper().readValue(descjson, Descriptor.class);
            }
        }
        this.fresh = true;
    }

    public boolean isFresh()
    {
        return this.fresh;
    }

    public void invalidate()
    {
        this.fresh = false;
    }

    public void assertFresh()
    {
        if (! isFresh())
        {
            throw new AssertionError("ArchiveInfoContext is no longer fresh");
        }
    }


    public int getBlockSize()
    {
        return blockSize;
    }

    public Descriptor getArchiveDescriptor()
    {
        return archiveDescriptor;
    }

    public Inventory getArchiveInventory()
    {
        return archiveInventory;
    }

    public long getNumBlocks()
    {
        return this.blockDataLength / this.blockSize;
    }

    public long getBlockDataLength()
    {
        return blockDataLength;
    }
}

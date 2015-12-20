package org.bunkr.core;

import org.bunkr.descriptor.IDescriptor;
import org.bunkr.exceptions.BaseBunkrException;
import org.bunkr.inventory.Inventory;
import org.bouncycastle.crypto.CryptoException;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-11-09
 */
public interface IArchiveInfoContext
{
    void refresh(UserSecurityProvider uic) throws IOException, CryptoException, BaseBunkrException;

    boolean isFresh();

    long getBlockDataLength();

    int getBlockSize();

    IDescriptor getDescriptor();

    Inventory getInventory();
}

package com.bunkr_beta.cli.commands;

import com.bunkr_beta.ArchiveInfoContext;
import com.bunkr_beta.MetadataWriter;
import com.bunkr_beta.PasswordProvider;
import com.bunkr_beta.cli.CLI;
import com.bunkr_beta.exceptions.CLIException;
import com.bunkr_beta.exceptions.IllegalPathException;
import com.bunkr_beta.exceptions.TraversalException;
import com.bunkr_beta.inventory.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.bouncycastle.crypto.CryptoException;

/**
 * Creator: benmeier
 * Created At: 2015-12-06
 */
public class RmCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_RECURSIVE = "recursive";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("remove a file or directory");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("archive path to create the new directory");
        target.addArgument("-r", "--recursive")
                .dest(ARG_RECURSIVE)
                .type(Boolean.class)
                .action(Arguments.storeTrue())
                .help("remove all subfolders and files");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        try
        {
            PasswordProvider passProv = makePasswordProvider(args);
            ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
            deleteItem(aic.getInventory(), args.getString(ARG_PATH), args.getBoolean(ARG_RECURSIVE));
            MetadataWriter.write(aic, passProv);
            System.out.println(String.format("Deleted %s from archive.", args.getString(ARG_PATH)));
        }
        catch (IllegalPathException | TraversalException e)
        {
            throw new CLIException(e);
        }
        catch (CryptoException e)
        {
            throw new CLIException("Decryption failed: %s", e.getMessage());
        }
    }

    public void deleteItem(Inventory inv, String targetPath, boolean recursive) throws TraversalException
    {
        if (targetPath.equals("/")) throw new TraversalException("Cannot remove root directory");

        String parentPath = InventoryPather.dirname(targetPath);

        IFFTraversalTarget parentDir = InventoryPather.traverse(inv, parentPath);

        String targetName = InventoryPather.baseName(targetPath);

        if (parentDir.isAFile()) throw new TraversalException("'%s' is a file and does not contain item '%s'", parentPath, targetName);

        IFFContainer parentContainer = (IFFContainer) parentDir;

        FolderInventoryItem folderItem = (FolderInventoryItem) parentContainer.findFolder(targetName);
        if (folderItem != null)
        {
            if (!recursive && (folderItem.getFiles().size() > 0 || folderItem.getFolders().size() > 0)) throw new TraversalException("Folder '%s' is not empty", targetPath);
            parentContainer.getFolders().remove(folderItem);
            return;
        }
        FileInventoryItem fileItem = parentContainer.findFile(targetName);
        if (fileItem != null)
        {
            parentContainer.getFiles().remove(fileItem);
            return;
        }

        throw new TraversalException("'%s' does not exist", targetPath);
    }
}

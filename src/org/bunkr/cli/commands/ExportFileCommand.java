package org.bunkr.cli.commands;

import org.bunkr.ArchiveInfoContext;
import org.bunkr.cli.passwords.PasswordProvider;
import org.bunkr.cli.CLI;
import org.bunkr.exceptions.CLIException;
import org.bunkr.inventory.FileInventoryItem;
import org.bunkr.inventory.IFFTraversalTarget;
import org.bunkr.inventory.InventoryPather;
import org.bunkr.streams.input.MultilayeredInputStream;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.*;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.util.Arrays;

/**
 * Creator: benmeier
 * Created At: 2015-12-08
 */
public class ExportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_DESTINATION_FILE = "destination";

    @Override
    public void buildParser(Subparser target)
    {
        target.help("read or export a file from the archive");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("source path in the archive");
        target.addArgument("destination")
                .dest(ARG_DESTINATION_FILE)
                .type(Arguments.fileType().acceptSystemIn())
                .help("file to export to or - for stdout");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        PasswordProvider passProv = makePasswordProvider(args);
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), passProv);
        IFFTraversalTarget target = InventoryPather.traverse(aic.getInventory(), args.getString(ARG_PATH));
        if (!target.isAFile()) throw new CLIException("'%s' is not a file.", args.getString(ARG_PATH));

        FileInventoryItem targetFile = (FileInventoryItem) target;

        File inputFile = args.get(ARG_DESTINATION_FILE);
        if (inputFile.getPath().equals("-"))
        {
            writeBlockFileToStream(aic, targetFile, System.out);
        }
        else
        {
            if (inputFile.exists()) throw new CLIException("'%s' already exists. Will not overwrite.", inputFile.getCanonicalPath());
            FileChannel fc = new RandomAccessFile(inputFile, "rw").getChannel();
            try(OutputStream contentOutputStream = Channels.newOutputStream(fc))
            {
                writeBlockFileToStream(aic, targetFile, contentOutputStream);
            }
        }
    }

    private void writeBlockFileToStream(ArchiveInfoContext ctxt, FileInventoryItem targetFile, OutputStream os)
            throws IOException
    {
        try (MultilayeredInputStream ms = new MultilayeredInputStream(ctxt, targetFile))
        {
            byte[] buffer = new byte[8196];
            int n;
            while ((n = ms.read(buffer)) != -1)
            {
                os.write(buffer, 0, n);
            }
            Arrays.fill(buffer, (byte) 0);
        }
    }
}
/**
 * Copyright (c) 2016 Bunkr
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.bunkr.cli.commands;

import org.bunkr.core.inventory.*;
import org.bunkr.core.usersec.UserSecurityProvider;
import org.bunkr.core.utils.AbortableShutdownHook;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.MetadataWriter;
import org.bunkr.cli.CLI;
import org.bunkr.cli.ProgressBar;
import org.bunkr.core.exceptions.BaseBunkrException;
import org.bunkr.core.exceptions.CLIException;
import org.bunkr.core.streams.output.MultilayeredOutputStream;
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
public class ImportFileCommand implements ICLICommand
{
    public static final String ARG_PATH = "path";
    public static final String ARG_SOURCE_FILE = "source";
    public static final String ARG_MEDIA_TYPE = "mediatype";
    public static final String ARG_NO_PROGRESS = "noprogress";


    @Override
    public void buildParser(Subparser target)
    {
        target.help("write or import a file");
        target.addArgument("path")
                .dest(ARG_PATH)
                .type(String.class)
                .help("destination path in the archive");
        target.addArgument("source")
                .dest(ARG_SOURCE_FILE)
                .type(Arguments.fileType().acceptSystemIn().verifyExists().verifyCanRead())
                .help("file to import or - for stdin");
        target.addArgument("--no-progress")
                .dest(ARG_NO_PROGRESS)
                .action(Arguments.storeTrue())
                .setDefault(false)
                .type(Boolean.class)
                .help("don't display a progress bar while importing the file");
        target.addArgument("-t", "--mediatype")
                .dest(ARG_MEDIA_TYPE)
                .choices(MediaType.ALL_TYPES)
                .type(String.class)
                .help("pick a media type");
    }

    @Override
    public void handle(Namespace args) throws Exception
    {
        UserSecurityProvider usp = new UserSecurityProvider(makeCLIPasswordProvider(args.get(CLI.ARG_PASSWORD_FILE)));
        ArchiveInfoContext aic = new ArchiveInfoContext(args.get(CLI.ARG_ARCHIVE_PATH), usp);

        if (args.getString(ARG_PATH).equals("/")) throw new CLIException("Cannot import as /.");

        IFFTraversalTarget parent = InventoryPather.traverse(aic.getInventory(),
                                                             InventoryPather.dirname(args.getString(ARG_PATH)));
        if (parent.isAFile()) throw new CLIException("Cannot create file as a child of a file.");
        IFFContainer container = (IFFContainer) parent;

        IFFTraversalTarget target = container.findFileOrFolder(InventoryPather.baseName(args.getString(ARG_PATH)));

        FileInventoryItem targetFile;
        if (target != null)
        {
            if (target.isAFolder()) throw new CLIException("Cannot overwrite folder with a file.");
            targetFile = (FileInventoryItem) target;
        }
        else
        {
            targetFile = new FileInventoryItem(InventoryPather.baseName(args.getString(ARG_PATH)));
            container.addFile(targetFile);
        }
        targetFile.setMediaType(args.getString(ARG_MEDIA_TYPE));

        File inputFile = args.get(ARG_SOURCE_FILE);

        AbortableShutdownHook emergencyShutdownThread = new EnsuredMetadataWriter(aic, usp);
        Runtime.getRuntime().addShutdownHook(emergencyShutdownThread);

        boolean noProgress = (Boolean) getOrDefault(args.get(ARG_NO_PROGRESS), false);
        try
        {
            if (inputFile.getPath().equals("-"))
            {
                importFileFromStream(aic, targetFile, System.in, !noProgress);
            }
            else
            {
                FileChannel fc = new RandomAccessFile(inputFile, "r").getChannel();
                try (InputStream fis = Channels.newInputStream(fc))
                {
                    importFileFromStream(aic, targetFile, fis, inputFile.length(), !noProgress);
                }
            }
        }
        finally
        {
            // This finally block is a basic attempt at handling bad problems like corrupted writes when saving a file.
            // if an exception was raised due to some IO issue, then we still want to write a hopefully correct
            // metadata section so that the file can be correctly read in future.
            MetadataWriter.write(aic, usp);
            Runtime.getRuntime().removeShutdownHook(emergencyShutdownThread);
        }
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target,
                                      InputStream is, boolean showProgress) throws IOException
    {
        importFileFromStream(context, target, is, is.available(), showProgress);
    }

    private void importFileFromStream(ArchiveInfoContext context, FileInventoryItem target,
                                      InputStream is, long expectedBytes, boolean showProgress) throws IOException
    {
        ProgressBar pb = new ProgressBar(120, expectedBytes, "Importing file: ", showProgress);
        pb.startFresh();

        try (MultilayeredOutputStream bwos = new MultilayeredOutputStream(context, target))
        {
            byte[] buffer = new byte[1024 * 1024];
            int n;
            while ((n = is.read(buffer)) != -1)
            {
                bwos.write(buffer, 0, n);
                pb.inc(n);
            }
            Arrays.fill(buffer, (byte) 0);
        }

        pb.finish();
    }

    private static class EnsuredMetadataWriter extends AbortableShutdownHook
    {
        private final ArchiveInfoContext context;
        private final UserSecurityProvider prov;

        public EnsuredMetadataWriter(ArchiveInfoContext context, UserSecurityProvider prov)
        {
            this.context = context;
            this.prov = prov;
        }

        @Override
        public void innerRun()
        {
            try
            {
                System.err.println("Performing emergency metadata write for recovery");
                MetadataWriter.write(this.context, this.prov);
            }
            catch (IOException | BaseBunkrException e)
            {
                e.printStackTrace();
            }
        }
    }

    private Object getOrDefault(Object v, Object d)
    {
        if (v == null) return d;
        return v;
    }
}

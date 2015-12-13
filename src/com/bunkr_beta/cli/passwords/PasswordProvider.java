package com.bunkr_beta.cli.passwords;

import com.bunkr_beta.exceptions.CLIException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Arrays;
import java.util.Set;

/**
 * Creator: benmeier
 * Created At: 2015-12-01
 */
public class PasswordProvider
{
    private byte[] archivePassword;
    private IPasswordPrompter prompter;

    public PasswordProvider()
    {
        this.archivePassword = null;
        this.prompter = null;
    }

    public PasswordProvider(IPasswordPrompter prompter)
    {
        this.archivePassword = null;
        this.prompter = prompter;
    }

    public PasswordProvider(byte[] password)
    {
        this.archivePassword = password;
        this.prompter = null;
    }

    public byte[] getArchivePassword()
    {
        if (archivePassword == null)
        {
            if (prompter != null)
            {
                this.archivePassword = prompter.getPassword();
            }
            else
            {
                throw new IllegalArgumentException("Password requested, but no password prompt available.");
            }
        }
        return archivePassword;
    }

    public void setArchivePassword(byte[] archivePassword)
    {
        this.archivePassword = archivePassword;
    }

    public void setArchivePassword(File passwordFile) throws CLIException, IOException
    {
        Set<PosixFilePermission> permissions = Files.getPosixFilePermissions(passwordFile.toPath());
        if (permissions.contains(PosixFilePermission.OTHERS_READ))
            throw new CLIException("For security reasons, the password file may not be world-readable.");
        if (permissions.contains(PosixFilePermission.OTHERS_WRITE))
            throw new CLIException("For security reasons, the password file may not be world-writeable.");
        try(BufferedReader br = new BufferedReader(new FileReader(passwordFile)))
        {
            this.setArchivePassword(br.readLine().getBytes());
        }
    }

    public void clearArchivePassword()
    {
        if (this.archivePassword != null) Arrays.fill(this.archivePassword, (byte) 0);
        this.archivePassword = null;
    }

    public IPasswordPrompter getPrompter()
    {
        return prompter;
    }

    public void setPrompter(IPasswordPrompter prompter)
    {
        this.prompter = prompter;
    }

    @Override
    protected void finalize() throws Throwable
    {
        this.clearArchivePassword();
        super.finalize();
    }
}

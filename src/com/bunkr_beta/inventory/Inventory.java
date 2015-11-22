package com.bunkr_beta.inventory;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Queue;

/**
 * Creator: benmeier
 * Created At: 2015-11-08
 */
public class Inventory implements IFFContainer
{
    private final FFContainer ffcontainer;

    @JsonCreator
    public Inventory(
            @JsonProperty("files") ArrayList<FileInventoryItem> files,
            @JsonProperty("folders") ArrayList<FolderInventoryItem> folders
    )
    {
        this.ffcontainer = new FFContainer(files, folders);
    }


    @Override
    public ArrayList<FolderInventoryItem> getFolders()
    {
        return this.ffcontainer.folders;
    }

    @Override
    public ArrayList<FileInventoryItem> getFiles()
    {
        return this.ffcontainer.files;
    }
}

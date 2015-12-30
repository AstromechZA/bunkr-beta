package org.bunkr.core.inventory;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Creator: benmeier
 * Created At: 2015-12-13
 */
public class FolderInventoryItemJSON
{
    @SuppressWarnings("unchecked")
    public static JSONAware encodeO(FolderInventoryItem input)
    {
        JSONObject out = new JSONObject();

        out.put("name", input.getName());
        out.put("uuid", input.getUuid().toString());

        JSONArray files = new JSONArray();
        for (FileInventoryItem item : input.getFiles())
        {
            files.add(FileInventoryItemJSON.encodeO(item));
        }
        out.put("files", files);

        JSONArray folders = new JSONArray();
        for (FolderInventoryItem item : input.getFolders())
        {
            folders.add(FolderInventoryItemJSON.encodeO(item));
        }
        out.put("folders", folders);
        return out;
    }

    public static String encode(FolderInventoryItem input)
    {
        return encodeO(input).toJSONString();
    }

    @SuppressWarnings("unchecked")
    public static FolderInventoryItem decodeO(JSONObject input)
    {
        FolderInventoryItem outputFolder = new FolderInventoryItem(
                (String) input.get("name"),
                UUID.fromString((String) input.get("uuid")),
                new ArrayList<>(),
                new ArrayList<>()
        );

        for (Object item : (JSONArray) input.get("files"))
        {
            outputFolder.addFile(FileInventoryItemJSON.decodeO((JSONObject) item));
        }

        for (Object item : (JSONArray) input.get("folders"))
        {
            outputFolder.addFolder(FolderInventoryItemJSON.decodeO((JSONObject) item));
        }

        return outputFolder;
    }

    public static FolderInventoryItem decode(String input)
    {
        return decodeO((JSONObject) JSONValue.parse(input));
    }
}

package org.bunkr.gui.components;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeView;
import javafx.scene.image.ImageView;
import javafx.util.Callback;
import org.bunkr.core.Resources;
import org.bunkr.gui.controllers.InventoryCMController;

import java.io.IOException;

/**
 * Creator: benmeier
 * Created At: 2015-12-27
 */
public class CellFactoryCallback implements Callback<TreeView<IntermedInvTreeDS>, TreeCell<IntermedInvTreeDS>>
{
    private final String fileImagePath, folderImagePath;

    private final InventoryCMController callbackContainer;

    public CellFactoryCallback(InventoryCMController callbackContainer)
    {
        this.callbackContainer = callbackContainer;

        String temp = null;
        try
        {
            temp = Resources.getExternalPath("/resources/images/file.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.fileImagePath = temp;

        temp = null;
        try
        {
            temp = Resources.getExternalPath("/resources/images/folder.png");
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        this.folderImagePath = temp;
    }

    @Override
    public TreeCell<IntermedInvTreeDS> call(TreeView<IntermedInvTreeDS> param)
    {
        return new TreeCell<IntermedInvTreeDS>()
        {
            @Override
            protected void updateItem(IntermedInvTreeDS item, boolean empty)
            {
                super.updateItem(item, empty);
                if (! empty)
                {
                    setText(item != null ? item.getName() : "");
                    if (item != null)
                    {
                        if (item.getType().equals(IntermedInvTreeDS.Type.ROOT))
                        {
                            setGraphic(new ImageView(folderImagePath));
                            setContextMenu(CellFactoryCallback.this.callbackContainer.rootContextMenu);
                        }
                        else if (item.getType().equals(IntermedInvTreeDS.Type.FOLDER))
                        {
                            setGraphic(new ImageView(folderImagePath));
                            setContextMenu(CellFactoryCallback.this.callbackContainer.dirContextMenu);
                        }
                        else
                        {
                            setGraphic(new ImageView(fileImagePath));
                            setContextMenu(CellFactoryCallback.this.callbackContainer.fileContextMenu);
                        }
                    }
                }
                else
                {
                    setText(null);
                    setGraphic(null);
                    setContextMenu(null);
                }
            }
        };
    }
}
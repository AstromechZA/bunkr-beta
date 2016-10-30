package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.gui.BlockImageGenerator;

import java.io.IOException;

/**
 * Created At: 2016-10-30
 */
public class BlockDistributionWindow extends BaseWindow
{
    private final String cssPath;
    private final ArchiveInfoContext archive;
    private ImageView imageView;
    private BorderPane imagePanel;

    public BlockDistributionWindow(ArchiveInfoContext archive) throws IOException
    {
        super();
        this.archive = archive;
        this.cssPath = Resources.getExternalPath("/resources/css/block_distrib.css");
        this.initialise();
    }

    @Override
    public void initControls()
    {
        this.imagePanel = new BorderPane();
        this.imageView = new ImageView();
    }

    @Override
    public Parent initLayout()
    {
        BorderPane root = new BorderPane();

        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);
        root.setPadding(new Insets(10));

        imagePanel.setCenter(imageView);

        imageView.setImage(BlockImageGenerator.buildImageFromArchiveInfo(this.archive, 400));
        imageView.setSmooth(false);
        imageView.fitWidthProperty().bind(imagePanel.widthProperty().subtract(10));
        imageView.fitHeightProperty().bind(imagePanel.heightProperty().subtract(10));

        root.setCenter(imagePanel);

        return root;
    }

    @Override
    public void bindEvents()
    {

    }

    @Override
    public void applyStyling()
    {
        this.imagePanel.getStyleClass().addAll("block-image");
    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout());
        scene.getStylesheets().add(this.cssCommon);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Block Distribution");
        this.getStage().setMinWidth(800);
        this.getStage().setMinHeight(600);
        this.getStage().setScene(scene);
        this.getStage().initModality(Modality.APPLICATION_MODAL);
        this.getStage().setResizable(true);
        return scene;
    }
}
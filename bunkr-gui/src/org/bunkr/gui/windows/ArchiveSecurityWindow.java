package org.bunkr.gui.windows;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.*;
import org.bunkr.core.ArchiveInfoContext;
import org.bunkr.core.Resources;
import org.bunkr.core.descriptor.PBKDF2Descriptor;
import org.bunkr.core.descriptor.PlaintextDescriptor;
import org.bunkr.core.exceptions.IllegalPasswordException;
import org.bunkr.core.usersec.PasswordRequirements;
import org.bunkr.gui.dialogs.QuickDialogs;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Creator: benmeier
 * Created At: 2016-01-06
 */
public class ArchiveSecurityWindow extends BaseWindow
{
    private static final int WINDOW_WIDTH = 600, WINDOW_HEIGHT = 300;

    private static final String SM_PLAINTEXT = "none (plaintext)";
    private static final String SM_PBKDF2 = "PBKDF2 / AES256";

    private static final String PW_NOTE_DEFAULT = "Please enter a password";
    private static final String PW_NOTE_CONFIRM = "Please confirm password";
    private static final String PW_NOTE_MATCH = "Confirmation matches password";
    private static final String PW_NOTE_NO_MATCH = "Confirmation does not match password";
    private static final String PW_NOTE_CLASS_OK = "pw-note-success";
    private static final String PW_NOTE_CLASS_NOT_OK = "pw-note-failure";

    private final ArchiveInfoContext archive;
    private final String cssPath;

    private Consumer<String> onSaveDescriptorRequest;

    private Button btnCancel;
    private Button btnApply;
    private BorderPane centerPane;
    private ComboBox<String> modelBox;
    private PasswordField pbkdf2_passwordField;
    private PasswordField pbkdf2_confirmPasswordField;
    private Label pbkdf2_passwordNote;

    public ArchiveSecurityWindow(ArchiveInfoContext archive) throws IOException
    {
        super();
        this.archive = archive;
        this.cssPath = Resources.getExternalPath("/resources/css/archive_settings_window.css");
        this.initialise();

        this.modelBox.getSelectionModel().select(0);
    }

    @Override
    public void initControls()
    {
        this.centerPane = new BorderPane();
        this.centerPane.getStyleClass().add("center-pane");
        this.btnCancel = new Button("Cancel");
        this.btnApply = new Button("Apply");
        this.btnApply.setDisable(true);

        this.modelBox = new ComboBox<String>();
        this.modelBox.getItems().addAll(SM_PLAINTEXT, SM_PBKDF2);
    }

    @Override
    public Parent initLayout()
    {
        BorderPane rootLayout = new BorderPane();
        rootLayout.setPadding(new Insets(10));

        Label label = new Label("Current security model: ");
        HBox topBox = new HBox(5, label, modelBox);
        topBox.setAlignment(Pos.CENTER_LEFT);
        topBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(modelBox, Priority.NEVER);
        HBox.setHgrow(label, Priority.ALWAYS);

        BorderPane.setMargin(centerPane, new Insets(10, 0, 10, 0));

        HBox buttonBox = new HBox(5, btnCancel, btnApply);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        // set rows to border pane
        rootLayout.setTop(topBox);
        rootLayout.setCenter(centerPane);
        rootLayout.setBottom(buttonBox);

        return rootLayout;
    }

    @Override
    public void bindEvents()
    {
        this.btnCancel.setOnAction(event -> this.getStage().close());
        this.modelBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            // clear the settings box
            centerPane.setDisable(false);
            centerPane.setCenter(null);
            btnApply.setDisable(true);

            // now add the correct content based on the requirements
            switch (newValue)
            {
                case SM_PLAINTEXT:
                    centerPane.setDisable(true);
                    centerPane.setCenter(new Label("No settings required for plaintext security."));
                    btnApply.setDisable(false);
                    break;
                case SM_PBKDF2:
                    // build form
                    this.build_pbkdf2_form();
                    break;
                default:
                    QuickDialogs.error("Cannot handle security model: %s", newValue);
                    break;
            }
        });

        this.btnApply.setOnAction(event -> {

            // first confirm that the user does want to change the encryption type

            switch (this.modelBox.getSelectionModel().getSelectedItem())
            {
                case SM_PLAINTEXT:
                    this.archive.setDescriptor(new PlaintextDescriptor());
                    this.onSaveDescriptorRequest.accept("Switched to PlaintextDescriptor.");
                    this.getStage().close();
                    return;
                case SM_PBKDF2:
                    this.archive.setDescriptor(PBKDF2Descriptor.makeDefaults());
                    // TODO how to do this... need to somehow set the UserSecurityProvider to the new password and perform the save
                    this.onSaveDescriptorRequest.accept("Switched to PBKDF2Descriptor.");
                    this.getStage().close();
                    return;
                default:
                    QuickDialogs.error("Cannot handle security model: %s", this.modelBox.getSelectionModel().getSelectedItem());
            }
        });
    }

    @Override
    public void applyStyling()
    {

    }

    @Override
    public Scene initScene()
    {
        Scene scene = new Scene(this.getRootLayout(), WINDOW_WIDTH, WINDOW_HEIGHT);
        scene.getStylesheets().add(this.cssPath);
        this.getStage().setTitle("Bunkr - Archive Security");
        this.getStage().setScene(scene);
        this.getStage().setResizable(true);
        return scene;
    }

    public void setOnSaveDescriptorRequest(Consumer<String> onSaveDescriptorRequest)
    {
        this.onSaveDescriptorRequest = onSaveDescriptorRequest;
    }

    private void build_pbkdf2_form()
    {
        this.pbkdf2_passwordField = new PasswordField();
        this.pbkdf2_passwordField.setPromptText("Enter a password");
        this.pbkdf2_confirmPasswordField = new PasswordField();
        this.pbkdf2_confirmPasswordField.setPromptText("Confirm the password");
        this.pbkdf2_confirmPasswordField.setDisable(true);
        this.pbkdf2_passwordNote = new Label(PW_NOTE_DEFAULT);

        this.pbkdf2_passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.pbkdf2_confirmPasswordField.setText("");
            this.pbkdf2_confirmPasswordField.setDisable(true);
            btnApply.setDisable(true);
            this.pbkdf2_passwordNote.getStyleClass().clear();

            if (this.pbkdf2_passwordField.getText().equals(""))
            {
                this.pbkdf2_passwordNote.setText(PW_NOTE_DEFAULT);
            }
            else
            {
                try
                {
                    PasswordRequirements.checkPasses(this.pbkdf2_passwordField.getText().getBytes());
                    this.pbkdf2_confirmPasswordField.setDisable(false);
                    this.pbkdf2_passwordNote.setText(PW_NOTE_CONFIRM);
                }
                catch (IllegalPasswordException e)
                {
                    this.pbkdf2_passwordNote.setText(e.getMessage());
                }
                this.pbkdf2_passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        this.pbkdf2_confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
            this.pbkdf2_passwordNote.getStyleClass().clear();
            btnApply.setDisable(true);
            if (this.pbkdf2_confirmPasswordField.getText().equals(this.pbkdf2_passwordField.getText()))
            {
                this.pbkdf2_passwordNote.setText(PW_NOTE_MATCH);
                this.pbkdf2_passwordNote.getStyleClass().add(PW_NOTE_CLASS_OK);
                btnApply.setDisable(false);
            }
            else if (this.pbkdf2_confirmPasswordField.getText().equals(""))
            {
                this.pbkdf2_passwordNote.setText(PW_NOTE_CONFIRM);
            }
            else
            {
                this.pbkdf2_passwordNote.setText(PW_NOTE_NO_MATCH);
                this.pbkdf2_passwordNote.getStyleClass().add(PW_NOTE_CLASS_NOT_OK);
            }
        });

        VBox vbox = new VBox(10, this.pbkdf2_passwordField, this.pbkdf2_confirmPasswordField, this.pbkdf2_passwordNote);
        centerPane.setCenter(vbox);
    }
}

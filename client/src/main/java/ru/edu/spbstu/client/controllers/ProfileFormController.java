package ru.edu.spbstu.client.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.http.HttpStatus;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.services.ProfileFormService;
import ru.edu.spbstu.clientComponents.PasswordTextField;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ResourceBundle;

import static ru.edu.spbstu.client.utils.Verifiers.checkEmail;

public class ProfileFormController {
    @FXML
    private Button changePasswordButton;

    @FXML
    private ImageView profilePictureImageView;

    @FXML
    private TextField emailTextField;

    @FXML
    private TextField newEmailTextField;

    @FXML
    private Button changeEmailButton;

    @FXML
    public Label userLoginLabel;

    @FXML
    private PasswordTextField oldPasswordTextField;
    @FXML
    private PasswordTextField newPasswordTextField;
    @FXML
    private PasswordTextField repeatPasswordTextField;


    private ProfileFormService profileFormService;

    private Stage primaryStage;
    private Stage currentStage;
    private ChatFormController prevController;
    private ResourceBundle bundle;


    public void initialize() {
        GridPane.setFillWidth(changePasswordButton, true);
        GridPane.setFillWidth(profilePictureImageView, true);
        GridPane.setFillHeight(profilePictureImageView, true);
        changeEmailButton.setDisable(true);
        changePasswordButton.setDisable(true);
    }

    void init() throws IOException {
        userLoginLabel.setText(profileFormService.getLogin());
        try {
            byte[] imageBytes = profileFormService.getProfilePicture();
            Image image = new Image(new ByteArrayInputStream(imageBytes),160,160,false,false);
            profilePictureImageView.setImage(image);
        } catch (IOException e) {
            showError(e.getMessage());
        }

        emailTextField.setText(profileFormService.getEmail());

        currentStage.setOnCloseRequest(e -> {
            close();
        });
    }

    @FXML
    private void close() {
        prevController.setLogin(profileFormService.getLogin());
        primaryStage.show();
        prevController.resumeLoad();
        currentStage.close();
    }

    public ProfileFormController() {
        profileFormService = new ProfileFormService();
    }


    private void showError(String errorText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("Error"));
        alert.setHeaderText(errorText);
        alert.show();
    }

    public void changeProfilePictureButtonPress(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(bundle.getString("choosePicture"));
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PNG/JPEG", "*.png", "*.jpeg","*.jpg"));
        File file = fileChooser.showOpenDialog(currentStage);
        if (file == null) {
            return;
        }
        try {
            byte[] fileContent = Files.readAllBytes(file.toPath());
            Image image = new Image(new ByteArrayInputStream(fileContent));
            if (image.getHeight() > 1000 || image.getWidth() > 1000) {
                showError(bundle.getString("pictureSizeTooBigError"));
                return;
            }
            if (image.getHeight() < 200 || image.getWidth() < 200) {
                showError(bundle.getString("pictureSizeTooSmallError"));
                return;
            }
            profileFormService.setProfilePicture(fileContent);
            profilePictureImageView.setPreserveRatio(false);
            profilePictureImageView.setImage(image);
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    public void changeEmailTextFieldChanged(KeyEvent keyEvent) {
        changeEmailButton.setDisable(emailTextField.getText().isBlank() || newEmailTextField.getText().isBlank());
    }

    public void changeEmailButtonPress(ActionEvent actionEvent) {
        try {
            checkEmail(newEmailTextField.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        try {
            profileFormService.changeEmail(newEmailTextField.getText());
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(bundle.getString("Message"));
            alert.setHeaderText(bundle.getString("emailChangedInfo"));
            alert.show();
            emailTextField.setText(newEmailTextField.getText());
            newEmailTextField.setText("");
            changeEmailButton.setDisable(true);
        } catch (InvalidDataException e) {
            showError(bundle.getString(e.getMessage()));
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    public void setLogin(String login) {
        profileFormService.setLogin(login);
    }

    public void setCurrentStage(Stage stage) {
        currentStage = stage;
    }

    public void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public void setPrevController(ChatFormController prevController) {
        this.prevController = prevController;
    }

    public void passwordTextFieldsChanged(KeyEvent keyEvent) {
        changePasswordButton.setDisable(oldPasswordTextField.getText().isEmpty() ||
                newPasswordTextField.getText().isEmpty() || repeatPasswordTextField.getText().isEmpty());
    }

    public void changePasswordButtonPress(ActionEvent actionEvent) {
        if (!newPasswordTextField.getText().equals(repeatPasswordTextField.getText())) {
            showError(bundle.getString("passwordsDontMatchError"));
            return;
        }
        if (newPasswordTextField.getText().length() < 8 || newPasswordTextField.getText().length() > 128) {
            showError(bundle.getString("wrongPasswordLengthError"));
            return;
        }
        try {
            int code = profileFormService.changePassword(oldPasswordTextField.getText(), newPasswordTextField.getText());
            if (code == HttpStatus.SC_OK) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle(bundle.getString("Message"));
                alert.setHeaderText(bundle.getString("passwordChangedInfo"));
                alert.show();
                oldPasswordTextField.setText("");
                newPasswordTextField.setText("");
                repeatPasswordTextField.setText("");
                changePasswordButton.setDisable(true);
            } else if (code == HttpStatus.SC_BAD_REQUEST) {
                showError(bundle.getString("wrongOldPasswordError"));
            }
        } catch (IOException e) {
            showError(e.getMessage());
        }
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }
}

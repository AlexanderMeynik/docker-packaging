package ru.edu.spbstu.client.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.exception.InvalidHttpClientFactoryStateException;
import ru.edu.spbstu.client.services.LogInService;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.clientComponents.PasswordTextField;

import java.io.IOException;
import java.util.ResourceBundle;

import static ru.edu.spbstu.client.utils.Verifiers.checkEmail;
import static ru.edu.spbstu.client.utils.Verifiers.checkLogin;

public class LoginFormController {

    public Tab regTab;
    private ResourceBundle bundle;

    public CheckBox rememberMeCheckBox;
    public TextField loginTextBox;
    public PasswordTextField passwordTextBox;
    public TextField emailTextBox;
    public TextField regLoginTextBox;
    public PasswordTextField regPasswordTextBox;


    public Button forgetPasswordButton;
    public Button logInButton;
    public Button registerButton;

    private LogInService service;
    private Stage stage;


    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    void showError(String errorText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("Error"));
        alert.setHeaderText(errorText);
        alert.show();
    }

    @FXML
    void initialize() {
        service = new LogInService();
        logInButton.setDisable(true);
        registerButton.setDisable(true);
        forgetPasswordButton.setDisable(true);
        stage = ClientApplication.getStage();
    }

    public boolean skipLoginViaToken() {
        HttpClientFactory fact = HttpClientFactory.getInstance();
        HttpClient client;
        try {
            client = fact.getHttpClient();
        }
        catch (InvalidHttpClientFactoryStateException | IOException e) {
            return false;//если что-то произошло, то ничего не делаем(форма логина открывается)
        }
        try{
            String getChatsUrlBlueprint = "http://localhost:8080/get_login";
            HttpGet httpGet = new HttpGet(getChatsUrlBlueprint);
            HttpResponse re = client.execute(httpGet);
            HttpClientFactory.tryUpdateRememberMe(re);
            String json = EntityUtils.toString(re.getEntity());
            if (re.getStatusLine().getStatusCode() != 200) {
                throw new HttpResponseException(re.getStatusLine().getStatusCode(), "Error getLogin");
            }
            ObjectMapper jsonMapper = new ObjectMapper();
            String login1 = json;
            openChatForm(login1);//если ничего не произойдёт мы дойдём до сюда и откроем 2 форму
            return true;
        } catch (IOException e) {
            fact.invalidateToken();//если что-то произошло, то токен инвалидируем
            return false;
        }
    }

    private void clear() {
        passwordTextBox.setText("");
        emailTextBox.setText("");
        regPasswordTextBox.setText("");
        passwordTextBox.setText("");
        logInButton.setDisable(true);
        registerButton.setDisable(true);
    }


    public void logInButtonPress(ActionEvent actionEvent) {
        if (passwordTextBox.getText().length() < 8 || passwordTextBox.getText().length() > 128) {
            showError(bundle.getString("wrongPasswordLengthError"));
            return;
        }
        try {
            checkLogin(loginTextBox.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }

        try {
            service.logIn(
                    loginTextBox.getText(),
                    passwordTextBox.getText(),
                    rememberMeCheckBox.isSelected()
            );
        } catch (HttpResponseException ex) {
            if (ex.getStatusCode() == 401) {
                showError(bundle.getString("InvalidLogPasswordError"));
                return;

            }
        } catch (IOException e) {
            showError(bundle.getString("InternalErrorText"));
            return;
        }
        try {
            openChatForm(loginTextBox.getText());

        } catch (IOException e) {
            showError(bundle.getString("SecondFormOpenError"));

            return;
        }
        regLoginTextBox.setText("");
        registerButton.setDisable(true);
        clear();
        stage.hide();
    }

    public void forgotPasswordButtonPress(ActionEvent actionEvent) {
        try {
            checkLogin(loginTextBox.getText());
            boolean isPresent = service.isUserPresent(loginTextBox.getText());
            if (!isPresent) {
                showError(bundle.getString("NoAccountForLoginError"));
                return;
            }
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        catch (IOException e) {
            showError(bundle.getString("InternalErrorText"));
            return;
        }



        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/fxmls/forgot_password.fxml"), bundle);

        Parent window = null;
        try {
            window = (Pane) fmxlLoader.load();
        } catch (IOException e) {
            showError(bundle.getString("ForgotFormOpenError"));
            return;
        }
        ForgotPasswordFormController conF = fmxlLoader.<ForgotPasswordFormController>getController();
        conF.setBundle(bundle);

        Scene scene = new Scene(window);
        conF.setLogin(loginTextBox.getText());
        Stage stage = new Stage();
        stage.setScene(scene);
        conF.init();
        clear();
        regLoginTextBox.setText("");
        registerButton.setDisable(true);
        stage.show();
    }

    public void registerButtonPress(ActionEvent actionEvent) {
        try {
            checkLogin(regLoginTextBox.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        if (regPasswordTextBox.getText().length() < 8 || regPasswordTextBox.getText().length() > 128) {
            showError(bundle.getString("wrongPasswordLengthError"));
            return;
        }

        try {

            checkEmail(emailTextBox.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        try {
            boolean res = service.isUserPresent(regLoginTextBox.getText());
            //checkLogin(regLoginTextBox.getText());
            if (res) {
                showError(bundle.getString("AccountWithLoginExistsError"));
                return;
            }
            boolean res2 = service.isEmailUsed(emailTextBox.getText());
            if (res2) {
                showError(bundle.getString("EmailInAlreadyUsedError"));
                return;
            }
            service.register(regLoginTextBox.getText(), regPasswordTextBox.getText(), emailTextBox.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        catch (IOException ex) {
            showError(bundle.getString("InternalErrorText"));
            return;
        }
        try {
            openChatForm(regLoginTextBox.getText());
        } catch (IOException e) {
            showError(bundle.getString("SecondFormOpenError"));
            return;
        }
        clear();
        loginTextBox.setText("");
        logInButton.setDisable(true);
        forgetPasswordButton.setDisable(true);
        stage.hide();

    }

    private void openChatForm(String regLoginTextBox) throws IOException {
        FXMLLoader fmxlLoader = new FXMLLoader(getClass().getResource("/fxmls/chat_form.fxml"), bundle);
        Parent window = (Pane) fmxlLoader.load();
        // private Scene scene;
        ChatFormController conC = fmxlLoader.<ChatFormController>getController();
        conC.setBundle(bundle);
        Scene scene = new Scene(window);
        conC.setLogin(regLoginTextBox);
        Stage nstage = new Stage();
        nstage.setScene(scene);
        nstage.setTitle(bundle.getString("ChatFormTitle"));
        conC.setCurrStage(nstage);
        conC.setPrimaryStage(this.stage);
        conC.init();
        nstage.show();
    }

    public void updateLogin(KeyEvent keyEvent) {
        if (loginTextBox.getText().length() == 0) {
            logInButton.setDisable(true);
            forgetPasswordButton.setDisable(true);
            return;
        }
        forgetPasswordButton.setDisable(false);
        if (passwordTextBox.getText().length() == 0) {
            logInButton.setDisable(true);
            return;
        }
        logInButton.setDisable(false);
    }

    public void updateRegisterButton(KeyEvent keyEvent) {
        if (emailTextBox.getText().length() == 0) {
            registerButton.setDisable(true);
            return;
        }
        if (regLoginTextBox.getText().length() == 0) {
            registerButton.setDisable(true);
            return;
        }

        if (regPasswordTextBox.getText().length() == 0) {
            registerButton.setDisable(true);
            return;
        }

        registerButton.setDisable(false);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}

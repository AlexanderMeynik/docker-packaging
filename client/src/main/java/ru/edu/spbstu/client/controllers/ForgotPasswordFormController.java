package ru.edu.spbstu.client.controllers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.request.CheckEmailRequest;
import ru.edu.spbstu.request.SendTemporaryPasswordRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;

import static ru.edu.spbstu.client.utils.Verifiers.checkEmail;

public class ForgotPasswordFormController {
    private  String login;
    @FXML
    private Button changePasswordButton;
    public TextField emailTextBox2;
    private ResourceBundle bundle;
    private HashMap<String,Language> countryToEnum;


    public  void setLogin(String sLogin)
    {
        login= sLogin;
    }
    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    void showError(String errorText)
    {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("Error"));
        alert.setHeaderText(errorText);
        alert.show();
    }

    void init() {
        countryToEnum=HashMap.newHashMap(2);
        countryToEnum.put("RU",Language.RUSSIAN);
        countryToEnum.put("UK",Language.ENGLISH);
    }




    public void changePasswordButtonClick(MouseEvent mouseEvent) {
        try {
            checkEmail(emailTextBox2.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }

        boolean isValid;
        String email= emailTextBox2.getText();
        CheckEmailRequest signUpRequest = new CheckEmailRequest(login,email);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost signUpReq = new HttpPost("http://localhost:8080/check_user_email");
            signUpReq.addHeader("content-type", "application/json");
            signUpReq.setEntity(new StringEntity(jsonMapper.writeValueAsString(signUpRequest), "UTF-8"));
            var temp=client.execute(signUpReq);
            int code=temp.getStatusLine().getStatusCode();
            if(code !=200)
            {
                throw  new IOException("");
            }
            String json = EntityUtils.toString(temp.getEntity());
            isValid=jsonMapper.readValue(json, new TypeReference<>() {});
        }
        catch (IOException ex)
        {
            showError(bundle.getString("InternalErrorText"));
            return;
        }
        if(isValid) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            if(forgotAction()) {
                alert.setTitle(bundle.getString("InformationHeader"));
                alert.setHeaderText(bundle.getString("MessageSendSuccess"));
                alert.showAndWait().ifPresent(rs -> {
                    if (rs == ButtonType.OK) {
                        Stage stage = (Stage) changePasswordButton.getScene().getWindow();
                        stage.close();
                    }
                });
            }
        }
        else
        {
            showError(bundle.getString("BadEmailErrorText"));
        }
    }

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    private boolean forgotAction() {

        int sendStatus;

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPatch signUpReq = new HttpPatch("http://localhost:8080/send-tmp-password");
            signUpReq.addHeader("content-type", "application/json");

            SendTemporaryPasswordRequest sendTemporaryPasswordRequest = new SendTemporaryPasswordRequest();
            sendTemporaryPasswordRequest.setLogin(login);
            sendTemporaryPasswordRequest.setLanguage(getLanguage());

            signUpReq.setEntity(new StringEntity(jsonMapper.writeValueAsString(sendTemporaryPasswordRequest), "UTF-8"));
            sendStatus= client.execute(signUpReq).getStatusLine().getStatusCode();
            if (sendStatus != 200) {
                throw new HttpResponseException(sendStatus,"Error while sendtmppass");
            }
            HttpClientFactory.getInstance().invalidateToken();
            return true;
        }
        catch (IOException e)
        {
            showError( bundle.getString("MessageErrorText"));
            return false;
        }
    }

    private Language getLanguage() {
        String country=bundle.getLocale().getCountry();
        return countryToEnum.get(country);
    }
}

package ru.edu.spbstu.client.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.services.CreateChatFormService;
import ru.edu.spbstu.clientComponents.ListViewWithButtons;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.Language;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;

import static ru.edu.spbstu.client.utils.Verifiers.*;

public class CreateChatFormController {


    public ListViewWithButtons<ChatUser> usersToAddListView;
    public TextField loginTextField;
    public TextField chatNameTextBox;




    public Button createChatButton;
    public Button AddUserButton;
    public Label chatUsersLabel;
    public Label loginLabel;
    public Label ChatNameLabel;

    private CreateChatFormService service=new CreateChatFormService();
    private Stage primaryStage;

    public Stage getCurrStage() {
        return currStage;
    }

    private Stage currStage;
    private List<ChatUser> userList;
    private ChatFormController prevController;
    private ResourceBundle bundle;

    private HashMap<String,Language> countryToEnum;

    public void setPrevController(ChatFormController prevController) {
        this.prevController = prevController;
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
    public void setLogin(String login)
    {
        service.setLogin(login);
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setCurrStage(Stage currStage) {
        this.currStage = currStage;
    }
    @FXML
    void initialize() throws IOException {
    }

    void init() throws IOException {
        AddUserButton.setDisable(true);
        createChatButton.setDisable(true);
        currStage.setOnCloseRequest(event -> {
            primaryStage.show();
        });
        userList=new ArrayList<>(0);

        currStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                primaryStage.show();
                prevController.resumeLoad();
                currStage.close();
            }
        });
        countryToEnum=HashMap.newHashMap(2);
        countryToEnum.put("RU",Language.RUSSIAN);
        countryToEnum.put("UK",Language.ENGLISH);
    }

    /*private void update()
    {

        int size=userList.size();
        ArrayList<Image> images= service.getImageList(userList);
        usersToAddListView.resetList(userList,images);
    }*/


    public void AddUserButtonClick(ActionEvent actionEvent) {
        String username= loginTextField.getText();
        try {
            checkLogin(loginTextField.getText());
        }
        catch (InvalidDataException dat)
        {
            showError(bundle.getString(dat.getMessage()));
            return;
        }
        try {
            boolean pres=service.isUserPresent(username);
            if(!pres)
            {
                showError(bundle.getString("NoUserWithSuchLoginError"));
                return;
            }
        }
        catch(IOException e)
        {
            showError(bundle.getString("InternalErrorText"));
            return;
        }

        String userLof=service.getLogin();

        if(username.equals(userLof))
        {
            showError(bundle.getString("NoNeedToAddCreatorError"));
            return;
        }

        if(usersToAddListView.getList().stream().anyMatch(user -> user.getLogin().equals(username)))
        {
            showError(bundle.getString("UserAlreadyInAddListError"));
            return;
        }
        ChatUser user=new ChatUser(username,false);
        userList.add(user);
        Image image= null;
        try {
            image = service.getImage(user.getLogin());
        } catch (IOException e) {
            image=new Image((getClass().getResource("/images/dAvatar.bmp")).getPath().replaceFirst("/",""));
        }

        usersToAddListView.addInList(user,image);
    }

    public void createChatButtonClick(ActionEvent actionEvent) {

        String name=chatNameTextBox.getText();
        try {
            checkChatName(chatNameTextBox.getText());
        }
        catch (InvalidDataException ex)
        {
            showError(bundle.getString(ex.getMessage()));
            return;
        }
        List<String> logins=new ArrayList<>();
        for (var elem: usersToAddListView.getList())
        {
            logins.add(elem.getLogin());
        }
        try {
            service.addChat(name, logins, countryToEnum.get(bundle.getLocale().getCountry()));
        }
        catch (IOException ex)
        {
            showError(bundle.getString("InternalErrorText"));
            return;
        }
        prevController.addNewChat(name);

        currStage.close();
        primaryStage.show();
    }

    public void chatnameKeyTyped(KeyEvent keyEvent) {
        createChatButton.setDisable(chatNameTextBox.getText().length() == 0);
    }

    public void userLoginTextTypedAction(KeyEvent keyEvent) {
        AddUserButton.setDisable(loginTextField.getText().length()==0);
    }
}

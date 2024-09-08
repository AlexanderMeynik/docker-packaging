package ru.edu.spbstu.client.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.services.ConfigureChatFormService;
import ru.edu.spbstu.clientComponents.ListViewWithButtons;
import ru.edu.spbstu.clientComponents.ListViewWithCheckBoxes;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static ru.edu.spbstu.client.utils.Verifiers.checkLogin;

public class ConfigureChatFormController {
    public ListViewWithCheckBoxes chatMembersConfigurationLV;
    public ListViewWithButtons<ChatUser> usersToAddListView;

    public TabPane mainTabPanel;
    public TextField loginTextField;
    public Tab tabChatSettings;
    public Tab AddUsersTab;
    public Button addUsersButton;
    public Button AddUserButton;
    public Button confirmSettingsButton;
    public Label chatUsersLabel;
    public Label excludeFromChatLabel;
    public Label makeAdministratorLabel;
    public Label userLoginLabel;
    public Label chatNameLabel;


    private final ConfigureChatFormService service=new ConfigureChatFormService();
    private Stage primaryStage;
    private Stage currStage;
    private ChatUser user=new ChatUser();
    private Chat chatToConfigure;
    private ChatFormController prevController;
    private ResourceBundle bundle;
    public boolean test=false;
    public Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> loadAllChatMembers()));

    private void loadAllChatMembers() {


        List<ChatUser> userList= null;
        try {
            userList = service.getChatMembers(chatToConfigure);
        } catch (IOException e) {
            showError(bundle.getString("InternalErrorText"));
            closeStage();
            return;
        }
        var temp=new ChatUser();
        for(var elem:userList)
        {
            if(elem.getLogin().equals(service.getLogin()))
            {
                temp=elem;
                break;
            }
        }
        if(temp.equals(new ChatUser()))
        {
            showError(bundle.getString("ExcludedFromChat"));
            closeStage();
            return;
        }
        user=temp;
        if(!user.getIs_admin())
        {
            return;
        }
        if (mainTabPanel.getTabs().size() < 2) {
            System.out.println("add tab");
            mainTabPanel.getTabs().add(tabChatSettings);//Todo return second tab
        }
        if(userList.size()==1)
        {
            userList=new ArrayList<>();
            tabChatSettings.setDisable(true);
        }
        else
        {
            userList.remove(user);
        }
        ArrayList<Image> images=service.getImageList(userList);
        chatMembersConfigurationLV.resetList(userList,images,false);

    }


    public ResourceBundle getBundle() {
        return bundle;
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }
    
    public void setLogin(String login)
    {
        this.service.setLogin(login);
    }
    public void setChat(Chat selectedItem) {
        chatToConfigure=selectedItem;
    }
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    public void setCurrStage(Stage currStage) {
        this.currStage = currStage;
    }

    @FXML
    void initialize() {


    }
    private void closeStage()
    {
        primaryStage.show();
        this.timeline.stop();
        prevController.resumeLoad();
        currStage.close();
    }
    void init() {
        currStage.getScene().setUserData(this);
        AddUserButton.setDisable(true);
        List<ChatUser> userList= null;
        try {
            userList = service.getChatMembers(chatToConfigure);
        } catch (IOException e) {

            showError(bundle.getString("InternalErrorText"));
            closeStage();
            return;
        }
        for(var elem:userList)
        {
            if(elem.getLogin().equals(service.getLogin()))
            {
                user=elem;
                break;
            }
        }
        if(userList.size()==1)
        {
            userList=new ArrayList<>();
            tabChatSettings.setDisable(true);
        }
        else
        {
            userList.remove(user);
        }
        if(!user.getIs_admin())
        {
            mainTabPanel.getTabs().remove(1);
        }

        ArrayList<Image> images=service.getImageList(userList);
        chatMembersConfigurationLV.resetList(userList,images,false);

        currStage.setOnCloseRequest(e -> {
            closeStage();
        });
        chatNameLabel.setText(chatToConfigure.getName());
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    public void addUsersToChatButtonPress() throws IOException {
        List<String> logins=new ArrayList<>();
        for (var elem: usersToAddListView.getList())
        {
            logins.add(elem.getLogin());
        }
        if(logins.isEmpty())
        {
            return;
        }

        service.addUsersToChat(chatToConfigure,logins);
        tabChatSettings.setDisable(false);
        usersToAddListView.setItems(FXCollections.observableList(new ArrayList<>()));
        //loadAllChatMembers();
       // usersToAddListView.getList().clear();
        update();

    }

    private void update() throws IOException {
        var userList=service.getChatMembers(chatToConfigure);
        if(userList.size()==1)
        {
            userList=new ArrayList<>();
            tabChatSettings.setDisable(true);
            SingleSelectionModel<Tab> selectionModel =mainTabPanel.getSelectionModel();
            selectionModel.select(0);
            mainTabPanel.setSelectionModel(selectionModel);
        }
        else
        {
            userList.remove(user);
        }
        ArrayList<Image> images=service.getImageList(userList);
        chatMembersConfigurationLV.resetList(userList,images,true);
    }

    public void confirmSettingsButtonPress() {
        try {
            service.deleteChatUsers(chatToConfigure, chatMembersConfigurationLV.getUsersToDelete(),test);
            service.setChatUsersAdmins(chatToConfigure, chatMembersConfigurationLV.getUsersToMakeAdmins(),test);
            update();
        }
        catch (IOException e)
        {
            showError(bundle.getString("InternalErrorText"));
            return;
        }
    }

    public void AddUserButtonClick() {

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
            showError(bundle.getString("yourselfNotToAddError"));
            return;
        }
        var userList=chatMembersConfigurationLV.getUsers();
        if(userList.stream().anyMatch(user -> user.getLogin().equals(username)))
        {
            showError(bundle.getString("UserAlreadyChatMemberError"));
            return;
        }

        if(usersToAddListView.getItems().stream().anyMatch(hbox->hbox.getLoginLabelText().equals(username)))
        {
            showError(bundle.getString("UserAlreadyInAddListError"));
            return;
        }
        ChatUser user=new ChatUser(username,false);
        Image image= null;
        try {
            image = service.getImage(user.getLogin());
        } catch (IOException e) {
            image=new Image((getClass().getResource("/images/dAvatar.bmp")).getPath().replaceFirst("/",""));
        }
        usersToAddListView.addInList(user,image);

    }

    private void showError(String s) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("Error"));
        alert.setHeaderText(s);
        alert.show();
    }




    public void setPrevController(ChatFormController prevController) {
        this.prevController = prevController;
    }


    public void userLoginTextTypedAction(KeyEvent keyEvent) {
        AddUserButton.setDisable(loginTextField.getText().length()==0);
    }

    public Stage getCurrStage() {
        return currStage;
    }
}

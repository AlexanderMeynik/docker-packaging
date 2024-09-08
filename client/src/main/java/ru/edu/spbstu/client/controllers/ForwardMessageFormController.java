package ru.edu.spbstu.client.controllers;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.ScrollEvent;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import ru.edu.spbstu.client.services.ForwardMessageFormService;
import ru.edu.spbstu.model.Chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ForwardMessageFormController {

    @FXML
    private ListView<Chat> chatsListView;

    @FXML
    private Button forwardButton;

    @FXML
    private TextField searchTextField;

    private final ForwardMessageFormService service = new ForwardMessageFormService();

    private Stage primaryStage;

    private Stage currentStage;

    private ChatFormController prevController;

    private ResourceBundle bundle;

    public Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(2), e -> loadAllChatPages()));

    private void loadAllChatPages() {
        int temp=(findMode)?chatsFindPage:chatsPage;
        int index=chatsListView.getSelectionModel().getSelectedIndex();
        findChatsEvent();
        for (int i=1;i<temp;i++)
        {
            loadChatPage();
        }
        chatsListView.getSelectionModel().select(index);

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }


    private int chatsPage = 1;
    private int chatsFindPage = 1;
    private boolean findMode = false;

    public void initialize() {
        forwardButton.disableProperty()
                .bind(chatsListView.getSelectionModel().selectedItemProperty().isNull());

    }

    public void init() {
        try {
            chatsListView.getItems().setAll(service.getChats(1));
        } catch (IOException e) {
            showError(e.getMessage());
        }
        currentStage.setOnCloseRequest(e -> {
            close();
        });

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.playFromStart();
    }

    private void showError(String errorText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("Error"));
        alert.setHeaderText(errorText);
        alert.show();
    }

    public void setBundle(ResourceBundle bundle) {
        this.bundle = bundle;
    }

    public void setLogin(String login) {
        service.setLogin(login);
    }

    public void setCurrentStage(Stage currentStage) {
        this.currentStage = currentStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public void setPrevController(ChatFormController prevController) {
        this.prevController = prevController;
    }

    public void forwardMessageButtonPress(ActionEvent event) {
        Chat chat = chatsListView.getSelectionModel().getSelectedItem();
        prevController.forwardNewMessage(chat);
        prevController.resumeLoad();
        this.timeline.stop();
        close();
    }

    private void close() {
        primaryStage.show();
        prevController.resumeLoad();
        this.timeline.stop();
        currentStage.close();
    }


    public void onScrollChatsListView(ScrollEvent scrollEvent) {
        if (scrollEvent.getDeltaY() < 0) {
            loadChatPage();
        }
    }

    public void findChatsEvent() {
        List<Chat> foundChatsList;
        if (searchTextField.getText().isEmpty()) {
            findMode = false;
            try {
                foundChatsList = service.getChats(1);
            } catch (IOException e) {
                showError(e.getMessage());
                return;
            }
            chatsListView.getItems().setAll(foundChatsList);
            chatsPage = 1;
        } else {
            findMode = true;
            try {
                foundChatsList = service.find(searchTextField.getText(), 1L);
            } catch (IOException e) {
                showError(e.getMessage());
                return;
            }
            chatsListView.getItems().setAll(foundChatsList);
            chatsFindPage = 1;
        }
    }

    void loadChatPage() {
        if (!findMode) {
            try {
                chatsPage++;
                List<Chat> temp = service.getChats(chatsPage);
                if (temp.size() != 0) {
                    chatsListView.getItems().addAll(temp);
                } else {
                    chatsPage--;
                }
            } catch (IOException e) {
                showError(bundle.getString("InternalErrorText"));
                chatsListView.getItems().clear();
            }
        } else {
            try {
                chatsFindPage++;
                List<Chat> temp = service.find(searchTextField.getText(), (long) chatsFindPage);
                if (temp.size() != 0) {
                    chatsListView.getItems().addAll(temp);
                } else {
                    chatsFindPage--;
                }
            } catch (IOException e) {
                showError(bundle.getString("InternalErrorText"));
                chatsListView.getItems().clear();
            }
        }
    }
}

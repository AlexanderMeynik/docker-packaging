package SystemTests;

import javafx.application.Platform;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.EmptyNodeQueryException;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.controllers.ConfigureChatFormController;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.clientComponents.ListViewWithCheckBoxes;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.Message;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.testfx.internal.JavaVersionAdapter.getWindows;

public class TestSetUserAdmin extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {

        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestSetUserAdmin ch=new TestSetUserAdmin();
        ch.func();

    }
    @Override
    public  void func() throws IOException {
        sleep(10000);
        for (int i = 0; i < loggins.size(); i++) {
            try {
                register(loggins.get(i), paswwords.get(i), loggins.get(i)+"@gmail.com");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        List<String> temp1 = loggins.subList(1, loggins.size());
        addChat("AllInChat", temp1);
        for (int i = 1; i < 10; i++) {
            addChat("chat" + i, temp1);
        }
        addChat(adminChat, loggins.subList(1, loggins.size()));
    }

    @Test
    public void testSetUserAdmin() {
        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);


        ListView<Chat> chatsListView = find("#chatsListView");
        int n = getChatIndex(chatsListView.getItems().stream().toList(), adminChat);

        clickOnItemInListView(lw, n, 1);
        clickOn("#ConfigChatButton");
        clickOn("#tabChatSettings");

        ListViewWithCheckBoxes LV = find(CHAT_CONFIGURATION_LV);
        int n1 = getUserIndex(LV.getUsers().stream().toList(), loggins.get(2));
        var cell = getListCell(LV, n1).getItem();
        clickOn(cell.getAdminCB());
        clickOn(CONFIRM_SETTINGS_BUTTON);

        ConfigureChatFormController cf = (ConfigureChatFormController) getWindows().get(0).getScene().getUserData();
        Stage st = cf.getCurrStage();
        Platform.runLater(() -> st.fireEvent(new WindowEvent(st, WindowEvent.WINDOW_CLOSE_REQUEST)));
        sleep(100);

        clickOn(Exit_Button);

        logIn(loggins.get(2), paswwords.get(2));
        lw = find(chatListViewStr);


        chatsListView = find("#chatsListView");
        n = getChatIndex(chatsListView.getItems().stream().toList(), adminChat);

        clickOnItemInListView(lw, n, 1);
        clickOn("#ConfigChatButton");
        try {
            lookup("#tabChatSettings");
        } catch (EmptyNodeQueryException ex) {
            throw new InvalidDataException("Expected behaviour: user has admin privileges. Resulting behaviour: user does not have admin privileges");
        }
    }
}

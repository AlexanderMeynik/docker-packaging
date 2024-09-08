package SystemTests;

import javafx.fxml.FXMLLoader;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.EmptyNodeQueryException;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestCreateChat extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {

        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestCreateChat ch=new TestCreateChat();
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
    }

    @Test
    public void testChatAdd() {

        logIn(loggins.get(0), paswwords.get(0));
        clickOn("#addChatButton");
        TextField loginTF = find(USER_LOGIN_TF);

        for (int i = 1; i < 4; i++) {
            String login = loggins.get(i);
            textFieldFastFill(loginTF, login);
            clickOn(ADD_USER_BUTTON);
        }
        String chatName = "chatrand";
        clickOn(CREATE_CHAT_TEXTBOX).write(chatName);
        clickOn(CREATE_CHAT_BUTTON);
        ListView<Chat> chatListView = find("#chatsListView");
        Chat chat = getListCell(chatListView, 0).getItem();
        assertEquals(chatName, chat.getName());
        clickOn(Exit_Button);
        for (int i = 1; i < 4; i++) {
            logIn(loggins.get(i), paswwords.get(i));
            assertEquals(chatName, chat.getName());
            clickOn(Exit_Button);
        }
    }
}

package SystemTests;

import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.Message;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSendMessage extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {

        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestSendMessage ch=new TestSendMessage();
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
        addChat(messagechat, loggins.subList(1, loggins.size()));
    }

    @Test
    public void testSendMessage() {

        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);
        int id = getChatIndex(lw.getItems().stream().toList(), messagechat);
        String chatName = getListCell(lw, id).getItem().getName();
        clickOnItemInListView(lw, id, 0);
        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);

        String message = "message";
        clickOn(MESSAGE_TEXT_AREA).write(message);
        clickOn(SEND_MESSAGE_BUTTON);
        assertEquals(message, messageListView.getItems().get(0).getContent());

        assertEquals(chatName, lw.getItems().get(0).getName());
        clickOn(Exit_Button);
        for (int i = 1; i < loggins.size(); i++) {
            logIn(loggins.get(i), paswwords.get(i));
            assertEquals(chatName, lw.getItems().get(0).getName());
            clickOn(Exit_Button);
        }

    }
}

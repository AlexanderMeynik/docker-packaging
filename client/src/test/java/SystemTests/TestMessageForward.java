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
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TestMessageForward extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {

        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestMessageForward ch=new TestMessageForward();
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
        addChat(replyChat, loggins.subList(1, loggins.size()));
        addChat(messagechat, loggins.subList(1, loggins.size()));
        List<Chat> chats = getChats(1, loggins.get(0));
        int id = getChatIndex(chats, messagechat);
        for (int j = 0; j <= 3; j++) {

            sendMessage(prov, loggins.get(j), chats.get(id).getId(), "Message send" + j + " to chat " + chats.get(id).getName() + " from " + loggins.get(j));
        }
    }

    @Test
    public void testMessageForward() {

        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);
        int id = getChatIndex(lw.getItems().stream().toList(), messagechat);
        clickOnItemInListView(lw, id, 0);
        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        int msg_index = 2;
        clickOnItemInListView(messageListView, msg_index, 1);
        Message originalMessage = messageListView.getItems().get(msg_index).toBuilder().build();

        clickOn("#ForwardMessageButton");


        TextField find = find("#searchTextField");
        clickOn(find).write("ZZZ");
        lw = find("#chatsListView");
        assertEquals(lw.getItems().size(), 0);

        find.clear();
        clickOn(find).write("");

        String Start = "chat";
        clickOn(find).write(Start);

        lw = find("#chatsListView");
        int n = getChatIndex(lw.getItems().stream().toList(), replyChat);
        clickOnItemInListView(lw, n, 0);
        clickOn("#forwardButton");

        clickOn(Exit_Button);
        for (int i = 1; i < loggins.size(); i++) {
            logIn(loggins.get(i), paswwords.get(i));
            lw = find(chatListViewStr);
            assertEquals(replyChat, lw.getItems().get(0).getName());
            clickOnItemInListView(lw, 0, 0);
            messageListView = find(MESSAGE_LIST_VIEW);
            assertEquals(messageListView.getItems().get(0).getContent(), originalMessage.getContent());
            clickOn(Exit_Button);
        }
    }
}

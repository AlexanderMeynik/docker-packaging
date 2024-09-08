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

public class TestChatFind extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {

        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestChatFind ch=new TestChatFind();
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
        addChat(adminChat, loggins.subList(1, loggins.size()));
        addChat(exitChat, loggins.subList(1, loggins.size()));
        addChat(exitChat2, loggins.subList(1, loggins.size()));
        addChat(messagechat, loggins.subList(1, loggins.size()));
    }

    @Test
    public void testChatFind() {
        String Start = "chat";
        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);
        TextField findAr = find("#findChatTextBox");
        clickOn(findAr).write(Start);
        var list = new ArrayList<>(lw.getItems().stream().toList());
        boolean good = true;
        {
            for (Chat el : list) {
                if (!el.getName().startsWith(Start)) {
                    good = false;
                    break;
                }
            }
        }
        assertTrue(good);

        findAr.clear();
        clickOn(findAr).write("");
        clickOn(findAr).write("ZZZ");
        assertEquals(lw.getItems().size(), 0);
    }
}

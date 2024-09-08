package SystemTests;

import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.EmptyNodeQueryException;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.utils.ClientProperties;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class TestNonExistingUserAdd extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestNonExistingUserAdd ch=new TestNonExistingUserAdd();
        ch.func();

    }
    @Override
    public  void func() throws IOException {
        sleep(10000);
        String email=loggins.get(0)+"@gmail.com";
        register(loggins.get(0), paswwords.get(0), email);
        register(loggins.get(1), paswwords.get(1), loggins.get(1)+"@gmail.com");
        List<String> temp1 = loggins.subList(1, 1);
        addChat("AllInChat", temp1);
    }

    @Test
    public void testNonExistingUserAdd() {

        logIn(loggins.get(0), paswwords.get(0));
        clickOn("#addChatButton");
        TextField loginTF = find(USER_LOGIN_TF);

        String login = "someInvalidLogin";
        textFieldFastFill(loginTF, login);

        clickOn(ADD_USER_BUTTON);
        checkAlertHeaderText("NoUserWithSuchLoginError");


    }
}

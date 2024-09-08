package SystemTests;

import javafx.fxml.FXMLLoader;
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

public class TestSucessfulRegister extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestSucessfulRegister ch=new TestSucessfulRegister();
        ch.func();

    }
    @Override
    public  void func() throws IOException {
        sleep(10000);
    }

    @Test
    public void testSucessfullRegister() {

        clickOn("#regTab");
        clickOn("#regLoginTextBox").write("olegoleg2");

        clickOn("#regPasswordTextBox").write("olegoleg2");
        clickOn("#emailTextBox").write("olegoleg2@gmail.com");
        clickOn("#registerButton");
        try {
            lookup("#addChatButton").queryButton();
        } catch (EmptyNodeQueryException ex) {
            throw new InvalidDataException("Expected behaviour: second form opens. Resulting behaviour: second form is not opened");
        }
    }
}

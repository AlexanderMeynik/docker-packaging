package SystemTests;

import javafx.fxml.FXMLLoader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testfx.framework.junit5.ApplicationTest;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.utils.ClientProperties;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

public class TestRegisterAlreadyExists extends SystemTestBase{

    @BeforeAll
    @Override
    public void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        TestRegisterAlreadyExists ch=new TestRegisterAlreadyExists();
        ch.func();

    }
    @Override
    public  void func() throws IOException {
        sleep(10000);
        String email=loggins.get(0)+"@gmail.com";
        register(loggins.get(0), paswwords.get(0), email);
    }

    @Test
    public void testExistingUserRegister() {

        // clean();
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();

        clickOn("#regTab");
        clickOn("#regLoginTextBox").write(loggins.get(0));

        clickOn("#regPasswordTextBox").write(paswwords.get(0));
        clickOn("#emailTextBox").write("olegoleg2@gmail.com");
        clickOn("#registerButton");
        checkAlertHeaderText("AccountWithLoginExistsError");
    }
}

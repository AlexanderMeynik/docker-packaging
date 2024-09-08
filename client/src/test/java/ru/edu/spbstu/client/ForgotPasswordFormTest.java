package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.DialogPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import ru.edu.spbstu.client.controllers.ChatFormController;
import ru.edu.spbstu.client.controllers.ConfigureChatFormController;
import ru.edu.spbstu.client.controllers.ForwardMessageFormController;
import ru.edu.spbstu.client.controllers.LoginFormController;
import ru.edu.spbstu.client.utils.ClientProperties;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.internal.JavaVersionAdapter.getWindows;
import static ru.edu.spbstu.client.ChatFormTest.stub;
import static ru.edu.spbstu.client.ChatFormTest.stubSuccessful;


public class ForgotPasswordFormTest extends ApplicationTest {
    public static final String IS_USER_PRESENT_LOGIN = "/is_user_present\\?login=.*";
    public static final String CHECK_USER_EMAIL = "/check_user_email";
    public static final String SEND_TMP_PASSWORD = "/send-tmp-password";


    public static final String EMAIL_TEXT_BOX = "#emailTextBox2";
    public static final String CHANGE_PASSWORD_BUTTTON = "#changePasswordButton";
    private static WireMockServer wireMockServer;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    LoginFormController concC;


    private static void reset_to_defaults() throws JsonProcessingException {
        wireMockServer.resetToDefaultMappings();

        //to open 2 form
        stubSuccessful(IS_USER_PRESENT_LOGIN, "get", jsonMapper.writeValueAsString(true));

    }

    @BeforeAll
    public static void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();
        reset_to_defaults();
    }


    @BeforeEach
    public void initSecondStage() {
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();


        TextField tx1 = find("#loginTextBox");
        tx1.setText("olegoleg");
        //clickOn("#loginTextBox").write("olegoleg");
        Button fg = find("#forgetPasswordButton");
        fg.setDisable(false);
        clickOn(fg);

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }

    private static Stream<Arguments> checkInvalidEmailFormatFx() {
        return Stream.of(
                arguments("<$efig", "BadFormatEmailErrorText"),
                arguments("o@c.", "BadFormatEmailErrorText"),
                arguments("sss", "InvalidEmailSizeError"),
                arguments("ooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooo", "InvalidEmailSizeError")

        );
    }

    @ParameterizedTest
    @MethodSource("checkInvalidEmailFormatFx")
    public void checkInvalidEmailFormat(String email, String bundle_message) {
        TextField tx = find(EMAIL_TEXT_BOX);
        tx.clear();
        tx.setText(email);

        clickOn(EMAIL_TEXT_BOX).write("");
        clickOn(CHANGE_PASSWORD_BUTTTON);
        checkAlertHeaderText(bundle_message);
    }


    private static Stream<Arguments> checkInvalidEmailForAccount() {
        return Stream.of(
                arguments(200, "BadEmailErrorText", false),
                arguments(404, "InternalErrorText", false),
                arguments(200, "MessageErrorText", true)
        );
    }

    @ParameterizedTest
    @MethodSource("checkInvalidEmailForAccount")
    public void checkInvalidEmailForAccount(int code, String bundle_message, boolean value) throws JsonProcessingException {
        stub(CHECK_USER_EMAIL, "post", code, jsonMapper.writeValueAsString(value));
        String email = "olegoleg@gmail.com";
        TextField tx = find(EMAIL_TEXT_BOX);
        tx.clear();
        tx.setText(email);

        clickOn(EMAIL_TEXT_BOX).write("");
        clickOn(CHANGE_PASSWORD_BUTTTON);
        checkAlertHeaderText(bundle_message);
        reset_to_defaults();
    }

    @Test
    public void testMessageSendSuccess() throws JsonProcessingException {
        stubSuccessful(CHECK_USER_EMAIL, "post", jsonMapper.writeValueAsString(true));
        stubSuccessful(SEND_TMP_PASSWORD, "patch", jsonMapper.writeValueAsString(true));
        String email = "olegoleg@gmail.com";
        TextField tx = find(EMAIL_TEXT_BOX);
        tx.clear();
        tx.setText(email);

        clickOn(EMAIL_TEXT_BOX).write("");
        clickOn(CHANGE_PASSWORD_BUTTTON);
        checkAlertHeaderText("MessageSendSuccess");
        reset_to_defaults();
    }

    @AfterEach
    public void HideStages() throws TimeoutException {
        if (getWindows().size() > 0) {
            for (int i = 0; i < getWindows().size(); i++) {
                Scene currectScene = getWindows().get(i).getScene();
                if (currectScene.getUserData() == null) {
                    continue;
                }

                try {
                    ChatFormController controller = ((ChatFormController) currectScene.getUserData());
                    controller.timeline.stop();
                } catch (ClassCastException ex) {
                    try {
                        ConfigureChatFormController controller = ((ConfigureChatFormController) currectScene.getUserData());
                        controller.timeline.stop();
                    } catch (ClassCastException ex2) {
                        try {
                            ForwardMessageFormController controller = ((ForwardMessageFormController) currectScene.getUserData());
                            controller.timeline.stop();
                        } catch (ClassCastException ex3) {

                        }
                    }
                }
            }


        }
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});

    }


    @AfterAll
    public static void destroy() throws TimeoutException {

        if (getWindows().size() > 0) {
            for (int i = 0; i < getWindows().size(); i++) {

                Scene currectScene = getWindows().get(i).getScene();

                if (currectScene.getUserData() == null) {
                    continue;
                }

                try {
                    ChatFormController controller = ((ChatFormController) currectScene.getUserData());
                    controller.timeline.stop();
                } catch (ClassCastException ex) {
                    try {
                        ConfigureChatFormController controller = ((ConfigureChatFormController) currectScene.getUserData());
                        controller.timeline.stop();
                    } catch (ClassCastException ex2) {
                        ForwardMessageFormController controller = ((ForwardMessageFormController) currectScene.getUserData());
                        controller.timeline.stop();
                    }
                }
            }
        }
        FxToolkit.cleanupStages();
        wireMockServer.shutdown();
        //wireMockServer.stop();
        wireMockServer.resetToDefaultMappings();
    }

    private void checkAlertHeaderText(String bundledMessageId) {
        DialogPane dialog;
        List<DialogPane> lst = lookup(".alert").queryAll().stream()
                .map(node -> (DialogPane) node)
                .collect(Collectors.toList());

        if (lst.size() < 1) {
            throw new NoAlertFoundException();
        }
        dialog = lst.get(lst.size() - 1);
        //dialog = (DialogPane) lookup(".alert").queryAll().iterator().next();

        String alertTitle = dialog.getHeaderText();
        clickOn("OK");

        String expectedMessage = (concC.getBundle().getString(bundledMessageId));
        if (!expectedMessage.equals(alertTitle)) {
            String errtext = (String.format("Expected alert with title \"%s\", got alert with header \"%s\"", expectedMessage, alertTitle));
            throw new AssertionError(errtext);
        }
    }


}

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.edu.spbstu.client.controllers.*;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.User;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Stream.generate;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.internal.JavaVersionAdapter.getWindows;
import static ru.edu.spbstu.client.ChatFormTest.stub;
import static ru.edu.spbstu.client.ChatFormTest.stubSuccessful;

public class ProfileFormTest extends ApplicationTest {

    private static final String GET_EMAIL = "/get_user\\?login=.*";
    private static final String OLD_PASSWORD = "olegoleg";
    private static final String GET_CHATS_LOGIN_PAGE_NUMBER_D = "/get_chats\\?login=.*&page_number=\\d+";

    private static final String GET_PROFILE_PICTURE = "/get_profile_photo\\?login=.+";
    private static final String UPDATE_PASSWORD = "/update_password";
    private static final String UPDATE_EMAIL = "/update_email";
    private static final String EMAIL = "olegoleg@gmail.com";

    private static final String[] PASSWORD_FIELD_QUERIES = {"#oldPasswordTextField", "#newPasswordTextField",
            "#repeatPasswordTextField"};
    private static WireMockServer wireMockServer;

    private static ObjectMapper jsonMapper = new ObjectMapper();

    private LoginFormController concC;

    @BeforeAll
    public static void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        wireMockServer = new WireMockServer(8080);
        wireMockServer.start();//start server

        reset_to_defaults();

    }

    private static void reset_to_defaults() throws JsonProcessingException {
        wireMockServer.resetToDefaultMappings();

        String response = jsonMapper.writeValueAsString(List.of(new Chat(1L, "first")));

        // for opening the chat form
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", response);

        // for opening the profile form
        stubSuccessful(GET_PROFILE_PICTURE, "get", null);
        stubSuccessful(GET_EMAIL, "get",
                jsonMapper.writeValueAsString(new User("olegoleg",
                        "olegoleg", EMAIL)));
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }

    @BeforeEach
    public void initProfile() {
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();
        ((TextField) find("#loginTextBox")).setText("olegoleg");
        ((TextField) find("#passwordTextBox")).setText("olegoleg");
        Button b = find("#logInButton");
        b.setDisable(false);
        clickOn("#logInButton");
        clickOn("#profileButton");
    }

    @AfterEach
    public void afterEachTest() throws TimeoutException {
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
                }
            }
        }
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

    @AfterAll
    public static void cleanup() throws TimeoutException {
        wireMockServer.shutdown();
        wireMockServer.resetToDefaultMappings();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    @Test
    public void testProfileDefaults() {
        TextField emailTextField = find("#emailTextField");
        Assertions.assertTrue(emailTextField.isDisabled());
        Assertions.assertFalse(emailTextField.isEditable());
        Assertions.assertEquals(emailTextField.getText(), EMAIL);

        TextField newEmailTextField = find("#newEmailTextField");
        Assertions.assertTrue(newEmailTextField.isEditable());
        Assertions.assertFalse(newEmailTextField.isDisabled());
        Assertions.assertEquals(newEmailTextField.getText(), "");

        for (String query : PASSWORD_FIELD_QUERIES) {
            TextField passwordField = find(query);
            Assertions.assertFalse(passwordField.isDisabled());
            Assertions.assertEquals(passwordField.getText(), "");
            Assertions.assertTrue(passwordField.isEditable());
        }

        Button changeEmailButton = find("#changeEmailButton");
        Assertions.assertTrue(changeEmailButton.isDisabled());

        Button changePasswordButton = find("#changeEmailButton");
        Assertions.assertTrue(changePasswordButton.isDisabled());

        Button backToChatsButton = find("#backToChatsButton");
        Assertions.assertFalse(backToChatsButton.isDisabled());

        Button changeProfilePictureButton = find("#changeProfilePictureButton");
        Assertions.assertFalse(changeProfilePictureButton.isDisabled());

//        ImageView profilePictureImageView = find("#profilePictureImageView");
//        Image profileImage = profilePictureImageView.getImage();
//        Image correctImage = new Image((getClass().getResource("/images/dAvatar.bmp"))
//                .getPath().replaceFirst("/", ""));
//        byte[] ciBytes = getImageContents(correctImage);
//        byte[] pBytes = getImageContents(profileImage);
//        Assertions.assertEquals(ciBytes, pBytes);
    }

//    private static byte[] getImageContents(Image image) {
//        int width = (int) image.getWidth();
//        int height = (int) image.getHeight();
//        byte[] buffer = new byte[width * height * 4];
//        image.getPixelReader().getPixels(
//                0,
//                0,
//                width,
//                height,
//                PixelFormat.getByteBgraInstance(),
//                buffer,
//                0,
//                width * 4
//        );
//        return buffer;
//    }

    @Test
    public void passwordButtonDisabled() {
        Button changePasswordButton = find("#changePasswordButton");
        String samplePassword = "12";
        final int ALL_ONES = 0b111;
        String[] fields = {"#oldPasswordTextField", "#newPasswordTextField", "#repeatPasswordTextField"};
        for (int i = 0b000; i < ALL_ONES; i++) {
            for (int j = 0; j < fields.length; j++) {
                if (((i >> j) & 1) == 1) {
                    clickOn(fields[j]).write(samplePassword);
                }
            }
            Assertions.assertTrue(changePasswordButton.isDisabled());
            for (String query : fields) {
                TextField textField = find(query);
                clickOn(query).eraseText(textField.getText().length());
            }
        }
    }


    @Test
    public void invalidPasswordLength() {
        List<Integer> invalidLengths = List.of(1, 3, 7, 129, 135);
        spedUpWrite("#oldPasswordTextField", OLD_PASSWORD);
        for (Integer length : invalidLengths) {
            String newPassword = generate(() -> "a").limit(length).collect(joining());
            spedUpWrite("#newPasswordTextField", newPassword);
            spedUpWrite("#repeatPasswordTextField", newPassword);
            clickOn("#changePasswordButton");
            checkAlertHeaderText("wrongPasswordLengthError");
        }
    }

    @Test
    public void differentPasswords() {
        spedUpWrite("#oldPasswordTextField", OLD_PASSWORD);
        spedUpWrite("#newPasswordTextField", "firstPassword");
        spedUpWrite("#repeatPasswordTextField", "secondPassword");
        verifyThat("#changePasswordButton", NodeMatchers.isEnabled());
        clickOn("#changePasswordButton");
        checkAlertHeaderText("passwordsDontMatchError");
    }

    @Test
    public void updatePassword() {
        stubSuccessful(UPDATE_PASSWORD, "post", null);
        String newPassword = "newPassword";
        spedUpWrite("#oldPasswordTextField", OLD_PASSWORD);
        spedUpWrite("#newPasswordTextField", newPassword);
        spedUpWrite("#repeatPasswordTextField", newPassword);
        verifyThat("#changePasswordButton", NodeMatchers.isEnabled());
        clickOn("#changePasswordButton");
        checkAlertHeaderText("passwordChangedInfo");
    }

    @Test
    public void wrongOldPassword() {
        stub(UPDATE_PASSWORD, "post", HttpStatus.SC_BAD_REQUEST, null);
        String newPassword = "newPassword";
        spedUpWrite("#oldPasswordTextField", "wrong");
        spedUpWrite("#newPasswordTextField", newPassword);
        spedUpWrite("#repeatPasswordTextField", newPassword);
        verifyThat("#changePasswordButton", NodeMatchers.isEnabled());
        clickOn("#changePasswordButton");
        checkAlertHeaderText("wrongOldPasswordError");
    }


    private static Stream<Arguments> invalidEmailArguments() {
        List<Integer> wrongLengths = List.of(1, 2, 3, 129, 135);
        List<Arguments> argumentsList = new java.util.ArrayList<>(wrongLengths.stream().map(length -> {
            String testEmail = generate(() -> "a").limit(length).collect(joining());
            return arguments(testEmail, "InvalidEmailSizeError");
        }).toList());
        List<String> badFormatEmails = List.of("@@@@", "....",
                "ab@ab@a.b", "letters", "ab@ab", "ab.ab.ab", "/\\$ab@ab.ru",
                "ab@ab\\$.ru", "ab@ab.\\$ru");
        badFormatEmails.stream()
                .map(badFormatEmail -> arguments(badFormatEmail,
                        "BadFormatEmailErrorText"))
                .forEachOrdered(argumentsList::add);
        return argumentsList.stream();
    }

    @ParameterizedTest
    @MethodSource("invalidEmailArguments")
    public void invalidEmail(String email, String bundleError) throws JsonProcessingException {
        spedUpWrite("#newEmailTextField", email);
        clickOn("#changeEmailButton");
        checkAlertHeaderText(bundleError);
    }

    @Test
    public void changeEmail() {
        stubSuccessful(UPDATE_EMAIL, "post", null);
        String newEmail = "new@gmail.com";
        spedUpWrite("#newEmailTextField", newEmail);
        clickOn("#changeEmailButton");
        checkAlertHeaderText("emailChangedInfo");
        Assertions.assertEquals(newEmail,
                ((TextField) find("#emailTextField")).getText());
    }

    @Test
    public void closeForm() {
        clickOn("#backToChatsButton");
        Assertions.assertThrows(NoSuchElementException.class, () -> find("#newEmailTextField"));
        try {
            find("#messageTextArea");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: profile form closes, chat form opens. Result: "
                    + "profile form is not closed");
        }
    }

    private void spedUpWrite(String query, String text) {
        ((TextField) find(query)).setText(text.substring(0, text.length() - 1));
        clickOn(query).write(text.charAt(text.length() - 1));
    }

    private void checkAlertHeaderText(String bundledMessageId) {
        DialogPane dialog;
        List<DialogPane> lst = lookup(".alert").queryAll().stream()
                .map(node -> (DialogPane) node).toList();

        if (lst.size() < 1) {
            throw new NoAlertFoundException();
        }
        dialog = lst.get(lst.size() - 1);

        String alertTitle = dialog.getHeaderText();
        clickOn("OK");

        String expectedMessage = (concC.getBundle().getString(bundledMessageId));
        if (!expectedMessage.equals(alertTitle)) {
            String errtext = (String.format("Expected alert with title \"%s\", got alert with header \"%s\"", expectedMessage, alertTitle));
            throw new AssertionError(errtext);
        }
    }

}

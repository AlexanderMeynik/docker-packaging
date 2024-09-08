package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.edu.spbstu.client.controllers.*;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.clientComponents.HBoxCell;
import ru.edu.spbstu.clientComponents.ListViewWithButtons;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.User;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.internal.JavaVersionAdapter.getWindows;
import static ru.edu.spbstu.client.ChatFormTest.*;


public class CreateChatFormTest extends ApplicationTest {
    public static final String GET_CHATS_LOGIN_PAGE_NUMBER_D = "/get_chats\\?login=.*&page_number=\\d+";
    public static final String GET_IMAGE = "/get_profile_photo\\?login=.+";
    public static final String MAKE_ADMINS = "/make_users_admins";
    public static final String GET_EMAIL = "/get_user\\?login=.*";
    public static final String FIND_CHATS = "/get_chats_by_search\\?login=.*&begin=.*&page_number=\\d+";
    public static final String IS_USER_PRESENT = "/is_user_present\\?login=.*";
    public static final String CREATE_CHAT = "/create_chat";


    private static final String ADD_USER_BUTTON = "#AddUserButton";
    public static final String USER_LOGIN_TF = "#loginTextField";
    private static final String CREATE_CHAT_BUTTON = "#createChatButton";
    private static final String CREATE_CHAT_TEXTBOX = "#chatNameTextBox";
    private static final String USERS_TO_ADD_LIST_VIEW = "#usersToAddListView";
    private static WireMockServer wireMockServer;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    LoginFormController concC;
    private static List<Chat> chats = new ArrayList<>(Arrays.asList(new Chat(1L, "first"),
            new Chat(2L, "second"),
            new Chat(3L, "third")));


    private static void reset_to_defaults() throws JsonProcessingException {
        wireMockServer.resetToDefaultMappings();
        String responce = jsonMapper.writeValueAsString(chats);

        //to open 2 form
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", responce);

        stubSuccessful(GET_IMAGE, "get", String.valueOf((byte[]) null));
        List<ChatUser> usserlist = Arrays.asList(new ChatUser("login1", false),
                new ChatUser("olegoleg", true),
                new ChatUser("olegoleg2", false));

        stubSuccessful(MAKE_ADMINS, "patch", jsonMapper.writeValueAsString(usserlist));

        stubSuccessful(GET_EMAIL, "get", jsonMapper.writeValueAsString(new User("olegoleg", "olegoleg", "olegoleg@gmail.com")));
        stubSuccessful(IS_USER_PRESENT, "get", jsonMapper.writeValueAsString(true));
        stubSuccessful(CREATE_CHAT, "post", jsonMapper.writeValueAsString(true));
    }

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


    @BeforeEach
    public void initSecondStage() {
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();

        ((TextField) find("#loginTextBox")).setText("olegoleg");
        ((TextField) find("#passwordTextBox")).setText("olegoleg");
        Button b = find("#logInButton");
        b.setDisable(false);
        clickOn("#logInButton");
        clickOn("#addChatButton");

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }


    @Test
    public void testButtonsInit() {
        verifyThat(ADD_USER_BUTTON, NodeMatchers.isDisabled());
        verifyThat(CREATE_CHAT_BUTTON, NodeMatchers.isDisabled());
    }

    @Test
    public void testUserAdd() {
        String login = "sampleLogin";
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);

        ListViewWithButtons<ChatUser> ch = find(USERS_TO_ADD_LIST_VIEW);
        ListCell<HBoxCell<ChatUser>> cell = getListCell(ch, 0);
        assertEquals(login, cell.getItem().getLoginLabelText());
    }

    @Test
    public void testSameUserAdd() {
        String login = "sampleLogin";
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);
        clickOn(ADD_USER_BUTTON);
        checkAlertHeaderText("UserAlreadyInAddListError");
    }

    @Test
    public void testUsersDelete() {
        TextField loginTF = find(USER_LOGIN_TF);
        for (int i = 0; i < 6; i++) {
            String login = "chatUser" + i;
            String temp = login.substring(0, login.length() - 1);
            loginTF.setText(temp);
            clickOn(loginTF).write(login.charAt(login.length() - 1));
            clickOn(ADD_USER_BUTTON);
        }
        int delIndex = 3;
        ListViewWithButtons<ChatUser> ch = find(USERS_TO_ADD_LIST_VIEW);
        ListCell<HBoxCell<ChatUser>> cell = getListCell(ch, delIndex);
        String delName = cell.getItem().getLabelText();
        clickOn(cell.getItem().getButton());
        assertFalse(ch.getList().stream().anyMatch(user -> user.getLogin().equals(delName)));
    }

    @Test
    public void testCreateChat() throws JsonProcessingException {
        TextField loginTF = find(USER_LOGIN_TF);
        for (int i = 0; i < 4; i++) {
            String login = "chatUser" + i;
            String temp = login.substring(0, login.length() - 1);
            loginTF.setText(temp);
            clickOn(loginTF).write(login.charAt(login.length() - 1));
            clickOn(ADD_USER_BUTTON);
        }
        String chatName = "chatName";
        clickOn(CREATE_CHAT_TEXTBOX).write(chatName);
        stubSuccessful(FIND_CHATS, "get", jsonMapper.writeValueAsString(Collections.singletonList(new Chat(4L, chatName))));
        clickOn(CREATE_CHAT_BUTTON);
        ListView<Chat> chatListView = find("#chatsListView");
        Chat chat = getListCell(chatListView, 0).getItem();
        assertEquals(chatName, chat.getName());
    }

    @Test
    public void testCreateChatDuringFind() throws JsonProcessingException, TimeoutException {
        String chatName = "chatName";
        stubSuccessful(FIND_CHATS, "get", jsonMapper.writeValueAsString(Collections.singletonList(new Chat(4L, chatName))));
        TextField loginTF;
        CreateChatFormController cf = (CreateChatFormController) getWindows().get(0).getScene().getUserData();

        Stage st = cf.getCurrStage();


        Platform.runLater(() -> st.fireEvent(new WindowEvent(st, WindowEvent.WINDOW_CLOSE_REQUEST)));
        sleep(100);
        clickOn("#findChatTextBox").write("s");
        clickOn("#addChatButton");
        loginTF = find(USER_LOGIN_TF);
        for (int i = 0; i < 4; i++) {
            String login = "chatUser" + i;
            String temp = login.substring(0, login.length() - 1);
            loginTF.setText(temp);
            clickOn(loginTF).write(login.charAt(login.length() - 1));
            clickOn(ADD_USER_BUTTON);
        }

        clickOn(CREATE_CHAT_TEXTBOX).write(chatName);
        clickOn(CREATE_CHAT_BUTTON);
        ListView<Chat> chatListView = find("#chatsListView");
        Chat chat = getListCell(chatListView, 0).getItem();
        assertEquals(chatName, chat.getName());
    }

    private static Stream<Arguments> testAddUserFixture() {
        return Stream.of(
                arguments(200, "NoUserWithSuchLoginError", "sampleLogin", false),
                arguments(200, "NoNeedToAddCreatorError", "olegoleg", true),
                arguments(404, "InternalErrorText", "sampleLogin", false)

        );
    }

    @ParameterizedTest
    @MethodSource("testAddUserFixture")
    public void testAddUserThatDoesNotExist(int code, String bundleMessage, String login, Boolean status) throws JsonProcessingException {
        if (code == 200) {
            stub(IS_USER_PRESENT, "get", code, jsonMapper.writeValueAsString(status));
        } else {
            stubFor(get(urlMatching(IS_USER_PRESENT))
                    .willReturn(aResponse()
                            .withStatus(code)
                            .withHeader("Content-Type", "application/json")
                    ));
        }
        //stubSuccessful(IS_USER_PRESENT,"get",jsonMapper.writeValueAsString(false));
        //String login="sampleLogin";
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);

        checkAlertHeaderText(bundleMessage);
        stubSuccessful(IS_USER_PRESENT, "get", jsonMapper.writeValueAsString(true));
    }

    private static Stream<Arguments> testInvalidChatNameFixture() {
        return Stream.of(
                arguments("<$ef", "InvalidChatFormatError"),
                arguments("ooooooooooooooooooooooooooooooooooooooooooooooooooo", "InvalidChatSizeError")

        );
    }

    @ParameterizedTest
    @MethodSource("testInvalidChatNameFixture")
    public void testInvalidChatNameFormat(String chatName, String bundleMessage) {
        TextField chatNameTb = find(CREATE_CHAT_TEXTBOX);
        String temp = chatName.substring(0, chatName.length() - 1);
        chatNameTb.setText(temp);
        clickOn(chatNameTb).write(chatName.charAt(chatName.length() - 1));
        clickOn("#createChatButton");
        checkAlertHeaderText(bundleMessage);
    }


    private static Stream<Arguments> testInvalidUserLoginFixture() {
        return Stream.of(
                arguments("<$ef", "BadFormatLoginErrorText"),
                arguments("ooooooooooooooooooooooooooooooooooooooooooooooooooo", "InvalidLoginSizeError")

        );
    }

    @ParameterizedTest
    @MethodSource("testInvalidUserLoginFixture")
    public void testInvalidUserLoginFormat(String login, String bundleMessage) {
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);
        checkAlertHeaderText(bundleMessage);
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
        //wireMockServer.stop();
        wireMockServer.shutdown();
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

        //Interesting way to get loaclization for our application(via getting the fxmloader that was store previously)

        String expectedMessage = (concC.getBundle().getString(bundledMessageId));
        if (!expectedMessage.equals(alertTitle)) {
            String errtext = (String.format("Expected alert with title \"%s\", got alert with header \"%s\"", expectedMessage, alertTitle));
            throw new AssertionError(errtext);
        }
    }


    private <T> ListCell<T> getListCell(ListView<T> listView, int index) {
        VirtualFlow<ListCell<T>> virtualFlow = (VirtualFlow) listView.lookup("#virtual-flow");
        return virtualFlow.getCell(index);
    }


}

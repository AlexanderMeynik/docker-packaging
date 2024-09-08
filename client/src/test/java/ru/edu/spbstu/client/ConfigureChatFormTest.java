package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.matcher.base.NodeMatchers;
import ru.edu.spbstu.client.controllers.ChatFormController;
import ru.edu.spbstu.client.controllers.ConfigureChatFormController;
import ru.edu.spbstu.client.controllers.ForwardMessageFormController;
import ru.edu.spbstu.client.controllers.LoginFormController;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.clientComponents.HBoxCell;
import ru.edu.spbstu.clientComponents.ListViewWithButtons;
import ru.edu.spbstu.clientComponents.ListViewWithCheckBoxes;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.internal.JavaVersionAdapter.getWindows;
import static ru.edu.spbstu.client.ChatFormTest.*;


public class ConfigureChatFormTest extends ApplicationTest {
    public static final String GET_CHATS_LOGIN_PAGE_NUMBER_D = "/get_chats\\?login=.*&page_number=\\d+";
    public static final String GET_IMAGE = "/get_profile_photo\\?login=.+";
    public static final String GET_MEMBERS = "/get_chat_members\\?chat_id=\\d+";
    public static final String DELETE_USERS = "/delete_users_from_chat";
    public static final String MAKE_ADMINS = "/make_users_admins";
    public static final String GET_EMAIL = "/get_user\\?login=.*";
    public static final String FIND_CHATS = "/get_chats_by_search\\?login=.*&begin=.*&page_number=\\d+";
    public static final String IS_USER_PRESENT = "/is_user_present\\?login=.*";


    private static final String chatListViewStr = "#chatsListView";
    public static final String USERS_TO_ADD_LV = "#usersToAddListView";
    public static final String USER_LOGIN_TF = "#loginTextField";
    public static final String ADD_USER_BUTTON = "#AddUserButton";
    public static final String ADD_USERS_BUTTON = "#addUsersButton";
    public static final String CHAT_CONF_TAB = "#tabChatSettings";
    public static final String CHAT_CONFIGURATION_LV = "#chatMembersConfigurationLV";
    public static final String CONFIRM_SETTINGS_BUTTON = "#confirmSettingsButton";
    private static WireMockServer wireMockServer;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    LoginFormController concC;

    private static ChatUser currUser = new ChatUser("olegoleg", true);

    private static List<Chat> chats = new ArrayList<>(Arrays.asList(new Chat(1L, "first"),
            new Chat(2L, "second"),
            new Chat(3L, "third")));

    private static ArrayList<ChatUser> usserlist = new ArrayList<>(Arrays.asList(new ChatUser("login1", false),
            new ChatUser("olegoleg", true),
            new ChatUser("olegoleg2", false)));

    private static void reset_to_defaults() throws JsonProcessingException {
        wireMockServer.resetToDefaultMappings();
        String responce = jsonMapper.writeValueAsString(chats);

        //to open 2 form
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", responce);

        //to find chats
        stubSuccessful(FIND_CHATS, "get", responce);

        //get messages
        {
            stubSuccessful(GET_IMAGE, "get", String.valueOf((byte[]) null));
        }
        // usserlist

        //to config
        stubSuccessful(IS_USER_PRESENT, "get", jsonMapper.writeValueAsString(true));
        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
        //to exit chat
        {
            stubSuccessful(DELETE_USERS, "patch", null);
            stubSuccessful(MAKE_ADMINS, "patch", null);
        }

        stubSuccessful(GET_EMAIL, "get", jsonMapper.writeValueAsString(new User("olegoleg", "olegoleg", "olegoleg@gmail.com")));
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


        ((TextField) find("#loginTextBox")).setText("olegoleg");
        ((TextField) find("#passwordTextBox")).setText("olegoleg");
        Button b = find("#logInButton");
        b.setDisable(false);
        clickOn("#logInButton");
        ListView<Chat> chatListView = find(chatListViewStr);

        clickOnItemInListView(chatListView, 0, 1);
        clickOn("#ConfigChatButton");
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
    }


    @Test
    public void testUserAdd() {
        String login = "sampleLogin";
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);

        ListViewWithButtons<ChatUser> ch = find(USERS_TO_ADD_LV);
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
    public void testDeleteUserFromAdditionList() {
        TextField loginTF = find(USER_LOGIN_TF);
        for (int i = 0; i < 6; i++) {
            String login = "chatUser" + i;
            String temp = login.substring(0, login.length() - 1);
            loginTF.setText(temp);
            clickOn(loginTF).write(login.charAt(login.length() - 1));
            clickOn(ADD_USER_BUTTON);
        }
        int delIndex = 3;
        ListViewWithButtons<ChatUser> ch = find(USERS_TO_ADD_LV);
        ListCell<HBoxCell<ChatUser>> cell = getListCell(ch, delIndex);
        String delName = cell.getItem().getLabelText();
        clickOn(cell.getItem().getButton());
        assertFalse(ch.getList().stream().anyMatch(user -> user.getLogin().equals(delName)));
    }

    private static Stream<Arguments> testAddUserFixture() {
        return Stream.of(
                arguments(200, "NoUserWithSuchLoginError", "sampleLogin", false),
                arguments(200, "yourselfNotToAddError", "olegoleg", true),
                arguments(200, "UserAlreadyChatMemberError", "login1", true),
                arguments(404, "InternalErrorText", "sampleLogin", false)

        );
    }

    @ParameterizedTest
    @MethodSource("testAddUserFixture")
    public void testAddUserLabelErrors(int code, String bundleMessage, String login, Boolean status) throws JsonProcessingException {
        if (code == 200) {
            stub(IS_USER_PRESENT, "get", code, jsonMapper.writeValueAsString(status));
        } else {
            stubFor(get(urlMatching(IS_USER_PRESENT))
                    .willReturn(aResponse()
                            .withStatus(code)
                            .withHeader("Content-Type", "application/json")
                    ));
        }
        TextField loginTF = find(USER_LOGIN_TF);
        String temp = login.substring(0, login.length() - 1);
        loginTF.setText(temp);
        clickOn(loginTF).write(login.charAt(login.length() - 1));
        clickOn(ADD_USER_BUTTON);

        checkAlertHeaderText(bundleMessage);
        stubSuccessful(IS_USER_PRESENT, "get", jsonMapper.writeValueAsString(true));
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

    @Test
    public void testAddUsers() throws JsonProcessingException {
        TextField loginTF = find(USER_LOGIN_TF);
        for (int i = 0; i < 4; i++) {
            String login = "chatUser" + i;
            usserlist.add(new ChatUser(login, false));
            String temp = login.substring(0, login.length() - 1);
            loginTF.setText(temp);
            clickOn(loginTF).write(login.charAt(login.length() - 1));
            clickOn(ADD_USER_BUTTON);
        }

        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
        clickOn(ADD_USERS_BUTTON);
        clickOn(CHAT_CONF_TAB);
        ListViewWithCheckBoxes LV = find(CHAT_CONFIGURATION_LV);
        var temp = new ArrayList<>(usserlist);
        temp.remove(currUser);
        assertEquals(LV.getUsers(), temp);
        usserlist = new ArrayList<>(Arrays.asList(new ChatUser("login1", false),
                new ChatUser("olegoleg", true),
                new ChatUser("olegoleg2", false)));
        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
    }

    @Test
    public void testDisableOtherOption() {
        clickOn(CHAT_CONF_TAB);
        ListViewWithCheckBoxes lv = find(CHAT_CONFIGURATION_LV);
        var cell = getListCell(lv, 0).getItem();

        clickOn(cell.getAdminCB());
        verifyThat(cell.getDeleteCB(), NodeMatchers.isDisabled());

    }


    @Test
    public void testDeleteAllUsers() throws JsonProcessingException {
        ConfigureChatFormController controller = (ConfigureChatFormController) getWindows().get(0).getScene().getUserData();
        controller.timeline.pause();


        clickOn(CHAT_CONF_TAB);
        ListViewWithCheckBoxes lv = find(CHAT_CONFIGURATION_LV);
        for (int i = 0; i < lv.getUsers().size(); i++) {
            var cell = getListCell(lv, i).getItem();
            clickOn(cell.getDeleteCB());
        }

        usserlist = new ArrayList<>(Collections.singletonList(new ChatUser("olegoleg", true)));


        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));

        controller.test = true;
        clickOn(CONFIRM_SETTINGS_BUTTON);


        assertTrue(controller.tabChatSettings.isDisabled());


        usserlist = new ArrayList<>(Arrays.asList(new ChatUser("login1", false),
                new ChatUser("olegoleg", true),
                new ChatUser("olegoleg2", false)));
        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
    }


    @Test
    public void testUsersDeleteAndMakeAdmins() throws JsonProcessingException {


        for (int i = 0; i < 4; i++) {
            String login = "chatUser" + i;
            usserlist.add(new ChatUser(login, false));
        }
        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));

        sleep(2000);
        clickOn(CHAT_CONF_TAB);
        ListViewWithCheckBoxes lv = find(CHAT_CONFIGURATION_LV);
        ConfigureChatFormController controller = (ConfigureChatFormController) getWindows().get(0).getScene().getUserData();
        controller.timeline.pause();
        int[] adminArr = {0};
        int[] deletearr = {3, 4};

        for (int i : adminArr) {
            var cell = getListCell(lv, i).getItem();
            clickOn(cell.getAdminCB());
            usserlist.get(i).setIs_admin(true);
        }

        for (int i = deletearr.length - 1; i >= 0; i--) {
            var cell = getListCell(lv, deletearr[i]).getItem();
            clickOn(cell.getDeleteCB());
            usserlist.remove(deletearr[i]);
        }

        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));

        controller.test = true;
        clickOn(CONFIRM_SETTINGS_BUTTON);


        for (int i : adminArr) {
            var cell = getListCell(lv, i).getItem();
            assertTrue(cell.getDeleteCB().isDisabled());
            assertTrue(cell.getAdminCB().isDisabled());
        }
        usserlist = new ArrayList<>(Arrays.asList(new ChatUser("login1", false),
                new ChatUser("olegoleg", true),
                new ChatUser("olegoleg2", false)));

        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
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
        // wireMockServer.stop();
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

        String expectedMessage = (concC.getBundle().getString(bundledMessageId));
        if (!expectedMessage.equals(alertTitle)) {
            String errtext = (String.format("Expected alert with title \"%s\", got alert with header \"%s\"", expectedMessage, alertTitle));
            throw new AssertionError(errtext);
        }
    }

    private <T> void clickOnItemInListView(ListView<T> listView, int index, int type) {
        VirtualFlow<ListCell<T>> virtualFlow = (VirtualFlow) listView.lookup("#virtual-flow");
        ListCell<T> cell = virtualFlow.getCell(index);
        if (type > 0)
            rightClickOn(cell);
        else {
            clickOn(cell);
        }
    }

    private <T> ListCell<T> getListCell(ListView<T> listView, int index) {
        VirtualFlow<ListCell<T>> virtualFlow = (VirtualFlow) listView.lookup("#virtual-flow");
        return virtualFlow.getCell(index);
    }


}

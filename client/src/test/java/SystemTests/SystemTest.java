package SystemTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.BeforeClass;
import org.junit.jupiter.api.*;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import org.testfx.service.query.EmptyNodeQueryException;
import org.testfx.util.WaitForAsyncUtils;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.NoAlertFoundException;
import ru.edu.spbstu.client.controllers.*;
import ru.edu.spbstu.client.exception.InvalidDataException;
import ru.edu.spbstu.client.utils.AuthScheme;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.clientComponents.ListViewWithCheckBoxes;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.request.CreateChatRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.request.SignUpRequest;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;
import static org.testfx.internal.JavaVersionAdapter.getWindows;


public class SystemTest extends ApplicationTest {
   /* LoginFormController concC;
    private static final List<String> loggins = Arrays.asList("olegoleg", "Llananoker", "Relldastr", "Onadelestr", "Jusarorol");
    private static final List<String> paswwords = Arrays.asList("olegoleg", "Llananoker", "Relldastr", "Onadelestr", "Jusarorol");
    private final CredentialsProvider prov = new BasicCredentialsProvider();
    private final String MESSAGE_LIST_VIEW = "#messagesListView";
    public static final String MESSAGE_TEXT_AREA = "#messageTextArea";
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final String CONFIRM_SETTINGS_BUTTON = "#confirmSettingsButton";

    private static final String ADD_USER_BUTTON = "#AddUserButton";
    public static final String CHAT_CONFIGURATION_LV = "#chatMembersConfigurationLV";
    public static final String USER_LOGIN_TF = "#loginTextField";
    private static final String CREATE_CHAT_BUTTON = "#createChatButton";
    private static final String CREATE_CHAT_TEXTBOX = "#chatNameTextBox";
    private static final String Exit_Button = "#logOutButton";

    private static final String chatListViewStr = "#chatsListView";

    public static final String SEND_MESSAGE_BUTTON = "#sendMessageButton";


    final String replyChat = "chatreply";
    final String adminChat = "adminChat";
    final String exitChat = "exitChat";
    final String exitChat2 = "exitChat2";
    final String messagechat = "messagechat";

    @BeforeClass
    public static void setUpdHeadless()
    {

    }
    @BeforeAll
    public static void initServer() throws Exception {
        System.out.println("start");
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        SystemTest ch=new SystemTest();
        ch.func();

    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
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
    }

    public void register(String login, String password, String email) throws IOException {
        int regStatus ;
        SignUpRequest signUpRequest = new SignUpRequest(login, password, email);
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost signUpReq = new HttpPost("http://localhost:8080/register");
            signUpReq.addHeader("content-type", "application/json");
            signUpReq.setEntity(new StringEntity(jsonMapper.writeValueAsString(signUpRequest), "UTF-8"));
            regStatus= client.execute(signUpReq).getStatusLine().getStatusCode();
        }
        if (regStatus != 200) {
            throw new HttpResponseException(regStatus, "Error while register");
        }


        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        prov.setCredentials(AuthScope.ANY, credentials);
    }


    private static void sendMessage(CredentialsProvider provider, String login, Long chat_id, String message) throws IOException {
        SendMessageRequest request = new SendMessageRequest(login, login, chat_id, message);
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost post = new HttpPost("http://localhost:8080/send_message");
            post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
            post.addHeader("content-type", "application/json");
            CloseableHttpResponse re = client.execute(post);
            re.getStatusLine().getStatusCode();
        }
    }




    public List<Chat> getChats(Integer page, String login) throws IOException {

        String getChatsUrlBlueprint = "http://localhost:8080/get_chats?login=%s&page_number=%d";

        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(prov)
                .build()) {
            HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint, login, page));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            if (re.getStatusLine().getStatusCode() == 400) {
                return new ArrayList<>();
            }
            return jsonMapper.readValue(json, new TypeReference<>() {
            });
        }
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }

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

        List<Chat> chats = getChats(1, loggins.get(0));
        int id = getChatIndex(chats, messagechat);
        for (int j = 0; j <= 3; j++) {

            sendMessage(prov, loggins.get(j), chats.get(id).getId(), "Message send" + j + " to chat " + chats.get(id).getName() + " from " + loggins.get(j));
        }
    }


    public void addChat(String chatName, List<String> users) throws IOException {

        CreateChatRequest request = new CreateChatRequest();
        request.setAdmin_login(loggins.get(0));
        request.setChat_name(chatName);
        request.setUser_logins(users);
        request.setLanguage(Language.ENGLISH);
        int reqStatusCreateChat;
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(prov)
                .build()) {
            HttpPost post = new HttpPost("http://localhost:8080/create_chat");
            post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
            post.addHeader("content-type", "application/json");
            CloseableHttpResponse re = client.execute(post);
            reqStatusCreateChat = re.getStatusLine().getStatusCode();
        }

        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat, "Error while addChat");
        }
    }



    private static int getChatIndex(List<Chat> list, String chatName) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(chatName)) {
                return i;
            }
        }
        return -1;
    }

    private static int getUserIndex(List<ChatUser> list, String userLogin) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLogin().equals(userLogin)) {
                return i;
            }
        }
        return -1;
    }
    private void textFieldFastFill(TextField textField, String string) {
        String temp = string.substring(0, string.length() - 1);
        textField.setText(temp);
        clickOn(textField).write(string.charAt(string.length() - 1));
    }

    private void logIn(String login, String password) {
        clickOn("#logInTab");
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();

        TextField t1 = find("#loginTextBox");
        t1.clear();
        TextField t2 = find("#passwordTextBox");
        t2.clear();
        textFieldFastFill(t1, login);
        textFieldFastFill(t2, password);
        clickOn("#logInButton");
    }


    private void checkAlertHeaderText(String bundledMessageId) {
        DialogPane dialog;
        List<DialogPane> lst = lookup(".alert").queryAll().stream()
                .map(node -> (DialogPane) node)
                .toList();

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


    @Test
    public void testChatAdd() {

        logIn(loggins.get(0), paswwords.get(0));
        clickOn("#addChatButton");
        TextField loginTF = find(USER_LOGIN_TF);

        for (int i = 1; i < 4; i++) {
            String login = loggins.get(i);
            textFieldFastFill(loginTF, login);
            clickOn(ADD_USER_BUTTON);
        }
        String chatName = "chatrand";
        clickOn(CREATE_CHAT_TEXTBOX).write(chatName);
        clickOn(CREATE_CHAT_BUTTON);
        ListView<Chat> chatListView = find("#chatsListView");
        Chat chat = getListCell(chatListView, 0).getItem();
        assertEquals(chatName, chat.getName());
        clickOn(Exit_Button);
        for (int i = 1; i < 4; i++) {
            logIn(loggins.get(i), paswwords.get(i));
            assertEquals(chatName, chat.getName());
            clickOn(Exit_Button);
        }
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

    @Test
    public void testSetUserAdmin() {
        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);


        ListView<Chat> chatsListView = find("#chatsListView");
        int n = getChatIndex(chatsListView.getItems().stream().toList(), adminChat);

        clickOnItemInListView(lw, n, 1);
        clickOn("#ConfigChatButton");
        clickOn("#tabChatSettings");

        ListViewWithCheckBoxes LV = find(CHAT_CONFIGURATION_LV);
        int n1 = getUserIndex(LV.getUsers().stream().toList(), loggins.get(2));
        var cell = getListCell(LV, n1).getItem();
        clickOn(cell.getAdminCB());
        clickOn(CONFIRM_SETTINGS_BUTTON);

        ConfigureChatFormController cf = (ConfigureChatFormController) getWindows().get(0).getScene().getUserData();
        Stage st = cf.getCurrStage();
        Platform.runLater(() -> st.fireEvent(new WindowEvent(st, WindowEvent.WINDOW_CLOSE_REQUEST)));
        sleep(100);

        clickOn(Exit_Button);

        logIn(loggins.get(2), paswwords.get(2));
        lw = find(chatListViewStr);


        chatsListView = find("#chatsListView");
        n = getChatIndex(chatsListView.getItems().stream().toList(), adminChat);

        clickOnItemInListView(lw, n, 1);
        clickOn("#ConfigChatButton");
        try {
            lookup("#tabChatSettings");
        } catch (EmptyNodeQueryException ex) {
            throw new InvalidDataException("Expected behaviour: user has admin privileges. Resulting behaviour: user does not have admin privileges");
        }
    }


    @Test
    public void testUserExit() {
        int exitId = 1;
        logIn(loggins.get(exitId), paswwords.get(exitId));
        ListView<Chat> lw = find(chatListViewStr);


        ListView<Chat> chatsListView = find("#chatsListView");
        int n = getChatIndex(chatsListView.getItems().stream().toList(), exitChat2);
        clickOnItemInListView(lw, n, 1);
        clickOn("#ExitChatButton");
        clickOn(Exit_Button);


        logIn(loggins.get(0), paswwords.get(0));
        chatsListView = find("#chatsListView");
        n = getChatIndex(chatsListView.getItems().stream().toList(), exitChat2);
        clickOnItemInListView(chatsListView, n, 1);
        clickOn("#ConfigChatButton");

        clickOn("#tabChatSettings");

        ListViewWithCheckBoxes LV = find(CHAT_CONFIGURATION_LV);
        var arr2 = LV.getUsers();
        boolean isPresent = false;
        for (int i = 0; i < LV.getItems().size(); i++) {
            if (arr2.get(i).getLogin().equals(loggins.get(exitId))) {
                isPresent = true;
                break;
            }
        }
        assertFalse(isPresent);
    }


    @Test
    public void testSetAdminAfterExit() {
        logIn(loggins.get(0), paswwords.get(0));
        ListView<Chat> lw = find(chatListViewStr);


        ListView<Chat> chatsListView = find("#chatsListView");
        int n = getChatIndex(chatsListView.getItems().stream().toList(), exitChat);

        clickOnItemInListView(lw, n, 1);
        clickOn("#ExitChatButton");
        clickOn(Exit_Button);


        boolean admin = false;
        int i = 1;
        for (; i < loggins.size(); i++) {
            logIn(loggins.get(i), paswwords.get(i));
            chatsListView = find("#chatsListView");
            var arr = chatsListView.getItems();

            int j = getChatIndex(chatsListView.getItems().stream().toList(), exitChat);
            clickOnItemInListView(chatsListView, j, 1);
            clickOn("#ConfigChatButton");
            try {
                clickOn("#tabChatSettings");
                admin = true;
                break;

            } catch (EmptyNodeQueryException ex) {

            } catch (org.testfx.api.FxRobotException ex) {

            }
            ConfigureChatFormController cf = (ConfigureChatFormController) getWindows().get(0).getScene().getUserData();

            Stage st = cf.getCurrStage();


            Platform.runLater(() -> st.fireEvent(new WindowEvent(st, WindowEvent.WINDOW_CLOSE_REQUEST)));
            sleep(100);
            clickOn(Exit_Button);
        }
        assertTrue(admin);

    }*/



}

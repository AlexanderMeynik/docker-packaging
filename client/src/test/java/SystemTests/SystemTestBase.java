package SystemTests;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.DialogPane;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit5.ApplicationTest;
import ru.edu.spbstu.client.ClientApplication;
import ru.edu.spbstu.client.NoAlertFoundException;
import ru.edu.spbstu.client.controllers.ChatFormController;
import ru.edu.spbstu.client.controllers.ConfigureChatFormController;
import ru.edu.spbstu.client.controllers.ForwardMessageFormController;
import ru.edu.spbstu.client.controllers.LoginFormController;
import ru.edu.spbstu.client.utils.AuthScheme;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.Language;
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

import static org.testfx.internal.JavaVersionAdapter.getWindows;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SystemTestBase  extends ApplicationTest {
    LoginFormController concC;
    protected static final List<String> loggins = Arrays.asList("olegoleg", "Llananoker", "Relldastr", "Onadelestr", "Jusarorol");
    protected static final List<String> paswwords = Arrays.asList("olegoleg", "Llananoker", "Relldastr", "Onadelestr", "Jusarorol");
    protected final CredentialsProvider prov = new BasicCredentialsProvider();
    protected final String MESSAGE_LIST_VIEW = "#messagesListView";
    public static final String MESSAGE_TEXT_AREA = "#messageTextArea";
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public static final String CONFIRM_SETTINGS_BUTTON = "#confirmSettingsButton";

    protected static final String ADD_USER_BUTTON = "#AddUserButton";
    public static final String CHAT_CONFIGURATION_LV = "#chatMembersConfigurationLV";
    public static final String USER_LOGIN_TF = "#loginTextField";
    protected static final String CREATE_CHAT_BUTTON = "#createChatButton";
    protected static final String CREATE_CHAT_TEXTBOX = "#chatNameTextBox";
    protected static final String Exit_Button = "#logOutButton";

    protected static final String chatListViewStr = "#chatsListView";

    public static final String SEND_MESSAGE_BUTTON = "#sendMessageButton";

    @BeforeAll
    public void initServer() throws Exception {
        ClientProperties.setProperties("RU");
        ApplicationTest.launch(ClientApplication.class);
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
        //todo init spring server here
        SystemTestBase ch=new SystemTestBase();
        ch.func();

    }



    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
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

    final String replyChat = "chatreply";
    final String adminChat = "adminChat";
    final String exitChat = "exitChat";
    final String exitChat2 = "exitChat2";
    final String messagechat = "messagechat";

    protected static void sendMessage(CredentialsProvider provider, String login, Long chat_id, String message) throws IOException {
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

    public void func() throws IOException {
        ArrayList<String> emails = new ArrayList<>(0);

        for (var elem : loggins) {
            emails.add(elem + "@gmail.com");
        }
        for (int i = 0; i < loggins.size(); i++) {
            try {
                register(loggins.get(i), paswwords.get(i), emails.get(i));
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
    protected static int getChatIndex(List<Chat> list, String chatName) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getName().equals(chatName)) {
                return i;
            }
        }
        return -1;
    }

    protected static int getUserIndex(List<ChatUser> list, String userLogin) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getLogin().equals(userLogin)) {
                return i;
            }
        }
        return -1;
    }
    protected void textFieldFastFill(TextField textField, String string) {
        String temp = string.substring(0, string.length() - 1);
        textField.setText(temp);
        clickOn(textField).write(string.charAt(string.length() - 1));
    }

    protected void logIn(String login, String password) {
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

    protected void checkAlertHeaderText(String bundledMessageId) {
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
    public void destroy() throws TimeoutException {

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
        //Todo kill server
    }

    protected <T> void clickOnItemInListView(ListView<T> listView, int index, int type) {
        VirtualFlow<ListCell<T>> virtualFlow = (VirtualFlow) listView.lookup("#virtual-flow");
        ListCell<T> cell = virtualFlow.getCell(index);
        if (type > 0)
            rightClickOn(cell);
        else {
            clickOn(cell);
        }
    }

    protected <T> ListCell<T> getListCell(ListView<T> listView, int index) {
        VirtualFlow<ListCell<T>> virtualFlow = (VirtualFlow) listView.lookup("#virtual-flow");
        return virtualFlow.getCell(index);
    }

}

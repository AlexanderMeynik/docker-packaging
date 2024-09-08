package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
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
import ru.edu.spbstu.client.controllers.ChatFormController;
import ru.edu.spbstu.client.controllers.ConfigureChatFormController;
import ru.edu.spbstu.client.controllers.ForwardMessageFormController;
import ru.edu.spbstu.client.controllers.LoginFormController;
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.Message;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.internal.JavaVersionAdapter.getWindows;
import static ru.edu.spbstu.client.ChatFormTest.*;

public class ForwardMessageFormTest  extends ApplicationTest {
    private static WireMockServer wireMockServer;
    private static ObjectMapper jsonMapper = new ObjectMapper();
    private LoginFormController concC;
    private Message originalMessage;
    private static final String GET_CHATS_LOGIN_PAGE_NUMBER_D = "/get_chats\\?login=.*&page_number=\\d+";
    private static final String GET_PROFILE_PICTURE = "/get_profile_photo\\?login=.+";
    private static final String GET_MESSAGES = "/get_messages\\?chat_id=\\d+&page_number=\\d+";
    private static final String GET_MESSAGES_1 = "/get_messages\\?chat_id=1&page_number=\\d+";
    private static final String GET_MESSAGES_2 = "/get_messages\\?chat_id=2&page_number=\\d+";
    private static final String GET_CHATS_BY_SEARCH = "/get_chats_by_search\\?login=.+&begin=.+&page_number=\\d+";
    public static final String FORWARD_MESSAGE = "/forward_message\\?message_id=\\d+&sender_login=.*&chat_id=\\d+";


    private static final List<Chat> chats = new ArrayList<>(Arrays.asList(new Chat(1L, "chat1"),
            new Chat(2L, "chat2"),
            new Chat(3L, "thirdChat"),
            new Chat(4L, "fourthChat")));

    private static final ArrayList<Message> messages = new ArrayList<>(Arrays.asList(messageGenerator(1L, 1L, "login1", "login1", "message1", ChatFormTest.MessageType.SIMPLE),
            messageGenerator(2L, 1L, "olegoleg", "olegoleg", "message21", ChatFormTest.MessageType.SIMPLE),
            messageGenerator(3L, 1L, "olegoleg", "olegoleg", "message22", ChatFormTest.MessageType.SIMPLE)));

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

        String response = jsonMapper.writeValueAsString(chats);

        // for opening the chat form
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", response);

        // profile pictures for messages
        stubSuccessful(GET_PROFILE_PICTURE, "get", null);

        //get messages
        String response2 = jsonMapper.writeValueAsString(messages);
        stubSuccessful(GET_MESSAGES, "get", response2);

        stubSuccessful(FORWARD_MESSAGE, "post", String.valueOf((byte[]) null));
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }

    private void spedUpWrite(String query, String text) {
        ((TextField) find(query)).setText(text.substring(0, text.length() - 1));
        clickOn(query).write(text.charAt(text.length() - 1));
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    @BeforeEach
    public void initProfile() {
        concC = ((FXMLLoader) find("#forgetPasswordButton").getScene().getUserData()).getController();
        ((TextField) find("#loginTextBox")).setText("olegoleg");
        ((TextField) find("#passwordTextBox")).setText("olegoleg");
        Button b = find("#logInButton");
        b.setDisable(false);
        clickOn("#logInButton");
        ListView<Chat> chatLst = find("#chatsListView");
        clickOnItemInListView(chatLst, 0, 0);

        ListView<Message> messageListView = find("#messagesListView");
        originalMessage = messageListView.getItems().get(1);
        clickOnItemInListView(messageListView, 1, 1);
        clickOn("#ForwardMessageButton");
    }

    @Test
    public void forwardFormDefaults() {
        verifyThat("#forwardButton", Node::isDisabled);
        TextField searchTextField = find("#searchTextField");
        Assertions.assertEquals("", searchTextField.getText());
        ListView<Chat> chatsListView = find("#chatsListView");
        // the chat list should have all the chats
        Assertions.assertEquals(chats, chatsListView.getItems());
    }

    @Test
    public void findChats() throws JsonProcessingException {
        List<List<Chat>> chatNames = List.of(chats.subList(0,2),
                chats.subList(2,3),
                chats.subList(3,4));
        String[] searchItems =  {"chat", "third", "fourth"};
        for (int i = 0; i < searchItems.length; i++) {
            String response = jsonMapper.writeValueAsString(chatNames.get(i));
            stubSuccessful(GET_CHATS_BY_SEARCH, "get", response);
            spedUpWrite("#searchTextField", searchItems[i]);
            ListView<Chat> chatsListView = find("#chatsListView");
            Assertions.assertEquals(chatNames.get(i),
                    chatsListView.getItems());
            verifyThat("#forwardButton", Node::isDisabled);
        }
        String response = jsonMapper.writeValueAsString(chats);
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", response);
        doubleClickOn("#searchTextField").write("a").eraseText(2);
        ListView<Chat> chatsListView = find("#chatsListView");
        Assertions.assertEquals(chats,
                chatsListView.getItems());
    }

    private static Stream<Arguments> requests_bases() {
        return Stream.of(
                arguments("/get_chats\\?login=.*&page_number=", GET_CHATS_LOGIN_PAGE_NUMBER_D),
                arguments("/get_chats_by_search\\?login=.*&begin=.*&page_number=", FIND_CHATS)

        );
    }

    @ParameterizedTest
    @MethodSource("requests_bases")
    public void testScrollChats(String request, String baseRequest) throws JsonProcessingException {
        String tem1 = request + "1";
        String tem2 = request + "2";
        stub(baseRequest, "get", 404, null);

        ArrayList<Chat> templst2 = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            templst2.add(new Chat((long) i, String.format("2Chat%d", i)));
        }
        stubSuccessful(tem1, "get", jsonMapper.writeValueAsString(chats));
        stubSuccessful(tem2, "get", jsonMapper.writeValueAsString(templst2));
        if (baseRequest.equals(FIND_CHATS)) {
            clickOn("#searchTextField").write("chat");
        }
        sleep(2000);
        ListView<Chat> chatsListView = find("#chatsListView");

        clickOn(chatsListView).scroll(1);

        assertEquals(chatsListView.getItems().get(chatsListView.getItems().size() - 1), templst2.get(24));
        sleep(2000);
        assertEquals(chatsListView.getItems().get(chatsListView.getItems().size() - 1), templst2.get(24));

        stub(tem2, "get", 404, null);
        stub(tem1, "get", 404, null);

        stubSuccessful(baseRequest, "get", jsonMapper.writeValueAsString(chats));
    }

    @Test
    public void noChatsFound() {
        stub(FIND_CHATS, "get", 400, null);
        spedUpWrite("#searchTextField", "nonexistent");
        ListView<Chat> chatsListView = find("#chatsListView");
        Assertions.assertTrue(chatsListView.getItems().isEmpty());
        verifyThat("#forwardButton", Node::isDisabled);
    }

    @Test
    public void checkForwarding() throws JsonProcessingException {
        String response = jsonMapper.writeValueAsString(chats);
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", response);
        ListView<Chat> chatsListView = find("#chatsListView");
        Chat chosenChat = chatsListView.getItems().get(1);
        clickOnItemInListView(chatsListView, 1, 0);

//        Message forwardedMessage = new Message(4L, "olegoleg", originalMessage.getAuthor_login(),
//                chosenChat.getId(), new Date(), originalMessage.getContent(), false, false, true);

        response = jsonMapper.writeValueAsString(messages);
        stubSuccessful(GET_MESSAGES_1, "get", response);
//        response = jsonMapper.writeValueAsString(List.of(forwardedMessage));
//        stubSuccessful(GET_MESSAGES_2, "get", response);

        clickOn("#forwardButton");
        // check that the chat form has been reached
        assertThrows(NoSuchElementException.class, () -> find("#forwardButton"));
        try {
            find("#messageTextArea");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: forward message form closes, chat form opens. "
                    + "Result: forward message form is not closed");
        }
        chatsListView = find("#chatsListView");
//        clickOnItemInListView(chatsListView, 0, 0);
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
    public static void cleanup() throws TimeoutException {
        wireMockServer.shutdown();
        wireMockServer.resetToDefaultMappings();
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
}

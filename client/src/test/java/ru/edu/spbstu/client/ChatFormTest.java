package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.google.common.base.Strings;
import javafx.collections.ObservableList;
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
import ru.edu.spbstu.client.utils.ClientProperties;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.model.User;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.testfx.api.FxAssert.verifyThat;
import static org.testfx.internal.JavaVersionAdapter.getWindows;


public class ChatFormTest extends ApplicationTest {
    public static final String GET_CHATS_LOGIN_PAGE_NUMBER_D = "/get_chats\\?login=.*&page_number=\\d+";
    public static final String GET_MESSAGES = "/get_messages\\?chat_id=\\d+&page_number=\\d+";
    public static final String GET_IMAGE = "/get_profile_photo\\?login=.+";
    public static final String GET_MEMBERS = "/get_chat_members\\?chat_id=\\d+";
    public static final String DELETE_USERS = "/delete_users_from_chat";
    public static final String MAKE_ADMINS = "/make_users_admins";
    public static final String SEND_MESSAGE = "/send_message";
    public static final String FORWARD_MESSAGE = "/forward_message\\?message_id=\\d+&sender_login=.*&chat_id=\\d+";
    public static final String GET_EMAIL = "/get_user\\?login=.*";
    public static final String EDIT_MESSAGE = "/edit_message";
    public static final String DELETE_MESSAGE = "/delete_message\\?message_id=\\d+";
    public static final String FIND_CHATS = "/get_chats_by_search\\?login=.*&begin=.*&page_number=\\d+";


    public static final String MESSAGE_TEXT_AREA = "#messageTextArea";
    public static final String SEND_MESSAGE_BUTTON = "#sendMessageButton";
    private static final String chatListViewStr = "#chatsListView";
    private static WireMockServer wireMockServer;
    private static ObjectMapper jsonMapper = new ObjectMapper();

    private static List<Chat> chats = new ArrayList<>(Arrays.asList(new Chat(1L, "first"),
            new Chat(2L, "second"),
            new Chat(3L, "third")));

    private static ArrayList<Message> messages = new ArrayList<>(Arrays.asList(messageGenerator(1L, 1L, "login1", "login1", "message1", MessageType.SIMPLE),
            messageGenerator(2L, 1L, "olegoleg", "olegoleg", "message21", MessageType.SIMPLE),
            messageGenerator(3L, 1L, "olegoleg", "olegoleg", "message22", MessageType.SIMPLE)));
    private final String MESSAGE_LIST_VIEW = "#messagesListView";


    private static boolean[] intToBooleanArray2(int num) {
        boolean[] res = {false, false, false};

        for (int i = 0; i < 3; i++) {
            res[2 - i] = num % 2 > 0;
            num /= 2;
        }

        return res;
    }

    static Message messageGenerator(Long m_id, Long chat_id, String lg1, String lg2, String text, MessageType type) {
        boolean[] arr = intToBooleanArray2(type.value);
        return new Message(m_id, lg1, lg2, chat_id, new Date(), text, arr[0], arr[1], arr[2]);
    }

    enum MessageType {
        SIMPLE(0),
        FORWARDED(1),
        CHANGED(2),
        DELETED(4);

        private final int value;

        MessageType(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    //add new chat func
    //forward func
    static void stubSuccessful(String url, String type, String responseBody) {
        stub(url, type, 200, responseBody);
    }

    static void stub(String url, String type, int code, String responceBody) {
        switch (type) {
            case "get" -> stubFor(get(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(code)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responceBody)));
            case "post" -> stubFor(post(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(code)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responceBody)));
            case "patch" -> stubFor(patch(urlMatching(url))
                    .willReturn(aResponse()
                            .withStatus(code)
                            .withHeader("Content-Type", "application/json")
                            .withBody(responceBody)));
        }

    }

    private static void reset_to_defaults() throws JsonProcessingException {
        wireMockServer.resetToDefaultMappings();
        String responce = jsonMapper.writeValueAsString(chats);

        //to open 2 form
        stubSuccessful(GET_CHATS_LOGIN_PAGE_NUMBER_D, "get", responce);

        //to find chats
        stubSuccessful(FIND_CHATS, "get", responce);

        //get messages
        {

            String responce2 = jsonMapper.writeValueAsString(messages);
            stubSuccessful(GET_MESSAGES, "get", responce2);
            stubSuccessful(GET_IMAGE, "get", String.valueOf((byte[]) null));
        }
        List<ChatUser> usserlist = Arrays.asList(new ChatUser("login1", false),
                new ChatUser("olegoleg", true),
                new ChatUser("olegoleg2", false));

        //to config
        stubSuccessful(GET_MEMBERS, "get", jsonMapper.writeValueAsString(usserlist));
        //to exit chat
        {
            stubSuccessful(DELETE_USERS, "patch", jsonMapper.writeValueAsString(usserlist));
            stubSuccessful(MAKE_ADMINS, "patch", jsonMapper.writeValueAsString(usserlist));
        }
        stubSuccessful(SEND_MESSAGE, "post", String.valueOf((byte[]) null));
        stubSuccessful(FORWARD_MESSAGE, "post", String.valueOf((byte[]) null));

        stubSuccessful(GET_EMAIL, "get", jsonMapper.writeValueAsString(new User("olegoleg", "olegoleg", "olegoleg@gmail.com")));
        stubSuccessful(EDIT_MESSAGE, "patch", jsonMapper.writeValueAsString(new User("olegoleg", "olegoleg", "olegoleg@gmail.com")));
        stubSuccessful(DELETE_MESSAGE, "patch", jsonMapper.writeValueAsString(new User("olegoleg", "olegoleg", "olegoleg@gmail.com")));

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
        ((TextField) find("#loginTextBox")).setText("olegoleg");
        ((TextField) find("#passwordTextBox")).setText("olegoleg");
        Button b = find("#logInButton");
        b.setDisable(false);
        clickOn("#logInButton");
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    private static Long getLastMessageId() {
        return messages.get(messages.size() - 1).getId();
    }

    public <T extends Node> T find(final String query) {
        return (T) lookup(query).queryAll().iterator().next();
    }


    @Test
    public void testChatSelect() throws Exception {

        ListView<Chat> chatLst = find(chatListViewStr);

        clickOnItemInListView(chatLst, 2, 0);
    }

    @Test
    public void testExitChat() {
        int index = 0;
        ListView<Chat> chatLst = find(chatListViewStr);
        clickOnItemInListView(chatLst, index, 1);
        Chat itemToDelete = chatLst.getItems().get(index);
        clickOn("#ExitChatButton");
        ObservableList<Chat> data = chatLst.getItems();
        assertFalse(data.contains(itemToDelete));
    }

    @Test
    public void testFindChatAction() {
        TextField findAr = find("#findChatTextBox");
        clickOn(findAr).write("chat");
        ListView<Chat> chtListView = find(chatListViewStr);
        ListCell<Chat> cell = getListCell(chtListView, 0);
        assertEquals(chats.get(0), cell.getItem());

        clickOn(findAr);
        while (findAr.getText().length() > 0) {
            press(KeyCode.BACK_SPACE);
            release(KeyCode.BACK_SPACE);
        }

        assertEquals(chats.get(0), cell.getItem());
    }

    private static Stream<Arguments> requests_bases() {
        return Stream.of(
                arguments("/get_chats\\?login=.*&page_number=", GET_CHATS_LOGIN_PAGE_NUMBER_D),
                arguments("/get_chats_by_search\\?login=.*&begin=.*&page_number=", FIND_CHATS)

        );
    }

    @ParameterizedTest
    @MethodSource("requests_bases")
    public void testScrollChats(String request, String base_rquest) throws JsonProcessingException {
        String tem1 = request + "1";
        String tem2 = request + "2";
        if (base_rquest.equals(FIND_CHATS)) {
            clickOn("#findChatTextBox").write("chat");
        }
        stub(base_rquest, "get", 404, null);

        ArrayList<Chat> templst2 = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            templst2.add(new Chat((long) i, String.format("2Chat%d", i)));
        }
        stubSuccessful(tem1, "get", jsonMapper.writeValueAsString(chats));
        stubSuccessful(tem2, "get", jsonMapper.writeValueAsString(templst2));
        sleep(2000);
        ListView<Chat> chtListView = find(chatListViewStr);

        clickOn(chtListView).scroll(1);

        assertEquals(chtListView.getItems().get(chtListView.getItems().size() - 1), templst2.get(24));
        sleep(2000);
        assertEquals(chtListView.getItems().get(chtListView.getItems().size() - 1), templst2.get(24));

        stub(tem2, "get", 404, null);
        stub(tem1, "get", 404, null);

        stubSuccessful(base_rquest, "get", jsonMapper.writeValueAsString(chats));
    }

    @Test
    public void testScrollMessages() throws JsonProcessingException {
        ChatFormController controller = ((ChatFormController) getWindows().get(0).getScene().getUserData());
        controller.timeline.stop();
        ListView<Chat> chatListView = find(chatListViewStr);
        int chatIndex = 1;
        String tem1 = "/get_messages\\?chat_id=\\d+&page_number=1";
        String tem2 = "/get_messages\\?chat_id=\\d+&page_number=2";
        stub(GET_MESSAGES, "get", 404, null);


        ArrayList<Message> templst2 = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            templst2.add(messageGenerator((long) i + 3, (long) chatIndex, "olegoleg", "olegoleg", String.format("2Message%d", i + 3), MessageType.SIMPLE));
        }
        stubSuccessful(tem1, "get", jsonMapper.writeValueAsString(messages));
        stubSuccessful(tem2, "get", jsonMapper.writeValueAsString(templst2));
        clickOnItemInListView(chatListView, chatIndex, 0);
        ListView<Message> msg = find(MESSAGE_LIST_VIEW);


        clickOn(MESSAGE_LIST_VIEW).scroll(1);

        assertEquals(templst2.get(24), msg.getItems().get(msg.getItems().size() - 1));
        sleep(2000);
        assertEquals(templst2.get(24), msg.getItems().get(msg.getItems().size() - 1));
        reset_to_defaults();
    }

    @Test
    public void testOpenCreateChatForm() {
        clickOn("#addChatButton");
        try {
            find("#chatUsersLabel");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: create chat form opens. Result: create chat form is not opened.");
        }
    }

    @Test
    public void testOpenProfileForm() {
        clickOn("#profileButton");
        try {
            find("#oldPasswordTextField");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: profile form opens. Result: profile form is not opened.");
        }
    }

    @Test
    public void testOpenForwardMessageForm() {
        ListView<Chat> chatLst = find(chatListViewStr);
        clickOnItemInListView(chatLst, 1, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        clickOnItemInListView(messageListView, 1, 1);
        clickOn("#ForwardMessageButton");
        try {
            find("#searchTextField");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: forward message form opens. Result: forward message form is not opened.");
        }
    }

    @Test
    public void testLanguageSwitch() throws IOException {

        ComboBox<String> cb = find("#LanguageComboBox");
        clickOn(cb);


        int cbIndex = 1;
        interact(() -> cb.getSelectionModel().select(cbIndex));
        String res = ClientProperties.getProperties().getProperty("Language");
        assertEquals(res, "EN");

    }

    @Test
    public void testButtonsStateDuringStartup() {

        verifyThat(SEND_MESSAGE_BUTTON, NodeMatchers.isDisabled());
    }

    @Test
    public void testTextInputWithNoChat() {
        clickOn(MESSAGE_TEXT_AREA).write("sampleMessage");
        TextArea tx = find(MESSAGE_TEXT_AREA);
        assertEquals("", tx.getText());
    }

    @Test
    public void testSendMessage() throws JsonProcessingException {
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        clickOnItemInListView(chatLst, chatIndex, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);

        Button sendButton = find(SEND_MESSAGE_BUTTON);
        String message = "message";
        clickOn(MESSAGE_TEXT_AREA).write(message);
        messages.add(0, messageGenerator(getLastMessageId() + 1, chatLst.getItems().get(chatIndex).getId(), "olegoleg", "olegoleg", message, MessageType.SIMPLE));
        String responce2 = jsonMapper.writeValueAsString(messages);


        stubSuccessful(GET_MESSAGES, "get", responce2);
        clickOn(sendButton);
        assertEquals(messages.get(0), messageListView.getItems().get(0));
    }

    @Test
    public void testMessageTooLongInsert() {
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        clickOnItemInListView(chatLst, chatIndex, 0);
        String message = Strings.repeat("o", 512);
        TextArea textArea = find(MESSAGE_TEXT_AREA);
        textArea.setText(message);
        clickOn(MESSAGE_TEXT_AREA).write("oo");

        assertEquals(textArea.getText().length(), 512);
    }

    @Test
    public void testMessageReplaceMechanism() {
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        int caretPosition = 100;
        clickOnItemInListView(chatLst, chatIndex, 0);

        String message = Strings.repeat("o", 511);
        TextArea textArea = find(MESSAGE_TEXT_AREA);
        textArea.setText(message);
        clickOn(MESSAGE_TEXT_AREA).moveTo(1, 0);
        textArea.positionCaret(caretPosition);
        push(KeyCode.K);
        push(KeyCode.K);

        assertEquals(textArea.getText(), Strings.repeat("o", caretPosition) + "k" + Strings.repeat("o", 511 - caretPosition));
    }

    @Test
    public void testExitEditMode() {
        ListView<Chat> chatLst = find(chatListViewStr);
        clickOnItemInListView(chatLst, 1, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        clickOnItemInListView(messageListView, 1, 1);
        clickOn("#EditMessageButton");
        Button sendButton = find(SEND_MESSAGE_BUTTON);

        Scene currectScene = getWindows().get(0).getScene();
        ChatFormController controller = ((ChatFormController) currectScene.getUserData());
        assertEquals(controller.getBundle().getString("editMessageButton"), sendButton.getText());
        TextArea messageTexAread = find(MESSAGE_TEXT_AREA);
        var cell = getListCell(messageListView, 1);

        assertEquals(cell.getItem().getContent(), messageTexAread.getText());
        press(KeyCode.ESCAPE);
        release(KeyCode.ESCAPE);
        assertEquals(controller.getBundle().getString("SendMessageButton"), sendButton.getText());
        assertEquals("", messageTexAread.getText());
    }


    @Test
    public void testExitReplyMode() {
        ListView<Chat> chatLst = find(chatListViewStr);
        clickOnItemInListView(chatLst, 1, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        clickOnItemInListView(messageListView, 1, 1);
        clickOn("#AnswerMessageButton");
        Button sendButton = find(SEND_MESSAGE_BUTTON);


        Scene currectScene = getWindows().get(0).getScene();
        ChatFormController controller = ((ChatFormController) currectScene.getUserData());
        assertEquals(controller.getBundle().getString("AnswerMessageButton"), sendButton.getText());
        press(KeyCode.ESCAPE);
        release(KeyCode.ESCAPE);
        assertEquals(controller.getBundle().getString("SendMessageButton"), sendButton.getText());
    }

    @Test
    public void testReplyToMessageAction() throws JsonProcessingException {
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        clickOnItemInListView(chatLst, chatIndex, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        int messageIndxe = 0;
        clickOnItemInListView(messageListView, messageIndxe, 1);
        clickOn("#AnswerMessageButton");
        String replyMessage = "reply";
        clickOn(MESSAGE_TEXT_AREA).write(replyMessage);

        messages.add(messageGenerator(getLastMessageId(), chatLst.getItems().get(chatIndex).getId(), "olegoleg",
                messageListView.getItems().get(messageIndxe).getAuthor_login(), messageListView.getItems().get(messageIndxe).getContent(), MessageType.FORWARDED));


        messages.add(messageGenerator(getLastMessageId(), chatLst.getItems().get(chatIndex).getId(), "olegoleg",
                "olegoleg", replyMessage, MessageType.SIMPLE));

        stubSuccessful(GET_MESSAGES, "get", jsonMapper.writeValueAsString(messages));

        clickOn(SEND_MESSAGE_BUTTON);
    }

    @Test
    public void testDeleteMessageAction() {
        ChatFormController controller = ((ChatFormController) getWindows().get(0).getScene().getUserData());
        controller.timeline.pause();
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        clickOnItemInListView(chatLst, chatIndex, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        int messageIndex = 2;
        var cell = getListCell(messageListView, messageIndex);


        Message prev_message = cell.getItem().toBuilder().build();
        prev_message.setIs_deleted(true);

        //sleep(200);
        clickOnItemInListView(messageListView, messageIndex, 1);
        clickOn("#DeleteMessageButton");
        Message new_message = getListCell(messageListView, messageIndex).getItem();

        assertEquals(prev_message, new_message);
        controller.timeline.play();
    }

    @Test
    public void testMessageEditAction() {
        ListView<Chat> chatLst = find(chatListViewStr);
        int chatIndex = 1;
        int messageIndex = 1;
        clickOnItemInListView(chatLst, chatIndex, 0);

        ListView<Message> messageListView = find(MESSAGE_LIST_VIEW);
        var cell = getListCell(messageListView, messageIndex);

        clickOnItemInListView(messageListView, messageIndex, 1);
        clickOn("#EditMessageButton");
        Message prev_message = cell.getItem().toBuilder().build();
        int insertPart = 2;
        TextArea textArea = find(MESSAGE_TEXT_AREA);
        clickOn(MESSAGE_TEXT_AREA);
        textArea.positionCaret(insertPart);
        press(KeyCode.K);
        press(KeyCode.S);
        clickOn(SEND_MESSAGE_BUTTON);
        Message new_message = getListCell(messageListView, messageIndex).getItem();

        prev_message.setIs_edited(true);
        prev_message.setContent(prev_message.getContent().substring(0, insertPart) + "ks" + prev_message.getContent().substring(insertPart));

        assertEquals(prev_message, new_message);
    }

    @Test
    public void testConfigChatFormOpen() {
        ListView<Chat> chatLst = find(chatListViewStr);
        clickOnItemInListView(chatLst, 1, 1);
        clickOn("#ConfigChatButton");
        try {
            find("#loginTextField");
        } catch (NoSuchElementException ex) {
            throw new NoSuchElementException("Expected behaviour: config chat form opens. Result: config chat form is not opened.");
        }

        //Scene currectScene = getWindows().get(0).getScene();

        // ConfigureChatFormController controller = ((ConfigureChatFormController) currectScene.getUserData());
        //controller.timeline.stop();
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

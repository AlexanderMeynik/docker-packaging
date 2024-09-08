package ru.edu.spbstu.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.utils.AuthScheme;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.request.CreateChatRequest;
import ru.edu.spbstu.request.EditMessageRequest;
import ru.edu.spbstu.request.SendMessageRequest;
import ru.edu.spbstu.request.SignUpRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static ru.edu.spbstu.client.utils.Conf.ip;

public class fillServer {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private CredentialsProvider prov= new BasicCredentialsProvider();
    private String login;





    public CredentialsProvider getCredentialsProvider()
    {
        return prov;
    }
    public String getLogin()
    {
        return login;
    }

    public void setCredentialsProvider(CredentialsProvider prov,String login)
    {
        this.prov=prov;
        this.login=login;
    }
    public List<Chat> getChats(Integer page) throws IOException {
        //getAllChats(prov, login, page).forEach(chat -> System.out.println(chat.getName()))
        return getAllChats(prov, login, page);
    }
    public void addChat(String chatName, List<String>users) throws IOException {
        int reqStatusCreateChat = createChat(prov, chatName, users, login);
        //System.out.println(reqStatusCreateChat);

        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while addChat");
        }
    }


    public void addChat(String chatName) throws IOException {
        int reqStatusCreateChat = createChat(prov, chatName, Collections.emptyList(), login);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while addChat");
        }
    }
    public List<Message> getMessages(Long chat_id, int page) throws IOException {
        return getMessages(prov, chat_id, (long) page);
    }
    public void sendMessage(Long chatId,String message) throws IOException {
        int reqStatusCreateChat = sendMessage(prov,login,chatId,message);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while send message");
        }
    }
    public void deleteMessage(Long message_id) throws IOException {
        int reqStatusCreateChat = deleteMessage(prov,message_id);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while delete message");
        }
    }

    public void editMessage(Long message_id,String Newcontents) throws IOException {
        int reqStatusCreateChat = editMessage(prov,message_id,Newcontents);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while delete message");
        }
    }

    private static final List<String> loggins = Arrays.asList("olegoleg","Llananoker", "Relldastr", "Onadelestr","Jusarorol");
    private static final List<String> paswwords = Arrays.asList("olegoleg","Llananoker", "Relldastr", "Onadelestr","Jusarorol");

    public static void main(String[] args) throws IOException {
        fillServer fs=new fillServer();
        fs.start();

    }
    public void start() throws IOException {

        ArrayList<String>emails=new ArrayList<>(0);
        for(var elem:loggins)
        {
            emails.add(elem+"@gmail.com");
        }

        for (int i=0;i<loggins.size();i++)
        {
            register(loggins.get(i),paswwords.get(i),emails.get(i));
        }
        logIn(loggins.get(0),paswwords.get(0));
        List<String> temp1=loggins.subList(1,loggins.size());
        addChat("AllInChat",temp1);

        for (int i = 1; i<10; i++)
        {
            //temp1=loggins.subList(1,loggins.size()-i);
            addChat("chat"+ i,temp1);
        }
        List<Chat> chats=getChats(1);
        /*var temp=getChats(2);
        chats.addAll(temp);
        temp=getChats(3);
        chats.addAll(temp);
        temp=getChats(4);
        chats.addAll(temp);*/
        for(int i=0;i<=0;i++)
        {
            //logIn(loggins.get(i),paswwords.get(i));
            for (int id=0;id<chats.size();id++) {
                for (int j=0;j<=8;j++) {

                    sendMessage(chats.get(id).getId(), "Message send" +j+" to chat " + chats.get(id).getName() + " from " + login);
                    //sendMessage(id.getId(), "Message send 2 to chat " + id.getName() + " from " + login);
                }
                System.out.println(chats.get(id).toString());
            }
        }
        logIn(loggins.get(0),paswwords.get(0));
        List<Message> messages=getMessages(chats.get(0).getId(),1);
        editMessage(messages.get(0).getId(),"Changed msg");
        List<Message> afterChange=getMessages(chats.get(0).getId(),1);
        deleteMessage(messages.get(1).getId());
        List<Message> afterDelete=getMessages(chats.get(0).getId(),1);





    }

    //private String lo;

    public  void register(String login,String password,String email) throws IOException {
        int regStatus = registerImplementation(login, password, email);
        //System.out.println(regStatus);

        if (regStatus != 200) {
            throw new HttpResponseException(regStatus,"Error while register");
        }


        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        prov.setCredentials(AuthScope.ANY, credentials);
    }
    public void logIn(String login,String password) throws IOException {
        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        prov.setCredentials(AuthScope.ANY, credentials);
        this.login=login;
    }

    private int registerImplementation(String login, String password, String email) throws IOException {
        SignUpRequest signUpRequest = new SignUpRequest(login, password, email);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost signUpReq = new HttpPost(ip+":8080/register");
            signUpReq.addHeader("content-type", "application/json");
            signUpReq.setEntity(new StringEntity(jsonMapper.writeValueAsString(signUpRequest), "UTF-8"));
            return client.execute(signUpReq).getStatusLine().getStatusCode();
        }
    }

    private List<Chat> getAllChats(CredentialsProvider provider, String login, Integer page) throws IOException {

        String getChatsUrlBlueprint = ip+":8080/get_chats?login=%s&page_number=%d";

        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint, login, page));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            if(re.getStatusLine().getStatusCode()==400)
            {
                return new ArrayList<>();
            }
            return jsonMapper.readValue(json, new TypeReference<>() {});
        }
    }

    private int createChat(CredentialsProvider provider, String chatName, List<String> users, String admin) throws IOException {
        CreateChatRequest request = new CreateChatRequest();
        request.setAdmin_login(admin);
        request.setChat_name(chatName);
        request.setUser_logins(users);
        request.setLanguage(Language.ENGLISH);

        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost post = new HttpPost(ip+":8080/create_chat");
            post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
            post.addHeader("content-type", "application/json");
            CloseableHttpResponse re = client.execute(post);
            return re.getStatusLine().getStatusCode();
        }
    }


    public List<Chat> find(String name) {
        List<Chat> res=Collections.emptyList();

        return res;
    }



    List<Message> getMessages(CredentialsProvider provider,Long chatId,Long page) throws IOException {

        String getMessagesUrlBlueprint = ip+":8080/get_messages?chat_id=%d&page_number=%d";
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpGet httpGet = new HttpGet(String.format( getMessagesUrlBlueprint, chatId, page));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity(), "UTF-8");
            int code=re.getStatusLine().getStatusCode();
            if(code==400)
            {
                return new ArrayList<>();
            }
            return jsonMapper.readValue(json, new TypeReference<>() {});
        }
    }

    private static int sendMessage(CredentialsProvider provider, String login,Long chat_id,String message) throws IOException {
        SendMessageRequest request=new SendMessageRequest(login,login,chat_id,message);
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPost post = new HttpPost(ip+":8080/send_message");
            post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
            post.addHeader("content-type", "application/json");
            CloseableHttpResponse re = client.execute(post);
            return re.getStatusLine().getStatusCode();
        }
    }
    private static int deleteMessage(CredentialsProvider provider, Long message_id) throws IOException {

        String getMessagesUrlBlueprint = ip+":8080/delete_message?message_id=%d";
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPatch httpGet = new HttpPatch(String.format( getMessagesUrlBlueprint, message_id));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            int code=re.getStatusLine().getStatusCode();

            return re.getStatusLine().getStatusCode();
        }
    }
    private static int forwardMessage(CredentialsProvider provider, Long message_id, String login,String chat_id) throws IOException {
        String getMessagesUrlBlueprint = ip+":8080/forward_message?message_id=%d?sender_login=%s?chat_id=%d";
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {

            HttpPost httpGet = new HttpPost(String.format( getMessagesUrlBlueprint, message_id,login,chat_id));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            int code=re.getStatusLine().getStatusCode();

            return re.getStatusLine().getStatusCode();
        }
    }
    private static int editMessage(CredentialsProvider provider,Long message_id,String message) throws IOException {
        EditMessageRequest request=new EditMessageRequest(message_id,message);
        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                .setDefaultCredentialsProvider(provider)
                .build()) {
            HttpPatch post = new HttpPatch(ip+":8080/edit_message");
            post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
            post.addHeader("content-type", "application/json");
            CloseableHttpResponse re = client.execute(post);
            return re.getStatusLine().getStatusCode();
        }
    }

}

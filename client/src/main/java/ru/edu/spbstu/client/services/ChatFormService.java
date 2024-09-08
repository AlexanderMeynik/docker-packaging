package ru.edu.spbstu.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.model.Message;
import ru.edu.spbstu.request.ChatUpdateRequest;
import ru.edu.spbstu.request.EditMessageRequest;
import ru.edu.spbstu.request.SendMessageRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static ru.edu.spbstu.client.utils.Conf.ip;

public class ChatFormService {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private String login;

    public String getLogin()
    {
        return login;
    }

    public void setCredentialsProvider(String login)
    {
        this.login=login;
    }
    public List<Chat> getChats(Integer page) throws IOException {
        String getChatsUrlBlueprint = ip+":8080/get_chats?login=%s&page_number=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint, URLEncoder.encode(login, StandardCharsets.UTF_8), page));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        if(re.getStatusLine().getStatusCode()!=200)
        {
            if(re.getStatusLine().getStatusCode()==400) {
                return new ArrayList<>(0);
            }
            throw new HttpResponseException(re.getStatusLine().getStatusCode(),"HttpCode");
        }
        return jsonMapper.readValue(json, new TypeReference<>() {});
    }


    public List<Message> getMessages(Long chat_id, int page) throws IOException {
        String getMessagesUrlBlueprint = ip+":8080/get_messages?chat_id=%d&page_number=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getMessagesUrlBlueprint, chat_id, page));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        int code = re.getStatusLine().getStatusCode();
        if (code == 400) {
            return new ArrayList<>();
        }
        return jsonMapper.readValue(json, new TypeReference<>() {
        });
    }

    public void sendMessage(Long chatId,String message) throws IOException {
        int reqStatusCreateChat ;
        SendMessageRequest request=new SendMessageRequest(login,login,chatId,message);

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPost post = new HttpPost(ip+":8080/send_message");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        reqStatusCreateChat= re.getStatusLine().getStatusCode();
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while send message");
        }
    }
    public void deleteMessage(Long message_id) throws IOException {
        int reqStatusCreateChat;
        String getMessagesUrlBlueprint = ip+":8080/delete_message?message_id=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch httpGet = new HttpPatch(String.format( getMessagesUrlBlueprint, message_id));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        reqStatusCreateChat=re.getStatusLine().getStatusCode();
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while delete message");
        }
    }

    public void editMessage(Long message_id,String Newcontents) throws IOException {
        int reqStatusCreateChat;
        EditMessageRequest request=new EditMessageRequest(message_id,Newcontents);

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch post = new HttpPatch(ip+":8080/edit_message");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        reqStatusCreateChat = re.getStatusLine().getStatusCode();
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while edit message");
        }
    }
    public void leaveChat(Long chatId, String login) throws IOException {
        int reqStatusCreateChat;
        ChatUpdateRequest request = new ChatUpdateRequest(chatId, Collections.singletonList(login));

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch post = new HttpPatch(ip+":8080/delete_users_from_chat");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        reqStatusCreateChat=re.getStatusLine().getStatusCode();

        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while leave chat");
        }

    }
    public void setChatUsersAdmins(Chat chatToConfigure,String login) throws IOException {
        List<String>logins=Collections.singletonList(login);
        ChatUpdateRequest request=new ChatUpdateRequest(chatToConfigure.getId(),logins);
        int code=0;

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch post = new HttpPatch(ip+":8080/make_users_admins");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        code = re.getStatusLine().getStatusCode();
    }
    public List<ChatUser> getChatMembers(Chat chatToConfigure) throws IOException {
        String getChatsUrlBlueprint = ip+":8080/get_chat_members?chat_id=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint,chatToConfigure.getId()));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        int code=re.getStatusLine().getStatusCode();
        if(code!=200) {
            if (code == 400) {
                return new ArrayList<>();
            } else {
                throw new HttpResponseException(code, "Error when get chat members!");
            }
        }
        return jsonMapper.readValue(json, new TypeReference<>() {});
    }

    public void deleteChat(Chat chatToDelete) throws IOException {
        String getChatsUrlBlueprint = ip+":8080/delete_chat?chat_id=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpDelete httpGet = new HttpDelete(String.format(getChatsUrlBlueprint,chatToDelete.getId()));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        int code=re.getStatusLine().getStatusCode();
        if(code!=200) {
                throw new HttpResponseException(code, "Error when get chat members!");
        }
    }



    public List<Chat> find(String name,Long page) throws IOException {
        String getMessagesUrlBlueprint = ip+":8080/get_chats_by_search?login=%s&begin=%s&page_number=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();

        HttpGet httpGet = new HttpGet(String.format( getMessagesUrlBlueprint,URLEncoder.encode(login, StandardCharsets.UTF_8)
                ,URLEncoder.encode(name, StandardCharsets.UTF_8),page));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        int code=re.getStatusLine().getStatusCode();
        if(code!=200)
        {
            if (code==400)
            {
                return new ArrayList<>();
            }
            else
            {
                throw new HttpResponseException(code,"Error while find");
            }
        }

        return jsonMapper.readValue(json, new TypeReference<>() {
        });
    }


    public  void forwardMessage(Long message_id, String login,Long chat_id) throws IOException {
        String getMessagesUrlBlueprint = ip+":8080/forward_message?message_id=%d&sender_login=%s&chat_id=%d";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPost httpGet = new HttpPost(String.format( getMessagesUrlBlueprint, message_id,login,chat_id));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        int code=re.getStatusLine().getStatusCode();
        if(code!=200)
        {
            throw new HttpResponseException(code,"forwardMessage");
        }

    }


    /*public ArrayList<Image> getImageList(List<String> userList) {
        int size=userList.size();
        ArrayList<Image> images= new ArrayList<Image>();
        for (int i=0;i<size;i++)
        {
            var res=(getClass().getResource("/images/dAvatar.bmp")).getPath().replaceFirst("/","");
            Image temp=new Image(res);
            images.add(temp);
        }
        return  images;
    }*/

  
    public Image getImage(String userLogin) throws IOException {
        return new Image(new ByteArrayInputStream(getProfilePicture(userLogin)),40,40,false,false);
    }
    public byte[] getProfilePicture(String userLogin) throws IOException {

        String getProfilePictureUrlBlueprint = ip+":8080/get_profile_photo?login=%s";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getProfilePictureUrlBlueprint,userLogin));
        HttpResponse response = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(response);

        var entity = response.getEntity();
        if (entity.getContentType() == null) {
            return getClass().getResourceAsStream("/images/dAvatar.bmp").readAllBytes();
        } else {

            String json = EntityUtils.toString(entity);
            return Base64.getDecoder().decode(json);
        }
    }


}

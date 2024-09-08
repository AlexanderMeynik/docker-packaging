package ru.edu.spbstu.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.model.ChatUser;
import ru.edu.spbstu.request.ChatUpdateRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import static ru.edu.spbstu.client.utils.Conf.ip;

public class ConfigureChatFormService {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private String login;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login)
    {
        this.login=login;
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

    public void deleteChatUsers(Chat chatToConfigure, List<String> logins, boolean type) throws IOException {
        ChatUpdateRequest request=new ChatUpdateRequest(chatToConfigure.getId(),logins);

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        int code=0;
        HttpPatch post = new HttpPatch(ip+":8080/delete_users_from_chat");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        if(!type) {
            HttpResponse re = client.execute(post);
            HttpClientFactory.tryUpdateRememberMe(re);
            code = re.getStatusLine().getStatusCode();
        }
    }

    public void setChatUsersAdmins(Chat chatToConfigure, List<String> logins, boolean type) throws IOException {
        ChatUpdateRequest request=new ChatUpdateRequest(chatToConfigure.getId(),logins);
        int code=0;

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch post = new HttpPatch(ip+":8080/make_users_admins");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");

        if(!type) {
            HttpResponse re = client.execute(post);
            HttpClientFactory.tryUpdateRememberMe(re);
            code = re.getStatusLine().getStatusCode();
        }
    }
    public void addUsersToChat(Chat chatToConfigure,List<String>logins) throws IOException {

        ChatUpdateRequest request=new ChatUpdateRequest(chatToConfigure.getId(),logins);
        int code=0;

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPatch post = new HttpPatch(ip+":8080/add_users_to_chat");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        code = re.getStatusLine().getStatusCode();
    }

    public ArrayList<Image> getImageList(List<ChatUser> userList) {
        int size=userList.size();
        ArrayList<Image> images= new ArrayList<Image>();
        for (int i=0;i<size;i++) {
            Image temp;
            try {
                temp=getImage(userList.get(i).getLogin());

            } catch (IOException e) {
                temp=new Image((getClass().getResource("/images/dAvatar.bmp")).getPath().replaceFirst("/",""));
            }
            images.add(temp);
        }
        return  images;
    }
    public Image getImage(String userLogin) throws IOException {

        return new Image(new ByteArrayInputStream(getProfilePicture(userLogin)),40,40,false,false);
    }
    public byte[] getProfilePicture(String userLogin) throws IOException {

        String getProfilePictureUrlBlueprint = ip+":8080/get_profile_photo?login=%s";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getProfilePictureUrlBlueprint,
                URLEncoder.encode(userLogin, StandardCharsets.UTF_8)));
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
    public Boolean isUserPresent(String login) throws IOException {
        String getChatsUrlBlueprint = ip+":8080/is_user_present?login=%s";


        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .build()) {
            HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint,
                    URLEncoder.encode(login, StandardCharsets.UTF_8)));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            return jsonMapper.readValue(json, new TypeReference<>() {});
        }
    }
}

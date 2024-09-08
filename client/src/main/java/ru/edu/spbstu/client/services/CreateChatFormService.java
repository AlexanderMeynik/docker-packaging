package ru.edu.spbstu.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.image.Image;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpResponseException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.Language;
import ru.edu.spbstu.request.CreateChatRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import static ru.edu.spbstu.client.utils.Conf.ip;

public class CreateChatFormService {

    private static final ObjectMapper jsonMapper = new ObjectMapper();
    public String getLogin() {
        return login;
    }
    private String login;

    public void setLogin(String login)
    {
        this.login=login;
    }

    public void addChat(String chatName, List<String>users, Language language) throws IOException {
        int reqStatusCreateChat = createChat(chatName, users, login, language);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while addChat");
        }
    }
    /*public void addChat(String chatName, Language language) throws IOException {
        int reqStatusCreateChat = createChat(chatName, Collections.emptyList(), login, language);
        if (reqStatusCreateChat != 200) {
            throw new HttpResponseException(reqStatusCreateChat,"Error while addChat");
        }
    }*/



    private static int createChat(String chatName, List<String> users, String admin, Language language) throws IOException {
        CreateChatRequest request = new CreateChatRequest();
        request.setAdmin_login(admin);
        request.setChat_name(chatName);
        request.setUser_logins(users);
        request.setLanguage(language);

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpPost post = new HttpPost(ip+":8080/create_chat");
        post.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        post.addHeader("content-type", "application/json");
        HttpResponse re = client.execute(post);
        HttpClientFactory.tryUpdateRememberMe(re);
        return re.getStatusLine().getStatusCode();
    }

    /*public ArrayList<Image> getImageList(List<ChatUser> userList) {
        int size=userList.size();
        ArrayList<Image> images= new ArrayList<Image>();
        for (int i=0;i<size;i++) {
            Image temp;
            try {
                temp=getImage(userList.get(i).getLogin());

            } catch (IOException e) {
                temp=new Image((getClass().getResource("/images/dAvatar.bmp")).getPath().replaceFirst("/",""));
            }
            images.set(i,temp);
        }
        return  images;
    }*/
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

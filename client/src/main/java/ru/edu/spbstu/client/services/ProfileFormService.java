package ru.edu.spbstu.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.util.EntityUtils;
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.User;
import ru.edu.spbstu.request.EmailUpdateRequest;
import ru.edu.spbstu.request.PasswordUpdateRequest;
import ru.edu.spbstu.request.ProfilePhotoUpdateRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static ru.edu.spbstu.client.utils.Conf.ip;

public class ProfileFormService {
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private String login;

    public byte[] getProfilePicture() throws IOException {

        String getProfilePictureUrlBlueprint = ip+":8080/get_profile_photo?login=%s";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getProfilePictureUrlBlueprint,
                URLEncoder.encode(login, StandardCharsets.UTF_8)));
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

    public void setProfilePicture(byte[] imageBytes) throws IOException {
        String imageString = Base64.getEncoder().encodeToString(imageBytes);
        String setProfilePictureUrlBlueprint = ip+":8080/update_profile_photo";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        var request = new ProfilePhotoUpdateRequest(login, imageString);
        HttpPost httpPost = new HttpPost(String.format(setProfilePictureUrlBlueprint));
        httpPost.setEntity(new StringEntity(jsonMapper.writeValueAsString(request), "UTF-8"));
        httpPost.addHeader("content-type", "application/json");
        HttpResponse response = client.execute(httpPost);
        HttpClientFactory.tryUpdateRememberMe(response);
        String json = EntityUtils.toString(response.getEntity());
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getLogin() {
        return login;
    }

    public void changeEmail(String newEmail) throws IOException {
        String updateEmailUrlBlueprint = ip+":8080/update_email";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        EmailUpdateRequest updateRequest = new EmailUpdateRequest(login, newEmail);
        HttpPost httpPost = new HttpPost(updateEmailUrlBlueprint);
        httpPost.setEntity(new StringEntity(jsonMapper.writeValueAsString(updateRequest), "UTF-8"));
        httpPost.addHeader("content-type", "application/json");
        HttpResponse updateResponse = client.execute(httpPost);
        HttpClientFactory.tryUpdateRememberMe(updateResponse);
    }

    public String getEmail() throws IOException {
        String updateEmailUrlBlueprint = ip+":8080/get_user?login=%s";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(updateEmailUrlBlueprint, login));
        HttpResponse getEmailResponse = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(getEmailResponse);
        String json = EntityUtils.toString(getEmailResponse.getEntity());
        User user = jsonMapper.readValue(json, new TypeReference<>() {});
        return user.getEmail();
    }
    public int changePassword(String oldPassword, String newPassword) throws IOException {
        String updatePasswordUrlBlueprint = ip+":8080/update_password";

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        PasswordUpdateRequest updateRequest = new PasswordUpdateRequest(login, newPassword, oldPassword);
        HttpPost httpPost = new HttpPost(updatePasswordUrlBlueprint);
        httpPost.setEntity(new StringEntity(jsonMapper.writeValueAsString(updateRequest), "UTF-8"));
        httpPost.addHeader("content-type", "application/json");

        HttpResponse checkResponse = client.execute(httpPost);
        HttpClientFactory.tryUpdateRememberMe(checkResponse);
        if (checkResponse.getStatusLine().getStatusCode() == 200) {
            UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, newPassword);
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(AuthScope.ANY, credentials);
            HttpClientFactory.getInstance().setCredentialsProvider(provider);
        }

        return checkResponse.getStatusLine().getStatusCode();
    }
}

package ru.edu.spbstu.client.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
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
import ru.edu.spbstu.client.utils.HttpClientFactory;
import ru.edu.spbstu.model.Chat;
import ru.edu.spbstu.request.SignUpRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import static ru.edu.spbstu.client.utils.Conf.ip;


public class LogInService {

    private static final ObjectMapper jsonMapper = new ObjectMapper();

    public  void register(String login,String password,String email) throws IOException {
        int regStatus = registerImplementation(login, password, email);

        if (regStatus != 200) {
            throw new HttpResponseException(regStatus,"Error while register");
        }

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClientFactory.getInstance().setCredentialsProvider(provider);
    }
    public void logIn(String login,String password, boolean isRememberMeChecked) throws IOException {

        UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(login, password);
        CredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(AuthScope.ANY, credentials);
        HttpClientFactory.getInstance().setCredentialsProvider(provider);
        getAllChats(login,1, isRememberMeChecked);
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

    public Boolean isEmailUsed(String email) throws IOException {
        String getChatsUrlBlueprint = ip+":8080/is_email_used?email=%s";

        try (CloseableHttpClient client = HttpClientBuilder
                .create()
                .build()) {
            HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint,
                    email));
            CloseableHttpResponse re = client.execute(httpGet);
            String json = EntityUtils.toString(re.getEntity());
            return jsonMapper.readValue(json, new TypeReference<>() {});
        }
    }

    private  static List<Chat> getAllChats(String login, Integer page, boolean isRememberMeChecked) throws IOException {

        String getChatsUrlBlueprint = ip+":8080/get_chats?login=%s&page_number=%d";
        if (isRememberMeChecked) {
            getChatsUrlBlueprint += "&remember-me=on";
        }

        HttpClient client = HttpClientFactory.getInstance().getHttpClient();
        HttpGet httpGet = new HttpGet(String.format(getChatsUrlBlueprint,
                URLEncoder.encode(login, StandardCharsets.UTF_8),
                page));
        HttpResponse re = client.execute(httpGet);
        HttpClientFactory.tryUpdateRememberMe(re);
        String json = EntityUtils.toString(re.getEntity());
        if(re.getStatusLine().getStatusCode()!=200) {
            if (re.getStatusLine().getStatusCode() == 400) {
                return Collections.emptyList();
            } else {
                throw new HttpResponseException(re.getStatusLine().getStatusCode(), "Error while getAllChats");
            }
        }
        return jsonMapper.readValue(json, new TypeReference<>() {});
    }

    private int registerImplementation(String login, String password, String email) throws IOException {
        SignUpRequest signUpRequest = new SignUpRequest(login, password, email);

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost signUpReq = new HttpPost(ip+":8080/register");
            signUpReq.addHeader("content-type", "application/json");
            signUpReq.setEntity(new StringEntity(jsonMapper.writeValueAsString(signUpRequest), "UTF-8"));
            var temp= client.execute(signUpReq);
            return temp.getStatusLine().getStatusCode();
        }
    }
}
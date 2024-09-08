package ru.edu.spbstu.client.utils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import ru.edu.spbstu.client.exception.InvalidHttpClientFactoryStateException;

import java.io.IOException;
import java.util.Properties;

public class HttpClientFactory {

    private static volatile HttpClientFactory httpClientManagementFactory;

    private volatile HttpClient client;

    private String token;

    private CredentialsProvider credentialsProvider;

    private HttpClientFactory() {
    }

    public static HttpClientFactory getInstance() {
        if (httpClientManagementFactory == null) {
            synchronized (HttpClientFactory.class) {
                if (httpClientManagementFactory == null) {
                    httpClientManagementFactory = new HttpClientFactory();
                }
            }
        }
        return httpClientManagementFactory;
    }

    public static void tryUpdateRememberMe(HttpResponse re) {
        Header[] cookies = re.getHeaders("Set-Cookie");
        String rememberMeToken = null;
        for (Header cookie : cookies) {
            String content = cookie.getValue();
            if (content.substring(0, content.indexOf('=')).trim().equals("remember-me")) {
                rememberMeToken = content.substring(content.indexOf('=') + 1, content.indexOf(';'));
                break;
            }
        }
        if (rememberMeToken == null) {
            return;
        }
        HttpClientFactory.getInstance().setRememberToken(rememberMeToken);
    }

    public void setRememberToken(String token) {
        this.token = token;
        try {
            Properties properties = ClientProperties.getProperties();
            properties.setProperty("token", token);
            ClientProperties.saveProperties(properties);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        client = null;
    }

    public void  setCredentialsProvider(CredentialsProvider provider) {
        this.credentialsProvider = provider;
        client = null;
        invalidateToken();
    }

    public void invalidateToken() {
        token = null;
        Properties properties;
        try {
            properties = ClientProperties.getProperties();
            if (properties.containsKey("token")) {
                properties.remove("token");
                ClientProperties.saveProperties(properties);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public HttpClient getHttpClient() throws IOException {
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    if (token == null) {
                        Properties properties = ClientProperties.getProperties();
                        String token1 = properties.getProperty("token");
                        if (token1 != null) {
                            token = token1;
                        }
                    }
                    if (token != null) {
                        BasicCookieStore cookieStore = new BasicCookieStore();
                        BasicClientCookie cookie = new BasicClientCookie("remember-me", token);
                        cookie.setDomain("localhost");
                        cookie.setPath("/");
                        cookieStore.addCookie(cookie);

                        client = HttpClientBuilder
                                .create()
                                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                                .setDefaultCookieStore(cookieStore)
                                .build();
                    } else if (credentialsProvider != null) {
                        client = HttpClientBuilder
                                .create()
                                .setDefaultAuthSchemeRegistry(AuthScheme.getAuthScheme())
                                .setDefaultCredentialsProvider(credentialsProvider)
                                .build();
                    } else {
                        throw new InvalidHttpClientFactoryStateException("can't build client, specify credential provider or token");
                    }
                }
            }
        }

        return client;
    }
}

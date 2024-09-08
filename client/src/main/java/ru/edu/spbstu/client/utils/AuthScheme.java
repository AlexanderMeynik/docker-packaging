package ru.edu.spbstu.client.utils;

import org.apache.http.Consts;
import org.apache.http.auth.AuthSchemeProvider;
import org.apache.http.client.config.AuthSchemes;
import org.apache.http.config.Lookup;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.impl.auth.BasicSchemeFactory;

public class AuthScheme {
    public static Lookup<AuthSchemeProvider> getAuthScheme() {
        return RegistryBuilder.<org.apache.http.auth.AuthSchemeProvider>create()
                .register(AuthSchemes.BASIC, new BasicSchemeFactory(Consts.UTF_8)).build();
    }
}

package com.boaglio.player456.old;

import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;

public class HttpClientRequestFactory implements ClientHttpRequestFactory {

    private final SimpleClientHttpRequestFactory factory;

    public HttpClientRequestFactory(Duration timeout) {
        this.factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout((int) timeout.toMillis());
        factory.setReadTimeout((int) timeout.toMillis());
    }

    @Override
    public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) throws IOException {
        return factory.createRequest(uri, httpMethod);
    }
}
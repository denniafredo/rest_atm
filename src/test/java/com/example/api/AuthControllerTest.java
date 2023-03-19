package com.example.api;

import java.net.URI;
import java.net.URISyntaxException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import com.example.api.dto.jwt.JwtLogoutResponseDTO;
import com.example.api.dto.jwt.JwtRequestDTO;
import com.example.api.dto.jwt.JwtResponseDTO;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class AuthControllerTest {
    @Autowired
    private TestRestTemplate restTemplate;

    private String jwtToken;

    @Before
    @Test
    public void testLoginSuccess() throws URISyntaxException {
        final String baseUrl = "http://localhost:8080/auth/login/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        // headers.set("X-COM-PERSIST", "true");

        // login as Alice
        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO("Alice", "Alice");
        HttpEntity<JwtRequestDTO> request = new HttpEntity<>(jwtRequestDTO, headers);
        ResponseEntity<JwtResponseDTO> result = this.restTemplate.postForEntity(uri, request, JwtResponseDTO.class);

        Assert.assertEquals(200, result.getStatusCodeValue());
        assertThat(result.getBody().getJwttoken(), notNullValue());
        Assert.assertTrue(result.getBody().getMessage().size() >= 2);

        jwtToken = result.getBody().getJwttoken();

    }

    @Test
    public void testLogoutSuccess() throws URISyntaxException {

        final String baseUrl = "http://localhost:8080/auth/logout/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", jwtToken));

        JwtRequestDTO jwtRequestDTO = new JwtRequestDTO("", "");
        HttpEntity<JwtRequestDTO> request = new HttpEntity<>(jwtRequestDTO, headers);

        ResponseEntity<JwtLogoutResponseDTO> result = this.restTemplate.postForEntity(uri, request,
                JwtLogoutResponseDTO.class);

        Assert.assertEquals("Goodbye, Alice!", result.getBody().getMessage());
        Assert.assertEquals(200, result.getStatusCodeValue());
        // Assert.assertEquals(result.getBody().getMessage(), "Goodbye, Alice!");

    }
}
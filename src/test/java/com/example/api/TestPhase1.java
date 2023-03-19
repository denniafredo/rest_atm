package com.example.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
import com.example.api.dto.transaction.DepositDTO;
import com.example.api.dto.transaction.DepositLogDTO;
import com.example.api.dto.transaction.DepositLogResponseDTO;
import com.example.api.dto.transaction.ResponseDTO;
import com.example.api.dto.transaction.TransferDTO;
import com.example.api.dto.transaction.WithdrawDTO;
import com.example.api.service.DepositLogService;
import com.example.api.service.TransactionService;
import com.example.api.service.UserService;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TestPhase1 {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private DepositLogService depositLogService;

    @Autowired
    private UserService userService;

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
    public void testDepositSuccess() throws URISyntaxException {

        final String baseUrl = "http://localhost:8080/api/deposit/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", jwtToken));

        DepositDTO depositDTO = new DepositDTO(200.0);
        HttpEntity<DepositDTO> request = new HttpEntity<>(depositDTO, headers);

        ResponseEntity<ResponseDTO> result = this.restTemplate.postForEntity(uri, request,
                ResponseDTO.class);

        Assert.assertEquals("Processing your deposit...", result.getBody().getMessage().get(0));
        Assert.assertEquals("Please check the deposit log periodically", result.getBody().getMessage().get(1));
        Assert.assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testTransferFailed() throws URISyntaxException {

        final String baseUrl = "http://localhost:8080/api/transfer/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", jwtToken));

        TransferDTO transferDTO = new TransferDTO("Bob", 50.0);
        HttpEntity<TransferDTO> request = new HttpEntity<>(transferDTO, headers);

        ResponseEntity<ResponseDTO> result = this.restTemplate.postForEntity(uri, request,
                ResponseDTO.class);

        Assert.assertEquals("Receiver is not exist", result.getBody().getMessage().get(0));
        Assert.assertEquals(200, result.getStatusCodeValue());

    }

    @Test
    public void testWithdrawSuccess() throws URISyntaxException {

        final String baseUrl = "http://localhost:8080/api/withdraw/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", jwtToken));

        WithdrawDTO withdrawDTO = new WithdrawDTO(100.0);
        HttpEntity<WithdrawDTO> request = new HttpEntity<>(withdrawDTO, headers);

        ResponseEntity<ResponseDTO> result = this.restTemplate.postForEntity(uri, request,
                ResponseDTO.class);

        Assert.assertEquals("$100.0 Withdrawed Successfully", result.getBody().getMessage().get(0));
        Assert.assertEquals("Your balance is $100.0", result.getBody().getMessage().get(1));
        Assert.assertEquals(200, result.getStatusCodeValue());
    }

    @Test
    public void testWithdrawFailed() throws URISyntaxException {

        final String baseUrl = "http://localhost:8080/api/withdraw/";
        URI uri = new URI(baseUrl);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", String.format("Bearer %s", jwtToken));

        WithdrawDTO withdrawDTO = new WithdrawDTO(1000.0);
        HttpEntity<WithdrawDTO> request = new HttpEntity<>(withdrawDTO, headers);

        ResponseEntity<ResponseDTO> result = this.restTemplate.postForEntity(uri, request,
                ResponseDTO.class);

        Assert.assertEquals("Withdraw failed, Insufficient Balance!", result.getBody().getMessage().get(0));
        Assert.assertEquals("Your balance is $100.0", result.getBody().getMessage().get(1));
        Assert.assertEquals(200, result.getStatusCodeValue());
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
    }
}
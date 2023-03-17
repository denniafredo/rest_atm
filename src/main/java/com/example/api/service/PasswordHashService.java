package com.example.api.service;

import com.password4j.Password;
import com.password4j.BcryptFunction;
import org.springframework.stereotype.Service;

@Service
public class PasswordHashService {
    public String hashPassword(String plainPassword) {
        return Password.hash(plainPassword)
                .withBcrypt()
                .getResult();
    }
    public boolean checkPassword(String plainPassword, String hashedPassword) {
        BcryptFunction bcryptFunction = BcryptFunction.getInstanceFromHash(hashedPassword);
        return bcryptFunction.check(plainPassword, hashedPassword);
    }
}

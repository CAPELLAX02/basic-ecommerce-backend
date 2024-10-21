package com.capellax.ecommerce.service;

import com.capellax.ecommerce.api.model.LoginBody;
import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final EncryptionService encryptionService; // TODO: create the service
    private final JWTService jwtService // TODO: create the service

    public LocalUser registerUser(
            RegistrationBody registrationBody
    ) throws UserAlreadyExistsException {
        if (localUserDAO.findByEmailIgnoreCase(registrationBody.getEmail()).isPresent()
                || localUserDAO.findByUsernameIgnoreCase(registrationBody.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException();
        }
        LocalUser user = new LocalUser();
        user.setEmail(registrationBody.getEmail());
        user.setFirstName(registrationBody.getFirstName());
        user.setLastName(registrationBody.getLastName());
        user.setUsername(registrationBody.getUsername());
        // TODO: Encrypt password
        user.setPassword(registrationBody.getPassword());
        return localUserDAO.save(user);
    }

    public String loginUser(
            LoginBody loginBody
    ) {
        Optional<LocalUser> optionalUser = localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername());
        if (optionalUser.isPresent()) {
            LocalUser user = optionalUser.get();

            if (encryptionService.verifyPassword(loginBody.getPassword()), user.getPassword()) {
                return jwtService.generateJWT(user);
            }
        }
        return null;
    }











}

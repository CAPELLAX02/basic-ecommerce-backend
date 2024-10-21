package com.capellax.ecommerce.service;

import com.capellax.ecommerce.api.model.LoginBody;
import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;

    public void registerUser(
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
        user.setPassword(encryptionService.encryptPassword(registrationBody.getPassword()));
        localUserDAO.save(user);
    }

    public String loginUser(
            LoginBody loginBody
    ) {
        return localUserDAO.findByUsernameIgnoreCase(loginBody.getUsername())
                .filter(user -> encryptionService.verifyPassword(loginBody.getPassword(), user.getPassword()))
                .map(jwtService::generateJWT)
                .orElse(null);
    }


}

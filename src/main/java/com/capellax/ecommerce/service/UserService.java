package com.capellax.ecommerce.service;

import com.capellax.ecommerce.api.model.LoginBody;
import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.VerificationToken;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import com.capellax.ecommerce.model.dao.VerificationTokenDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LocalUserDAO localUserDAO;
    private final VerificationTokenDAO verificationTokenDAO;
    private final EncryptionService encryptionService;
    private final JWTService jwtService;
    // TODO: Inject email service later on

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

        VerificationToken verificationToken = createVerificationToken(user);

        // TODO: send the verification token using the email service

        localUserDAO.save(user);
    }

    public VerificationToken createVerificationToken(
            LocalUser user
    ) {
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(jwtService.generateVerificationJWT(user));
        verificationToken.setCreatedTimestamp(new Timestamp(System.currentTimeMillis()));
        verificationToken.setUser(user);
        user.getVerificationTokens().add(verificationToken);
        return verificationToken;
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

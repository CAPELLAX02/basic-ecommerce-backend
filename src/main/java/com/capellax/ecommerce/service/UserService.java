package com.capellax.ecommerce.service;

import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final LocalUserDAO localUserDAO;

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

}

package com.capellax.ecommerce.service;

import com.capellax.ecommerce.api.model.LoginBody;
import com.capellax.ecommerce.api.model.PasswordResetBody;
import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.EmailFailureException;
import com.capellax.ecommerce.exception.EmailNotFoundException;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.exception.UserNotVerifiedException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.VerificationToken;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import com.capellax.ecommerce.model.dao.VerificationTokenDAO;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.junit5.GreenMailExtension;
import com.icegreen.greenmail.util.ServerSetupTest;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class UserServiceTest {

    @RegisterExtension
    private static final GreenMailExtension greenMailExtension = new GreenMailExtension(ServerSetupTest.SMTP)
            .withConfiguration(
                    GreenMailConfiguration.
                            aConfig().
                            withUser("springboot", "secret")
            )
            .withPerMethodLifecycle(true);

    @Autowired
    private UserService userService;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private LocalUserDAO localUserDAO;

    @Autowired
    private EncryptionService encryptionService;

    @Autowired
    private VerificationTokenDAO verificationTokenDAO;

    @Test
    @Transactional
    public void testRegisterUser() throws MessagingException {
        RegistrationBody registrationBody = new RegistrationBody();

        registrationBody.setUsername("UserA");
        registrationBody.setEmail("UserServiceTest$testRegisterUser@junit.com");
        registrationBody.setFirstName("FirstName");
        registrationBody.setLastName("LastName");
        registrationBody.setPassword("MySecretPassword123");

        Assertions.assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(registrationBody), "Username should already be in use."
        );

        registrationBody.setUsername("UserServiceTest$testRegisterUser");
        registrationBody.setEmail("UserA@junit.com");

        Assertions.assertThrows(
                UserAlreadyExistsException.class,
                () -> userService.registerUser(registrationBody), "Email should already be in use.");

        registrationBody.setEmail("UserServiceTest$testRegisterUser@junit.com");

        Assertions.assertDoesNotThrow(
                () -> userService.registerUser(registrationBody), "User should register successfully."
        );

        Assertions.assertEquals(
                registrationBody.getEmail(),
                greenMailExtension
                        .getReceivedMessages()[0]
                        .getRecipients(Message.RecipientType.TO)[0]
                        .toString());

    }

    @Test
    @Transactional
    public void testLoginUser() throws UserNotVerifiedException, EmailFailureException {
        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserA-NotExists");
        loginBody.setPassword("PasswordA123-BadPassword");

        Assertions.assertNull(
                userService.loginUser(loginBody),
                "The user should not exist."
        );

        loginBody.setUsername("UserA");

        Assertions.assertNull(
                userService.loginUser(loginBody),
                "The password should be incorrect."
        );

        loginBody.setPassword("PasswordA123");

        Assertions.assertNotNull(
                userService.loginUser(loginBody),
                "The user should login successfully."
        );

        loginBody.setUsername("UserB");
        loginBody.setPassword("PasswordB123");

        try {
            userService.loginUser(loginBody);
            Assertions.assertTrue(
                    false,
                    "User should not have email verified."
            );
        } catch (UserNotVerifiedException exception) {
            Assertions.assertTrue(
                    exception.isNewEmailSent(),
                    "Email verification should be sent."
            );
        }

        try {
            userService.loginUser(loginBody);
            Assertions.assertTrue(
                    false,
                    "User should not have email verified."
            );
        } catch (UserNotVerifiedException exception) {
            Assertions.assertFalse(
                    exception.isNewEmailSent(),
                    "Email verification should not be resent."
            );
            Assertions.assertEquals(
                    1,
                    greenMailExtension.getReceivedMessages().length
            );
        }
    }

    @Test
    @Transactional
    public void testVerifyUser() throws EmailFailureException {
        Assertions.assertFalse(
                userService.verifyUser("Bad Token"),
                "Token that is bad or does not exist should return false."
        );

        LoginBody loginBody = new LoginBody();

        loginBody.setUsername("UserB");
        loginBody.setPassword("PasswordB123");

        try {
            userService.loginUser(loginBody);
            Assertions.assertTrue(
                    false,
                    "User should not have email verified."
            );
        } catch (UserNotVerifiedException exception) {
            List<VerificationToken> tokens = verificationTokenDAO.findByUser_IdOrderByIdDesc(2L);
            String token = tokens.get(0).getToken();
            Assertions.assertTrue(
                    userService.verifyUser(token),
                    "Token should be valid."
            );
            Assertions.assertNotNull(
                    loginBody,
                    "The user should now be verified."
            );
        }
    }

    @Test
    @Transactional
    public void testForgotPassword() throws MessagingException {
        Assertions.assertThrows(
                EmailNotFoundException.class,
                () -> userService.forgotPassword("UserNotExist@junit.com")
        );

        Assertions.assertDoesNotThrow(
                () -> userService.forgotPassword("UserA@junit.com"),
                "Non existing email should be rejected."
        );

        Assertions.assertEquals(
                "UserA@junit.com",
                greenMailExtension.getReceivedMessages()[0]
                        .getRecipients(Message.RecipientType.TO)[0].toString(),
                "Password " + "reset email should be sent."
        );
    }

    @Test
    public void testResetPassword() {
        LocalUser user = localUserDAO.findByUsernameIgnoreCase("UserA").get();
        String token = jwtService.generatePasswordResetJWT(user);
        PasswordResetBody passwordResetBody = new PasswordResetBody();

        passwordResetBody.setToken(token);
        passwordResetBody.setPassword("Password123456");

        userService.resetPassword(passwordResetBody);

        user = localUserDAO.findByUsernameIgnoreCase("UserA").get();

        Assertions.assertTrue(
                encryptionService.verifyPassword(
                        "Password123456",
                        user.getPassword()
                ),
                "Password change should be written to database."
        );
    }

}

package com.capellax.ecommerce.api.controller.auth;

import com.capellax.ecommerce.api.model.LoginBody;
import com.capellax.ecommerce.api.model.LoginResponse;
import com.capellax.ecommerce.api.model.RegistrationBody;
import com.capellax.ecommerce.exception.EmailFailureException;
import com.capellax.ecommerce.exception.UserAlreadyExistsException;
import com.capellax.ecommerce.exception.UserNotVerifiedException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<Void> registerUser(
            @Valid @RequestBody RegistrationBody registrationBody
    ) {
        try {
            userService.registerUser(registrationBody);
            return ResponseEntity.ok().build();
        } catch (UserAlreadyExistsException exp) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (EmailFailureException exp) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(
            @Valid @RequestBody LoginBody loginBody
    ) {
        String jwt = null;
        try {
            jwt = userService.loginUser(loginBody);
        } catch (UserNotVerifiedException exp) {
            LoginResponse response = new LoginResponse();
            response.setSuccess(false);
            String reason = "USER_NOT_VERIFIED";
            if (exp.isNewEmailSent()) {
                reason += "EMAIL_RESENT";
            }
            response.setFailureReason(reason);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        } catch (EmailFailureException exp) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        if (jwt == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } else {
          LoginResponse response = new LoginResponse();
          response.setJwt(jwt);
          response.setSuccess(true);
          return ResponseEntity.ok(response);
      }
    }

    @PostMapping("/verify")
    public ResponseEntity<Void> verifyEmail(
            @RequestBody String token
    ) {
        if (userService.verifyUser(token)) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @GetMapping("/me")
    public LocalUser getLoggedInUserProfile(
            @AuthenticationPrincipal LocalUser user
    ) {
        return user;
    }











}

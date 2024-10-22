package com.capellax.ecommerce.exception;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class UserNotVerifiedException extends Exception {

    private boolean newEmailSent;

    public boolean isNewEmailSent() {
        return newEmailSent;
    }

}

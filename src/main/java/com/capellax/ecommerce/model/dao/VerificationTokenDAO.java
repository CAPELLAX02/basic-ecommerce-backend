package com.capellax.ecommerce.model.dao;

import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.VerificationToken;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface VerificationTokenDAO extends ListCrudRepository<VerificationToken, Long> {

    Optional<VerificationToken> findByToken(String token);
    void deleteByUser(LocalUser user);

}

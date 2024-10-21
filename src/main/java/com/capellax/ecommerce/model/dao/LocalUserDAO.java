package com.capellax.ecommerce.model.dao;

import com.capellax.ecommerce.model.LocalUser;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface LocalUserDAO extends CrudRepository<LocalUser, Long> {

    Optional<LocalUser> findByUsernameIgnoreCase(String name);
    Optional<LocalUser> findByEmailIgnoreCase(String email);

}

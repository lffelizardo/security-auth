package br.com.test.repository;

import br.com.test.domain.User;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Integer> {
    <User> Optional<User> findByEmail(String username);
}

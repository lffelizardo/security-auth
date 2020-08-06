package br.com.test.service;

import br.com.test.config.authentication.CustomUserDetails;
import br.com.test.domain.User;
import br.com.test.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository repository;

    public UserService() {
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = (User) repository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found: " + username));
        return new CustomUserDetails(user, user.getUsername());
    }
}

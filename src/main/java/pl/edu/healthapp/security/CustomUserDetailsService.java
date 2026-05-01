package pl.edu.healthapp.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.edu.healthapp.exception.UserNotFoundException;
import pl.edu.healthapp.repository.UserRepository;

import java.util.UUID;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;


    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    /*@Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return new CustomUserDetails(userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("username", username)));
    }*/

   @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        UUID uuid = UUID.fromString(userId);

        return new CustomUserDetails(userRepository.findById(uuid)
                .orElseThrow(() -> new UserNotFoundException("uuid", uuid.toString())));
    }
}

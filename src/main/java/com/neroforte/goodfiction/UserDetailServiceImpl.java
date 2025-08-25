package com.neroforte.goodfiction;

import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserEntity> userEntity = userRepository.findByUsername(username);

        return userEntity.map(UserDetailsImpl::new)
                .orElseThrow(() -> new UsernameNotFoundException(username + " not found"));

    }
}

package com.neroforte.goodfiction.serivce;


import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.UserAlreadyExistsException;
import com.neroforte.goodfiction.exception.UserNotFoundException;
import com.neroforte.goodfiction.model.Password;
import com.neroforte.goodfiction.model.User;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public Optional<User> getUserByUsername(String username) throws UserNotFoundException {
        var userEntity = userRepository.findByUsername(username);
        if (userEntity.isEmpty()) {
            return Optional.ofNullable(User.entityToUser(userEntity.get()));
        } else {
            throw new UserNotFoundException("user with such username not found: " + username);
        }

    }

    public Optional<User> getUserById(Long id) throws UserNotFoundException {
        var userEntity = userRepository.findById(id);
        if (userEntity.isPresent()) {
            return Optional.of(User.entityToUser(userEntity.get()));
        } else {
            throw new UserNotFoundException("user with such id not found: " + id);
        }
    }

    public List<User> getAllUsers() throws UserNotFoundException {
        List<UserEntity> entities = userRepository.findAll();
        if (!entities.isEmpty()) {
            return entities.stream().map(User::entityToUser).toList();
        } else {
            throw new UserNotFoundException("users not found");
        }
    }

    public Optional<User> saveUser(UserEntity userEntity) throws UserAlreadyExistsException {
        userEntity.setPassword(bCryptPasswordEncoder.encode(userEntity.getPassword()));
        if (userRepository.findByUsername(userEntity.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("user with such username already exists: " + userEntity.getUsername());
        } else {
            userEntity.setCreatedDate(LocalDateTime.now());
            UserEntity saved = userRepository.save(userEntity);
            return Optional.of(User.entityToUser(saved));
        }
    }


    public Optional<User> updateUser(Long id, UserEntity userEntity) throws UserNotFoundException {
        Optional<UserEntity> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            UserEntity temp = existingUser.get();
            temp.setUsername(userEntity.getUsername());
            temp.setEmail(userEntity.getEmail());
            userRepository.save(temp);
            return Optional.of(User.entityToUser(temp));
        } else {
            throw new UserNotFoundException("user with such id not found: " + userEntity.getId());
        }
    }

    public void updatePassword(Long id , Password password) throws Exception {
        Optional<UserEntity> existingUser = userRepository.findById(id);
        if(existingUser.isPresent()){
            UserEntity temp = existingUser.get();
            if(!bCryptPasswordEncoder.matches(password.getOldPassword(), temp.getPassword())){
                throw new Exception("Passwords do not match");
            }else{
                temp.setPassword(bCryptPasswordEncoder.encode(password.getNewPassword()));
                userRepository.save(temp);
            }
        }
    }

    @Transactional
    public void deleteUser(Long id) throws UserNotFoundException {
        Optional<UserEntity> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            userRepository.deleteById(Math.toIntExact(id));
        } else {
            throw new UserNotFoundException("user with such id not found: " + id);
        }
    }

    @Transactional
    public void deleteUserByUsername(String username) throws UserNotFoundException {
        Optional<UserEntity> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            userRepository.deleteByUsername(username);
        } else {
            throw new UserNotFoundException("user with such username not found: " + username);
        }
    }
}

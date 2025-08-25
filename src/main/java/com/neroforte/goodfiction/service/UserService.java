package com.neroforte.goodfiction.service;


import com.neroforte.goodfiction.DTO.*;
import com.neroforte.goodfiction.mapper.UserMapper;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.exception.PasswordsDontMatchException;
import com.neroforte.goodfiction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final UserMapper userMapper;


    public UserResponse getUserById(Long id) throws NotFoundException {
        return userMapper.userToUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with such id not found: " + id)));
    }


    public UserEntity getUserEntityById(Long id) throws NotFoundException {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with such id not found: " + id));
    }


    public List<UserResponse> getAllUsers(int limit) throws NotFoundException {
        Pageable pageable = PageRequest.of(0, limit);
        List<UserEntity> entities = userRepository.findAll(pageable).getContent();
        return entities.stream().map(userMapper::userToUserResponse).toList();
    }


    @Transactional
    public UserResponse saveUser(UserRegisterRequest user) throws AlreadyExistsException {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new AlreadyExistsException("user with such username already exists: " + user.getUsername());
        } else {
            UserEntity userEntity = userMapper.userRegisterToUserEntity(user);
            userEntity.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
            userEntity.setRoles("ROLE_USER");
            userRepository.save(userEntity);
            return userMapper.userToUserResponse(userEntity);
        }
    }


    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) throws NotFoundException {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with such id not found: " + id));
        existingUser.setUsername(userUpdateRequest.getUsername());
        existingUser.setEmail(userUpdateRequest.getEmail());
        userRepository.save(existingUser);
        return userMapper.userToUserResponse(existingUser);
    }


    public void updatePassword(Long id, Password password) throws PasswordsDontMatchException {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("user with such id not found: " + id));
        if (!bCryptPasswordEncoder.matches(password.getOldPassword(), existingUser.getPassword())) {
            throw new PasswordsDontMatchException("Passwords do not match");
        } else {
            existingUser.setPassword(bCryptPasswordEncoder.encode(password.getNewPassword()));
            userRepository.save(existingUser);
        }
    }


    @Transactional
    public void deleteUser(Long id) throws EmptyResultDataAccessException {
        userRepository.deleteById(id);
    }


    @Transactional
    public void deleteUserByUsername(String username) throws EmptyResultDataAccessException {
        userRepository.deleteByUsername(username);
    }

}

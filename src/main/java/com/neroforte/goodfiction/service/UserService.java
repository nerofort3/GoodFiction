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

    public UserResponse getUserByUsername(String username) throws NotFoundException {
        var userEntity = userRepository.findByUsername(username);
        if (userEntity.isPresent()) {
            return userMapper.userToUserResponse(userEntity.get());
        } else {
            throw new NotFoundException("user with such username not found: " + username);
        }

    }

    public UserResponse getUserById(Long id) throws NotFoundException {
        var userEntity = userRepository.findById(id).orElseThrow(() -> new NotFoundException("user with such username not found: " + id));
        return userMapper.userToUserResponse(userEntity);
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
            userRepository.save(userEntity);
            return userMapper.userToUserResponse(userEntity);
        }
    }


    public UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest) throws NotFoundException {
        Optional<UserEntity> existingUser = userRepository.findById(id);
        if (existingUser.isPresent()) {
            UserEntity temp = existingUser.get();
            temp.setUsername(userUpdateRequest.getUsername());
            temp.setEmail(userUpdateRequest.getEmail());
            userRepository.save(temp);
            return userMapper.userToUserResponse(temp);
        } else {
            throw new NotFoundException("user with such id not found: " + id);
        }
    }

    public void updatePassword(Long id , Password password) throws PasswordsDontMatchException {
        Optional<UserEntity> existingUser = userRepository.findById(id);
        if(existingUser.isPresent()){
            UserEntity temp = existingUser.get();
            if(!bCryptPasswordEncoder.matches(password.getOldPassword(), temp.getPassword())){
                throw new PasswordsDontMatchException("Passwords do not match");
            }else{
                temp.setPassword(bCryptPasswordEncoder.encode(password.getNewPassword()));
                userRepository.save(temp);
            }
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

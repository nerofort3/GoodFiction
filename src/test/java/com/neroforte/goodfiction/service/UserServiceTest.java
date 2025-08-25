package com.neroforte.goodfiction.service;

import com.neroforte.goodfiction.DTO.Password;
import com.neroforte.goodfiction.DTO.UserRegisterRequest;
import com.neroforte.goodfiction.DTO.UserResponse;
import com.neroforte.goodfiction.DTO.UserUpdateRequest;
import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.AlreadyExistsException;
import com.neroforte.goodfiction.exception.NotFoundException;
import com.neroforte.goodfiction.exception.PasswordsDontMatchException;
import com.neroforte.goodfiction.mapper.UserMapper;
import com.neroforte.goodfiction.repository.UserRepository;
import lombok.ToString;
import org.hibernate.validator.constraints.Mod10Check;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @InjectMocks
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    private UserEntity user;


    @BeforeEach
    void setup() {
        user = new UserEntity();
        user.setId(1L);
        user.setUsername("john");
        user.setPassword("encoded-old");
        user.setEmail("john@example.com");
        user.setRoles("ROLE_USER");
    }


    @Test
    void getUserById_found_returnsMappedResponse() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserResponse response = new UserResponse();
        when(userMapper.userToUserResponse(user)).thenReturn(response);
        //when
        UserResponse actualResponse = userService.getUserById(1L);
        //then
        assertSame(response, actualResponse);
        verify(userRepository).findById(1L);
        verify(userMapper).userToUserResponse(user);
    }

    @Test
    void getUserById_not_found_throwsNotFoundException() {

        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(42L));
    }

    @Test
    void getUserEntityById_found_returnsUserEntity() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        //when
        UserEntity actualEntity = userService.getUserEntityById(1L);
        //then
        assertSame(actualEntity, user);
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserEntityById_not_found_throwsNotFoundException() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserEntityById(42L));
    }

    @Test
    void getAllUsers_found_returnsMappedListWithLimit() {
        //given
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new UserEntity(), new UserEntity())));
        when(userMapper.userToUserResponse(any(UserEntity.class))).thenReturn(new UserResponse());
        //when
        var list = userService.getAllUsers(2);
        //then
        assertEquals(2, list.size());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void getAllUsers_not_found_returnsEmptyList() {
        //given
        when(userRepository.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        //when
        var list = userService.getAllUsers(2);
        //then
        assertEquals(0, list.size());
        verify(userRepository).findAll(any(Pageable.class));
    }

    @Test
    void saveUser_whenNew_encodesPassword_setsRole_savesUser() {
        //given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("new-name");
        request.setPassword("new-password");

        UserEntity userToSave = new UserEntity();
        userToSave.setUsername("new-name");
        //when
        when(userRepository.findByUsername("new-name")).thenReturn(Optional.empty());
        when(userMapper.userRegisterToUserEntity(request)).thenReturn(userToSave);
        when(bCryptPasswordEncoder.encode("new-password")).thenReturn("new-password-encoded");
        when(userMapper.userToUserResponse(userToSave)).thenReturn(new UserResponse());
        //then
        UserResponse userResponse = userService.saveUser(request);
        assertNotNull(userResponse);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        UserEntity saved = captor.getValue();

        assertEquals("new-password-encoded", saved.getPassword());
        assertEquals("ROLE_USER", saved.getRoles());
    }

    @Test
    void saveUser_whenUsernameExists_throwsAlreadyExistsException() {
        //given
        UserRegisterRequest request = new UserRegisterRequest();
        request.setUsername("john");
        //when
        when(userRepository.findByUsername("john")).thenReturn(Optional.of(new UserEntity()));
        //then
        assertThrows(AlreadyExistsException.class, () -> userService.saveUser(request));
        verify(userRepository, never()).save(any(UserEntity.class));
    }


    @Test
    void updateUser_whenExists_updatesUsername_updatesEmail_savesAndReturnsUserResponse() {
        //given
        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("new-name");
        request.setEmail("new-email");

        //when
        UserResponse mapped = new UserResponse();
        mapped.setUsername("new-username");
        mapped.setEmail("new-email");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.userToUserResponse(user)).thenReturn(mapped);

        UserResponse result = userService.updateUser(1L, request);
        //then
        assertSame(mapped, result);
        assertEquals("new-username", mapped.getUsername());
        assertEquals("new-email", mapped.getEmail());
        verify(userRepository).save(user);
    }


    @Test
    void updatePassword_whenMatches_encodesAndSaves() {
        //given
        Password password = new Password();
        password.setOldPassword("encoded-old");
        password.setNewPassword("encoded-new");
        //when
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches(user.getPassword(), "encoded-old")).thenReturn(true);
        when(bCryptPasswordEncoder.encode("encoded-new")).thenReturn("encoded-new");
        //then
        userService.updatePassword(1L, password);

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(user);
    }

    @Test
    void updatePassword_whenOldMismatch_throwsPasswordDontMatchException() {
        Password password = new Password();
        password.setOldPassword("request-wrong-old");
        password.setNewPassword("encoded-new");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bCryptPasswordEncoder.matches("request-wrong-old", "encoded-old")).thenReturn(false);


        assertThrows(PasswordsDontMatchException.class, () -> userService.updatePassword(1L, password));
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void deleteUser_invokesRepository() {
        userService.deleteUser(1L);
        verify(userRepository).deleteById(1L);
    }


    @Test
    void deleteUserByUsername_found_invokesRepository() {
        userService.deleteUserByUsername("john");
        verify(userRepository).deleteByUsername("john");
    }
}
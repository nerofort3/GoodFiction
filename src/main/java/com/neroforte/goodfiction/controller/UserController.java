package com.neroforte.goodfiction.controller;

import com.neroforte.goodfiction.entity.UserEntity;
import com.neroforte.goodfiction.exception.UserAlreadyExistsException;
import com.neroforte.goodfiction.exception.UserNotFoundException;
import com.neroforte.goodfiction.model.Password;
import com.neroforte.goodfiction.model.User;
import com.neroforte.goodfiction.serivce.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@AllArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/check")
    public ResponseEntity availabilityCheck() {
        try {
            return ResponseEntity.ok("all good, it works");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error, service unavailable");
        }
    }

    @GetMapping("/all")
    public List<User> findAllUsers(){
        try{
            return userService.getAllUsers();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/get_user_by_id/{id}")
    public User findUserById(@PathVariable Long id){
        try{
            return userService.getUserById(id).get();
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    @PostMapping("create_user")
    public User createUser(@RequestBody UserEntity user){
        try {
            return userService.saveUser(user).get();
        } catch (UserAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }


    @PostMapping("/jiggy")
    public User addTempUsesr(){
        try {
            return userService.saveUser(new UserEntity("jiggy","aboba@gmail.com","piggy" )).get();
        } catch (UserAlreadyExistsException e) {
            throw new RuntimeException(e);
        }
    }
    @PutMapping("/update_user/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody UserEntity user){
        try {
            return userService.updateUser(id, user).get();
        } catch (UserNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @PatchMapping("update_user_password/{id}")
    public ResponseEntity updatePassword(@PathVariable Long id, @RequestBody Password password){
        try {
            userService.updatePassword(id,password);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
    }




    @DeleteMapping("/delete_user/{id}")
    public ResponseEntity deleteUser(@PathVariable  Long id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok("User deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body("something went wrong");
        }
    }

    @DeleteMapping("/delete_user_by_username/{username}")
    public ResponseEntity deleteUser(@PathVariable  String username){
        try {
            userService.deleteUserByUsername(username);
            return ResponseEntity.ok("User deleted successfully");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}

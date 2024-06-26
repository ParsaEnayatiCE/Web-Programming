package com.sharif.edu.hw1.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sharif.edu.hw1.DataBase.User;
import com.sharif.edu.hw1.Model.Error;
import com.sharif.edu.hw1.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @PutMapping("/users")
    public ResponseEntity<?> activateUser(@RequestParam String username, @RequestParam boolean active) {
        Error error= userService.activateUser(username, active);
        return error.equals(Error.USER_NAME_NOT_FOUND) ? ResponseEntity.status(400).body(error.getErrorDesc()) : ResponseEntity.ok(error.getErrorDesc());
    }

    @GetMapping("/users")
    public ResponseEntity<String> activateUser() throws JsonProcessingException {
        List<User> allUsers = userService.allUsers();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonArray = objectMapper.writeValueAsString(allUsers);
        return ResponseEntity.status(200).body(jsonArray);
    }
}
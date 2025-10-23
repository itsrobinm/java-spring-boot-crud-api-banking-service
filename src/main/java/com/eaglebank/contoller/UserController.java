package com.eaglebank.contoller;

import com.eaglebank.dto.UserDTO;
import com.eaglebank.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{userId}")
    public UserDTO getUser(@PathVariable String userId) {
        System.out.println(userId);
        return userService.getUserById(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody UserDTO user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    //add a patch mapping to update user partially
    @PatchMapping("/{userId}")
    public UserDTO patchUser(
            @PathVariable String userId,
            @RequestBody UserDTO userDTO) {
        return userService.patchUser(userId, userDTO);
    }
}
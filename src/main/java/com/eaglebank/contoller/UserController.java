package com.eaglebank.contoller;

import com.eaglebank.dto.UserDTO;
import com.eaglebank.exception.AccessDeniedException;
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
    public UserDTO getUser(@RequestHeader("user-id") String userIdHeader, @PathVariable String userId) {
        if (!userId.equals(userIdHeader)) {
            throw new AccessDeniedException(userIdHeader, userId);
        }
        return userService.getUserById(userIdHeader, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDTO createUser(@Valid @RequestBody UserDTO user) {
        return userService.createUser(user);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@RequestHeader("user-id") String userIdHeader, @PathVariable String userId) {
        userService.deleteUser(userIdHeader, userId);
    }

    @PatchMapping("/{userId}")
    public UserDTO patchUser(
            @RequestHeader("user-id") String userIdHeader,
            @PathVariable String userId,
            @RequestBody UserDTO userDTO) {
        if (!userId.equals(userIdHeader)) {
            throw new AccessDeniedException(userIdHeader, userId);
        }
        return userService.patchUser(userIdHeader, userId, userDTO);
    }
}
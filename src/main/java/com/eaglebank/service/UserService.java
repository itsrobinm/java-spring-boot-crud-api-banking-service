package com.eaglebank.service;

import com.eaglebank.dto.UserDTO;
import com.eaglebank.exception.UserNotFoundException;
import com.eaglebank.model.Address;
import com.eaglebank.model.User;
import com.eaglebank.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class UserService {
    private final UserRepository userRepository;
    private static final String ID_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int DEFAULT_ID_LENGTH = 5;
    private static final int MAX_ID_GEN_ATTEMPTS = 5;
    private static final String ID_PREFIX = "usr-";

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getUserById(String userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return user.toDTO();
    }

    public UserDTO createUser(UserDTO dto) {
        User user = new User();

        // Always generate the id on the server; ignore any client-provided id
        String id = null;
        int attempts = 0;
        do {
            // generate the alphanumeric suffix and prefix it with 'usr-'
            id = ID_PREFIX + generateId(DEFAULT_ID_LENGTH);
            attempts++;
        } while (userRepository.existsById(id) && attempts < MAX_ID_GEN_ATTEMPTS);

        if (userRepository.existsById(id)) {
            throw new IllegalStateException("Failed to generate a unique user id after " + MAX_ID_GEN_ATTEMPTS + " attempts");
        }

        user.setId(id);
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        // map new fields
        user.setAddress(Address.fromDTO(dto.getAddress()));
        user.setPhoneNumber(dto.getPhoneNumber());

        User saved = userRepository.save(user);
        return new UserDTO(saved.getId(), saved.getName(), saved.getEmail(), saved.getAddress() != null ? saved.getAddress().toDTO() : null, saved.getPhoneNumber());
    }

    public UserDTO patchUser(String userId, UserDTO userDTO) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Only update fields that are non-null in the DTO
        if (userDTO.getName() != null) {
            existingUser.setName(userDTO.getName());
        }
        if (userDTO.getEmail() != null) {
            existingUser.setEmail(userDTO.getEmail());
        }
        if (userDTO.getPhoneNumber() != null) {
            existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        }

        if (userDTO.getAddress() != null) {
            // merge address fields to avoid overwriting unspecified subfields with nulls
            Address existingAddress = existingUser.getAddress();
            if (existingAddress == null) {
                existingAddress = new Address();
            }
            if (userDTO.getAddress().getLine1() != null) existingAddress.setLine1(userDTO.getAddress().getLine1());
            if (userDTO.getAddress().getLine2() != null) existingAddress.setLine2(userDTO.getAddress().getLine2());
            if (userDTO.getAddress().getLine3() != null) existingAddress.setLine3(userDTO.getAddress().getLine3());
            if (userDTO.getAddress().getTown() != null) existingAddress.setTown(userDTO.getAddress().getTown());
            if (userDTO.getAddress().getCounty() != null) existingAddress.setCounty(userDTO.getAddress().getCounty());
            if (userDTO.getAddress().getPostcode() != null)
                existingAddress.setPostcode(userDTO.getAddress().getPostcode());
            existingUser.setAddress(existingAddress);
        }

        User savedUser = userRepository.save(existingUser);
        return savedUser.toDTO();
    }


    public void deleteUser(String userId) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        userRepository.delete(existingUser);
    }

    private String generateId(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int idx = RANDOM.nextInt(ID_ALPHABET.length());
            sb.append(ID_ALPHABET.charAt(idx));
        }
        return sb.toString();
    }
}

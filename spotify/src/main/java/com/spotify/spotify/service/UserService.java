package com.spotify.spotify.service;

import com.spotify.spotify.constaint.PredefinedRole;
import com.spotify.spotify.dto.request.UserCreationRequest;
import com.spotify.spotify.dto.request.UserUpdateRequest;
import com.spotify.spotify.dto.response.UserResponse;
import com.spotify.spotify.entity.Role;
import com.spotify.spotify.entity.User;
import com.spotify.spotify.exception.AppException;
import com.spotify.spotify.exception.ErrorCode;
import com.spotify.spotify.mapper.UserMapper;
import com.spotify.spotify.repository.RoleRepository;
import com.spotify.spotify.repository.UserRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserService {
    UserMapper userMapper;
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    public UserResponse createUser(UserCreationRequest request){
        if (userRepository.existsByUsername(request.getUsername())){
            throw new AppException(ErrorCode.USER_ALREADY_EXIST);
        }

        User user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);
        user.setRoles(roles);
        user.setEnabled(true);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e){
            if (userRepository.existsByEmail(request.getEmail())){
                throw new AppException(ErrorCode.EMAIL_EXISTED);
            }
        }
        return userMapper.toUserResponse(user);
    }

    public UserResponse updateUser(UserUpdateRequest request, String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found."));

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(String userId){
        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if (user.getUsername().equalsIgnoreCase("admin")){
            user.setEnabled(false);
            userRepository.save(user);
            log.info("Admin account disabled instead of deleted.");
            return;
        }
        user.getRoles().clear();
        userRepository.save(user);
        userRepository.deleteById(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUser(){
        return userRepository.findAll()
                .stream() //thay cho for
                .map(userMapper::toUserResponse)
                .toList();
    }
}
package com.vn.go_toeic.service;

import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private  final UserRepository userRepository;
    private  final RoleRepository roleRepository;



}

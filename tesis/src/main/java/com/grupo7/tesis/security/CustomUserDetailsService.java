package com.grupo7.tesis.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.grupo7.tesis.models.Estudiante;
import com.grupo7.tesis.models.Role;
import com.grupo7.tesis.models.UserEntity;
import com.grupo7.tesis.repositories.EstudianteRepository;
import com.grupo7.tesis.repositories.RoleRepository;
import com.grupo7.tesis.repositories.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity userDB = userRepository.findByUsername(username);
        if (userDB == null) {
            throw new UsernameNotFoundException("User not found");
        }
        UserDetails userDetails = new User(userDB.getUsername(),
            userDB.getPassword(),
            mapRolesToAuthorities(userDB.getRoles()));

        return userDetails;
    }

    private Collection<GrantedAuthority> mapRolesToAuthorities(List<Role> roles) {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.getName())).collect(Collectors.toList());
    }

    public UserEntity saveUser(User estudiante){
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(estudiante.getUsername());
        userEntity.setPassword(passwordEncoder.encode(estudiante.getPassword()));

        Role roles = roleRepository.findByName("USER");
        userEntity.setRoles(List.of(roles));
        return userRepository.save(userEntity);
    }
}

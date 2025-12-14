package ua.edu.viti.military.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ua.edu.viti.military.dto.request.LoginRequest;
import ua.edu.viti.military.dto.request.RegisterRequest;
import ua.edu.viti.military.dto.response.JwtResponse;
import ua.edu.viti.military.entity.Role;
import ua.edu.viti.military.entity.RoleName;
import ua.edu.viti.military.entity.User;
import ua.edu.viti.military.exception.DuplicateResourceException;
import ua.edu.viti.military.exception.ResourceNotFoundException;
import ua.edu.viti.military.repository.RoleRepository;
import ua.edu.viti.military.repository.UserRepository;
import ua.edu.viti.military.security.JwtUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public JwtResponse login(LoginRequest dto) {
        log.info("User login attempt: {}", dto.getUsername());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateToken(authentication);

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Користувача не знайдено"));

        return new JwtResponse(jwt, userDetails.getUsername(), user.getEmail(), roles);
    }

    @Transactional
    public String register(RegisterRequest dto) {
        log.info("User registration attempt: {}", dto.getUsername());

        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new DuplicateResourceException("Username вже зайнятий");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateResourceException("Email вже зареєстрований");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setMilitaryRank(dto.getMilitaryRank());
        user.setEnabled(true);

        Set<Role> roles = new HashSet<>();
        if (dto.getRoles() == null || dto.getRoles().isEmpty()) {
            Role viewerRole = roleRepository.findByName(RoleName.ROLE_VIEWER)
                    .orElseThrow(() -> new ResourceNotFoundException("Роль VIEWER не знайдено"));
            roles.add(viewerRole);
        } else {
            for (String roleName : dto.getRoles()) {
                try {
                    RoleName roleEnum = RoleName.valueOf(roleName);
                    Role role = roleRepository.findByName(roleEnum)
                            .orElseThrow(() -> new ResourceNotFoundException("Роль не знайдено: " + roleName));
                    roles.add(role);
                } catch (IllegalArgumentException e) {
                    throw new ResourceNotFoundException("Некоректна роль: " + roleName);
                }
            }
        }
        user.setRoles(roles);

        userRepository.save(user);
        return "Користувача зареєстровано успішно";
    }
}

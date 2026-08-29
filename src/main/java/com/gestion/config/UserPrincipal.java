package com.gestion.config;


import com.gestion.enums.Role;
import jakarta.annotation.Nullable;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserPrincipal implements UserDetails {

    private Long userId;
    private String email;
    private String name;
    private Role role;

    @Override
    @NonNull
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    @Nullable
    public String getPassword() {
        // esto es necesario porque vamos a usar UserDetails para manejar información, no para realizar el login
        return null;
    }

    @Override
    @NonNull
    public String getUsername() {
        return this.name;
    }
}

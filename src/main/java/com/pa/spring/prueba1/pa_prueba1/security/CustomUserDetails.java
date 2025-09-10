package com.pa.spring.prueba1.pa_prueba1.security;

import com.pa.spring.prueba1.pa_prueba1.model.Usuario;
import com.pa.spring.prueba1.pa_prueba1.model.Rol;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class CustomUserDetails implements UserDetails {

    private final Usuario usuario;

    public CustomUserDetails(Usuario usuario) {
        this.usuario = usuario;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return usuario.getRoles().stream()
                .map(rol -> new SimpleGrantedAuthority(rol.getNombre()))
                .collect(Collectors.toSet());
    }

    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    @Override
    public String getUsername() {
        return usuario.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // puedes agregar lógica si quieres controlar expiración
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // puedes agregar lógica si controlas bloqueo
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // idem arriba
    }

    @Override
    public boolean isEnabled() {
        return true; // puedes agregar campo "activo" en Usuario si lo necesitas
    }
}





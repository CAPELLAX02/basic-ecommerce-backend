package com.capellax.ecommerce.api.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.capellax.ecommerce.model.LocalUser;
import com.capellax.ecommerce.model.dao.LocalUserDAO;
import com.capellax.ecommerce.service.JWTService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JWTRequestFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final LocalUserDAO localUserDAO;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
      String tokenHeader = request.getHeader("Authorization");
      if (tokenHeader != null && tokenHeader.startsWith("Bearer")) {
          String token = tokenHeader.substring(7);
          try {
              String username = jwtService.getUsername(token);
              Optional<LocalUser> opUser = localUserDAO.findByUsernameIgnoreCase(username);
              if (opUser.isPresent()) {
                  LocalUser user = opUser.get();
                  if (user.isEmailVerified()) {
                      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                              user,
                              null,
                              new ArrayList<>()
                      );
                      authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                      SecurityContextHolder.getContext().setAuthentication(authentication);
                  }
              }
          } catch (JWTDecodeException exp) {
              throw new RuntimeException("Invalid JWT token");
          }
      }
      filterChain.doFilter(request, response);
    }
}

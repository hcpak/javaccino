package com.emotie.api.auth.domain;

import com.emotie.api.auth.infra.JwtTokenProvider;
import com.emotie.api.member.domain.Member;
import com.emotie.api.member.domain.MemberRole;
import com.emotie.api.member.domain.MemberRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends GenericFilterBean {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String token = jwtTokenProvider.getToken((HttpServletRequest) request);
        Optional.ofNullable(token)
                .map(jwtTokenProvider::getSubject)
                .map(userDetailsService::loadUserByUsername)
                .map(userDetails -> new UsernamePasswordAuthenticationToken(userDetails,
                        "",
                        userDetails.getAuthorities()))
                .ifPresentOrElse(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication), () -> {
                    Member publicMember = new Member(MemberRoles.getDefaultFor(MemberRole.PUBLIC));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(publicMember, "", publicMember.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                });

        chain.doFilter(request, response);
    }
}

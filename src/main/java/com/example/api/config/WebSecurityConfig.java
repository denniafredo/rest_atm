package com.example.api.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.api.service.JwtUserDetailsService;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    private static final String[] NO_AUTH = {
            "/auth/**",
            "/h2-console/**",
            "/api/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        // We don't need CSRF for this example
        // httpSecurity.csrf().disable()
        // .authorizeRequests().antMatchers(NO_AUTH).permitAll().and()
        // .headers(headers -> headers.frameOptions().disable())
        // .csrf(csrf -> csrf.ignoringAntMatchers(NO_AUTH))
        // .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint).and().sessionManagement()
        // .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // // Add a filter to validate the tokens with every request
        // httpSecurity.addFilterBefore(jwtRequestFilter,
        // UsernamePasswordAuthenticationFilter.class);

        // httpSecurity.csrf().disable()
        // .authorizeRequests()
        // .antMatchers(NO_AUTH).permitAll().and()
        // .headers(headers -> headers.frameOptions().disable())
        // .csrf(csrf -> csrf.ignoringAntMatchers(NO_AUTH))
        // .authorizeRequests().antMatchers("/api/**").authenticated()
        // .anyRequest().permitAll()
        // .and()
        // .addFilterBefore(jwtRequestFilter,
        // UsernamePasswordAuthenticationFilter.class);

        httpSecurity.csrf().disable()
                .authorizeRequests()
                .antMatchers(NO_AUTH).permitAll().and()
                .headers(headers -> headers.frameOptions().disable())
                // .csrf(csrf -> csrf.ignoringAntMatchers(NO_AUTH))
                // .authorizeRequests().antMatchers("/api/**").authenticated()
                // .anyRequest().permitAll()
                // .and()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.sessionManagement(session -> session
                .maximumSessions(1)
                .maxSessionsPreventsLogin(true));
        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, BCryptPasswordEncoder bCryptPasswordEncoder,
            JwtUserDetailsService userDetailService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailService)
                .passwordEncoder(bCryptPasswordEncoder)
                .and()
                .build();
    }
}
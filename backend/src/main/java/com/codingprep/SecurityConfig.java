package com.codingprep;

import java.time.Duration;
import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.LogoutConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.codingprep.features.auth.filter.JwtAuthenticationFilter;
import com.codingprep.features.auth.service.UserDetailsServiceImplementation;



@Configuration //Skip Spring Security autoconfig
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {


    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(UserDetailsService uD,JwtAuthenticationFilter jAF){
        this.jwtAuthFilter = jAF;
        this.userDetailsService = uD;
    }


    @Bean
    @Order(1)
    public SecurityFilterChain publicFilterChain(HttpSecurity http) throws Exception {
        http
                .securityMatcher("/actuator/health", "/ws/**")
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                );
        return http.build();
    }

	@Bean
    @Order(2)
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

       http.csrf((csrfCustomizer) -> csrfCustomizer.disable()). 
           authorizeHttpRequests((authorize) -> authorize.requestMatchers("/api/v1/auth/login","/api/v1/auth/register").permitAll()
                   .anyRequest().authenticated())
                     .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authenticationProvider(authenticationProvider()).addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                                                                 

       //The main part of Spring Security is implemented in filters, 
       //if you want to authenticate in a web application (regardless the mechanism) you will need to add a filter to the chain.
       //.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); thats why we do this 


       return http.build();
	}

//In Spring, the objects that form the backbone of your application and that
//are managed by the Spring IoC container are called beans. A bean is an object
//that is instantiated, assembled, and otherwise managed by a Spring IoC
//container.
//Inversion of Control (IoC) is a process in which an object defines its dependencies without creating them. 

    CorsConfigurationSource corsConfigurationSource() { 

        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET","POST"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
   @Bean
     public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }


    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {

        return config.getAuthenticationManager();

    }


//
//	private RSAPublicKey publicKey() {
//		// ...
//	}
//	//@Bean
	//public PasswordEncoder passwordEncoder() {
	//	return new BCryptPasswordEncoder();
	//}

	//@Bean
	//public UserDetailsService userDetailsService() {
	//	BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
	//	UserDetails user =
	//		 User.builder()
	//			.username("user")
	//			.password(encoder.encode("password"))
	//			.roles("USER")
	//			.build();

	//	return new InMemoryUserDetailsManager(user);
	//}
}

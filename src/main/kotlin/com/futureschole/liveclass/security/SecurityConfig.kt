package com.futureschole.liveclass.security

import com.futureschole.liveclass.security.filter.AuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
class SecurityConfig(
    private val authenticationFilter: AuthenticationFilter
) {

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun filterChain(httpSecurity: HttpSecurity): SecurityFilterChain {
        return httpSecurity
            .formLogin{ it.disable() }
            .csrf { it.disable() }
            .logout { it.disable() }
            .addFilterBefore(authenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .authorizeHttpRequests { requests ->
                requests.requestMatchers(HttpMethod.GET, "/api/sale-record").hasAnyRole("ADMIN", "CREATOR")
                requests.requestMatchers(HttpMethod.POST, "/api/sale-record").authenticated()
                requests.requestMatchers(HttpMethod.POST, "/api/cancel-record").authenticated()
                requests.requestMatchers(HttpMethod.GET, "/api/settlement/summary").hasRole("ADMIN")
                requests.requestMatchers(HttpMethod.GET, "/api/settlement").hasAnyRole("ADMIN", "CREATOR")
                requests.requestMatchers(HttpMethod.POST, "/api/settlement").hasRole("ADMIN")

                requests.anyRequest().denyAll()
            }
            .build()
    }
}

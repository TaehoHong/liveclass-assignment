package com.futureschole.liveclass.security.filter

import com.futureschole.liveclass.domain.user.entity.User
import com.futureschole.liveclass.domain.user.service.UserService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class AuthenticationFilter(
    private val userService: UserService,
): OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val userId = request.getHeader("userId")?.toLong()

        if(userId != null) {
            userService.getUserById(userId)
                .apply {
                    SecurityContextHolder.getContext().authentication = getAuthentication(this)
                }
        }


        filterChain.doFilter(request, response)
    }

    private fun getAuthentication(user: User): UsernamePasswordAuthenticationToken {
        return UsernamePasswordAuthenticationToken(user, "", listOf(SimpleGrantedAuthority("ROLE_${user.role}")))
    }
}

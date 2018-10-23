package com.procurement.operation.configuration

import com.procurement.operation.configuration.properties.ActuatorSecurityProperties
import org.springframework.boot.actuate.autoconfigure.security.servlet.EndpointRequest
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.provisioning.InMemoryUserDetailsManager

@Configuration
@EnableConfigurationProperties(
    value = [
        ActuatorSecurityProperties::class
    ]
)
class ActuatorSecurity(private val actuatorSecurityProperties: ActuatorSecurityProperties) :
    WebSecurityConfigurerAdapter() {

    companion object {
        private const val SECURE_ROLE = "ACTUATOR"
    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity) {
        http.requestMatcher(EndpointRequest.toAnyEndpoint())
            .authorizeRequests()
            .anyRequest().hasRole(SECURE_ROLE)
            .and()
            .httpBasic()
    }

    @Bean
    public override fun userDetailsService(): UserDetailsService =
        actuatorUserDetails()?.let {
            InMemoryUserDetailsManager(it)
        } ?: InMemoryUserDetailsManager()

    private fun actuatorUserDetails(): UserDetails? {
        val username = actuatorSecurityProperties.username ?: ""
        val passwordHash = actuatorSecurityProperties.passwordHash ?: ""
        return if (username.isNotBlank() && passwordHash.isNotBlank()) {
            User.withUsername(actuatorSecurityProperties.username)
                .password("${actuatorSecurityProperties.passwordHash}")
                .roles(SECURE_ROLE)
                .build()
        } else
            null
    }
}

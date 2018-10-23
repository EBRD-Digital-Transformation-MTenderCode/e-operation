package com.procurement.operation.configuration.properties

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "management.actuator-security")
class ActuatorSecurityProperties {
    var username: String? = ""
    var passwordHash: String? = ""
}
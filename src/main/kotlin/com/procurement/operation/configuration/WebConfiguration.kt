package com.procurement.operation.configuration

import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = ["com.procurement.operation.controller"])
class WebConfiguration
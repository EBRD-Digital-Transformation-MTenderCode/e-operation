package com.procurement.operation.configuration

import org.springframework.boot.web.servlet.ServletComponentScan
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc

@Configuration
@EnableWebMvc
@ServletComponentScan(basePackages = ["com.procurement.operation.filter"])
@ComponentScan(basePackages = ["com.procurement.operation.controller"])
class WebConfiguration
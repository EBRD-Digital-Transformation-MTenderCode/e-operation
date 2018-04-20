package com.procurement.operation.filter

import com.procurement.operation.logging.MDCKey
import com.procurement.operation.logging.mdc
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter(urlPatterns = ["/*"])
class AuthorizationInfoServletFilter : Filter {
    override fun init(arg0: FilterConfig) {}

    override fun destroy() {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        with(request as HttpServletRequest) {
            val uri = requestURI + (queryString?.let { "?$it" } ?: "")
            mdc(
                MDCKey.REMOTE_ADDRESS to remoteAddr,
                MDCKey.HTTP_METHOD to method,
                MDCKey.REQUEST_URI to uri
            ) { chain.doFilter(request, response) }
        }
    }
}

package com.procurement.operation.filter

import org.slf4j.MDC
import javax.servlet.*
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpServletRequest

@WebFilter(urlPatterns = ["/*"])
class AuthorizationInfoServletFilter : Filter {
    companion object {
        private const val REMOTE_ADDRESS = "remoteAddr"
        private const val HTTP_METHOD = "httpMethod"
        private const val REQUEST_URI = "uri"
    }

    override fun init(arg0: FilterConfig) {}

    override fun destroy() {}

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        with(request as HttpServletRequest) {
            MDC.put(REMOTE_ADDRESS, remoteAddr)
            MDC.put(HTTP_METHOD, method)

            val uri = requestURI + queryString?.let { "?" + it }
            MDC.put(REQUEST_URI, uri)
        }

        try {
            chain.doFilter(request, response)
        } finally {
            MDC.remove(REMOTE_ADDRESS)
            MDC.remove(HTTP_METHOD)
            MDC.remove(REQUEST_URI)
        }
    }
}
package com.hiru.globaltrade.web.filter;

import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
public class SecurityHeadersFilter implements ContainerResponseFilter {
    @Override
    public void filter(jakarta.ws.rs.container.ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().putSingle("X-Content-Type-Options", "nosniff");
        responseContext.getHeaders().putSingle("X-Frame-Options", "DENY");
        responseContext.getHeaders().putSingle("Referrer-Policy", "same-origin");
        responseContext.getHeaders().putSingle("Cache-Control", "no-store");
    }
}

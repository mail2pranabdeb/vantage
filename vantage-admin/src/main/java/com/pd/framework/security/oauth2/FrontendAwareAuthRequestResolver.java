package com.pd.framework.security.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.util.StringUtils;

import java.net.URI;

/**
 * Custom OAuth2 authorization request resolver that embeds the frontend URL
 * into the state parameter so it survives the OAuth2 redirect flow.
 */
public class FrontendAwareAuthRequestResolver implements OAuth2AuthorizationRequestResolver {

    private final OAuth2AuthorizationRequestResolver defaultResolver;

    public FrontendAwareAuthRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.defaultResolver = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request);
        return customizeState(request, authRequest);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest authRequest = defaultResolver.resolve(request, clientRegistrationId);
        return customizeState(request, authRequest);
    }

    private OAuth2AuthorizationRequest customizeState(HttpServletRequest request, OAuth2AuthorizationRequest authRequest) {
        if (authRequest == null) {
            return null;
        }

        String referer = request.getHeader("Referer");
        String frontendUrl = null;
        if (StringUtils.hasText(referer)) {
            try {
                URI uri = URI.create(referer);
                frontendUrl = uri.getScheme() + "://" + uri.getHost() + (uri.getPort() != -1 ? ":" + uri.getPort() : "");
            } catch (Exception ignored) {
            }
        }

        String originalState = authRequest.getState();
        String newState = (frontendUrl != null ? frontendUrl : "http://localhost:5173") + "|" + originalState;
        
        return OAuth2AuthorizationRequest.from(authRequest)
                .state(newState)
                .build();
    }
}

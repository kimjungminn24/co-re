package com.core.backend.service;

import com.core.backend.data.dto.users.UserGroupsDto;
import com.core.backend.data.dto.users.UserInfoDto;
import com.core.backend.data.entity.JiraOAuthToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class JiraService {

    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.jira.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.jira.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.jira.redirect-uri}")
    private String redirectUri;

    public JiraOAuthToken exchangeAuthorizationCode(String authorizationCode) {
        try {
            Map<String, String> requestBody = Map.of(
                    "grant_type", "authorization_code",
                    "client_id", clientId,
                    "client_secret", clientSecret,
                    "code", authorizationCode,
                    "redirect_uri", redirectUri
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

            Map<String, Object> response = restTemplate.exchange(
                    "https://auth.atlassian.com/oauth/token",
                    org.springframework.http.HttpMethod.POST,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            assert response != null;
            String accessToken = (String) response.get("access_token");
            String refreshToken = (String) response.get("refresh_token");
            log.info("Access Token: {}", accessToken);
            log.info("Refresh Token: {}", refreshToken);

            return new JiraOAuthToken(null, accessToken, refreshToken);
        } catch (Exception e) {
            log.info("exchangeAuthorizationCode Error : {}", e.getMessage());
        }
        return null;
    }

    public List<UserGroupsDto> getGroups(String accessToken) {
        try {
            HttpEntity<String> request = new HttpEntity<>(createHeadersWithAuthorization(accessToken));

            List<Map<String, Object>> response = restTemplate.exchange(
                    "https://api.atlassian.com/oauth/token/accessible-resources",
                    org.springframework.http.HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    }).getBody();
            assert response != null;
            return convertToUserGroupsDto(response);
        } catch (Exception ex) {
            log.info("getGroups Error : {}", ex.getMessage());
        }
        return new ArrayList<>();
    }

    public UserInfoDto getUserInfo(String accessToken) {

        try {
            HttpEntity<String> request = new HttpEntity<>(createHeadersWithAuthorization(accessToken));

            Map<String, Object> response = restTemplate.exchange(
                    "https://api.atlassian.com/me",
                    org.springframework.http.HttpMethod.GET,
                    request,
                    new ParameterizedTypeReference<Map<String, Object>>() {
                    }).getBody();

            assert response != null;
            return convertToUserInfoDto(response);
        } catch (Exception ex) {
            log.info("getUserInfo Error : {}", ex.getMessage());
        }
        return null;
    }

    private UserInfoDto convertToUserInfoDto(Map<String, Object> userInfo) {
        try {
            return new UserInfoDto(
                    (String) userInfo.get("account_id"),
                    (String) userInfo.get("email"),
                    (String) userInfo.get("name"),
                    (String) userInfo.get("picture"),
                    (String) userInfo.get("nickname")
            );
        } catch (Exception ex) {
            log.info("convertToUserInfoDto Error : {}", ex.getMessage());
        }
        return null;
    }

    private HttpHeaders createHeadersWithAuthorization(String accessToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
            return headers;
        } catch (Exception ex) {
            log.info("createHeadersWithAuthorization Error : {}", ex.getMessage());
        }
        return new HttpHeaders();
    }

    private ArrayList<UserGroupsDto> convertToUserGroupsDto(List<Map<String, Object>> groups) {
        try {
            ArrayList<UserGroupsDto> userGroups = new ArrayList<>();

            for (Map<String, Object> group : groups) {
                String groupId = (String) group.get("id");
                String groupName = (String) group.get("name");
                String groupUrl = (String) group.get("url");
                String groupAvatarUrl = (String) group.get("avatarUrl");

                UserGroupsDto groupDto = new UserGroupsDto(
                        groupId,
                        groupName,
                        groupUrl,
                        groupAvatarUrl
                );
                userGroups.add(groupDto);
            }
            return userGroups;
        } catch (Exception ex) {
            log.info("convertToUserGroupsDto Error : {}", ex.getMessage());
        }
        return new ArrayList<>();
    }
}

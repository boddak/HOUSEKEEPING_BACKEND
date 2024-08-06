package com.housekeeping.service.oauth2;

import com.housekeeping.DTO.UserDTO;
import com.housekeeping.DTO.oauth2.*;
import com.housekeeping.entity.enums.UserPlatform;
import com.housekeeping.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private static final Logger logger = LoggerFactory.getLogger(CustomOAuth2UserService.class);

    private final UserRepository userRepository;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String clientName = userRequest.getClientRegistration().getClientName();

        Map<String, Object> attributes = oAuth2User.getAttributes();
        logger.debug("OAuth2 attributes: {}", attributes);

        OAuth2Response response = null;
        if (clientName.equalsIgnoreCase("naver")) {
            response = new NaverResponse(attributes);
        } else if (clientName.equalsIgnoreCase("google")) {
            response = new GoogleResponse(attributes);
        } else if (clientName.equalsIgnoreCase("kakao")) {
            response = new KakaoResponse(attributes);
        } else {
            throw new OAuth2AuthenticationException("Unsupported OAuth2 provider");
        }

        String providerId = response.getProviderId();
        String provider = response.getProvider();
        String tempNickname = provider + "_" + providerId;

        // 사용자가 이미 존재하는지 확인
        boolean isNewUser = !userRepository.existsByNickname(tempNickname);

        UserDTO oAuth2UserDto = UserDTO.builder()
                .username(tempNickname)
                .name(response.getName())
                .email(response.getEmail())
                .phoneNumber(response.getPhoneNumber())
                .userPlatform(UserPlatform.valueOf(provider.toUpperCase()))
                .isNewUser(isNewUser)
                .build();

        return new CustomOAuth2User(oAuth2UserDto);
    }
}
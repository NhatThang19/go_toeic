package com.vn.go_toeic.config.security.oauth2;

import com.vn.go_toeic.config.security.CustomUserDetails;
import com.vn.go_toeic.model.Role;
import com.vn.go_toeic.model.SocialAccount;
import com.vn.go_toeic.model.User;
import com.vn.go_toeic.repository.RoleRepository;
import com.vn.go_toeic.repository.UserRepository;
import com.vn.go_toeic.util.abs.OAuth2UserInfo;
import com.vn.go_toeic.util.enums.RoleEnum;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(registrationId, oauth2User.getAttributes());

        if (oAuth2UserInfo.getEmail() == null || oAuth2UserInfo.getEmail().isEmpty()) {
            throw new OAuth2AuthenticationException("Email not found from OAuth2 provider");
        }


        User userEntity = processOAuth2User(registrationId, oAuth2UserInfo);

        CustomUserDetails userDetails = new CustomUserDetails(userEntity);

        userDetails.setAttributes(oauth2User.getAttributes());

        return userDetails;
    }

    private User processOAuth2User(String registrationId, OAuth2UserInfo oAuth2UserInfo) {
        Optional<User> userOptional = userRepository.findByEmail(oAuth2UserInfo.getEmail());
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            user.setAvatarUrl(oAuth2UserInfo.getImageUrl());
            linkSocialAccount(registrationId, oAuth2UserInfo.getId(), user);
        } else {
            user = createNewUser(oAuth2UserInfo);
            linkSocialAccount(registrationId, oAuth2UserInfo.getId(), user);
        }
        return userRepository.save(user);
    }

    private User createNewUser(OAuth2UserInfo oAuth2UserInfo) {
        Role defaultRole = roleRepository.findByName(RoleEnum.USER)
                .orElseThrow(() -> new IllegalStateException("Default role ROLE_USER not found in the database."));

        return User.builder()
                .email(oAuth2UserInfo.getEmail())
                .fullName(oAuth2UserInfo.getName())
                .avatarUrl(oAuth2UserInfo.getImageUrl())
                .verified(true)
                .roles(Collections.singleton(defaultRole))
                .build();
    }

    private void linkSocialAccount(String provider, String providerUserId, User user) {
        boolean alreadyLinked = user.getSocialAccounts().stream()
                .anyMatch(sa -> sa.getProvider().equalsIgnoreCase(provider) &&
                        sa.getProviderUserId().equals(providerUserId));

        if (!alreadyLinked) {
            SocialAccount socialAccount = SocialAccount.builder()
                    .user(user)
                    .provider(provider)
                    .providerUserId(providerUserId)
                    .build();
            user.getSocialAccounts().add(socialAccount);
        }
    }
}

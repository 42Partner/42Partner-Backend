package partner42.moduleapi.service.user;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import partner42.moduleapi.dto.user.CustomAuthenticationPrincipal;
import partner42.modulecommon.domain.model.member.Member;
import partner42.modulecommon.domain.model.tryjudge.MatchTryAvailabilityJudge;
import partner42.modulecommon.domain.model.user.Role;
import partner42.modulecommon.domain.model.user.RoleEnum;
import partner42.modulecommon.domain.model.user.User;
import partner42.modulecommon.domain.model.user.UserRole;
import partner42.modulecommon.repository.member.MemberRepository;
import partner42.modulecommon.repository.user.RoleRepository;
import partner42.modulecommon.repository.user.UserRepository;
import partner42.modulecommon.repository.user.UserRoleRepository;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final MemberRepository memberRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        Map<String, Object> attributes = super.loadUser(userRequest).getAttributes();
        //resource Server??? ?????? ????????? ????????? ????????? ?????? ??????.
        String apiId = ((Integer)attributes.get("id")).toString();
        //takim
        String login = (String)attributes.get("login");
        //takim@student.42seoul.kr
        String email = (String) attributes.get("email");

        String imageUrl = "";
        //https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim.jpg
        if (attributes.get("image") instanceof Map) {
            imageUrl = (String)((Map)(attributes.get("image"))).get("link") == null ?
                "" : (String)((Map)(attributes.get("image"))).get("link");
        }

        HashMap<String, Object> necessaryAttributes = createNecessaryAttributes(apiId, login,
            email, imageUrl);

        String username = email;
        Optional<User> userOptional = userRepository.findByUsername(username);
        OAuth2User oAuth2User = signUpOrUpdateUser(login, email, imageUrl, username, userOptional, necessaryAttributes);
        return oAuth2User;
    }

    private HashMap<String, Object> createNecessaryAttributes(String apiId, String login, String email, String imageUrl) {
        HashMap<String, Object> necessaryAttributes = new HashMap<>();
        necessaryAttributes.put("id", apiId);
        necessaryAttributes.put("login", login);
        necessaryAttributes.put("email", email);
        necessaryAttributes.put("image_url", imageUrl);
        return necessaryAttributes;
    }


    private OAuth2User signUpOrUpdateUser(String login, String email, String imageUrl, String username,
        Optional<User> userOptional, Map<String, Object> necessaryAttributes) {
        OAuth2User oAuth2User;
        User user;
        //????????????, ?????? ???????????? ?????? ?????? ????????? ????????? ??????.
        if (userOptional.isEmpty()) {
            //????????? ????????? ?????? ?????? ??? ??????

            String encodedPassword = passwordEncoder.encode(UUID.randomUUID().toString());

            MatchTryAvailabilityJudge matchTryAvailabilityJudge = MatchTryAvailabilityJudge.of();
            Member member = Member.of(login, matchTryAvailabilityJudge);
            memberRepository.save(member);

            Role role = roleRepository.findByValue(RoleEnum.ROLE_USER).orElseThrow(() ->
                new EntityNotFoundException(RoleEnum.ROLE_USER + "??? ???????????? Role??? ????????????."));
            user = User.of(username, encodedPassword, email, login, imageUrl, member);
            UserRole userRole = UserRole.of(role, user);

            userRepository.save(user);
            userRoleRepository.save(userRole);
            necessaryAttributes.put("create_flag", true);
            //??????????????? ?????? ????????? ??? ?????? ??? ??????.
        } else{
            //???????????? ??????
            user = userOptional.get();
            // ?????? ????????? ??? oauth2 ?????? ???????????? ??????????????????.
//            user.updateUserByOAuthIfo(imageUrl);
            necessaryAttributes.put("create_flag", false);
        }
        oAuth2User = CustomAuthenticationPrincipal.of(user, necessaryAttributes);
        return oAuth2User;
    }

}

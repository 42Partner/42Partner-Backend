package partner42.moduleapi.utils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import javax.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import partner42.modulecommon.domain.model.match.ConditionCategory;
import partner42.modulecommon.domain.model.matchcondition.MatchCondition;
import partner42.modulecommon.domain.model.matchcondition.Place;
import partner42.modulecommon.domain.model.matchcondition.TimeOfEating;
import partner42.modulecommon.domain.model.matchcondition.TypeOfStudy;
import partner42.modulecommon.domain.model.matchcondition.WayOfEating;
import partner42.modulecommon.domain.model.member.Member;
import partner42.modulecommon.domain.model.tryjudge.MatchTryAvailabilityJudge;
import partner42.modulecommon.domain.model.user.Authority;
import partner42.modulecommon.domain.model.user.Role;
import partner42.modulecommon.domain.model.user.RoleEnum;
import partner42.modulecommon.domain.model.user.User;
import partner42.modulecommon.domain.model.user.UserRole;
import partner42.modulecommon.repository.matchcondition.MatchConditionRepository;
import partner42.modulecommon.repository.member.MemberRepository;
import partner42.modulecommon.repository.user.AuthorityRepository;
import partner42.modulecommon.repository.user.RoleRepository;
import partner42.modulecommon.repository.user.UserRepository;
import partner42.modulecommon.repository.user.UserRoleRepository;

@RequiredArgsConstructor
@Component
public class CreateTestDataUtils {



    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final UserRoleRepository userRoleRepository;
    private final AuthorityRepository authorityRepository;

    private final RoleRepository roleRepository;

    private final MatchConditionRepository matchConditionRepository;

    public void signUpUsers() {
        createRoleAuthority();
        Map<String, Object> sorkim = new HashMap<>();
        sorkim.put("id", 3);
        sorkim.put("login", "sorkim");
        sorkim.put("email", "sorkim@student.42seoul.kr");
        sorkim.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/sorkim.jpg");

        Map<String, Object> hyenam = new HashMap<>();
        hyenam.put("id", 4);
        hyenam.put("login", "hyenam");
        hyenam.put("email", "hyenam@student.42seoul.kr");
        hyenam.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/hyenam.jpg");

        Map<String, Object> takim = new HashMap<>();
        takim.put("id", 4);
        takim.put("login", "takim");
        takim.put("email", "takim@student.42seoul.kr");
        takim.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim.jpg");

        Map<String, Object> takim1 = new HashMap<>();
        takim1.put("id", 4);
        takim1.put("login", "takim1");
        takim1.put("email", "takim1@student.42seoul.kr");
        takim1.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim1.jpg");

        Map<String, Object> takim2 = new HashMap<>();
        takim2.put("id", 4);
        takim2.put("login", "takim2");
        takim2.put("email", "takim2@student.42seoul.kr");
        takim2.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim2.jpg");

        Map<String, Object> takim3 = new HashMap<>();
        takim3.put("id", 4);
        takim3.put("login", "takim3");
        takim3.put("email", "takim3@student.42seoul.kr");
        takim3.put("image_url",
            "https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim3.jpg");

        loadUser(sorkim);
        loadUser(hyenam);
        loadUser(takim);
        loadUser(takim1);
        loadUser(takim2);
        loadUser(takim3);


    }



    public void createRoleAuthority() {
        //Authority ??????
        /**
         * ?????? ????????? ????????? ??????.
         * 1. ??????????????? create, update, read, delete??? ?????????.
         * 2. ?????? api??? ???????????? ????????????.
         * 3. ?????? ?????? opinion.delete??? ?????? opinion??? ????????? ??? ????????? ????????????.
         * 4. ??????, ?????? opinion??? ????????? ??? ????????? ????????? ????????? service?????? ????????????.
         */

        Authority createUser = saveNewAuthority("user.create");
        Authority updateUser = saveNewAuthority("user.update");
        Authority readUser = saveNewAuthority("user.read");
        Authority deleteUser = saveNewAuthority("user.delete");

        System.out.println("--------------------------------------------------------");
        Authority createOpinion = saveNewAuthority("opinion.create");
        Authority updateOpinion = saveNewAuthority("opinion.update");
        Authority readOpinion = saveNewAuthority("opinion.read");
        Authority deleteOpinion = saveNewAuthority("opinion.delete");
        System.out.println("--------------------------------------------------------");

        Authority createArticle = saveNewAuthority("article.create");
        Authority updateArticle = saveNewAuthority("article.update");
        Authority readArticle = saveNewAuthority("article.read");
        Authority deleteArticle = saveNewAuthority("article.delete");
        System.out.println("--------------------------------------------------------");

        Authority createMatch = saveNewAuthority("match.create");
        Authority updateMatch = saveNewAuthority("match.update");
        Authority readMatch = saveNewAuthority("match.read");
        Authority deleteMatch = saveNewAuthority("match.delete");
        System.out.println("--------------------------------------------------------");

        Authority createActivity = saveNewAuthority("activity.create");
        Authority updateActivity = saveNewAuthority("activity.update");
        Authority readActivity = saveNewAuthority("activity.read");
        Authority deleteActivity = saveNewAuthority("activity.delete");

        Authority createRandomMatch = saveNewAuthority("random-match.create");
        Authority updateRandomMatch = saveNewAuthority("random-match.update");
        Authority readRandomMatch = saveNewAuthority("random-match.read");
        Authority deleteRandomMatch = saveNewAuthority("random-match.delete");



        //Role ??????
        Role adminRole = saveNewRole(RoleEnum.ROLE_ADMIN);
        Role userRole = saveNewRole(RoleEnum.ROLE_USER);

//        Role adminRole = roleRepository.findByValue(RoleEnum.ROLE_ADMIN).get();
//        Role userRole = roleRepository.findByValue(RoleEnum.ROLE_USER).get();

        adminRole.getAuthorities().clear();
        adminRole.addAuthorities(createUser, updateUser, readUser, deleteUser,
            createOpinion, updateOpinion, readOpinion, deleteOpinion,
            createArticle, updateArticle, readArticle, deleteArticle,
            createMatch, updateMatch, readMatch, deleteMatch,
            createActivity, updateActivity, readActivity, deleteActivity,
            createRandomMatch, updateRandomMatch, readRandomMatch, deleteRandomMatch);


        userRole.getAuthorities().clear();
        userRole.addAuthorities(createUser, updateUser, readUser, deleteUser,
            createOpinion, updateOpinion, readOpinion, deleteOpinion,
            createArticle, updateArticle, readArticle, deleteArticle,
            createMatch, updateMatch, readMatch, deleteMatch,
            createActivity, updateActivity, readActivity, deleteActivity,
            createRandomMatch, updateRandomMatch, readRandomMatch, deleteRandomMatch);

        roleRepository.saveAll(Arrays.asList(adminRole, userRole));

    }

    public void createMatchCondition(){
        matchConditionRepository.save(
            MatchCondition.of(WayOfEating.DELIVERY.name(), ConditionCategory.WayOfEating));
        matchConditionRepository.save(MatchCondition.of(WayOfEating.EATOUT.name(), ConditionCategory.WayOfEating));
        matchConditionRepository.save(MatchCondition.of(WayOfEating.TAKEOUT.name(), ConditionCategory.WayOfEating));

        matchConditionRepository.save(MatchCondition.of(Place.SEOCHO.name(), ConditionCategory.Place));
        matchConditionRepository.save(MatchCondition.of(Place.GAEPO.name(), ConditionCategory.Place));
        matchConditionRepository.save(MatchCondition.of(Place.OUT_OF_CLUSTER.name(), ConditionCategory.Place));

        matchConditionRepository.save(MatchCondition.of(TimeOfEating.BREAKFAST.name(), ConditionCategory.TimeOfEating));
        matchConditionRepository.save(MatchCondition.of(TimeOfEating.LUNCH.name(), ConditionCategory.TimeOfEating));
        matchConditionRepository.save(MatchCondition.of(TimeOfEating.DUNCH.name(), ConditionCategory.TimeOfEating));
        matchConditionRepository.save(MatchCondition.of(TimeOfEating.DINNER.name(), ConditionCategory.TimeOfEating));
        matchConditionRepository.save(MatchCondition.of(TimeOfEating.MIDNIGHT.name(), ConditionCategory.TimeOfEating));

//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.KOREAN.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.JAPANESE.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.CHINESE.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.WESTERN.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.ASIAN.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.EXOTIC.name(), ConditionCategory.TypeOfEating));
//        matchConditionRepository.save(MatchCondition.of(TypeOfEating.CONVENIENCE.name(), ConditionCategory.TypeOfEating));

        matchConditionRepository.save(MatchCondition.of(TypeOfStudy.INNER_CIRCLE.name(), ConditionCategory.TypeOfStudy));
        matchConditionRepository.save(MatchCondition.of(TypeOfStudy.NOT_INNER_CIRCLE.name(), ConditionCategory.TypeOfStudy));

    }

    private void loadUser(Map<String, Object> attributes) {
        String apiId = ((Integer) attributes.get("id")).toString();
        //takim
        String login = (String) attributes.get("login");
        //takim@student.42seoul.kr
        String email = (String) attributes.get("email");
        //https://cdn.intra.42.fr/users/0f260cc3e59777f0f5ba926f19cc1ec9/takim.jpg
        String imageUrl = (String) attributes.get("image_url");

        HashMap<String, Object> necessaryAttributes = createNecessaryAttributes(apiId, login,
            email, imageUrl);

        String username = email;
        Optional<User> userOptional = userRepository.findByUsername(username);
        User oAuth2User = signUpOrUpdateUser(login, email, imageUrl, username, userOptional,
            necessaryAttributes);
    }

    private Role saveNewRole(RoleEnum roleEnum) {
        Role role = Role.of(roleEnum);
        return roleRepository.save(role);
    }

    private Authority saveNewAuthority(String s) {
        return authorityRepository.save(
            Authority.of(s));
    }

    private HashMap<String, Object> createNecessaryAttributes(String apiId, String login,
        String email, String imageUrl) {
        HashMap<String, Object> necessaryAttributes = new HashMap<>();
        necessaryAttributes.put("id", apiId);
        necessaryAttributes.put("login", login);
        necessaryAttributes.put("email", email);
        necessaryAttributes.put("image_url", imageUrl);
        return necessaryAttributes;
    }


    private User signUpOrUpdateUser(String login, String email, String imageUrl, String username,
        Optional<User> userOptional, Map<String, Object> necessaryAttributes) {
        User user;
        //????????????
        if (userOptional.isEmpty()) {
            //????????? ????????? ?????? ?????? ??? ??????


            MatchTryAvailabilityJudge matchTryAvailabilityJudge = MatchTryAvailabilityJudge.of();
            Member member = Member.of(login, matchTryAvailabilityJudge);
            memberRepository.save(member);

            Role role = roleRepository.findByValue(RoleEnum.ROLE_USER).orElseThrow(() ->
                new EntityNotFoundException(RoleEnum.ROLE_USER + "??? ???????????? Role??? ????????????."));
            user = User.of(username, UUID.randomUUID().toString(), email, login, imageUrl, member);
            UserRole userRole = UserRole.of(role, user);

            userRepository.save(user);
            userRoleRepository.save(userRole);
            necessaryAttributes.put("create_flag", true);
            //??????????????? ?????? ????????? ??? ?????? ??? ??????.
        } else {
            //???????????? ??????
            user = userOptional.get();
            // ?????? ????????? ??? oauth2 ?????? ???????????? ??????????????????.
//            user.updateUserByOAuthIfo(imageUrl);
            necessaryAttributes.put("create_flag", false);
        }
        return user;
    }
}

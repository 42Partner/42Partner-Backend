package partner42.moduleapi.service.opinion;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import partner42.moduleapi.dto.ListResponse;
import partner42.moduleapi.dto.opinion.OpinionDto;
import partner42.moduleapi.dto.opinion.OpinionOnlyIdResponse;
import partner42.moduleapi.dto.opinion.OpinionResponse;
import partner42.moduleapi.dto.opinion.OpinionUpdateRequest;
import partner42.moduleapi.mapper.OpinionMapper;
import partner42.modulecommon.domain.model.article.Article;
import partner42.modulecommon.domain.model.opinion.Opinion;
import partner42.modulecommon.domain.model.user.Role;
import partner42.modulecommon.domain.model.user.RoleEnum;
import partner42.modulecommon.domain.model.user.User;
import partner42.modulecommon.domain.model.user.UserRole;
import partner42.modulecommon.exception.ErrorCode;
import partner42.modulecommon.exception.NoEntityException;
import partner42.modulecommon.exception.NotAuthorException;
import partner42.modulecommon.repository.article.ArticleRepository;
import partner42.modulecommon.repository.member.MemberRepository;
import partner42.modulecommon.repository.opinion.OpinionRepository;
import partner42.modulecommon.repository.user.UserRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class OpinionService {

    private final OpinionRepository opinionRepository;
    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final ArticleRepository articleRepository;

    private final OpinionMapper opinionMapper;

    @Transactional
    public OpinionOnlyIdResponse createOpinion(OpinionDto request, String username) {
        Article article = articleRepository.findByApiIdAndIsDeletedIsFalse(request.getArticleId())
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        Opinion opinion = Opinion.of(request.getContent(),
            userRepository.findByUsername(username)
                .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND))
                .getMember(),
            article,
            request.getParentId(),
            request.getLevel()
        );
        opinionRepository.save(opinion);
        return opinionMapper.entityToOpinionOnlyIdResponse(opinion);
    }

    @Transactional
    public OpinionOnlyIdResponse updateOpinion(OpinionUpdateRequest request, String opinionId, String username) {
        verifyAuthorOfOpinion(username, opinionId);

        Opinion opinion = opinionRepository.findByApiId(opinionId)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        opinion.updateContent(request.getContent());
        return opinionMapper.entityToOpinionOnlyIdResponse(opinion);
    }



    public ListResponse<OpinionResponse> findAllOpinionsInArticle(String articleId) {
        List<OpinionResponse> opinionResponses = opinionRepository.findAllByArticleApiIdAndIsDeletedIsFalse(articleId).stream()
            .map(opinionMapper::entityToOpinionResponse)
            .collect(Collectors.toList());

        return ListResponse.<OpinionResponse>builder()
            .values(opinionResponses)
            .valueCount(opinionResponses.size())
            .build();
    }

    @Transactional
    public OpinionOnlyIdResponse recoverableDeleteOpinion(String opinionId, String username) {
        verifyAuthorOfOpinion(username, opinionId);
        Opinion opinion = opinionRepository.findByApiId(opinionId)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        opinion.delete();
        return opinionMapper.entityToOpinionOnlyIdResponse(opinion);
    }

    @Transactional
    public OpinionOnlyIdResponse completeDeleteOpinion(String opinionId, String username) {
        verifyAuthorOfOpinion(username, opinionId);
        Opinion opinion = opinionRepository.findByApiId(opinionId)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        opinionRepository.delete(opinion);
        return opinionMapper.entityToOpinionOnlyIdResponse(opinion);
    }

    public OpinionResponse getOneOpinion(String opinionId) {

        Opinion opinion = opinionRepository.findByApiId(opinionId)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        return opinionMapper.entityToOpinionResponse(opinion);
    }

    private void verifyAuthorOfOpinion(String username, String opinionId) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        Opinion opinion = opinionRepository.findByApiId(opinionId)
            .orElseThrow(() -> new NoEntityException(ErrorCode.ENTITY_NOT_FOUND));
        if (!user.getUserRoles().stream()
            .map(UserRole::getRole)
            .map(Role::getValue)
            .collect(Collectors.toSet())
            .contains(RoleEnum.ROLE_ADMIN) &&
            !opinion.getMemberAuthor().equals(user.getMember())){
            throw new NotAuthorException(ErrorCode.NOT_OPINION_AUTHOR);
        }
    }
}

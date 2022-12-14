package partner42.modulecommon.repository.opinion;

import partner42.modulecommon.domain.model.opinion.Opinion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OpinionRepository extends JpaRepository<Opinion, Long> {

    Optional<Opinion> findByApiId(String apiId);

    @EntityGraph(attributePaths = {"article", "memberAuthor"})
    @Query("select o from Opinion o where o.article.apiId LIKE :articleApiId and o.isDeleted = false")
    List<Opinion> findAllByArticleApiIdAndIsDeletedIsFalse(@Param("articleApiId") String articleApiId);
}

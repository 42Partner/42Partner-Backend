package com.seoul.openproject.partner.domain.repository;

import com.seoul.openproject.partner.domain.ArticleMatchCondition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleMatchConditionRepository extends
    JpaRepository<ArticleMatchCondition, Long> {

}

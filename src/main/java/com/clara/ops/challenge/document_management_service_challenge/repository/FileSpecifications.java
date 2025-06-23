package com.clara.ops.challenge.document_management_service_challenge.repository;

import com.clara.ops.challenge.document_management_service_challenge.entities.FileEntity;
import com.clara.ops.challenge.document_management_service_challenge.entities.TagEntity;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class FileSpecifications {

  public static Specification<FileEntity> withFilters(
      final String userName, final String fileName, final List<String> tags) {

    return new Specification<>() {
      @Override
      public Predicate toPredicate(
          Root<FileEntity> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        if (userName != null && !userName.isEmpty()) {
          predicates.add(
              cb.like(cb.lower(root.get("userName")), "%" + userName.toLowerCase() + "%"));
        }

        if (fileName != null && !fileName.isEmpty()) {
          predicates.add(
              cb.like(cb.lower(root.get("fileName")), "%" + fileName.toLowerCase() + "%"));
        }

        if (tags != null && !tags.isEmpty()) {
          Join<FileEntity, TagEntity> tagJoin = root.join("tags", JoinType.LEFT);
          predicates.add(tagJoin.get("name").in(tags));
          query.distinct(true);
        }

        return cb.and(predicates.toArray(new Predicate[0]));
      }
    };
  }
}

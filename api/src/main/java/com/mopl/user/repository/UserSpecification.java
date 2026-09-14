package com.mopl.user.repository;

import com.mopl.common.exception.MoplException;
import com.mopl.common.exception.user.UserErrorCode;
import com.mopl.core.common.enums.UserRole;
import com.mopl.core.domain.user.entity.User;
import com.mopl.user.dto.UserSearchCondition.SortDirection;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "email", "createdAt");

    private UserSpecification() {
    }

    public static String validateSortField(String sortBy) {
        if (sortBy == null || !SORTABLE_FIELDS.contains(sortBy)) {
            throw new MoplException(UserErrorCode.INVALID_SORT_FIELD, "지원하지 않는 정렬 기준입니다. sortBy=" + sortBy);
        }
        return sortBy;
    }

    public static Specification<User> notDeleted() {
        return (root, query, cb) -> cb.isNull(root.get("deletedAt"));
    }

    public static Specification<User> emailLike(String emailLike) {
        if (emailLike == null || emailLike.isBlank()) {
            return null;
        }
        return (root, query, cb) -> cb.like(root.get("email"), "%" + emailLike + "%");
    }

    public static Specification<User> roleEqual(UserRole role) {
        if (role == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("role"), role);
    }

    public static Specification<User> isLocked(Boolean locked) {
        if (locked == null) {
            return null;
        }
        return (root, query, cb) -> cb.equal(root.get("locked"), locked);
    }

    public static Specification<User> cursorAfter(
            String sortBy, SortDirection direction, String cursor, UUID idAfter
    ) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        boolean ascending = direction == SortDirection.ASCENDING;

        return (root, query, cb) -> {
            Object cursorValue = parseCursorValue(sortBy, cursor);
            Predicate strictAfter = compare(cb, root.get(sortBy), cursorValue, ascending);

            if (idAfter == null) {
                return strictAfter;
            }

            Predicate sameValueTieBreak = cb.and(
                    cb.equal(root.get(sortBy), cursorValue),
                    cb.greaterThan(root.get("id"), idAfter)
            );
            return cb.or(strictAfter, sameValueTieBreak);
        };
    }

    @SuppressWarnings("unchecked")
    private static <Y extends Comparable<? super Y>> Predicate compare(
            CriteriaBuilder cb, Path<?> path, Object value, boolean ascending
    ) {
        Path<Y> typedPath = (Path<Y>) path;
        Y typedValue = (Y) value;
        return ascending ? cb.greaterThan(typedPath, typedValue) : cb.lessThan(typedPath, typedValue);
    }

    private static Comparable<?> parseCursorValue(String sortBy, String cursor) {
        return "createdAt".equals(sortBy) ? LocalDateTime.parse(cursor) : cursor;
    }
}
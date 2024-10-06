package com.marceldev.ourcompanylunch.repository.comment;

import static com.marceldev.ourcompanylunch.entity.QComment.comment;
import static com.marceldev.ourcompanylunch.entity.QMember.member;

import com.marceldev.ourcompanylunch.dto.comment.GetCommentListRequest;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.type.CommentSort;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import com.marceldev.ourcompanylunch.type.SortDirection;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class CommentRepositoryImpl implements CommentRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Comment> getList(GetCommentListRequest dto, long myMemberId, long dinerId,
      Pageable pageable) {
    long total = Optional.ofNullable(
            queryFactory
                .select(comment.count())
                .from(comment)
                .where(
                    dinerEq(dinerId),
                    myComments(myMemberId).or(companyShared()),
                    contentContains(dto),
                    commentedByEq(dto)
                )
                .fetchOne())
        .orElse(0L);

    List<Comment> content = queryFactory
        .select(comment)
        .from(comment)
        .leftJoin(comment.member, member).fetchJoin()
        .where(
            dinerEq(dinerId),
            myComments(myMemberId).or(companyShared()),
            contentContains(dto),
            commentedByEq(dto)
        )
        .orderBy(getOrder(dto.getSortBy(), dto.getSortDirection()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression dinerEq(long dinerId) {
    return comment.diner.id.eq(dinerId);
  }

  private BooleanExpression myComments(long myMemberId) {
    return comment.member.id.eq(myMemberId);
  }

  private BooleanExpression companyShared() {
    return comment.shareStatus.eq(ShareStatus.COMPANY);
  }

  private BooleanExpression contentContains(GetCommentListRequest dto) {
    return dto.getKeyword() != null ? comment.content.contains(dto.getKeyword()) : null;
  }

  private BooleanExpression commentedByEq(GetCommentListRequest dto) {
    return dto.getCommentedBy() != null ? comment.member.name.eq(dto.getCommentedBy()) : null;
  }

  private OrderSpecifier<?> getOrder(CommentSort sort, SortDirection direction) {
    Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;

    return switch (sort) {
      case CREATED_AT -> new OrderSpecifier<>(order, comment.createdAt);
    };
  }
}

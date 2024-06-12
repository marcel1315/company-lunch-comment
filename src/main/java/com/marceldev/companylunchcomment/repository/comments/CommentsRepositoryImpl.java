package com.marceldev.companylunchcomment.repository.comments;

import static com.marceldev.companylunchcomment.entity.QComments.comments;
import static com.marceldev.companylunchcomment.entity.QMember.member;

import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.type.CommentsSort;
import com.marceldev.companylunchcomment.type.ShareStatus;
import com.marceldev.companylunchcomment.type.SortDirection;
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
public class CommentsRepositoryImpl implements CommentsRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<Comments> getList(GetCommentsListDto dto, long myMemberId, long dinerId,
      Pageable pageable) {
    long total = Optional.ofNullable(
            queryFactory
                .select(comments.count())
                .from(comments)
                .where(
                    dinerEq(dinerId),
                    myComments(myMemberId).or(companyShared()),
                    contentContains(dto),
                    commentedByEq(dto)
                )
                .fetchOne())
        .orElse(0L);

    List<Comments> content = queryFactory
        .select(comments)
        .from(comments)
        .leftJoin(comments.member, member).fetchJoin()
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
    return comments.diner.id.eq(dinerId);
  }

  private BooleanExpression myComments(long myMemberId) {
    return comments.member.id.eq(myMemberId);
  }

  private BooleanExpression companyShared() {
    return comments.shareStatus.eq(ShareStatus.COMPANY);
  }

  private BooleanExpression contentContains(GetCommentsListDto dto) {
    return dto.getKeyword() != null ? comments.content.contains(dto.getKeyword()) : null;
  }

  private BooleanExpression commentedByEq(GetCommentsListDto dto) {
    return dto.getCommentedBy() != null ? comments.member.name.eq(dto.getCommentedBy()) : null;
  }

  private OrderSpecifier<?> getOrder(CommentsSort sort, SortDirection direction) {
    Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;

    return switch (sort) {
      case CREATED_AT -> new OrderSpecifier<>(order, comments.createdAt);
    };
  }
}

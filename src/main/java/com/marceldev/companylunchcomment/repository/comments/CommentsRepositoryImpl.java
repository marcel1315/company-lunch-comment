package com.marceldev.companylunchcomment.repository.comments;

import static com.marceldev.companylunchcomment.entity.QComments.comments;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.type.CommentsSort;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
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
  public Page<CommentsOutputDto> getList(GetCommentsListDto dto, Pageable pageable) {
    long total = Optional.ofNullable(
            queryFactory.select(comments.count())
                .from(comments)
                .where(
                    contentContains(dto),
                    commentedByEq(dto)
                )
                .fetchOne())
        .orElse(0L);

    List<CommentsOutputDto> content = queryFactory.select(
            Projections.constructor(CommentsOutputDto.class,
                comments.id,
                comments.content,
                comments.shareStatus.stringValue(),
                comments.createdAt,
                comments.member.id.as("commentedBy"),
                comments.diner.id.as("dinerId")
            ))
        .from(comments)
        .where(
            contentContains(dto),
            commentedByEq(dto)
        )
        .orderBy(getOrder(dto.getCommentsSort()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression contentContains(GetCommentsListDto dto) {
    return dto.getKeyword() != null ? comments.content.contains(dto.getKeyword()) : null;
  }

  private BooleanExpression commentedByEq(GetCommentsListDto dto) {
    return dto.getCommentedBy() != null ? comments.member.name.eq(dto.getCommentedBy()) : null;
  }

  private OrderSpecifier<?> getOrder(CommentsSort sort) {
    return switch (sort) {
      case CREATED_AT_ASC -> new OrderSpecifier<>(Order.ASC, comments.createdAt);
      case CREATED_AT_DESC -> new OrderSpecifier<>(Order.DESC, comments.createdAt);
    };
  }
}

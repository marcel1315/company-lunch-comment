package com.marceldev.companylunchcomment.repository.diner;

import static com.marceldev.companylunchcomment.entity.QComments.comments;
import static com.marceldev.companylunchcomment.entity.QCompany.company;
import static com.marceldev.companylunchcomment.entity.QDiner.diner;

import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.type.DinerSort;
import com.marceldev.companylunchcomment.type.SortDirection;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparablePath;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class DinerRepositoryImpl implements DinerRepositoryCustom {

  private final JPAQueryFactory queryFactory;

  @Override
  public Page<DinerOutputDto> getList(long companyId, GetDinerListDto dto, Pageable pageable) {

    long total = Optional.ofNullable(
            queryFactory.select(diner.count())
                .from(diner)
                .where(
                    companyEq(companyId),
                    nameContains(dto)
                )
                .fetchOne())
        .orElse(0L);

    List<DinerOutputDto> content = queryFactory.select(
            Projections.constructor(DinerOutputDto.class,
                diner.id,
                diner.name,
                diner.link,
                diner.location,
                diner.tags,
                comments.count().as("commentsCount"),
                distance(diner.location, company.location).as("distanceInMeter")
            ))
        .from(diner)
        .leftJoin(diner.comments, comments)
        .leftJoin(diner.company, company)
        .where(
            companyEq(companyId),
            nameContains(dto)
        )
        .groupBy(diner.id)
        .orderBy(getOrder(dto.getSortBy(), dto.getSortDirection()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(content, pageable, total);
  }

  @Override
  public Integer getDistance(long companyId, long dinerId) {
    Tuple result = queryFactory.select(
            diner.id,
            distance(diner.location, company.location)
        )
        .from(diner)
        .join(diner.company, company)
        .where(
            diner.id.eq(dinerId),
            company.id.eq(companyId)
        )
        .fetchOne();

    if (result == null) {
      return null;
    }
    Number number = result.get(distance(diner.location, company.location));
    return number != null ? number.intValue() : null;
  }

  private BooleanExpression companyEq(long companyId) {
    return diner.company.id.eq(companyId);
  }

  private BooleanExpression nameContains(GetDinerListDto dto) {
    return dto.getKeyword() != null ? diner.name.contains(dto.getKeyword()) : null;
  }

  private NumberExpression<?> distance(ComparablePath<Point> location1,
      ComparablePath<Point> location2) {
    return Expressions.numberTemplate(Double.class,
        "ST_Distance_Sphere({0}, {1})", location1, location2);
  }

  private OrderSpecifier<?> getOrder(DinerSort sort, SortDirection direction) {
    final Order order = direction == SortDirection.ASC ? Order.ASC : Order.DESC;

    return switch (sort) {
      case DINER_NAME -> new OrderSpecifier<>(order, diner.name);
      case COMMENTS_COUNT -> new OrderSpecifier<>(order, comments.count());
      case DISTANCE -> new OrderSpecifier<>(order, diner.distance);
    };
  }
}

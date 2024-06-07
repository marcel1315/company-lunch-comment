package com.marceldev.companylunchcomment.repository.diner;

import static com.marceldev.companylunchcomment.entity.QDiner.diner;

import com.marceldev.companylunchcomment.dto.diner.DinerOutputDto;
import com.marceldev.companylunchcomment.dto.diner.GetDinerListDto;
import com.marceldev.companylunchcomment.type.DinerSort;
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
                diner.latitude,
                diner.longitude,
                diner.tags
            ))
        .from(diner)
        .where(
            companyEq(companyId),
            nameContains(dto)
        )
        .orderBy(getOrder(dto.getDinerSort()))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    return new PageImpl<>(content, pageable, total);
  }

  private BooleanExpression companyEq(long companyId) {
    return diner.company.id.eq(companyId);
  }

  private BooleanExpression nameContains(GetDinerListDto dto) {
    return dto.getKeyword() != null ? diner.name.contains(dto.getKeyword()) : null;
  }

  private OrderSpecifier<?> getOrder(DinerSort sort) {
    return switch (sort) {
      case DINER_NAME_ASC -> new OrderSpecifier<>(Order.ASC, diner.name);
      case DINER_NAME_DESC -> new OrderSpecifier<>(Order.DESC, diner.name);
    };
  }
}

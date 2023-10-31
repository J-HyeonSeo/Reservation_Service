package com.jhsfully.reservation.repository.custom.impl;

import com.jhsfully.reservation.domain.QShop;
import com.jhsfully.reservation.model.ShopTopResponse;
import com.jhsfully.reservation.repository.custom.ShopCustomRepository;
import com.jhsfully.reservation.type.SortingType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ShopCustomRepositoryImpl implements ShopCustomRepository {

  private static final OrderSpecifier<?> INITIAL_ORDER_BY = null;
  private final JPAQueryFactory jpaQueryFactory;

  public Page<ShopTopResponse> findByNameAndOrdering(String searchValue, double latitude, double longitude, SortingType type, boolean isAsc, Pageable pageable) {
    QShop shop = QShop.shop;

    OrderSpecifier<?> orderBy = INITIAL_ORDER_BY;
    switch (type) {
      case STAR:
        orderBy = isAsc ? Expressions.stringPath("star").asc() :
            Expressions.stringPath("star").desc();
        break;
      case TEXT:
        orderBy = isAsc ? Expressions.stringPath("name").asc() :
            Expressions.stringPath("name").desc();
        break;
      case DISTANCE:
        orderBy = isAsc ? Expressions.stringPath("distance").asc() :
            Expressions.stringPath("distance").desc();
        break;
    }

    //거리 계산을 위한 NativeQuery
    Expression<Double> distanceExpression = Expressions
        .numberTemplate(Double.class,
            "ST_DISTANCE_SPHERE(point({0}, {1}), point({2}, {3}))",
            shop.longitude, shop.latitude, longitude, latitude).as("distance");

    List<ShopTopResponse> responseList = jpaQueryFactory.select(Projections.constructor(ShopTopResponse.class,
            shop.id, shop.name, shop.introduce, shop.address, distanceExpression, shop.star))
        .from(shop)
        .where(shop.name.like(searchValue).and(shop.isDeleted.isFalse()))
        .orderBy(orderBy)
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long count = jpaQueryFactory.select(Wildcard.count)
        .from(shop)
        .where(shop.name.like(searchValue).and(shop.isDeleted.isFalse()))
        .fetchFirst();

    return new PageImpl<>(responseList, pageable, count);

  }

}

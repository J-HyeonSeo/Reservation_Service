package com.jhsfully.reservation.repository.custom;

import com.jhsfully.reservation.model.ShopTopResponse;
import com.jhsfully.reservation.type.SortingType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopCustomRepository {
  Page<ShopTopResponse> findByNameAndOrdering(String searchValue, double latitude,
      double longitude, SortingType type, boolean isAsc, Pageable pageable);
}

package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {
}

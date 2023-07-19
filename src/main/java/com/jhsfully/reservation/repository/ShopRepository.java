package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByNameStartingWith(String name);

    List<Shop> findByMember(Member member);

}

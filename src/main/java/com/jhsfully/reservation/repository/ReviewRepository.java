package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import com.jhsfully.reservation.domain.Review;
import com.jhsfully.reservation.domain.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    int countByMember(Member member);
    Page<Review> findByMember(Member member, Pageable pageable);

    int countByShop(Shop shop);
    Page<Review> findByShop(Shop shop, Pageable pageable);
}

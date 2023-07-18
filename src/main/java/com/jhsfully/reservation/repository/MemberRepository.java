package com.jhsfully.reservation.repository;

import com.jhsfully.reservation.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    int countByUsername(String username);
    Optional<Member> findByUsername(String username);

}

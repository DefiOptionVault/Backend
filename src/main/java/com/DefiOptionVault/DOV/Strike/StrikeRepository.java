package com.DefiOptionVault.DOV.Strike;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StrikeRepository extends JpaRepository<Strike, Integer> {
    List<Strike> findByOption_OptionId(int optionId);
}

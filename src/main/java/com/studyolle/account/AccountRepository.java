package com.studyolle.account;

import com.studyolle.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface AccountRepository extends JpaRepository<Account , Long> {

    public boolean existsByNickname(String nickname);

    public boolean existsByEmail(String email);

    Account findByEmail(String email);
}
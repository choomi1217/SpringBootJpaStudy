package com.studyolle.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.studyolle.account.UserAccount;
import com.studyolle.study.StudyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StudyTest {

    @Autowired
    StudyService studyService;

    Study study;
    Account account;
    UserAccount userAccount;

    @BeforeEach
    void settings() {
        study = new Study();
        account = Account.builder()
            .email("whdudal1217@naver.com")
            .password("123456789")
            .nickname("oomi")
            .build();
        userAccount = new UserAccount(account);
    }

    @Test
    @DisplayName("멤버인지 확인합니다.")
    void isMemberTest() {
        study.addMember(account);
        assertTrue(study.isMember(userAccount));
    }

    @DisplayName("매니저인지 확인합니다.")
    @Test
    void isManagerTest() {
        study.addManager(account);
        assertTrue(study.isManager(userAccount));
    }

    /*
     * 스터디 공개
     * 스터디 비공개
     * ----------
     * 인원 모집중
     * 인원 모집 마감
     * ----------
     * 스터디 멤버임
     * 스터디 관리자임
     * 비멤버임
     * ----------
     * 1 > 스터디 공개 + 인원 모집중 + 비멤버
     * 2 > 스터디 공개 + 인원 모집중 + 멤버
     * 3 > 스터디 공개 + 인원 모집중 + 관리자
     * 4 > 스터디 비공개 + 인원 모집 마감
     * */

    @DisplayName("스터디 공개 + 인원 모집중 + 비멤버")
    @Test
    void publicRecruitNonMember() {
        study.setPublished(true);
        study.setRecruiting(true);

        assertTrue(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 공개 + 인원 모집중 + 멤버")
    @Test
    void publicRecruitMember() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addMember(account);

        assertFalse(study.isJoinable(userAccount));
    }

    @DisplayName("스터디 공개 + 인원 모집중 + 관리자")
    @Test
    void publicRecruitManager() {
        study.setPublished(true);
        study.setRecruiting(true);
        study.addManager(account);

        assertFalse(study.isJoinable(userAccount));

    }

    @DisplayName("스터디 비공개 + 인원 모집 마감")
    @Test
    void privateClose() {
        study.setPublished(false);
        study.setRecruiting(false);

        assertFalse(study.isJoinable(userAccount));
    }
}

package com.studyolle.study;

import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class StudySettingsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired StudyService studyService;
    @Autowired StudyRepository studyRepository;
    @Autowired AccountRepository accountRepository;

    @WithAccount("oomi")
    @DisplayName(" 스터디 설정 화면 조회 - 성공 ")
    @Test
    void viewStudySetting_success() throws Exception {
        Account newAccount = createNewAccount();
        Study newStudy = createNewStudy("test-path" , newAccount);

        mockMvc.perform(get("/study/" + newStudy.getPath() + "/settings/description"))
            .andExpect(status().isForbidden());
    }

    @DisplayName(" 스터디 설정 화면 조회 - 실패 ")
    @Test
    void viewStudySetting_fail() {
    }

    private Account createNewAccount(){
        Account test = new Account();
        test.setNickname("test");
        test.setEmail("test@naver.com");

        accountRepository.save(test);

        return test;
    }

    private Study createNewStudy(String path, Account account){
        Study study = new Study();
        study.setPath(path);
        studyService.createNewStudy(study, account);
        return study;
    }

}

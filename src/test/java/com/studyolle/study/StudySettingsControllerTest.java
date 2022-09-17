package com.studyolle.study;

import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import lombok.RequiredArgsConstructor;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

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
    @DisplayName(" 스터디 설명 설정 화면 조회 - 성공 ")
    @Test
    void viewStudySetting_success() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study newStudy = createNewStudy("test-path" , oomi);

        mockMvc.perform(get("/study/" + newStudy.getPath() + "/settings/description"))
            .andExpect(status().isOk())
            .andExpect(view().name("study/settings/description"))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("study"))
            .andExpect(model().attributeExists("studyDescriptionForm"));
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 설명 설정 화면 조회 - 실패 ")
    @Test
    void viewStudySetting_fail() throws Exception {
        Account failAccount = createNewAccount();
        Study failStudy = createNewStudy("test-path", failAccount);

        mockMvc.perform(get("/study/"+ failStudy.getPath() + "/settings/description"))
            .andExpect(status().isForbidden());
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 설명 수정 - 성공")
    @Test
    void updateDescription_success() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study testStudy = createNewStudy("test-path", oomi);
        String path = "/study/"+testStudy.getPath()+"/settings/description";

        mockMvc.perform(post(path + "description")
                .param("shortDescription", "shortDescription")
                .param("fullDescription","fullDescription")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(path))
            .andExpect(flash().attributeExists("message"));
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 설명 수정 - 실패")
    @Test
    void updateDescription_fail() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study testStudy = createNewStudy("test-path", oomi);
        String path = "/study/"+testStudy.getPath()+"/settings/description";

        mockMvc.perform(post(path)
                .param("shortDescription", "shortDescription")
                .param("fullDescription","") // 실패!
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("studyDescriptionForm"))
            .andExpect(model().attributeExists("study"))
            .andExpect(model().attributeExists("account"))
        ;
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 설정 화면 조회 - 성공 ")
    @Test
    void viesBanner_success() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study testStudy = createNewStudy("test-path", oomi);
        String path = "/study/"+testStudy.getPath()+"/settings/banner";

        mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(view().name("study/settings/banner"))
            .andExpect(model().attributeExists("study"))
            .andExpect(model().attributeExists("account"))
            ;
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 설정 화면 조회 - 실패 ")
    @Test
    void viesBanner_fail() throws Exception {
        Account failAccount = createNewAccount();
        Study failStudy = createNewStudy("test-path", failAccount);
        String path = "/study/"+failStudy.getPath()+"/settings/banner";

        mockMvc.perform(get(path))
            .andExpect(status().isForbidden())
        ;
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 설정 - 성공 ")
    @Test
    void updateBanner_success() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study testStudy = createNewStudy("test-path", oomi);
        String path = "/study/"+testStudy.getPath()+"/settings/banner";

        mockMvc.perform(post(path)
                .param("image","이미지를 수정했습니다")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(path))
            .andExpect(flash().attributeExists("message"));
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 설정 - 실패 ")
    @Test
    void updateBanner_fail() throws Exception {
        Account failAccount = createNewAccount();
        Study failStudy = createNewStudy("test-path", failAccount);
        String path = "/study/"+failStudy.getPath()+"/settings/banner";

        mockMvc.perform(post(path)
                .param("image","image")
                .with(csrf()))
            .andExpect(status().isForbidden())
        ;
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 사용 설정")
    @Test
    void updateBanner_enable() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study newStudy = createNewStudy("test-path", oomi);
        String path = "/study/" + newStudy.getPath() + "/settings/banner/enable";

        mockMvc.perform(post(path).with(csrf()))
            .andExpect(status().is3xxRedirection());

        Study study = studyRepository.findByPath("test-path");
        assertThat(study.isUseBanner()).isTrue();
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 배너 미사용 설정")
    @Test
    void updateBanner_disable() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study newStudy = createNewStudy("test-path", oomi);
        String path = "/study/" + newStudy.getPath() + "/settings/banner/disable";

        mockMvc.perform(post(path).with(csrf()))
            .andExpect(status().is3xxRedirection());

        Study study = studyRepository.findByPath("test-path");
        assertThat(study.isUseBanner()).isFalse();
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 태그 설정 화면 조회 - 성공 ")
    @Test
    void viewTag_success() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Study newStudy = createNewStudy("test-path", oomi);
        String path = "/study/" + newStudy.getPath() + "/settings/tags";

        mockMvc.perform(get(path))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("study"))
            //.andExpect(model().attributeExists("tags"))
            .andExpect(model().attributeExists("whitelist"))
            .andExpect(view().name("study/settings/tags"))
        ;
    }

    @WithAccount("oomi")
    @DisplayName(" 스터디 태그 설정 화면 조회 - 실패 ")
    @Test
    void viewTag_fail() throws Exception {
        Account failAccount = createNewAccount();
        Study failStudy = createNewStudy("test-path", failAccount);
        String path = "/study/" + failStudy.getPath() + "/settings/tags";

        mockMvc.perform(get(path))
            .andExpect(status().isForbidden());
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

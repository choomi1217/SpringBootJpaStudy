package com.studyolle.settings;

import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.domain.Account;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach(){
        accountRepository.deleteAll();
    }

    @WithAccount("oomi")
    @DisplayName("프로필 수정 폼을 보여줍니다.")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_PROFILE_URL))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("profile"));

    }

    @WithAccount("oomi")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{

        String testBio = "소개를 수정합니다.";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
            .param("bio",testBio)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SettingsController.SETTINGS_PROFILE_URL))
            .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findAllByNickname("oomi");

        assertThat(testBio).isEqualTo(account.getBio());
    }


    @WithAccount("ParkChan-ho")
    @DisplayName("프로필 수정하기 - 너무 긴 소개 에러")
    @Test
    void updateProfile_error() throws Exception{

        String testBio = "안녕하세요. 박찬호라고 합니다. 제가 LA에 있을때는 말이죠 정말 제가 꿈에 무대인 메이저리그로 진출해서...";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio",testBio)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("profile"))
        ;
        Account account = accountRepository.findAllByNickname("ParkChan-ho");
        assertThat(account.getBio()).isNull();
    }

}

package com.studyolle.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.settings.form.TagForm;
import com.studyolle.tag.TagRepository;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
public class SettingsControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    AccountRepository accountRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    TagRepository tagRepository;

    @Autowired
    AccountService accountService;

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

        Account account = accountRepository.findByNickname("oomi");

        assertThat(testBio).isEqualTo(account.getBio());
    }


    @WithAccount("ParkChan-ho")
    @DisplayName("프로필 수정하기 - 너무 긴 소개 에러")
    @Test
    void updateProfile_error() throws Exception {

        String testBio = "안녕하세요. 박찬호라고 합니다. 제가 LA에 있을때는 말이죠 정말 제가 꿈에 무대인 메이저리그로 진출해서...";

        mockMvc.perform(post(SettingsController.SETTINGS_PROFILE_URL)
                .param("bio", testBio)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name(SettingsController.SETTINGS_PROFILE_VIEW))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("profile"))
        ;
        Account account = accountRepository.findByNickname("ParkChan-ho");
        assertThat(account.getBio()).isNull();
    }

    @WithAccount("oomi")
    @DisplayName("비밀번호 수정 폼을 보여줍니다")
    @Test
    void updatePasswrodForm() throws Exception {

        mockMvc.perform(get(SettingsController.SETTINGS_PASSWORD_URL))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW))
        ;
    }

    @DisplayName("비밀번호 변경 - 에러")
    @WithAccount("oomi")
    @Test
    void updatePassword_error() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","121712171217")
                .param("newPasswordConfirm","00000000")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name(SettingsController.SETTINGS_PASSWORD_VIEW))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("passwordForm"));
    }

    @DisplayName("비밀번호 변경 - 성공")
    @WithAccount("oomi")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(SettingsController.SETTINGS_PASSWORD_URL)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","123456789")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SettingsController.SETTINGS_PASSWORD_URL))
            .andExpect(flash().attributeExists("message"));

        Account accout = accountRepository.findByNickname("oomi");
        assertTrue(passwordEncoder.matches("123456789",accout.getPassword()));

    }

    @WithAccount("oomi")
    @DisplayName("태그 수정 폼")
    @Test
    void updateTagForm() throws Exception {
        mockMvc.perform(get(SettingsController.SETTINGS_TAGS_URL))
            .andExpect(view().name(SettingsController.SETTINGS_TAGS_VIEW))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("whiteList"))
            .andExpect(model().attributeExists("tags"));
    }

    @WithAccount("oomi")
    @DisplayName("계정에 태그를 추가합니다.")
    @Test
    void insertTag() throws Exception{
        String tagTitle = "testTag";
        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(tagTitle);
        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/add")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
            .andExpect(status().isOk());

        boolean tagIsPresent = tagRepository.findByTitle(tagTitle).isPresent();
        assertTrue(tagIsPresent);

        Tag tagsByTitle = tagRepository.findTagsByTitle(tagTitle);
        Account account = accountRepository.findByNickname("oomi");
        assertTrue(account.getTags().contains(tagsByTitle));

    }

    @WithAccount("oomi")
    @DisplayName("계정에 태그를 삭제합니다.")
    @Test
    void deleteTag() throws Exception{
        String tagTitle = "testTag";
        Tag newTag = tagRepository.save(Tag.builder().title(tagTitle).build());
        Account oomi = accountRepository.findByNickname("oomi");

        accountService.addTag(oomi,newTag);

        assertTrue(oomi.getTags().contains(newTag));

        TagForm tagForm = new TagForm();
        tagForm.setTagTitle(tagTitle);

        mockMvc.perform(post(SettingsController.SETTINGS_TAGS_URL+"/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
            .andExpect(status().isOk());

        Optional<Tag> byTitle = tagRepository.findByTitle(tagTitle);
        assertTrue(byTitle.isPresent());

        Tag tagsByTitle = tagRepository.findTagsByTitle(tagTitle);

        assertFalse(oomi.getTags().contains(tagsByTitle));

    }
}

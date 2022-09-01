package com.studyolle.settings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.WithAccount;
import com.studyolle.account.AccountRepository;
import com.studyolle.account.AccountService;
import com.studyolle.domain.Account;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.Notifications;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.zone.ZoneRepository;
import java.util.Optional;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static com.studyolle.settings.SettingsController.ACCOUNT;
import static com.studyolle.settings.SettingsController.NOTIFICATIONS;
import static com.studyolle.settings.SettingsController.PASSWORD;
import static com.studyolle.settings.SettingsController.PROFILE;
import static com.studyolle.settings.SettingsController.ROOT;
import static com.studyolle.settings.SettingsController.SETTINGS;
import static com.studyolle.settings.SettingsController.TAGS;
import static com.studyolle.settings.SettingsController.ZONES;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.not;
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

    @Autowired
    ZoneRepository zoneRepository;

    private Zone testZone = Zone.builder().city("test").localNameOfCity("테스트시").province("테스트주").build();
    @AfterEach
    void afterEach(){

        accountRepository.deleteAll();
        zoneRepository.deleteAll();
    }

    @BeforeEach
    void beforeEach() {
        zoneRepository.save(testZone);
    }

    @WithAccount("oomi")
    @DisplayName("프로필 수정 폼을 보여줍니다.")
    @Test
    void updateProfileForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + PROFILE))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("profile"))
            .andExpect(view().name(SETTINGS + PROFILE));

    }

    @WithAccount("oomi")
    @DisplayName("프로필 수정하기 - 입력값 정상")
    @Test
    void updateProfile() throws Exception{

        String testBio = "소개를 수정합니다.";

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio",testBio)
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SETTINGS + PROFILE))
            .andExpect(flash().attributeExists("message"));

        Account account = accountRepository.findByNickname("oomi");

        assertThat(testBio).isEqualTo(account.getBio());
    }

    @WithAccount("oomi")
    @DisplayName("닉네임 수정 폼을 보여줍니다.")
    @Test
    void updateNicknameForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ACCOUNT))
            .andExpect(view().name(SETTINGS + ACCOUNT))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("nicknameForm","account"));
    }

    @WithAccount("oomi")
    @DisplayName("닉네임 수정 - 에러")
    @Test
    void updateNicknameError() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname" , "om")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name(SETTINGS + ACCOUNT))
            .andExpect(model().attributeExists("account","nicknameForm"))
            .andExpect(model().hasErrors());
    }

    @WithAccount("oomi")
    @DisplayName("닉네임 수정 - 성공")
    @Test
    void updateNicknameSuccess() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + ACCOUNT)
                .param("nickname","test")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SETTINGS + ACCOUNT))
            .andExpect(model().attributeExists("account","nicknameForm"))
            .andExpect(flash().attributeExists("message"));
    }

    @WithAccount("ParkChan-ho")
    @DisplayName("프로필 수정하기 - 너무 긴 소개 에러")
    @Test
    void updateProfile_error() throws Exception {

        String testBio = "안녕하세요. 박찬호라고 합니다. 제가 LA에 있을때는 말이죠 정말 제가 꿈에 무대인 메이저리그로 진출해서...";

        mockMvc.perform(post(ROOT + SETTINGS + PROFILE)
                .param("bio", testBio)
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name( SETTINGS + PROFILE))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("profile"))
        ;
        Account account = accountRepository.findByNickname("ParkChan-ho");
        assertThat(account.getBio()).isNull();
    }

    @WithAccount("oomi")
    @DisplayName("비밀번호 수정 폼을 보여줍니다.")
    @Test
    void updatePasswrodForm() throws Exception {

        mockMvc.perform(get(ROOT + SETTINGS + PASSWORD))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(view().name(SETTINGS + PASSWORD))
        ;
    }

    @DisplayName("비밀번호 변경 - 에러")
    @WithAccount("oomi")
    @Test
    void updatePassword_error() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword","121712171217")
                .param("newPasswordConfirm","00000000")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name(SETTINGS + PASSWORD))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("passwordForm"));
    }

    @DisplayName("비밀번호 변경 - 성공")
    @WithAccount("oomi")
    @Test
    void updatePassword_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + PASSWORD)
                .param("newPassword","123456789")
                .param("newPasswordConfirm","123456789")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SETTINGS + PASSWORD))
            .andExpect(flash().attributeExists("message"));

        Account accout = accountRepository.findByNickname("oomi");
        assertTrue(passwordEncoder.matches("123456789",accout.getPassword()));
    }

    @WithAccount("oomi")
    @DisplayName("알림 수정 폼을 보여줍니다.")
    @Test
    void updateNotificationForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + NOTIFICATIONS))
            .andExpect(view().name(SETTINGS + NOTIFICATIONS))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("notifications"));
    }

    @WithAccount("oomi")
    @DisplayName("알림 수정 - 성공")
    @Test
    void updateNotification_success() throws Exception {
        mockMvc.perform(post(ROOT + SETTINGS + NOTIFICATIONS)
            .param("studyCreatedByEmail","false")
            .param("studyCreatedByWeb","true")
            .param("studyUpdatedByEmail","false")
            .param("studyUpdatedByWeb","true")
            .param("studyEnrollmentResultByEmail","false")
            .param("studyEnrollmentResultByWeb","true")
            .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl(SETTINGS + NOTIFICATIONS))
            .andExpect(flash().attributeExists("message"));

        Account oomi = accountRepository.findByNickname("oomi");
        assertFalse(oomi.isStudyCreatedByEmail());
        assertFalse(oomi.isStudyUpdatedByEmail());
        assertFalse(oomi.isStudyEnrollmentResultByEmail());
        assertTrue(oomi.isStudyCreatedByWeb());
        assertTrue(oomi.isStudyUpdatedByWeb());
        assertTrue(oomi.isStudyEnrollmentResultByWeb());
    }

    @WithAccount("oomi")
    @DisplayName("태그 수정 폼을 보여줍니다.")
    @Test
    void updateTagForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + TAGS))
            .andExpect(view().name(SETTINGS + TAGS))
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
        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/add")
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

        mockMvc.perform(post(ROOT + SETTINGS + TAGS + "/remove")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(tagForm))
                .with(csrf()))
            .andExpect(status().isOk());

        Optional<Tag> byTitle = tagRepository.findByTitle(tagTitle);
        assertTrue(byTitle.isPresent());

        Tag tagsByTitle = tagRepository.findTagsByTitle(tagTitle);

        assertFalse(oomi.getTags().contains(tagsByTitle));
    }

    @WithAccount("oomi")
    @DisplayName("계정의 지역 정보 수정 폼을 보여줍니다.")
    @Test
    void updateZoneForm() throws Exception {
        mockMvc.perform(get(ROOT + SETTINGS + ZONES))
            .andExpect(view().name(SETTINGS + ZONES))
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("whitelist"))
            .andExpect(model().attributeExists("zones"));
    }

    @WithAccount("oomi")
    @DisplayName("계정의 지역 정보를 수정합니다.")
    @Test
    void addZone() throws Exception {
        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
            .andExpect(status().isOk());

        Account oomi = accountRepository.findByNickname("oomi");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());

        assertTrue(oomi.getZones().contains(zone));

    }

    @WithAccount("oomi")
    @DisplayName("계정의 지역정보를 삭제합니다.")
    @Test
    void removeZone() throws Exception {
        Account oomi = accountRepository.findByNickname("oomi");
        Zone zone = zoneRepository.findByCityAndProvince(testZone.getCity(), testZone.getProvince());
        accountService.addZone(oomi , zone);

        ZoneForm zoneForm = new ZoneForm();
        zoneForm.setZoneName(testZone.toString());

        mockMvc.perform(post(ROOT + SETTINGS + ZONES + "/remove")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(zoneForm))
                .with(csrf()))
            .andExpect(status().isOk());

        assertFalse(oomi.getZones().contains(zone));
    }
}

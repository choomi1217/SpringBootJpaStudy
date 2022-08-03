package com.studyolle.account;

import com.studyolle.domain.Account;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
public class AccountControllerTest {
  @Autowired
  private MockMvc mockMvc;

  @Autowired
  AccountRepository accountRepository;

  @MockBean
  JavaMailSender javaMailSender;

  @DisplayName("회원가입 화면이 보이는지 테스트")
  @Test
  void signUpForm() throws Exception {
    mockMvc.perform(get("/sign-up"))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(view().name("account/sign-up"))
        .andExpect(model().attributeExists("signUpForm"));
  }

  @DisplayName("회원 가입 처리 - 입력값 오류")
  @Test
  void signUpSubmit_with_wrong_input() throws Exception {

    mockMvc.perform(post("/sign-up")
        .param("nickname","**oomi")
        .param("email","whdudal1217.naver.com")
        .param("password","12345")
        .with(csrf()))
        .andExpect(status().isOk())
        .andExpect(view().name("account/sign-up"));
  }

  @DisplayName("회원 가입 처리 - 입력값 정상")
  @Test
  void signUpSubmit_with_correct_input() throws Exception {

    mockMvc.perform(post("/sign-up")
            .param("nickname","oomi")
            .param("email","whdudal1217@naver.com")
            .param("password","1234578910")
            .with(csrf()))
        .andExpect(status().is3xxRedirection())
        .andExpect(view().name("redirect:/"));

    Account account = accountRepository.findByEmail("whdudal1217@naver.com");

    assertThat(account).isNotNull();
    assertThat(account.getPassword()).isNotEqualTo("1234578910");
    assertThat(account.getEmailCheckToken()).isNotNull();
    then(javaMailSender).should().send(any(SimpleMailMessage.class));
  }
}

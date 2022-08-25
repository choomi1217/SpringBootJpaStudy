package com.studyolle.account.form;

import lombok.Data;

import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {
  
  /*
  * JavaScript에서 행하는 validation check는 fail-fast의 의미는 있으나
  * 정상 루트가 아닌 행위로 명령이 들어 왔을땐 결국 back-end에서 한번 더 걸러줘야 함
  * */
  
  @NotBlank
  @Length(min = 3, max = 20)
  @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
  private String nickname;

  @NotBlank
  @Email
  private String email;

  @NotBlank
  @Length(min = 8, max = 50)
  private String password;

}

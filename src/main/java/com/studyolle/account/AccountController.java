package com.studyolle.account;

import com.studyolle.account.form.SignUpForm;
import com.studyolle.account.validator.SignUpFormValidator;
import com.studyolle.domain.Account;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;

import lombok.RequiredArgsConstructor;
import javax.validation.Valid;

@Controller
@RequiredArgsConstructor
public class AccountController {

  private final SignUpFormValidator signUpFormValidator;
  private final AccountService accountService;
  private final AccountRepository accountRepository;

  /**
   * InitBinder("signUpForm") : signUpForm 타입의 요청데이터가 왔을때 아래 메소드를 실행함.
   * */
  @InitBinder("signUpForm")
  public void initBinder(WebDataBinder webDataBinder){
    webDataBinder.addValidators(signUpFormValidator);
  }

  @GetMapping("/sign-up")
  public String signUpForm(Model model){
    // model에 attributeName은 생략이 가능 합니다.
    //model.addAttribute("signUpForm",new SignUpForm());
    model.addAttribute(new SignUpForm());

    return "account/sign-up";
  }

  @PostMapping("/sign-up")
  public String signUpSubmit(@Valid SignUpForm signUpForm, Errors errors){
    //@ModelAttribute : 복합객체를 받아야 할 때 사용하나 생략 가능
    //Errors : 데이터를 컨버젼하거나 바인딩 할 때 에러를 받습니다.
    // ( SignUpForm에서 Hibernate로 검증하는데 이에 통과하지 못하면 errors를 뱉을 수 있음)
    //errors에 에러가 있다면 form html을 다시 출력하면 됩니다!
    if(errors.hasErrors()){
      return "account/sign-up";
    }

    /*
    이런식으로 유효성 검증하는 것이 번거롭다면 InitBinder 사용!
    InitBinder 사용하면...
    PostMapping으로 요청이 오고 그 요청에 담긴 객체의 유효성을 검증 할 수 있습니다!
    signUpFormValidator.validate(signUpForm, errors);
    if(errors.hasErrors()){
      return "account/sign-up";
    }
     */
    Account loginAccount = accountService.processNewAccount(signUpForm);
    accountService.login(loginAccount);

    return "redirect:/";

  }

  @GetMapping("/check-email-token")
  public String checkEmailToken(String token, String email, Model model){

    Account account = accountRepository.findByEmail(email);
    String view = "account/checked-email";

    if(account == null){
      model.addAttribute("error","wrong.email");
      return view;
    }

    if(!account.isValidToken(token)){
      model.addAttribute("error","wrong.token");
      return view;
    }

    accountService.completeSignUp(account);
    model.addAttribute("numberOfUser", accountRepository.count());
    model.addAttribute("nickname", account.getNickname());

    return view;
  }

  @GetMapping("/check-email")
  public String checkEmail(@CurrentUser Account account, Model model){
    model.addAttribute("email", account.getEmail());
    return "account/check-email";
  }

  @GetMapping("/resend-confirm-email")
  public String resendConfirmEmail(@CurrentUser Account account, Model model){
    if(!account.canSendConfirmEmail()){
      model.addAttribute("error","인증 이메일은 1시간에 한번만 전송할 수 있습니다.");
      model.addAttribute("email", account.getEmail());
      return "account/check-email";
    }
    accountService.sendSignUpConfirmEmail(account);
    return "redirect:/";
  }

  @GetMapping("/profile/{nickname}")
  public String viewProfile(@PathVariable String nickname, @CurrentUser Account account, Model model){
    Account byNickname = accountRepository.findAllByNickname(nickname);
    if(byNickname == null ){
      throw new IllegalArgumentException(nickname + " 에 해당하는 사용자가 없습니다. ");
    }
    model.addAttribute(byNickname);
    model.addAttribute("isOwner", byNickname.equals(account));
    return "account/profile";
  }

}

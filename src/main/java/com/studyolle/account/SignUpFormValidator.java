package com.studyolle.account;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor //private final로 선언된 변수만 생성자로 넣어주는 롬복의 어노테이션
public class SignUpFormValidator implements Validator {

    //Spring 4.2 이후로는 Autowired를 하지 않아도 bean객체 주입이 자동이므로 어노테이션 필요가 없습니다.
    private final AccountRepository accountRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        //SignUpForm타입의 인스턴스를 검사할 것임
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;
        if(accountRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid email", new Object[]{signUpForm.getEmail()},"이미 사용중인 이메일입니다.");
        }
        if(accountRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid nickname", new Object[]{signUpForm.getNickname()},"이미 사용중인 닉네임입니다.");
        }
    }
}

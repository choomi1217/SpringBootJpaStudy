package com.studyolle.settings.validator;

import com.studyolle.settings.form.PasswordForm;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

public class PasswordFormValidator implements Validator{

    @Override
    public boolean supports(Class<?> clazz) {
        // 어떤 타입의 폼 객체를 검증 할 것인가
        return PasswordForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getNewPassword().equals(passwordForm.getNewPasswordConfirm())){
            errors.rejectValue("newPassword", "wrong.value","입력한 새 패스워드가 일치하지 않습니다.");
        }
    }
}

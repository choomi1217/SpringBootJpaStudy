package com.studyolle.settings;

import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentUser;
import com.studyolle.domain.Account;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class SettingsController {

    private final AccountService accountService;
    private final ModelMapper modelMapper;

    static final String SETTINGS_PROFILE_VIEW = "settings/profile";
    static final String SETTINGS_PROFILE_URL = "/settings/profile";

    @GetMapping(SETTINGS_PROFILE_URL)
    public String profileUpdate(@CurrentUser Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new Profile(account));
        //model.addAttribute(modelMapper.map(account, Profile.class));
        return SETTINGS_PROFILE_VIEW;
    }

    @PostMapping(SETTINGS_PROFILE_URL)
    public String updateProfile(@CurrentUser Account account, @Valid @ModelAttribute Profile profile, Errors errors, Model model, RedirectAttributes redirectAttributes){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return SETTINGS_PROFILE_VIEW;
        }
        accountService.updateProfile(account,profile);
        redirectAttributes.addFlashAttribute("message","프로필을 수정하셨습니다.");
        return "redirect:" + SETTINGS_PROFILE_URL;
    }
}

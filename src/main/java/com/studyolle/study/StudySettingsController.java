package com.studyolle.study;

import com.studyolle.account.CurrentAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.study.form.StudyDescriptionForm;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/study/{path}/settings")
@RequiredArgsConstructor
public class StudySettingsController {

    private final String ROOT = "/";
    private final String STUDY_SETTINGS_ROOT = "study/settings";
    private final String DESCRIPTION = "/description";
    private final String BANNER = "/banner";

    @Autowired
    StudyService studyService;

    @Autowired
    ModelMapper modelMapper;

    @GetMapping(DESCRIPTION)
    public String viewStudySetting(@CurrentAccount Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        model.addAttribute(modelMapper.map(study, StudyDescriptionForm.class));
        return STUDY_SETTINGS_ROOT + DESCRIPTION;
    }

    @PostMapping(DESCRIPTION)
    public String updateStudyDescription(@CurrentAccount Account account, @PathVariable String path
        , @Valid StudyDescriptionForm studyDescriptionForm, Errors errors, Model model, RedirectAttributes redirectAttributes){
        Study study = studyService.getStudyToUpdate(account,path);

        if(errors.hasErrors()){
            model.addAttribute(account);
            model.addAttribute(study);
            return STUDY_SETTINGS_ROOT + DESCRIPTION;
        }

        studyService.updateStudyDescription(study, studyDescriptionForm);
        redirectAttributes.addFlashAttribute("message", "스터디 소개를 수정했습니다.");
        return "redirect:/study/" + getPath(path) + "/settings/description";
    }

    @GetMapping(BANNER)
    public String updateBannerForm(@CurrentAccount Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(study);
        return STUDY_SETTINGS_ROOT + BANNER;
    }

    @PostMapping(BANNER)
    public String updateBannerImage(@CurrentAccount Account account, @PathVariable String path, String image , RedirectAttributes redirectAttributes){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.updateStudyImage(study,image);
        redirectAttributes.addFlashAttribute("message","스터디 이미지를 수정했습니다.");
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }

    @PostMapping(BANNER + "/enable")
    public String enableBanner(@CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.enableBanner(study);
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }

    @PostMapping(BANNER + "/disable")
    public String disableBanner(@CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        studyService.disableBanner(study);
        return "redirect:/study/"+ getPath(path) + "/settings/banner";
    }


    private String getPath(String path){
        return URLEncoder.encode(path, StandardCharsets.UTF_8);
    }

}

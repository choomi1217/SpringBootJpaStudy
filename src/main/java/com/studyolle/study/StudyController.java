package com.studyolle.study;

import com.studyolle.account.CurrentAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.study.form.StudyForm;
import com.studyolle.study.validator.StudyFormValidator;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class StudyController {

    private final ModelMapper modelMapper;
    private final StudyService studyService; //UnsatisfiedDependencyException
    private final StudyFormValidator studyFormValidator;
    private final StudyRepository studyRepository;

    @InitBinder("studyForm")
    public void initBinder(WebDataBinder webDataBinder){
        webDataBinder.addValidators(studyFormValidator);
    }

    /*
    * 스터디 개설 폼
    * */
    @GetMapping("/new-study")
    public String newStudyForm(@CurrentAccount Account account, Model model){
        model.addAttribute(account);
        model.addAttribute(new StudyForm());
        return "study/form";
    }

    /*
    * 스터디 개설
    * */

    @PostMapping("/new-study")
    public String newStudySubmit(@CurrentAccount Account account, @Valid StudyForm studyForm, Errors errors, Model model){
        if(errors.hasErrors()){
            model.addAttribute(account);
            return "study/form";
        }

        Study newStudy = studyService.createNewStudy(modelMapper.map(studyForm, Study.class), account);
        return "redirect:/study/" + URLEncoder.encode(newStudy.getPath(), StandardCharsets.UTF_8);
    }

    /*
    * 스터디 조회
    * */
    @GetMapping("/study/{path}")
    public String viewStudy(@CurrentAccount Account account, @PathVariable String path, Model model){
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));
        return "study/view";
    }

    /*
    * 스터디 멤버 조회
    * */
    @GetMapping("/study/{path}/members")
    public String viewStudyMember(@CurrentAccount Account account, @PathVariable String path, Model model){
        model.addAttribute(account);
        model.addAttribute(studyRepository.findByPath(path));
        return "study/members";
    }

    /*
    * 스터디 멤버 가입
    * */
    @PostMapping("/study/{path}/join")
    public String joinStudy(@CurrentAccount Account account, @PathVariable String path, Model model){
        /*
        Study study = studyRepository.findStudyWithMembersWithPath(path);
        studyService.addMember(study, account);
        return "redirect:/study/" + study.getPath() + "/members";
        */
        return "";
    }

    /*
     * 스터디 멤버 탈퇴
     * */
    @PostMapping("/study/{path}/leave")
    public String leaveStudy(@CurrentAccount Account account, @PathVariable String path, Model model){
        /*
        Study study = studyRepository.findStudyWithMembersWithPath(path);
        studyService.removeMember(study, account);
        return "redirect:/study/" + study.getPath() + "/members";
        */
        return "";
    }
}

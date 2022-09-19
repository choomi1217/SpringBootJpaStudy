package com.studyolle.study;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.studyolle.account.AccountService;
import com.studyolle.account.CurrentAccount;
import com.studyolle.domain.Account;
import com.studyolle.domain.Study;
import com.studyolle.domain.Tag;
import com.studyolle.domain.Zone;
import com.studyolle.settings.form.TagForm;
import com.studyolle.settings.form.ZoneForm;
import com.studyolle.study.form.StudyDescriptionForm;
import com.studyolle.tag.TagRepository;
import com.studyolle.tag.TagService;
import com.studyolle.zone.ZoneRepository;
import com.studyolle.zone.ZoneService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final String TAGS = "/tags";
    private final String ZONES = "/zones";
    private final String STUDY = "/study";

    @Autowired StudyService studyService;
    @Autowired ModelMapper modelMapper;
    @Autowired TagRepository tagRepository;
    @Autowired ObjectMapper objectMapper;
    @Autowired AccountService accountService;
    @Autowired TagService tagService;
    @Autowired ZoneRepository zoneRepository;

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
    public String viewUpdateBanner(@CurrentAccount Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
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

    @GetMapping(TAGS)
    public String viewUpdateTags(@CurrentAccount Account account, @PathVariable String path, Model model)
        throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);

        Set<Tag> tagSet = accountService.getTag(account);
        List<String> tags = tagSet.stream().map(Tag::getTitle).collect(Collectors.toList());

        List<Tag> allTags = tagRepository.findAll();
        String whitelist = objectMapper.writeValueAsString(allTags);

        model.addAttribute(study);
        model.addAttribute(tags);
        model.addAttribute("whitelist", whitelist);
        return STUDY_SETTINGS_ROOT + TAGS;
    }

    @PostMapping(TAGS + "/add")
    public ResponseEntity<Object> addTags(@RequestBody TagForm tagForm, @CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagService.findOrCreateNew(tagForm.getTagTitle());
        studyService.addTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @PostMapping(TAGS + "/remove")
    public ResponseEntity<Object> removeTags(@RequestBody TagForm tagForm, @CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdateTag(account,path);
        Tag tag = tagRepository.findTagsByTitle(tagForm.getTagTitle());
        if(tag == null){
            return ResponseEntity.badRequest().build();
        }
        studyService.removeTag(study, tag);
        return ResponseEntity.ok().build();
    }

    @GetMapping(ZONES)
    public String viewZoneUpdate(@CurrentAccount Account account, @PathVariable String path, Model model)
        throws JsonProcessingException {
        Study study = studyService.getStudyToUpdate(account,path);

        List<String> zones = study.getZones().stream().map(Zone::toString)
            .collect(Collectors.toList());

        List<String> allZones = zoneRepository.findAll().stream().map(Zone::toString)
            .collect(Collectors.toList());
        String whitelist = objectMapper.writeValueAsString(allZones);

        model.addAttribute(account);
        model.addAttribute("zones", zones);
        model.addAttribute(study);
        model.addAttribute("whitelist", whitelist);
        return STUDY_SETTINGS_ROOT + ZONES;
    }

    @PostMapping(ZONES + "/add")
    public ResponseEntity<Object> addZones(@RequestBody ZoneForm zoneForm, @CurrentAccount Account account , @PathVariable String path){
        Study study = studyService.getStudyToUpdateZone(account,path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        studyService.addZone(study, zone);
        return ResponseEntity.ok().build();
    }

    @PostMapping(ZONES + "/remove")
    public ResponseEntity<Object> removeZones(@RequestBody ZoneForm zoneForm, @CurrentAccount Account account, @PathVariable String path){
        Study study = studyService.getStudyToUpdate(account, path);
        Zone zone = zoneRepository.findByCityAndProvince(zoneForm.getCityName(), zoneForm.getProvinceName());
        studyService.removeZone(study,zone);
        return ResponseEntity.ok().build();
    }

    @GetMapping(STUDY)
    public String viewUpdateStudy(@CurrentAccount Account account, @PathVariable String path, Model model){
        Study study = studyService.getStudyToUpdate(account, path);
        model.addAttribute(account);
        model.addAttribute(study);
        return STUDY_SETTINGS_ROOT + STUDY;
    }

    // 스터디공개 /study/publish
    @PostMapping("/study/publish")
    public String updateStudyPublish(@CurrentAccount Account account, @PathVariable String path){
        return "";
    }

    // 스터디비공개 /study/close
    @PostMapping("/study/close")
    public void b(){}

    // 스터디원모집시작 /recruit/start
    @PostMapping("/recruit/start")
    public void c(){}

    // 스터디원모집중단 /recruit/stop
    @PostMapping("/recruit/stop")
    public void d(){}

    // 스터디경로수정 /study/path
    @PostMapping("/study/path")
    public void e(){}

    // 스터디이름수정 /study/title
    @PostMapping("/study/title")
    public void f(){}

    // 스터디삭제 /study/remove
    @PostMapping("/study/remove")
    public void g(){}

    private String getPath(String path){
        String encode = URLEncoder.encode(path, StandardCharsets.UTF_8);
        System.out.println("getPath > " + encode);
        return encode;
    }

}

package com.studyolle.study;

import com.studyolle.WithAccount;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@Transactional
@SpringBootTest
@AutoConfigureMockMvc
@RequiredArgsConstructor
public class StudyControllerTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    StudyService studyService;
    @Autowired
    StudyRepository studyRepository;

    @DisplayName("스터디 개설 폼을 보여줍니다.")
    @WithAccount("oomi")
    @Test
    void newStudyFormTest() throws Exception {
        mockMvc.perform(get("/new-study"))
            .andExpect(status().isOk())
            .andExpect(model().attributeExists("account"))
            .andExpect(model().attributeExists("studyForm"))
            .andExpect(view().name("study/form"));
    }

    @DisplayName("스터디 개설 성공")
    @WithAccount("oomi")
    @Test
    void newStudySubmit() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "test-title")
                .param("shortDescription", "shortDescription")
                .param("fullDescription", "fullDescription")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/study/test-path"));
    }


    @DisplayName("스터디 개설 실패")
    @WithAccount("oomi")
    @Test
    void newStudySubmit_fail() throws Exception {
        mockMvc.perform(post("/new-study")
                .param("path", "test-path")
                .param("title", "test-title")
                .param("shortDescription", "short Description is toooooooo loooooooooooooong")
                .param("fullDescription", "boo")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(view().name("/"));
    }
}

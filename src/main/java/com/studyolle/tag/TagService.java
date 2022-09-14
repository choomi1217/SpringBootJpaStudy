package com.studyolle.tag;

import com.studyolle.domain.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public Tag findOrCreateNew(String tagTitle) {
        Tag tagsByTitle = tagRepository.findTagsByTitle(tagTitle);
        if(tagsByTitle == null){
            tagRepository.save(Tag.builder().title(tagTitle).build());
        }
        return tagsByTitle;
    }
}

package com.studyolle.zone;

import com.studyolle.domain.Zone;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ZoneService {

    private final ZoneRepository zoneRepository;

    @PostConstruct
    public void initZoneData() throws IOException {
        if(zoneRepository.count() == 0){
            Resource resource = new ClassPathResource("zone_kor.csv");
            List<Zone> zoneList = Files.readAllLines(Path.of(resource.getFile().getPath()), StandardCharsets.UTF_8)
                .stream().map(line -> {
                    String[] split = line.split(",");
                    return Zone.builder().city(split[0]).localNameOfCity(split[1]).province(split[2]).build();
                }).collect(Collectors.toList());
            zoneRepository.saveAll(zoneList);
        }
    }

}

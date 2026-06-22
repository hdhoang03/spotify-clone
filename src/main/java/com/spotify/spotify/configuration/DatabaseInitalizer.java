package com.spotify.spotify.configuration;

import com.spotify.spotify.entity.Category;
import com.spotify.spotify.repository.CategoryRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitalizer implements CommandLineRunner {
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args){
        if (!categoryRepository.existsByName("Others")){
            Category others = Category.builder()
                    .name("Others")
                    .description("Default category for unclassified songs")
                    .active(true)
                    .deleted(false)
                    .build();
            categoryRepository.save(others);
            log.info("Initialized default category: Others");
        }
    }
}

package com.konai.kurong.faketee.schedule.repository;

import com.konai.kurong.faketee.schedule.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<Template, Long> {
    Long countTemplateByScheduleTypeId(Long typeId);

}

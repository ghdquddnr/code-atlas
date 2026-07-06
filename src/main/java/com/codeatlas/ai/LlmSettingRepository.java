package com.codeatlas.ai;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LlmSettingRepository extends JpaRepository<LlmSetting, Long> {
}

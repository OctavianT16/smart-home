package com.smartHome.backend.automation;

import com.smartHome.backend.automation.Automation;
import com.smartHome.backend.automation.AutomationTriggerType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AutomationRepository extends JpaRepository<Automation, Long> {

    List<Automation> findByEnabledTrue();

    List<Automation> findByTriggerTypeAndEnabledTrue(AutomationTriggerType triggerType);
}
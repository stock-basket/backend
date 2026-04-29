package com.hanyahunya.stockbasket.domain.user.repository;

import com.hanyahunya.stockbasket.domain.user.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
    UserSetting findByUser_Id(UUID userId);
}

package com.hanyahunya.stockbasket.domain.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID   id,
        String email,
        String nickname,
        String plan,        // e.g. "FREE" | "PRO"
        int    basketCount
) {}

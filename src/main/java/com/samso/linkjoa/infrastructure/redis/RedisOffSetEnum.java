package com.samso.linkjoa.infrastructure.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum RedisOffSetEnum {

    SIGN_UP(300);

    private final long value;
}

package cn.niuma.lingdi000721.startyuanreftain.service.account;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Objects;

/**
 * 生成九位玩家公开 UID。
 *
 * 生成器只负责产生候选值，
 * 最终唯一性由数据库唯一约束保证。
 */
@Component
public final class PlayerUidGenerator {
    public static final long MINIMUM_UID =
            100_000_000L;

    public static final long MAXIMUM_UID =
            999_999_999L;

    private static final int UID_SPACE_SIZE =
            900_000_000;

    private final SecureRandom secureRandom;

    public PlayerUidGenerator()
    {
        this(new SecureRandom());
    }

    /**
     * 包可见构造函数，方便同包测试注入可控随机源。
     * SecureRandom密码学安全随机，比普通Random随机性更强
     */
    PlayerUidGenerator(SecureRandom secureRandom)
    {
        this.secureRandom = Objects.requireNonNull(secureRandom, "secureRandom 不能为空");
    }

    public long generate()
    {
        return MINIMUM_UID + secureRandom.nextInt(UID_SPACE_SIZE);
    }
}

package cn.niuma.lingdi000721.startyuanreftain.common.security;

/**
 * JWT 自定义 Claim 的"常量池"，专门用来消除代码里的魔法字符串
 */
public final class NiumaJwtClaims {
    public static final String PLAYER_UID = "player_uid";

    // 令牌用途：launcher 或 game。
    public static final String TOKEN_USE = "token_use";

    // 游戏令牌所绑定的游戏标识。
    public static final String GAME_ID = "game_id";

    private NiumaJwtClaims()
    {

    }
}

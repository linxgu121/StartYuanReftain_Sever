package cn.niuma.lingdi000721.startyuanreftain.common.security;

/**
 * 令牌用途及其对应的接口权限
 * 两个用途一个用于登录启动器，一个用于游戏服务器连接
 *
 * claimValue 写入 JWT
 * authority 由服务端验证 JWT 后生成，不由客户端自由指定
 */
public enum AccessTokenUse
{
    LAUNCHER("launcher", "TOKEN_USE_LAUNCHER"),
    GAME("game", "TOKEN_USE_GAME");

    private final String claimValue;
    private final String authority;

    AccessTokenUse(String claimValue, String authority)
    {
        this.claimValue = claimValue;
        this.authority = authority;
    }

    public String getClaimValue()
    {
        return claimValue;
    }

    public String getAuthority()
    {
        return authority;
    }

    /**
     * 不认识或缺失的用途必须拒绝，不能默认当作某种令牌
     */
    public static AccessTokenUse fromClaim(Object value) {
        for (AccessTokenUse use : values()) {
            if (use.claimValue.equals(value)) {
                return use;
            }
        }

        throw new IllegalArgumentException("令牌用途缺失或无效");
    }
}

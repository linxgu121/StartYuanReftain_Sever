package cn.niuma.lingdi000721.startyuanreftain.mapper;

import cn.niuma.lingdi000721.startyuanreftain.entity.Account;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface AccountMapper extends BaseMapper<Account> {
    /**
     * 登录认证所需的账号查询。
     *
     * 使用 #{username} 参数绑定，不能拼接 SQL 字符串。
     */
    @Select("""
            SELECT
                id,
                account_uuid AS accountUuid,
                player_uid AS playerUid,
                username,
                password_hash AS passwordHash,
                status,
                created_at AS createdAt,
                updated_at AS updatedAt
            FROM account
            WHERE username = #{username}
            LIMIT 1
            """)
    Account selectByUsername(@Param("username") String username);
}

package cn.niuma.lingdi000721.startyuanreftain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 物品的数量堆叠策略，不表示空间层叠
 */
public enum ItemStackPolicy {
    NON_STACKABLE("NON_STACKABLE"),
    STACKABLE("STACKABLE");

    @EnumValue
    private final String databaseValue;

    ItemStackPolicy(String databaseValue)
    {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue()
    {
        return databaseValue;
    }
}

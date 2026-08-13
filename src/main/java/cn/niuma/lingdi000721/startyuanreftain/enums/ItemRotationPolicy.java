package cn.niuma.lingdi000721.startyuanreftain.enums;


import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 物品在二维仓库中的旋转策略
 */
public enum ItemRotationPolicy {
    FIXED("FIXED"),
    QUARTER_TURNS("QUARTER_TURNS");

    @EnumValue
    private final String databaseValue;

    ItemRotationPolicy(String databaseValue)
    {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue()
    {
        return databaseValue;
    }
}

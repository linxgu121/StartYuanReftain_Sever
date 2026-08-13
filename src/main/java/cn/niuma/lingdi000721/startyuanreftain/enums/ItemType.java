package cn.niuma.lingdi000721.startyuanreftain.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;

/**
 * 服务端权威物品分类
 */
public enum ItemType {
    WEAPON("WEAPON"),
    ARMOR("ARMOR"),
    STORAGE_ITEM("STORAGE_TIME"),
    CONSUMABLE("CONSUMABLE"),
    MISCELLANEOUS("MISCELLANEOUS");

    /**
     * 数据库中保存的稳定字符串。
     */
    @EnumValue
    private final String databaseValue;

    ItemType(String databaseValue)
    {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue()
    {
        return databaseValue;
    }
}

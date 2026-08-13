package cn.niuma.lingdi000721.startyuanreftain.mapper;

import org.apache.ibatis.annotations.Select;

/**
 * 物品目录全局版本的只读查询边界
 */
public interface ItemCatalogMetadataMapper {
    @Select("""
            SELECT catalog_version
            FROM item_catalog_metadata
            WHERE catalog_name = 'MAIN'
            LIMIT 1
            """)
    Integer selectMainCatalogVersion();
}

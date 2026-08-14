package cn.niuma.lingdi000721.startyuanreftain.service.warehouse.spatial;

import cn.niuma.lingdi000721.startyuanreftain.service.item.ItemFootprintCoordinate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
/**
 * 与 UE FNiumaItemFootprintUtility 保持一致的
 * Footprint 规范化与四向旋转工具。
 */
public final class ItemFootprintUtility
{
   private static final Comparator<ItemFootprintCoordinate> CELL_ORDER =
           Comparator
                   .comparingInt(
                           ItemFootprintCoordinate::y)
                   .thenComparingInt(
                           ItemFootprintCoordinate::x);

  private ItemFootprintUtility()
  {
  }

 /**
  * 把 Footprint 的最小 X、最小 Y 平移到零，
  * 并按照“先 Y、后 X”排序。
  */
 public static List<ItemFootprintCoordinate> normalize(
         List<ItemFootprintCoordinate> footprint)
 {
  Objects.requireNonNull(
          footprint,
          "footprint 不能为空");

  if (footprint.isEmpty())
  {
   throw new IllegalArgumentException(
           "物品至少需要一个占用格");
  }

  Set<ItemFootprintCoordinate> uniqueCells =
          new HashSet<>();

  int minimumX = Integer.MAX_VALUE;
  int minimumY = Integer.MAX_VALUE;

  for (ItemFootprintCoordinate cell : footprint)
  {
   if (cell == null)
   {
    throw new IllegalArgumentException(
            "Footprint 不能包含 null 坐标");
   }

   if (!uniqueCells.add(cell))
   {
    throw new IllegalArgumentException(
            "Footprint 不能包含重复坐标");
   }

   minimumX = Math.min(
           minimumX,
           cell.x());

   minimumY = Math.min(
           minimumY,
           cell.y());
  }

  List<ItemFootprintCoordinate> normalizedCells =
          new ArrayList<>(footprint.size());

  for (ItemFootprintCoordinate cell : footprint)
  {
   normalizedCells.add(
           new ItemFootprintCoordinate(
                   cell.x() - minimumX,
                   cell.y() - minimumY));
  }

  normalizedCells.sort(CELL_ORDER);

  return List.copyOf(normalizedCells);
 }

 /**
  * 先规范化原始 Footprint，再生成指定方向的规范化结果。
  *
  * orientationDegrees 使用协议角度：
  * 0、90、180、270，而不是 UE 枚举底层序号 0、1、2、3。
  */
 public static List<ItemFootprintCoordinate> rotate(
         List<ItemFootprintCoordinate> footprint,
         int orientationDegrees)
 {
  List<ItemFootprintCoordinate> normalizedCells =
          normalize(footprint);

  int maximumX = 0;
  int maximumY = 0;

  for (ItemFootprintCoordinate cell : normalizedCells)
  {
   maximumX = Math.max(
           maximumX,
           cell.x());

   maximumY = Math.max(
           maximumY,
           cell.y());
  }

  List<ItemFootprintCoordinate> rotatedCells =
          new ArrayList<>(normalizedCells.size());

  for (ItemFootprintCoordinate cell : normalizedCells)
  {
   ItemFootprintCoordinate rotatedCell =
           rotateCell(
                   cell,
                   maximumX,
                   maximumY,
                   orientationDegrees);

   rotatedCells.add(rotatedCell);
  }

  rotatedCells.sort(CELL_ORDER);

  return List.copyOf(rotatedCells);
 }

 private static ItemFootprintCoordinate rotateCell(
         ItemFootprintCoordinate cell,
         int maximumX,
         int maximumY,
         int orientationDegrees)
 {
  return switch (orientationDegrees)
  {
   case 0 ->
           new ItemFootprintCoordinate(
                   cell.x(),
                   cell.y());

   case 90 ->
           new ItemFootprintCoordinate(
                   maximumY - cell.y(),
                   cell.x());

   case 180 ->
           new ItemFootprintCoordinate(
                   maximumX - cell.x(),
                   maximumY - cell.y());

   case 270 ->
           new ItemFootprintCoordinate(
                   cell.y(),
                   maximumX - cell.x());

   default ->
           throw new IllegalArgumentException(
                   "orientationDegrees 必须是" + " 0、90、180 或 270");
  };
 }

}

package com.fireblaze.magic_overhaul.client.screen.utils;

public class ScreenController {

    private final int parentLeft;
    private final int parentTop;
    private final int parentWidth;
    private final int parentHeight;

    private final int listWidth;
    private final int rowHeight;

    private ScreenSide side;

    public ScreenController(
            int parentLeft,
            int parentTop,
            int parentWidth,
            int parentHeight,
            int rowHeight,
            int listWidth,
            ScreenSide side
    ) {
        this.parentLeft = parentLeft;
        this.parentTop = parentTop;
        this.parentWidth = parentWidth;
        this.parentHeight = parentHeight;
        this.rowHeight = rowHeight;
        this.listWidth = listWidth;
        this.side = side;
    }

    public ScreenSide getSide() {
        return side;
    }

    public void setSide(ScreenSide side) {
        this.side = side;
    }

    /**
     * Gibt die X-Position der Liste zurück, abhängig von LEFT oder RIGHT
     */
    public int getListX() {
        return switch (side) {
            case LEFT -> parentLeft - listWidth - 10;
            case RIGHT -> parentLeft + parentWidth + 10;
            case TOP -> parentLeft + (parentWidth - listWidth) / 2; // zentriert
        };
    }
    public int getListY() {
        return switch (side) {
            case LEFT, RIGHT -> parentTop + 4;
            case TOP -> parentTop - 50; // 50 als Margin über der GUI, anpassen nach Bedarf
        };
    }
    public int getListHeight() {
        return side == ScreenSide.TOP ? 40 : parentHeight;
    }


    public int getVisibleRows() {
        return parentHeight / rowHeight;
    }

    public int getListWidth() {
        return listWidth;
    }

    public int getRowHeight() {
        return rowHeight;
    }
}

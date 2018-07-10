package com.wxt.library.tree.pagetree;

import com.wxt.library.sql.model.DBVO;

public abstract class PageTreeNode extends DBVO {

    public abstract String getId();

    public abstract void setId(String id);

    public abstract String getPId();

    public abstract void setPId(String pId);

    /**
     * 是否展开标识
     */
    private boolean isExpand = false;

    boolean isExpand() {
        return isExpand;
    }

    /**
     * 设置展开
     *
     * @param isExpand
     */
    void setExpand(boolean isExpand) {
        this.isExpand = isExpand;
    }

}
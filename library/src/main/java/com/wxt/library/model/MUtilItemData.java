package com.wxt.library.model;

import com.wxt.library.sql.model.DBVO;

public class MUtilItemData extends DBVO {

        private int leftDrawableId;
        private String title;
        private String subTitle;
        private String descricption;
        private int rightDrawableId;

        public int getLeftDrawableId() {
            return leftDrawableId;
        }

        public void setLeftDrawableId(int leftDrawableId) {
            this.leftDrawableId = leftDrawableId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getSubTitle() {
            return subTitle;
        }

        public void setSubTitle(String subTitle) {
            this.subTitle = subTitle;
        }

        public String getDescricption() {
            return descricption;
        }

        public void setDescricption(String descricption) {
            this.descricption = descricption;
        }

        public int getRightDrawableId() {
            return rightDrawableId;
        }

        public void setRightDrawableId(int rightDrawableId) {
            this.rightDrawableId = rightDrawableId;
        }
    }
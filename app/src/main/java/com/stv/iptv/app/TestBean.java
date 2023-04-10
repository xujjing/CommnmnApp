package com.stv.iptv.app;

import java.util.HashMap;
import java.util.Map;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2021/10/14
 * 描    述:
 * 修订历史：
 * ================================================
 */
public class TestBean {

    private int id;

    private Map<String,ContentBean> content;

    public static TestBean createBean(int id){
        TestBean testBean = new TestBean();
        testBean.id = id;
        testBean.content = new HashMap<>();
        testBean.content.put("en", new ContentBean("en_title", "en_description"));
        testBean.content.put("pt", new ContentBean("pt_title", "pt_description"));
        return testBean;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Map<String, ContentBean> getContent() {
        return content;
    }

    public void setContent(Map<String, ContentBean> content) {
        this.content = content;
    }

    public static class ContentBean{

        public String title;
        public String description;

        public ContentBean() {
        }

        public ContentBean(String title, String description) {
            this.title = title;
            this.description = description;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

}

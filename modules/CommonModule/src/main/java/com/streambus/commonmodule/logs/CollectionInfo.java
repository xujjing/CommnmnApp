package com.streambus.commonmodule.logs;

import java.io.Serializable;

public class CollectionInfo implements Serializable {

    public static final int COLLECTION_TYPE_LOG = 0;
    public static final int COLLECTION_TYPE_CRASH = 1;

    private int collectionType;

    public CollectionInfo(int collectionType) {
        this.collectionType = collectionType;
    }

    private String exception_desc = "";


    private String code;
    private String mac;
    private String model;
    private String hardware;
    private String screen;
    private String os_version;
    private String sdk_number;
    private String ram_memroy;
    private String vm_size;
    private String app_package;
    private String app_version;
    private String app_patch_Version;
    private String Id;
    private String validity;
    private String log_time;
    private String exception_type;
    private String crash_dump;
    private String user_name;//用户名
    private String user_contact;// 联系方式

    public int getCollectionType() {
        return collectionType;
    }

    public void setCollectionType(int collectionType) {
        this.collectionType = collectionType;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getHardware() {
        return hardware;
    }

    public void setHardware(String hardware) {
        this.hardware = hardware;
    }

    public String getScreen() {
        return screen;
    }

    public void setScreen(String screen) {
        this.screen = screen;
    }

    public String getOs_version() {
        return os_version;
    }

    public void setOs_version(String os_version) {
        this.os_version = os_version;
    }

    public String getSdk_number() {
        return sdk_number;
    }

    public void setSdk_number(String sdk_number) {
        this.sdk_number = sdk_number;
    }

    public String getRam_memroy() {
        return ram_memroy;
    }

    public void setRam_memroy(String ram_memroy) {
        this.ram_memroy = ram_memroy;
    }

    public String getVm_size() {
        return vm_size;
    }

    public void setVm_size(String vm_size) {
        this.vm_size = vm_size;
    }

    public String getApp_package() {
        return app_package;
    }

    public void setApp_package(String app_package) {
        this.app_package = app_package;
    }

    public String getException_desc() {
        return exception_desc;
    }

    public void setException_desc(String exception_desc) {
        this.exception_desc = exception_desc;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getUser_contact() {
        return user_contact;
    }

    public void setUser_contact(String user_contact) {
        this.user_contact = user_contact;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getApp_patch_Version() {
        return app_patch_Version;
    }

    public void setApp_patch_Version(String app_patch_Version) {
        this.app_patch_Version = app_patch_Version;
    }

    public String getId() {
        return Id;
    }

    public void setId(String id) {
        Id = id;
    }

    public String getValidity() {
        return validity;
    }

    public void setValidity(String validity) {
        this.validity = validity;
    }

    public String getLog_time() {
        return log_time;
    }

    public void setLog_time(String log_time) {
        this.log_time = log_time;
    }

    public String getException_type() {
        return exception_type;
    }

    public void setException_type(String exception_type) {
        this.exception_type = exception_type;
    }

    public String getCrash_dump() {
        return crash_dump;
    }

    public void setCrash_dump(String crash_dump) {
        this.crash_dump = crash_dump;
    }
}

package com.example.pushin_v5.dataModel;
/***********************************************************************
 This file is the data model used for getting the name of materials which
 the materials are the exam items that admin user creates for users.
 ***********************************************************************/
public class DataMaterials {
    public int moduleId;
    public String moduleName;

    public DataMaterials() {
    }

    public int getModuleId() {
        return moduleId;
    }

    public void setModuleId(int moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public DataMaterials(int moduleId, String moduleName) {
        this.moduleId = moduleId;
        this.moduleName = moduleName;
    }
}

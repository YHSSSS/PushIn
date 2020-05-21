package com.example.pushin_v5.dataModel;
/***********************************************************************
 This file is the data model used for item of exam table. The variables
 is created according to the columns of the table. There are setter
 functions used for other java classes.
 ***********************************************************************/

import java.util.Date;

public class DataExam {
    private int moduleId;
    private int userId;
    private String moduleName;
    private Date moduleExamDate;
    private String moduleExamTime;

    public DataExam() {
    }

    public int getModuleId() {
        return moduleId;
    }

    public int getUserId() {
        return userId;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Date getModuleExamDate() {
        return moduleExamDate;
    }

    public String getModuleExamTime() {
        return moduleExamTime;
    }

    public DataExam(int moduleId, int userId, String moduleName, Date date, String time) {
        this.moduleId = moduleId;
        this.userId = userId;
        this.moduleName = moduleName;
        this.moduleExamDate = date;
        this.moduleExamTime = time;
    }
}

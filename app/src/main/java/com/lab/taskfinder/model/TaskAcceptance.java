package com.lab.taskfinder.model;

import java.io.Serializable;

public class TaskAcceptance implements Serializable {
    private String taskAcceptanceId;
    private String taskId;
    private String acceptance; // ชื่อผู้ใช้ที่รับงาน
    private String accetpAt;   // เวลาที่รับงาน
    private String status;     // สถานะ (in progress, completed, cancelled)
    private String completionNote; // บันทึกเพิ่มเติมเมื่อส่งงาน
    private String completedAt;    // เวลาที่ส่งงานสำเร็จ

    // จำเป็นต้องมี constructor ว่างเปล่าสำหรับ Firebase
    public TaskAcceptance() {
    }

    public TaskAcceptance(String taskAcceptanceId, String taskId, String acceptance, String accetpAt, String status) {
        this.taskAcceptanceId = taskAcceptanceId;
        this.taskId = taskId;
        this.acceptance = acceptance;
        this.accetpAt = accetpAt;
        this.status = status;
    }

    // Getters และ Setters
    public String getTaskAcceptanceId() {
        return taskAcceptanceId;
    }

    public void setTaskAcceptanceId(String taskAcceptanceId) {
        this.taskAcceptanceId = taskAcceptanceId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getAcceptance() {
        return acceptance;
    }

    public void setAcceptance(String acceptance) {
        this.acceptance = acceptance;
    }

    public String getAccetpAt() {
        return accetpAt;
    }

    public void setAccetpAt(String accetpAt) {
        this.accetpAt = accetpAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCompletionNote() {
        return completionNote;
    }

    public void setCompletionNote(String completionNote) {
        this.completionNote = completionNote;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }
}
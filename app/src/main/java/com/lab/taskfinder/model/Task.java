package com.lab.taskfinder.model;

import java.io.Serializable;

public class Task implements Serializable {
    private String taskId;
    private String taskTitle;
    private String taskDescription;
    private String taskAddress;
    private float taskPrice;
    private String taskStatus;
    private String taskClient;
    private String taskCreatedAt;

    public Task() {
    }

    public Task(String taskId, String taskTitle, String taskDescription, String taskAddress, float taskPrice, String taskStatus, String taskClient, String taskCreatedAt) {
        this.taskId = taskId;
        this.taskTitle = taskTitle;
        this.taskDescription = taskDescription;
        this.taskAddress = taskAddress;
        this.taskPrice = taskPrice;
        this.taskStatus = taskStatus;
        this.taskClient = taskClient;
        this.taskCreatedAt = taskCreatedAt;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskTitle() {
        return taskTitle;
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle = taskTitle;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getTaskAddress() {
        return taskAddress;
    }

    public void setTaskAddress(String taskAddress) {
        this.taskAddress = taskAddress;
    }

    public float getTaskPrice() {
        return taskPrice;
    }

    public void setTaskPrice(float taskPrice) {
        this.taskPrice = taskPrice;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTaskClient() {
        return taskClient;
    }

    public void setTaskClient(String taskClient) {
        this.taskClient = taskClient;
    }

    public String getTaskCreatedAt() {
        return taskCreatedAt;
    }

    public void setTaskCreatedAt(String taskCreatedAt) {
        this.taskCreatedAt = taskCreatedAt;
    }
}

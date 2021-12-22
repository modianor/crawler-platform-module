package com.example.crawler.entity;


public class Task {
    /**
     * self.task_id = task_id
     * self.parent_task_id = parent_task_id
     * self.spider_name = spider_name
     * self.task_type = task_type
     * self.url = url
     * self.param1 = param1
     * self.param2 = param2
     * self.param3 = param3
     * self.task_status = task_status
     * self.filter = filter
     * self.exception = exception
     * self.data = data
     * self.kibanalog = kibanalog
     * self.request = Request(request) if request else None
     * self.response = None
     */
    public Long task_id;
    public Long parent_task_id;
    public String spider_name;
    public String task_type;
    public String url;
    public String param1;
    public String param2;
    public String param3;
    public Integer task_status;
    public Boolean filter;
    public String exception;
    public String data;
    public String kibanalog;
    public String request = "";

    public Task() {
    }

    public Long getTask_id() {
        return task_id;
    }

    public void setTask_id(Long task_id) {
        this.task_id = task_id;
    }

    public Long getParent_task_id() {
        return parent_task_id;
    }

    public void setParent_task_id(Long parent_task_id) {
        this.parent_task_id = parent_task_id;
    }

    public String getSpider_name() {
        return spider_name;
    }

    public void setSpider_name(String spider_name) {
        this.spider_name = spider_name;
    }

    public String getTask_type() {
        return task_type;
    }

    public void setTask_type(String task_type) {
        this.task_type = task_type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParam1() {
        return param1;
    }

    public void setParam1(String param1) {
        this.param1 = param1;
    }

    public String getParam2() {
        return param2;
    }

    public void setParam2(String param2) {
        this.param2 = param2;
    }

    public String getParam3() {
        return param3;
    }

    public void setParam3(String param3) {
        this.param3 = param3;
    }

    public Integer getTask_status() {
        return task_status;
    }

    public void setTask_status(Integer task_status) {
        this.task_status = task_status;
    }

    public Boolean getFilter() {
        return filter;
    }

    public void setFilter(Boolean filter) {
        this.filter = filter;
    }

    public String getException() {
        return exception;
    }

    public void setException(String exception) {
        this.exception = exception;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getKibanalog() {
        return kibanalog;
    }

    public void setKibanalog(String kibanalog) {
        this.kibanalog = kibanalog;
    }

    public String getRequest() {
        return request;
    }

    public void setRequest(String request) {
        this.request = request;
    }


    @Override
    public String toString() {
        return "Task{" +
                "task_id=" + task_id +
                ", parent_task_id=" + parent_task_id +
                ", spider_name='" + spider_name + '\'' +
                ", task_type='" + task_type + '\'' +
                ", url='" + url + '\'' +
                ", param1='" + param1 + '\'' +
                ", param2='" + param2 + '\'' +
                ", param3='" + param3 + '\'' +
                ", task_status=" + task_status +
                ", filter=" + filter +
                ", exception='" + exception + '\'' +
                ", data='" + data + '\'' +
                ", kibanalog='" + kibanalog + '\'' +
                ", request='" + request + '\'' +
                '}';
    }
}

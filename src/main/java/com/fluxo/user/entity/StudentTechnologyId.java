package com.fluxo.user.entity;

import java.io.Serializable;
import java.util.Objects;

public class StudentTechnologyId implements Serializable {
    private Integer studentUser;
    private Integer technology;

    public StudentTechnologyId() {
    }

    public StudentTechnologyId(Integer studentUser, Integer technology) {
        this.studentUser = studentUser;
        this.technology = technology;
    }

    public Integer getStudentUser() {
        return studentUser;
    }

    public void setStudentUser(Integer studentUser) {
        this.studentUser = studentUser;
    }

    public Integer getTechnology() {
        return technology;
    }

    public void setTechnology(Integer technology) {
        this.technology = technology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StudentTechnologyId that = (StudentTechnologyId) o;
        return Objects.equals(studentUser, that.studentUser) && Objects.equals(technology, that.technology);
    }

    @Override
    public int hashCode() {
        return Objects.hash(studentUser, technology);
    }
}

package com.moon.prize.commons.db.entity;

import java.io.Serializable;
import java.util.Date;

public class SysRoleBtn implements Serializable {
    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_role_btn.id
     *
     * @mbg.generated
     */
    private Integer id;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_role_btn.role_id
     *
     * @mbg.generated
     */
    private Integer roleId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_role_btn.btn_id
     *
     * @mbg.generated
     */
    private Integer btnId;

    /**
     *
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database column sys_role_btn.create_time
     *
     * @mbg.generated
     */
    private Date createTime;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table sys_role_btn
     *
     * @mbg.generated
     */
    private static final long serialVersionUID = 1L;

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_role_btn.id
     *
     * @return the value of sys_role_btn.id
     *
     * @mbg.generated
     */
    public Integer getId() {
        return id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_role_btn.id
     *
     * @param id the value for sys_role_btn.id
     *
     * @mbg.generated
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_role_btn.role_id
     *
     * @return the value of sys_role_btn.role_id
     *
     * @mbg.generated
     */
    public Integer getRoleId() {
        return roleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_role_btn.role_id
     *
     * @param roleId the value for sys_role_btn.role_id
     *
     * @mbg.generated
     */
    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_role_btn.btn_id
     *
     * @return the value of sys_role_btn.btn_id
     *
     * @mbg.generated
     */
    public Integer getBtnId() {
        return btnId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_role_btn.btn_id
     *
     * @param btnId the value for sys_role_btn.btn_id
     *
     * @mbg.generated
     */
    public void setBtnId(Integer btnId) {
        this.btnId = btnId;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method returns the value of the database column sys_role_btn.create_time
     *
     * @return the value of sys_role_btn.create_time
     *
     * @mbg.generated
     */
    public Date getCreateTime() {
        return createTime;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method sets the value of the database column sys_role_btn.create_time
     *
     * @param createTime the value for sys_role_btn.create_time
     *
     * @mbg.generated
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
}
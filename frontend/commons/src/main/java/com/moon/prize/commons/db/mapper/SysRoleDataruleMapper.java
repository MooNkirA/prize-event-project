package com.moon.prize.commons.db.mapper;

import com.moon.prize.commons.db.entity.SysRoleDatarule;
import com.moon.prize.commons.db.entity.SysRoleDataruleExample;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface SysRoleDataruleMapper {
    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    long countByExample(SysRoleDataruleExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    int deleteByExample(SysRoleDataruleExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    @Delete({
        "delete from sys_role_datarule",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int deleteByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    @Insert({
        "insert into sys_role_datarule (role_id, datarule_id, ",
        "create_time)",
        "values (#{roleId,jdbcType=INTEGER}, #{dataruleId,jdbcType=INTEGER}, ",
        "#{createTime,jdbcType=TIMESTAMP})"
    })
    @SelectKey(statement="SELECT LAST_INSERT_ID()", keyProperty="id", before=false, resultType=Integer.class)
    int insert(SysRoleDatarule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    int insertSelective(SysRoleDatarule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    List<SysRoleDatarule> selectByExample(SysRoleDataruleExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    @Select({
        "select",
        "id, role_id, datarule_id, create_time",
        "from sys_role_datarule",
        "where id = #{id,jdbcType=INTEGER}"
    })
    @ResultMap("com.moon.prize.commons.db.mapper.SysRoleDataruleMapper.BaseResultMap")
    SysRoleDatarule selectByPrimaryKey(Integer id);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    int updateByExampleSelective(@Param("record") SysRoleDatarule record, @Param("example") SysRoleDataruleExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    int updateByExample(@Param("record") SysRoleDatarule record, @Param("example") SysRoleDataruleExample example);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    int updateByPrimaryKeySelective(SysRoleDatarule record);

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table sys_role_datarule
     *
     * @mbg.generated
     */
    @Update({
        "update sys_role_datarule",
        "set role_id = #{roleId,jdbcType=INTEGER},",
          "datarule_id = #{dataruleId,jdbcType=INTEGER},",
          "create_time = #{createTime,jdbcType=TIMESTAMP}",
        "where id = #{id,jdbcType=INTEGER}"
    })
    int updateByPrimaryKey(SysRoleDatarule record);
}
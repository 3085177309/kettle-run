package com.bkht.system.entity;

import com.bkht.core.jpa.entity.SimpleBaseEntity;
import com.bkht.system.utils.Sex;
import com.bkht.system.utils.Status;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.util.HashSet;
import java.util.Set;

/**
 * 系统用户信息实体类
 *
 * @author sofar
 */
@Entity
@Table(name = "SYS_USER")
@JsonIgnoreProperties(value = {"roles"})
@NamedEntityGraph(name = "User.detail",
        attributeNodes = @NamedAttributeNode("group"))
public class User extends SimpleBaseEntity<Long> {

    /**
     * 超级管理员用户登录名。
     */
    public static final String ADMIN = "admin";

    /**
     * 用户默认密码,当新添加或重置用户时使用该密码。
     */
    public static final String DEFAULT_PASSWORD = "666666";

    /**
     * 登录名
     */

    private String loginName;

    /**
     * 姓名
     */
    private String name;

    /**
     * 登录密码
     */
    private String password;

    private Sex sex;

    private String email;

    private String mobile;

    private String tel;

    /**
     * 用户状态
     */
    private Status status;


    private String salt;

    /**
     * 用户角色组
     */
    private Set<Role> roles = new HashSet<>();

    private Group group;

    public User() {
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Sex getSex() {
        return sex;
    }

    public void setSex(Sex sex) {
        this.sex = sex;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Enumerated(EnumType.ORDINAL)
    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Column(unique = true)
    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    @ManyToMany
    @JoinTable(
            name = "SYS_USER_ROLE"
            , joinColumns = {
            @JoinColumn(name = "USER_ID")
    }
            , inverseJoinColumns = {
            @JoinColumn(name = "ROLE_ID")
    }
    )
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id")
    public Group getGroup() {
        return group;
    }

    public void setGroup(Group group) {
        this.group = group;
    }

    /**
     * 对用户增加角色
     *
     * @param role 角色对象
     * @return 当前用户所有角色
     */
    public Set<Role> addRole(Role role) {
        this.roles.add(role);
        return this.roles;
    }

    /**
     * 删除用户角色
     *
     * @param role 角色对象
     * @return 当前用户所有角色
     */
    public Set<Role> removeRole(Role role) {
        this.roles.remove(role);
        return this.roles;
    }

    /**
     * 获取用户所有资源权限，包括用户角色所有权限和用户所在机构所有权限
     *
     * @return 用户当前所有资源权限
     */
    @Transient
    public Set<String> getAllPermission() {
        Set<String> permissions = new HashSet<String>();
        for (Role role : this.getRoles()) {
            //增加用户角色下所有资源权限
            permissions.addAll(role.getAllPermissons());
        }
        Group group = this.getGroup();
        if (group != null) {
            for (Role role : group.getRoles()) {
                //增加用户所在机构下所有资源权限
                permissions.addAll(role.getAllPermissons());
            }
        }
        return permissions;
    }

    /**
     * 获取用户所有角色名称 以逗号隔开
     *
     * @return 角色名称
     */
    @Transient
    public String getRoleNames() {
        String roleName = "";
        if (roles != null) {
            for (Role role : roles) {
                roleName += role.getCode() + ",";
            }
        }
        return roleName;
    }

    /**
     * 判断用户是否为超级管理员 如果loginName为"admin"则为超级管理员
     *
     * @return true/false
     */
    @Transient
    public boolean isSuperAdmin() {
        return ADMIN.equals(this.getLoginName());
    }

    @Override
    public String toString() {
        String email = this.email != null ? this.email : "";
        return "User [loginName=" + loginName + ",name=" + name + ",sex=" + sex.getSexName() + ",email=" + email + ",status=" + status.getStatusName() + "]";
    }

}
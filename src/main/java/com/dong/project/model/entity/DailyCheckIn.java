package com.dong.project.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 每日签到表
 * @TableName daily_check_in
 */
@TableName(value ="daily_check_in")
@Data
public class DailyCheckIn implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 签到人
     */
    private Long userId;

    /**
     * 描述
     */
    private String description;

    /**
     * 签到增加积分个数
     */
    private Integer addPoints;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除(0-未删,1-删除)
     */
    @TableLogic
    private Integer isDeleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
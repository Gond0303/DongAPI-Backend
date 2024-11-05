package com.dong.project.model.vo;

import lombok.Data;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import java.io.Serializable;
import java.util.List;

@Data
public class OrderVo implements Serializable {
    private static final long serialVersionUID = 1025634495698488400L;
    private List<ProductOrderVo> records;
    private long total;
    private long size;
    private long current;
    private List<OrderItem> orders;
    private boolean optimizeCountSql;
    private boolean searchCount;
    private boolean optimizeJoinOfCountSql;
    private String countId;
    private Long maxLimit;
}
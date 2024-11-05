package com.dong.project.model.dto.interfaceinfo;

import com.dong.project.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 搜索框搜索接口信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class InterfaceInfoSearchTextRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = -7761967292923943503L;

    /**
     * 搜索信息
     */
    private String searchText;
}

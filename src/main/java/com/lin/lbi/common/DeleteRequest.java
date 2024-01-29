package com.lin.lbi.common;

import java.io.Serializable;
import lombok.Data;

/**
 * 删除请求
 *
 * @author L
 *
 */
@Data
public class DeleteRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    private static final long serialVersionUID = 1L;
}
package com.vn.go_toeic.dto.req;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailReq implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private String to;
    private String subject;
    private String templateName;
    private Map<String, Object> variables;
}

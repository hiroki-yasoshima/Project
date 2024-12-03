package com.example.demo.form;

import java.sql.Time;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RequestForm {
	
	@NotNull(message = "{start.null.error}")
    private String startDate;

    @NotNull(message = "{end.null.error}")
    private String endDate;

    @NotNull(message = "{count.null.error}")
    @Max(value = 31, message = "{conut.error}")
    private Double paidCount;

    @NotEmpty(message = "{reason.null.error}")
    @Size(max = 255, message = "{length.error}")
    private String reason;
    
    private Time updateday;
    
    private Integer updateid;
}

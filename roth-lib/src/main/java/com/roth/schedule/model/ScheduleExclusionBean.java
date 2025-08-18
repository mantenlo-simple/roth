package com.roth.schedule.model;

import java.io.Serializable;
import java.time.LocalDate;

import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;


@JdbcTable(name = "schedule_exclusion",
           primaryKeyColumns = {"schedule_exclusion_id"})
@PermissiveBinding()
public class ScheduleExclusionBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private LocalDate endDate;
    private Integer retryLimit;
    private Long scheduleExclusionId;
    private Long scheduleId;
    private LocalDate startDate;

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    public Integer getRetryLimit() { return retryLimit; }
	public void setRetryLimit(Integer retryLimit) { this.retryLimit = retryLimit; }
	
	public Long getScheduleExclusionId() { return scheduleExclusionId; }
    public void setScheduleExclusionId(Long scheduleExclusionId) { this.scheduleExclusionId = scheduleExclusionId; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    @Override
    public boolean isNew() {
        boolean result = scheduleExclusionId == null;
        // TODO: Add update code to run every time.
        // Example:
        //   updatedDts = new Date();
        if (result) {
            // TODO: Add initialization code to run when new.
            // Example:
            //   createdBy = updatedBy;
            //   createdDts = updatedDts;
        }
        return result;
    }
}
package com.roth.schedule.model;

import com.roth.base.util.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "schedule",
           primaryKeyColumns = {"schedule_id"})
@PermissiveBinding()
public class ScheduleBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    /** Command Types **/
    public static final String COMMAND_TYPE_SHELL = "S";
    public static final String COMMAND_TYPE_JAVA = "J"; // e.g., com.package.ExampleClass.methodName(1, "Two", 3.0)
    public static final String COMMAND_TYPE_SQL = "Q";  // SELECT/INSERT/UPDATE/DELETE or CALL PROCEDURE
    
    /** Statuses **/
    public static final String STATUS_ACTIVE = "A";   // Allow processing of the schedule.
    public static final String STATUS_INACTIVE = "I"; // Disallow processing of the schedule.
    
    /** Catch-up Methods **/
    public static final String CATCHUP_SKIP = "S"; // Skip all past date-times, and set the next event to the next future iteration.
    public static final String CATCHUP_ALL = "A";  // Set next event to the next date-time iteration, even if it's in the past.
    
    /** Interval Types **/
    public static final String INTERVAL_TYPE_ONE_TIME = "-";
    public static final String INTERVAL_TYPE_MINUTE = "m";
    public static final String INTERVAL_TYPE_HOUR = "h";
    public static final String INTERVAL_TYPE_DAY = "D";
    public static final String INTERVAL_TYPE_MONTH = "M";
     
    /*
    
    	If parentScheduleId is null, then it is not dependent upon another scheduled item,
    	so the startDts means something.
    	
    	If parentScheduleId is NOT null, then it is dependent upon another scheduled item,
    	so the startDts means nothing; it should be set to that of its parent.
    	
    	Question: Should any item in a dependency tree be calculated for the next run before
    	the whole tree is processed (successfully or otherwise), or should the whole tree be 
    	processed first?
    
     */
    
    private String catchupMethod;
    private String command;
    private String commandType;
    private String description;
    private LocalDateTime endDts;
    private Integer intervalCount;
    private String intervalType;
    private Long parentScheduleId;
    private Integer retries;
    private String rules;
    private Long scheduleId;
    private LocalDateTime startDts;
    private String status;
    
    public String getCatchupMethod() { return catchupMethod; }
   	public void setCatchupMethod(String catchupMethod) { this.catchupMethod = catchupMethod; }
   	
   	public String getCommand() { return command; }
    public void setCommand(String command) { this.command = command; }

    public String getCommandType() { return commandType; }
    public void setCommandType(String commandType) { this.commandType = commandType; }
	
	public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getEndDts() { return endDts; }
	public void setEndDts(LocalDateTime endDts) { this.endDts = endDts; }
	
	public Integer getIntervalCount() { return intervalCount; }
	public void setIntervalCount(Integer intervalCount) { this.intervalCount = intervalCount; }
	
	public String getIntervalType() { return intervalType; }
	public void setIntervalType(String intervalType) { this.intervalType = intervalType; }
	
	public Long getParentScheduleId() { return parentScheduleId; }
    public void setParentScheduleId(Long parentScheduleId) { this.parentScheduleId = parentScheduleId; }

    public Integer getRetries() { return retries; }
	public void setRetries(Integer retries) { this.retries = retries; }
	
	public String getRules() { return rules; }
	public void setRules(String rules) { this.rules = rules; }
	
	public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

	public LocalDateTime getStartDts() { return startDts; }
	public void setStartDts(LocalDateTime startDts) { this.startDts = startDts; }
	
	public String getStatus() { return status; }
    public void setStatus(String status) { this.status = Data.trim(status); }
    
    
    @Override
    public boolean isNew() {
        boolean result = scheduleId == null;
        if (result) {
            // Add initialization code to run when new.
        }
        return result;
    }
}
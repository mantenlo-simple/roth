package com.roth.schedule.model;

import com.roth.base.util.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "schedule_event",
           primaryKeyColumns = {"schedule_event_id"})
@PermissiveBinding()
public class ScheduleEventBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    /** Statuses **/
    public static final String STATUS_PENDING = "P";
    public static final String STATUS_RUNNING = "R";
    public static final String STATUS_FINISHED = "F";
    public static final String STATUS_ERROR = "E";
    public static final String STATUS_CANCELED = "C";
    public static final String STATUS_SUSPENDED = "S"; // Only a pending or canceled event can be sustpended.
    
    private LocalDateTime endDts;
    private String errOut;
    private LocalDateTime eventDts;
    private Integer retries;
    private Integer ruleIndex;
    private Long scheduleEventId;
    private Long scheduleId;
    private LocalDateTime startDts;
    private String status;
    private String stdOut;
    private LocalDateTime submitDts;
    
    /**
     * The date-time that execution of the scheduled event ended, either through finishing, being canceled, or resulting in error.
     * @return
     */
    public LocalDateTime getEndDts() { return endDts; }
    public void setEndDts(LocalDateTime endDts) { this.endDts = endDts; }

    /**
     * Output from standard error.
     * @return
     */
    public String getErrOut() { return errOut; }
	public void setErrOut(String errOut) { this.errOut = errOut; }
	
	/**
	 * The date-time that execution of the schedule event is planned for.
	 * @return
	 */
	public LocalDateTime getEventDts() { return eventDts; }
	public void setEventDts(LocalDateTime eventDts) { this.eventDts = eventDts; }
	
	/**
	 * The number of times attempts were made to successfully execute the event.
	 * Attempts to execute the event cease when the last status is not in (P, E)
	 * or the number of attempts has exceeded the limit set in the schedule. 
	 * @return
	 */
	public Integer getRetries() { return retries; }
	public void setRetries(Integer retries) { this.retries = retries; }
	
	/**
	 * The index of the rule that was used to calculate the submitDts.
	 * @return
	 */
	public Integer getRuleIndex() { return ruleIndex; }
	public void setRuleIndex(Integer ruleIndex) { this.ruleIndex = ruleIndex; }
	
	public ScheduleEventBean() {}
    public ScheduleEventBean(Long scheduleId) { this.scheduleId = scheduleId; }
    
    public Long getScheduleEventId() { return scheduleEventId; }
    public void setScheduleEventId(Long scheduleEventId) { this.scheduleEventId = scheduleEventId; }

    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }

    /**
     * The date-time that execution of the scheduled event started.
     * @return
     */
    public LocalDateTime getStartDts() { return startDts; }
    public void setStartDts(LocalDateTime startDts) { this.startDts = startDts; }

    /**
     * Values:<br/>
     * <ul>
     * <li>P = Pending</li>
     * <li>R = Running</li>
     * <li>F = Finished</li>
     * <li>E = Error</li>
     * <li>C = Canceled</li>
     * <li>S = Suspended</li>
     * </ul>
     * Note: Only an event with status P or C can be suspended.
     * @return
     */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = Data.trim(status); }

    /**
     * Output from standard out.
     * @return
     */
    public String getStdOut() { return stdOut; }
	public void setStdOut(String stdOut) { this.stdOut = stdOut; }
	
	/**
	 * The date-time that the event was submitted to the system.
	 * @return
	 */
	public LocalDateTime getSubmitDts() { return submitDts; }
	public void setSubmitDts(LocalDateTime submitDts) { this.submitDts = submitDts; }
	
    @Override
    public boolean isNew() {
        boolean result = scheduleEventId == null;
        if (result) {
            status = STATUS_PENDING;
            submitDts = LocalDateTime.now();
        }
        return result;
    }
}
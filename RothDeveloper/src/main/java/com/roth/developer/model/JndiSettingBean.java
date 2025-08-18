package com.roth.developer.model;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.roth.base.util.Data;
import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;

@JdbcTable(name = "jndi_setting",
           primaryKeyColumns = {"jndi_setting_id"})
@PermissiveBinding()
public class JndiSettingBean implements Serializable, StateBean, Comparable<JndiSettingBean> {
    private static final long serialVersionUID = 1L;

    private String available;
    private String jndiName;
    private Long jndiSettingId;
    private String readonly;
    private String updatedBy;
    private LocalDateTime updatedDts;
    
    private String databaseName;

    public String getAvailable() { return available; }
    public void setAvailable(String available) { this.available = Data.trim(available); }

    public String getJndiName() { return jndiName; }
    public void setJndiName(String jndiName) { this.jndiName = jndiName; }

    public Long getJndiSettingId() { return jndiSettingId; }
    public void setJndiSettingId(Long jndiSettingId) { this.jndiSettingId = jndiSettingId; }

    public String getReadonly() { return readonly; }
    public void setReadonly(String readonly) { this.readonly = Data.trim(readonly); }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    public String getDatabaseName() { return databaseName; }
	public void setDatabaseName(String databaseName) { this.databaseName = databaseName; }
	
	@Override
    public boolean isNew() {
        boolean result = jndiSettingId == null;
        updatedDts = LocalDateTime.now();
        if (result) {
        	jndiSettingId = 0L;
        }
        return result;
    }
    
	@Override
	public int compareTo(JndiSettingBean o) {
		return jndiName.compareTo(o.jndiName);
	}
}
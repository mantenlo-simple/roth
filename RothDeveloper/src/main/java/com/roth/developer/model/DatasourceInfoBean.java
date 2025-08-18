/*
Copyright 2010 James M. Payne

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.roth.developer.model;

import java.io.Serializable;
import java.sql.SQLException;
import java.time.LocalDateTime;

import com.roth.jdbc.annotation.JdbcTable;
import com.roth.jdbc.annotation.PermissiveBinding;
import com.roth.jdbc.model.StateBean;
import com.roth.jdbc.util.TableUtil;

@JdbcTable(name = "datasource_info",
           primaryKeyColumns = {"datasource_info_id"})
@PermissiveBinding()
public class DatasourceInfoBean implements Serializable, StateBean {
    private static final long serialVersionUID = 1L;

    private Integer datasourceInfoId;
    private String dbmsName;
    private String infoStatement;
    private String infoType;
    private String updatedBy;
    private LocalDateTime updatedDts;

    private String trim(String source) { return (source == null) ? null : source.trim(); }

    public Integer getDatasourceInfoId() { return datasourceInfoId; }
    public void setDatasourceInfoId(Integer datasourceInfoId) { this.datasourceInfoId = datasourceInfoId; }

    public String getDbmsName() { return dbmsName; }
    public void setDbmsName(String dbmsName) { this.dbmsName = dbmsName; }

    public String getInfoStatement() { return infoStatement; }
    public void setInfoStatement(String infoStatement) { this.infoStatement = infoStatement; }

    public String getInfoType() { return infoType; }
    public void setInfoType(String infoType) { this.infoType = trim(infoType); }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }

    public LocalDateTime getUpdatedDts() { return updatedDts; }
    public void setUpdatedDts(LocalDateTime updatedDts) { this.updatedDts = updatedDts; }

    @Override
    public boolean isNew() { 
    	boolean result = datasourceInfoId == null;
    	if (result)
    		datasourceInfoId = -1;
    	return result;
    }
    
    public static DatasourceInfoBean get(Integer datasourceInfoId) throws SQLException {
    	TableUtil util = new TableUtil("roth");
    	String filter = util.applyParameters("datasource_info_id = {1}", datasourceInfoId);
    	return util.get(DatasourceInfoBean.class, filter);
    }
    
    public static DatasourceInfoBean get(String dbmsName, String infoType) throws SQLException {
    	TableUtil util = new TableUtil("roth");
    	String filter = util.applyParameters("dbms_name = {1} AND info_type = {2}", dbmsName, infoType);
    	return util.get(DatasourceInfoBean.class, filter);
    }
    
    public void save() throws SQLException {
    	TableUtil util = new TableUtil("roth");
    	util.save(this);
    }
}
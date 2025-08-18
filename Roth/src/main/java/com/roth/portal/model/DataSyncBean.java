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
package com.roth.portal.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class DataSyncBean implements Serializable {
	private static final long serialVersionUID = -6610226788158698226L;

	public static final int MF_LEAVE = 0;
	public static final int MF_INSERT = 1;
	public static final int MF_UPDATE = 2;
	public static final int MF_DELETE = 3;
	public static final int MF_REVERT = 4;
	
	private String tableName;
	// tableId1 and tableId2 represent the one or two components 
	// of a primary key, provided the key is one or two columns.
	private Object tableId1;
	private Object tableId2;
	private Object ownerId;  // What's this for?
	private String srcDescription;
	private String srcUpdatedBy;
	private LocalDateTime srcUpdatedDts;
	private String destDescription;
	private String destUpdatedBy;
	private LocalDateTime destUpdatedDts;
	private Integer moveFlag;
	private boolean doMove;
	
	public String getTableName() { return tableName; }
	public void setTableName(String tableName) { this.tableName = tableName; }
	
	public Object getTableId1() { return tableId1; }
	public void setTableId1(Object tableId1) { this.tableId1 = tableId1; }
	
	public Object getTableId2() { return tableId2; }
	public void setTableId2(Object tableId2) { this.tableId2 = tableId2; }
	
	public Object getOwnerId() { return ownerId; }
	public void setOwnerId(Object ownerId) { this.ownerId = ownerId; }
	
	public String getSrcDescription() { return srcDescription; }
	public void setSrcDescription(String srcDescription) { this.srcDescription = srcDescription; }
	
	public String getSrcUpdatedBy() { return srcUpdatedBy; }
	public void setSrcUpdatedBy(String srcUpdatedBy) { this.srcUpdatedBy = srcUpdatedBy; }
	
	public LocalDateTime getSrcUpdatedDts() { return srcUpdatedDts; }
	public void setSrcUpdatedDts(LocalDateTime srcUpdatedDts) { this.srcUpdatedDts = srcUpdatedDts; }
	
	public String getDestDescription() { return destDescription; }
	public void setDestDescription(String destDescription) { this.destDescription = destDescription; }
	
	public String getDestUpdatedBy() { return destUpdatedBy; }
	public void setDestUpdatedBy(String destUpdatedBy) { this.destUpdatedBy = destUpdatedBy; }
	
	public LocalDateTime getDestUpdatedDts() { return destUpdatedDts; }
	public void setDestUpdatedDts(LocalDateTime destUpdatedDts) { this.destUpdatedDts = destUpdatedDts; }
	
	public Integer getMoveFlag() { return moveFlag; }
	public void setMoveFlag(Integer moveFlag) { this.moveFlag = moveFlag; }
	
	public boolean getDoMove() { return doMove; }
	public void setDoMove(boolean doMove) { this.doMove = doMove; }
	
	public void merge(DataSyncBean source) {
		//ownerId;  // What's this for?
		destDescription = source.destDescription;
		destUpdatedBy = source.destUpdatedBy;
		destUpdatedDts = source.destUpdatedDts; 
	}
}

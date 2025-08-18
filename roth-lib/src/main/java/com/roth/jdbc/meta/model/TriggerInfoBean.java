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
package com.roth.jdbc.meta.model;

import java.io.Serializable;

import com.roth.jdbc.annotation.PermissiveBinding;

@PermissiveBinding()
public class TriggerInfoBean implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tableId;
    private String triggerBody;
    private String triggerId;
    private String triggeringEvent;
    private String triggerName;
    private String triggerType;

    public String getTableId() { return tableId; }
    public void setTableId(String tableId) { this.tableId = tableId; }

    public String getTriggerBody() { return triggerBody; }
    public void setTriggerBody(String triggerBody) { this.triggerBody = triggerBody; }

    public String getTriggerId() { return triggerId; }
    public void setTriggerId(String triggerId) { this.triggerId = triggerId; }

    public String getTriggeringEvent() { return triggeringEvent; }
    public void setTriggeringEvent(String triggeringEvent) { this.triggeringEvent = triggeringEvent; }

    public String getTriggerName() { return triggerName; }
    public void setTriggerName(String triggerName) { this.triggerName = triggerName; }

    public String getTriggerType() { return triggerType; }
    public void setTriggerType(String triggerType) { this.triggerType = triggerType; }
}